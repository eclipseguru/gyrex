/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.FilteredTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.lang.StringUtils;

public class HttpApplicationPage extends AdminPageWithTree {

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_MOUNTS = 2;

	private Button editButton;
	private Button addButton;
	private Button removeButton;
	private Button activateButton;
	private Button deactivateButton;

	Image activeApplication;
	Image inactiveApplication;

	/**
	 * Creates a new instance.
	 */
	public HttpApplicationPage() {
		super(3);
		setTitle("Manage Web Applications");
		setTitleToolTip("Define, configure and mount applications.");
	}

	void activateSelectedApplications() {
		final List<ApplicationItem> selectedAppRegs = getSelectedAppRegs();
		for (final ApplicationItem item : selectedAppRegs) {
			final ApplicationRegistration app = item.getApplicationRegistration();
			getApplicationManager().activate(app.getApplicationId());
			item.setActive(true);
			getTreeViewer().refresh(app, true);
		}
	}

	void addButtonPressed() {
		final EditApplicationDialog dialog = new EditApplicationDialog(SwtUtil.getShell(addButton), getApplicationManager(), null);
		dialog.openNonBlocking(new DialogCallback() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});

	}

	@Override
	protected void createButtons(final Composite parent) {
		addButton = createButton(parent, "New...");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		editButton = createButton(parent, "Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				editSelectedApplication();
			}
		});

		removeButton = createButton(parent, "Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeSelectedApplication();
			}
		});

		createButtonSeparator(parent);

		activateButton = createButton(parent, "Activate");
		activateButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				activateSelectedApplications();
			}
		});

		deactivateButton = createButton(parent, "Deactivate");
		deactivateButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				deactivateSelectedApplications();
			}
		});
	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new ApplicationBrowserContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(parent);
			infobox.setLayoutData(AdminUiUtil.createHorzFillData());
			infobox.addHeading("Web Applications in Gyrex.");
			infobox.addParagraph("In OSGi the HttpService is a common way of making Servlets and resources available. Out of the box (in development only) an HttpService is also available in Gyrex. However, that approache does not scale very well in a multi-tenant environment. Therefore, Gyrex allows to develop and integrate multiple kind of web applications. The OSGi HttpService is just one available example of a web application. It's possible to <a href=\"http://wiki.eclipse.org/Gyrex/Developer_Guide/Web_Applications\">develop your own applications</a>.");
			infobox.addParagraph("In order to make a new application accessible an instance need to be defined first and then it needs to be mounted to an URL.");
			return infobox;
		}

		return null;
	}

	@Override
	protected FilteredTree createTree(final Composite parent) {
		activeApplication = HttpUiActivator.getImageDescriptor("icons/obj/app_active.gif").createImage(parent.getDisplay());
		inactiveApplication = HttpUiActivator.getImageDescriptor("icons/obj/app_inactive.gif").createImage(parent.getDisplay());
		parent.addDisposeListener(new DisposeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetDisposed(final DisposeEvent event) {
				if (activeApplication != null) {
					activeApplication.dispose();
					activeApplication = null;
				}
				if (inactiveApplication != null) {
					inactiveApplication.dispose();
					inactiveApplication = null;
				}

			}
		});
		return super.createTree(parent);
	}

	void deactivateSelectedApplications() {
		final List<ApplicationItem> selectedAppRegs = getSelectedAppRegs();
		for (final ApplicationItem item : selectedAppRegs) {
			final ApplicationRegistration app = item.getApplicationRegistration();
			getApplicationManager().deactivate(app.getApplicationId());
			item.setActive(false);
			getTreeViewer().refresh(app, true);
		}
	}

	void editSelectedApplication() {

		if (getSelectedValue() == null)
			return;
		final ApplicationRegistration app = getSelectedValue().getApplicationRegistration();

		final EditApplicationDialog dialog = new EditApplicationDialog(SwtUtil.getShell(editButton), getApplicationManager(), app);
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});

	}

	/**
	 * @return
	 */
	private ApplicationManager getApplicationManager() {
		return HttpUiActivator.getAppManager();
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_ID:
				return "Application";
			case COLUMN_TYPE:
				return "Type";
			case COLUMN_MOUNTS:
				return "Mounts";

			default:
				return StringUtils.EMPTY;
		}
	}

	@Override
	protected Image getElementImage(final Object element, final int column) {
		if ((element instanceof ApplicationItem) && (column == 0)) {
			final ApplicationItem appItem = (ApplicationItem) element;
			if (appItem.isActive())
				return activeApplication;
			else
				return inactiveApplication;
		}
		return null;
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof TreeNode) {
			if (column == 0) {
				final Object value = ((TreeNode) element).getValue();
				if (value instanceof ContextDefinition) {
					final ContextDefinition contextDefinition = (ContextDefinition) value;
					if (StringUtils.isNotBlank(contextDefinition.getName()))
						return contextDefinition.getName();
					return contextDefinition.getPath().toString();
				}
			}
		} else if (element instanceof ApplicationGroup) {
			final Object value = ((ApplicationGroup) element).getValue();
			switch (column) {
				case NO_COLUMN:
				case COLUMN_ID:
					final LabelAdapter adapter = AdapterUtil.getAdapter(value, LabelAdapter.class);
					if (null != adapter)
						return adapter.getLabel(value);
					return String.valueOf(value);
				default:
					return StringUtils.EMPTY;
			}
		} else if (element instanceof ApplicationItem) {
			final ApplicationItem app = (ApplicationItem) element;
			switch (column) {
				case COLUMN_ID:
					return app.getApplicationId();
				case COLUMN_TYPE:
					return HttpUiAdapter.getLabel(app.getApplicationProviderRegistration());
				case COLUMN_MOUNTS:
					return StringUtils.join(app.getMounts(), ", ");

				default:
					return StringUtils.EMPTY;
			}
		}
		return StringUtils.EMPTY;
	}

	private List<ApplicationItem> getSelectedAppRegs() {
		final List<ApplicationItem> selectedOnes = new ArrayList<ApplicationItem>();
		final TreeSelection selection = (TreeSelection) getTreeViewer().getSelection();
		final Iterator it = selection.iterator();
		while (it.hasNext()) {
			final Object element = it.next();
			if (element instanceof ApplicationItem) {
				selectedOnes.add((ApplicationItem) element);
			}
		}
		return selectedOnes;
	}

	private ApplicationItem getSelectedValue() {
		final TreeSelection selection = (TreeSelection) getTreeViewer().getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof ApplicationItem))
			return (ApplicationItem) selection.getFirstElement();

		return null;
	}

	@Override
	protected Object getViewerInput() {
		return HttpUiActivator.getAppManager();
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return (column == COLUMN_ID) || (column == COLUMN_TYPE) || (column == COLUMN_MOUNTS);
	}

	@Override
	protected void refresh() {
		getTreeViewer().refresh();
	}

	void removeSelectedApplication() {
		final ApplicationItem applicationItem = getSelectedValue();
		if (applicationItem == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Remove Application", String.format("Do you really want to delete instance %s?", applicationItem.getApplicationId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				getApplicationManager().unregister(applicationItem.getApplicationId());
				refresh();
			}
		});
	}

	@Override
	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) getTreeViewer().getSelection()).size();
		if (selectedElementsCount == 0) {
			activateButton.setEnabled(false);
			deactivateButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		}

		boolean hasActiveApps = false;
		boolean hasInactiveApps = false;
		for (final Iterator stream = ((IStructuredSelection) getTreeViewer().getSelection()).iterator(); stream.hasNext();) {
			final Object object = stream.next();
			if (object instanceof ApplicationItem) {
				final ApplicationItem nodeItem = (ApplicationItem) object;
				hasActiveApps |= nodeItem.isActive();
				hasInactiveApps |= !nodeItem.isActive();
			}
			if (hasInactiveApps && hasActiveApps) {
				break;
			}
		}

		activateButton.setEnabled(hasInactiveApps);
		deactivateButton.setEnabled(hasActiveApps);
		editButton.setEnabled(selectedElementsCount == 1);
		removeButton.setEnabled(selectedElementsCount == 1);
	}
}
