/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.configuration.IConfigurationPageContainer;
import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.SaveablesLifecycleEvent;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.eclipse.ui.part.ViewPart;

import org.apache.commons.lang.StringUtils;

/**
 * RAP based View to display a specific {@link ConfigurationPage}. The trigger
 * to change the currently displayed page is an {@link SelectionEvent} from the
 * {@link ConfigurationNavigatorView}
 */
@SuppressWarnings("restriction")
public class ConfigurationPanelView extends ViewPart implements ISelectionListener, IConfigurationPageContainer, ISaveablesSource, ISaveablePart {

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

	private static final Saveable[] NO_SAVEABLES = new Saveable[] {};

	static final String EMPTY_STRING = "";

	public static final String ID = "org.eclipse.gyrex.admin.ui.view.content";
	private static final String REGISTRATION = "org.eclipse.gyrex.admin.ui.internal.configuration.page.registration";

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
					if (source instanceof ConfigurationPage) {
						maybeDirty((ConfigurationPage) source);
						updateSaveActions();
					}
					break;

				case IWorkbenchPartConstants.PROP_TITLE:
					updateHeader();
					break;
			}

		}

	};

	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private final ConcurrentMap<ConfigurationPage, ConfigurationPageSaveable> saveablesByPage = new ConcurrentHashMap<ConfigurationPage, ConfigurationPageSaveable>(4);

	private synchronized void addPage(final String id, final ConfigurationPageRegistration provider) {
		if (pagesById.containsKey(id)) {
			return;
		}

		try {
			final ConfigurationPage page = provider.createPage();
			pagesById.put(id, page);
			page.setContainer(this);
			final Composite pageParent = pageBook.createPage(id);
			pageParent.setLayout(new FillLayout());
			page.createPage(pageParent, getToolkit());
			page.getControl().setData(REGISTRATION, provider);
		} catch (final CoreException e) {
			Policy.getStatusHandler().show(e.getStatus(), "Error");
		} catch (final Exception e) {
			e.printStackTrace();
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, "Error loading page " + id, e), "Error");
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());

		parent.setLayout(new FillLayout());
		final ScrolledForm scform = toolkit.createScrolledForm(parent);
		scform.getForm().setData(FormUtil.IGNORE_BODY, Boolean.TRUE);

		saveAction = ActionFactory.SAVE.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		saveAllAction = ActionFactory.SAVE_ALL.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		scform.getToolBarManager().add(saveAction);
		scform.getToolBarManager().add(saveAllAction);
		scform.updateToolBar();

		headerForm = new HeaderForm(this, scform);
		toolkit.decorateFormHeading(scform.getForm());

		final Composite formBody = headerForm.getForm().getBody();
		formBody.setLayout(new FillLayout());
		pageBook = toolkit.createPageBook(formBody, SWT.NO_SCROLL);

		getSite().getPage().addSelectionListener(this);
	}

	@Override
	public void dispose() {
		// remove listener
		getSite().getPage().removeSelectionListener(this);

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
	public void doSave(final IProgressMonitor monitor) {
		throw new UnsupportedOperationException("no save on part");
	}

	@Override
	public void doSaveAs() {
		throw new UnsupportedOperationException("no save as on part");
	}

	@Override
	public ConfigurationPage getActivePageInstance() {
		return currentPage;
	}

	@Override
	public Saveable[] getActiveSaveables() {
		if ((currentPage != null) && currentPage.isDirty()) {
			return new Saveable[] { new ConfigurationPageSaveable(this, currentPage) };
		}
		return NO_SAVEABLES;
	}

	@Override
	public Object getAdapter(final Class adapter) {
//		if (adapter == IPropertySheetPage.class) {
//			return new TabbedPropertySheetPage(ConfigurationTabbedPropertySheetPageContributor.INSTANCE);
//		}
		return super.getAdapter(adapter);
	}

	@Override
	public Saveable[] getSaveables() {
		final Collection<ConfigurationPageSaveable> saveables = saveablesByPage.values();
		return saveables.toArray(new ConfigurationPageSaveable[saveables.size()]);
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
	public boolean isDirty() {
		return !saveablesByPage.isEmpty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return isDirty();
	}

	void maybeDirty(final ConfigurationPage source) {
		if (source.isDirty()) {
			final ConfigurationPageSaveable newSaveable = new ConfigurationPageSaveable(this, source);
			final ConfigurationPageSaveable existingSaveable = saveablesByPage.putIfAbsent(source, newSaveable);
			// fire event
			final ISaveablesLifecycleListener listener = (ISaveablesLifecycleListener) getSite().getAdapter(ISaveablesLifecycleListener.class);
			if (listener != null) {
				SaveablesLifecycleEvent event;
				if (existingSaveable == null) {
					// new savable
					event = new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_OPEN, new Saveable[] { newSaveable }, false);
				} else {
					// dirty changed for existing saveable
					event = new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.DIRTY_CHANGED, new Saveable[] { existingSaveable }, false);
				}
				listener.handleLifecycleEvent(event);
			}
		} else {
			final ConfigurationPageSaveable saveable = saveablesByPage.remove(source);
			final ISaveablesLifecycleListener listener = (ISaveablesLifecycleListener) getSite().getAdapter(ISaveablesLifecycleListener.class);
			if ((listener != null) && (saveable != null)) {
				// removed savable
				listener.handleLifecycleEvent(new SaveablesLifecycleEvent(this, SaveablesLifecycleEvent.POST_CLOSE, new Saveable[] { saveable }, false));
			}

		}
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection) {
		// we only care about the navigator view
		if (!(part instanceof ConfigurationNavigatorView)) {
			return;
		}

		if (!(selection instanceof StructuredSelection)) {
			return;
		}

		final Object firstElement = ((StructuredSelection) selection).getFirstElement();
		if (firstElement instanceof ConfigurationPageRegistration) {
			final ConfigurationPageRegistration provider = (ConfigurationPageRegistration) firstElement;
			final String id = provider.getId();

			// create page if necessary
			if (!pagesById.containsKey(id)) {
				addPage(id, provider);
			}

			showPage(id);
		}
	}

	void setBusy(final boolean busy) {
		headerForm.getForm().setBusy(busy);
	}

	@Override
	public void setFocus() {
		pageBook.setFocus();
	}

	private void showPage(final String id) {
		// deactivate current page
		if (currentPage != null) {
			currentPage.setActive(false);
			currentPage.removePropertyListener(pagePropertyListener);
			currentPage = null;
		}

		// update current page
		currentPage = pagesById.get(id);

		// show page
		pageBook.showPage(id);

		// update
		updateHeader();
		updateSaveActions();

		// activate
		if (currentPage != null) {
			currentPage.addPropertyListener(pagePropertyListener);
			currentPage.setActive(true);
		}
	}

	void updateHeader() {
		final ConfigurationPage page = getActivePageInstance();
		if (page != null) {
			headerForm.getForm().setImage(page.getTitleImage());
			String title = page.getTitle();
			if (StringUtils.isEmpty(title)) {
				final ConfigurationPageRegistration registration = (ConfigurationPageRegistration) page.getControl().getData(REGISTRATION);
				title = registration.getName();
			}
			headerForm.getForm().setText(title);
			headerForm.getForm().getForm().getHead().setToolTipText(page.getTitleToolTip());
			setTitleToolTip(page.getTitleToolTip());
			setContentDescription(title);
		} else {
			headerForm.getForm().setImage(null);
			headerForm.getForm().setText(EMPTY_STRING);
			headerForm.getForm().getForm().getHead().setToolTipText(EMPTY_STRING);
			setTitleToolTip(EMPTY_STRING);
			setContentDescription(EMPTY_STRING);
		}
	}

	void updateSaveActions() {
		// if current is dirty
		saveAction.setEnabled((null != currentPage) && currentPage.isDirty());
		// if any is dirty
		saveAllAction.setEnabled(isDirty());
	}
}