/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.configuration.IConfigurationPageContainer;
import org.eclipse.gyrex.admin.ui.internal.IContentProviderNode;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * RAP based View to display a specific {@link ConfigurationPage}. The trigger
 * to change the currently displayed page is an {@link SelectionEvent} from the
 * {@link ConfigurationNavigatorView}
 */
public class ConfigurationPanelView extends ViewPart implements ISelectionListener, IConfigurationPageContainer {

	private class ApplyContributionItem extends ContributionItem {
		@Override
		public void fill(final ToolBar parent, final int index) {
			final ToolItem item = new ToolItem(parent, SWT.PUSH);
			item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT));
			item.setDisabledImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT_DISABLED));
			item.setToolTipText("Save");
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(final Event event) {
//					performSave(null);
				}
			});
		}
	}

	private static class HeaderForm extends ManagedForm {
		public HeaderForm(final ConfigurationPanelView configurationPanelView, final ScrolledForm form) {
			super(configurationPanelView.getToolkit(), form);
			setContainer(configurationPanelView);
		}

		@Override
		public void dirtyStateChanged() {
			getView().firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
		}

		private ConfigurationPanelView getView() {
			return (ConfigurationPanelView) getContainer();
		}

		@Override
		public void staleStateChanged() {
			refresh();
		}
	}

	static final String EMPTY_STRING = "";

	public static final String ID = "org.eclipse.gyrex.admin.ui.view.content";

	private FormToolkit toolkit;
	private ConfigurationPage currentPage;
	private HeaderForm headerForm;
	private ScrolledPageBook pageBook;

	private final Map<String, ConfigurationPage> pagesById = new HashMap<String, ConfigurationPage>();

	private final IPropertyListener pagePropertyListener = new IPropertyListener() {

		@Override
		public void propertyChanged(final Object source, final int propId) {
			switch (propId) {
				case IWorkbenchPartConstants.PROP_DIRTY:
					updateSaveAction();
					break;

				case IWorkbenchPartConstants.PROP_TITLE:
					updateHeader();
					break;
			}

		}
	};

	private IWorkbenchAction saveAction;

	private synchronized void addPage(final String id, final IContentProviderNode provider) {
		if (pagesById.containsKey(id)) {
			return;
		}

		try {
			final ConfigurationPage page = provider.createPage();
			pagesById.put(id, page);
			final Composite pageParent = pageBook.createPage(id);
			pageParent.setLayout(new FillLayout());
			page.createPage(pageParent, getToolkit());
		} catch (final CoreException e) {
			Policy.getStatusHandler().show(e.getStatus(), "Error");
		}
	}

	@SuppressWarnings("restriction")
	@Override
	public void createPartControl(final Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

		parent.setLayout(new FillLayout());
		final ScrolledForm scform = toolkit.createScrolledForm(parent);
		scform.getForm().setData(FormUtil.IGNORE_BODY, Boolean.TRUE);

		saveAction = ActionFactory.SAVE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		scform.getToolBarManager().add(saveAction);
		scform.updateToolBar();

		headerForm = new HeaderForm(this, scform);
		toolkit.decorateFormHeading(scform.getForm());

		final Composite formBody = headerForm.getForm().getBody();
		formBody.setLayout(new FillLayout());
		pageBook = toolkit.createPageBook(formBody, SWT.NO_SCROLL);

		getSite().getPage().addSelectionListener(ConfigurationNavigatorView.ID, this);
		updateHeader();
		updateSaveAction();
	}

	@Override
	public void dispose() {
		// remove listener
		getSite().getPage().removeSelectionListener(ConfigurationNavigatorView.ID, this);

		// super
		super.dispose();

		// dispose and clear
		final Collection<ConfigurationPage> pages = pagesById.values();
		pagesById.clear();
		for (final ConfigurationPage page : pages) {
			page.dispose();
		}
	}

	@Override
	public ConfigurationPage getActivePageInstance() {
		return currentPage;
	}

	@Override
	public Object getAdapter(final Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			return new TabbedPropertySheetPage(ConfigurationTabbedPropertySheetPageContributor.INSTANCE);
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Returns the toolkit owned by this view.
	 * 
	 * @return the toolkit
	 */
	public FormToolkit getToolkit() {
		return toolkit;
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		if (!(selection instanceof StructuredSelection)) {
			return;
		}

		final Object firstElement = ((StructuredSelection) selection).getFirstElement();
		if (firstElement instanceof IContentProviderNode) {
			final IContentProviderNode provider = (IContentProviderNode) firstElement;
			final String id = provider.getId();

			// create page if necessary
			if (!pagesById.containsKey(id)) {
				addPage(id, provider);
			}

			showPage(id);
		}
	}

	@Override
	public void setFocus() {
		if (currentPage != null) {
			currentPage.setFocus();
		}
	}

	private void showPage(final String id) {
		// deactivate current page
		if (currentPage != null) {
			currentPage.removePropertyListener(pagePropertyListener);
			currentPage = null;
		}

		// update current page
		currentPage = pagesById.get(id);

		// show page
		pageBook.showPage(id);

		// update
		updateHeader();
		updateSaveAction();

		// activate
		if (currentPage != null) {
			currentPage.addPropertyListener(pagePropertyListener);
			currentPage.setFocus();
		}
	}

	void updateHeader() {
		final ConfigurationPage page = getActivePageInstance();
		if (page != null) {
			headerForm.getForm().setImage(page.getTitleImage());
			headerForm.getForm().setText(page.getTitle());
			headerForm.getForm().setToolTipText(page.getTitleToolTip());
			setTitleToolTip(page.getTitleToolTip());
			setContentDescription(page.getTitle());
		} else {
			headerForm.getForm().setImage(null);
			headerForm.getForm().setText(EMPTY_STRING);
			headerForm.getForm().setToolTipText(EMPTY_STRING);
			setTitleToolTip(EMPTY_STRING);
			setContentDescription(EMPTY_STRING);
		}
	}

	void updateSaveAction() {
		saveAction.setEnabled((currentPage != null) && currentPage.isDirty());
	}
}