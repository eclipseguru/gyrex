/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserComparator.SortIndex;
import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.AppRegItem;
import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.GroupingItem;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.FilteredTree;

/**
 *
 */
@SuppressWarnings("restriction")
public class HttpApplicationPage extends FilteredAdminPage {

	private final class ApplicationBrowserSortListener extends SelectionAdapter {
		private final ApplicationBrowserComparator comparator;
		private final TreeViewerColumn column;
		private final SortIndex sortIndex;

		private ApplicationBrowserSortListener(final ApplicationBrowserComparator comparator, final SortIndex sortIndex, final TreeViewerColumn column) {
			this.comparator = comparator;
			this.sortIndex = sortIndex;
			this.column = column;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (comparator.getIndex() == sortIndex) {
				comparator.setReverse(!comparator.isReverse());
			} else {
				comparator.setIndex(sortIndex);
				treeViewer.getTree().setSortColumn(column.getColumn());
			}
			treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);
			treeViewer.refresh();
			treeViewer.expandAll();
		}
	}

	/** APPLICATION_PROVIDER */
	private static final String FILTER_PROVIDER = "applicationProvider";

	/** CONTEXT */
	private static final String FILTER_CONTEXT = "context";

	private Composite pageComposite;

	private TreeViewer treeViewer;
	private Button editButton;
	private Button addButton;
	private final Image contextImage;
	private final Image applicationImage;

	/**
	 * Creates a new instance.
	 */
	public HttpApplicationPage() {
		setTitle("Web Application Setup");
		setTitleToolTip("Define, configure and mount applications.");
		setFilters(Arrays.asList(FILTER_CONTEXT, FILTER_PROVIDER));
		contextImage = createImage("resources/world.gif");
		applicationImage = createImage("resources/greendot.gif");
	}

	@Override
	public void activate() {
		super.activate();
		// TODO Auto-generated method stub

		if (treeViewer != null) {
			treeViewer.setInput(getApplicationManager());
			treeViewer.getControl().getDisplay();
			treeViewer.expandAll();
		} else {
		}

	}

	void activateButtonPressed() {

		final List<AppRegItem> selectedAppRegs = getSelectedAppRegs();
		for (final AppRegItem appRegItem : selectedAppRegs) {
			final ApplicationRegistration app = appRegItem.getApplicationRegistration();
			getApplicationManager().activate(app.getApplicationId());
		}

		treeViewer.refresh();
		treeViewer.expandAll();
	}

	void addButtonPressed() {
		final EditApplicationDialog dialog = new EditApplicationDialog(SwtUtil.getShell(addButton), getApplicationManager(), null);
		dialog.openNonBlocking(new Runnable() {
			@Override
			public void run() {
				if (dialog.getReturnCode() == Window.OK) {
					treeViewer.refresh();
					treeViewer.expandAll();
				}
			}
		});

	}

	private FilteredTree createApplicationBrowser(final Composite parent) {
		final FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, new ApplicationPatternFilter(), true);

		treeViewer = filteredTree.getViewer();
		treeViewer.getTree().setHeaderVisible(true);
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(60, 50));
		layout.addColumnData(new ColumnWeightData(30, 50));
		layout.addColumnData(new ColumnWeightData(60, 50));
		treeViewer.getTree().setLayout(layout);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(new ApplicationBrowserContentProvider());
		final ApplicationBrowserComparator comparator = new ApplicationBrowserComparator();
		treeViewer.setComparator(comparator);

		final TreeViewerColumn idColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		idColumn.getColumn().setText("Application ID");
		idColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.ID, idColumn));
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof AppRegItem) {
					return ((AppRegItem) element).getApplicationId();
				} else if (element instanceof GroupingItem) {
					return "Grouping Context:" + ((GroupingItem) element).getValue();
				}
				return String.valueOf(element);
			}

			@Override
			public void update(final ViewerCell cell) {
				super.update(cell);
				if (cell.getElement() instanceof GroupingItem) {
					cell.setImage(contextImage);
				} else {
					cell.setImage(applicationImage);
				}
			}
		});
		treeViewer.getTree().setSortColumn(idColumn.getColumn());
		treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);

		final TreeViewerColumn providerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		providerColumn.getColumn().setText("Provider ID");
		providerColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.PROVIDER_ID, providerColumn));
		providerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof AppRegItem) {
					return HttpUiAdapter.getLabel(((AppRegItem) element).getApplicationProviderRegistration());
				}
				return null;
			}
		});

		final TreeViewerColumn contextColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		contextColumn.getColumn().setText("ContextPath");
		contextColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.CONTEXT, contextColumn));
		contextColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof AppRegItem) {
					return ((AppRegItem) element).getContextPath();
				}
				return null;
			}
		});

		final TreeViewerColumn statusColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		statusColumn.getColumn().setText("Status");
		statusColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.STATUS, statusColumn));
		statusColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof AppRegItem) {
					return ((AppRegItem) element).getActivationStatus();
				}
				return null;
			}
		});

		final TreeViewerColumn mountsColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		mountsColumn.getColumn().setText("Mounts");
		mountsColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.MOUNTS, mountsColumn));
		mountsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof AppRegItem) {
					return ((AppRegItem) element).getMounts();
				}
				return null;
			}
		});

		return filteredTree;
	}

	/**
	 * @param buttons
	 * @param buttonLabel
	 * @return
	 */
	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button activateButton = new Button(buttons, SWT.NONE);
		activateButton.setText(buttonLabel);
		activateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return activateButton;
	}

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		//final Control connectGroup = createConnectGroup(pageComposite);
		GridData gd = AdminUiUtil.createHorzFillData();
		gd.verticalIndent = 10;
		//connectGroup.setLayoutData(gd);

		final Label wrappedLabel = new Label(pageComposite, SWT.WRAP);
		wrappedLabel.setLayoutData(AdminUiUtil.createHorzFillData());
		wrappedLabel.setText("Define your applications in a context path and mount them to urls:\n\nIn Gyrex you can implement your ApplicationProvider class. Based on this definition you can configure applications at any context path. The context path is used for tenant separation e.g. /tenant1/ and /tenant2/.  It can also be used for separating sub-Tenant configuration sets in your application  (e.g. /tenant1/warehouse1/ /tenat1/warehouse2/ /tenant2/warehouse1/");

		final Label tableTitle = new Label(pageComposite, SWT.NONE);
		tableTitle.setLayoutData(AdminUiUtil.createHorzFillData());
		tableTitle.setData(WidgetUtil.CUSTOM_VARIANT, "pageHeadline");
		tableTitle.setText("\nYour Configured Applications");

		final Composite description = new Composite(pageComposite, SWT.NONE);
		gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		final Control filteredTree = createApplicationBrowser(description);
		filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());

		addButton = createButton(buttons, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		editButton = createButton(buttons, "Edit");
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				editButtonPressed();
			}
		});

		final Button removeButton = createButton(buttons, "Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

		final Label label = new Label(buttons, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		final Button activateButton = createButton(buttons, "Activate");
		activateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				activateButtonPressed();
			}
		});

		final Button deactivateButton = createButton(buttons, "Deactivate");
		deactivateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				deactivateButtonPressed();
			}
		});

		return pageComposite;
	}

	private Image createImage(final String name) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return Graphics.getImage(name, classLoader);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.admin.ui.pages.AdminPage#deactivate()
	 */
	@Override
	public void deactivate() {
		super.deactivate();
		// TODO Auto-generated method stub

		// remove data inputs form controls
		if (treeViewer != null) {
			treeViewer.setInput(null);
		}

	}

	void deactivateButtonPressed() {
		final List<AppRegItem> selectedAppRegs = getSelectedAppRegs();
		for (final AppRegItem appRegItem : selectedAppRegs) {
			final ApplicationRegistration app = appRegItem.getApplicationRegistration();
			getApplicationManager().deactivate(app.getApplicationId());
		}
		treeViewer.refresh();
		treeViewer.expandAll();
	}

	void editButtonPressed() {

		if (getSelectedValue() == null) {
			return;
		}
		final ApplicationRegistration app = getSelectedValue().getApplicationRegistration();

		final EditApplicationDialog dialog = new EditApplicationDialog(SwtUtil.getShell(editButton), getApplicationManager(), app);
		dialog.openNonBlocking(new Runnable() {
			@Override
			public void run() {
				if (dialog.getReturnCode() == Window.OK) {
					treeViewer.refresh();
					treeViewer.expandAll();
				}
			}
		});

	}

	/*
		@Override
		protected void createFormContent(final IManagedForm managedForm) {
			final Composite body = managedForm.getForm().getBody();
			body.setLayout(FormLayoutFactory.createFormGridLayout(true, 1));
			body.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

			appSection = new ApplicationsSection(body, this);
			appSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
			managedForm.addPart(appSection);

	//		FormLayoutFactory.visualizeLayoutArea(body, SWT.COLOR_CYAN);
	//		FormLayoutFactory.visualizeLayoutArea(left, SWT.COLOR_DARK_GREEN);
	//		FormLayoutFactory.visualizeLayoutArea(right, SWT.COLOR_DARK_GREEN);
		}

		@Override
		public ISelectionProvider getSelectionProvider() {
			if (null != appSection) {
				return appSection.getSelectionProvider();
			}
			return null;
		}

		*/

	/**
	 * @return
	 */
	private ApplicationManager getApplicationManager() {
		return HttpUiActivator.getAppManager();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage#getFilterText(java.lang.String)
	 */
	@Override
	protected String getFilterText(final String filter) {
		if (FILTER_CONTEXT.equals(filter)) {
			return "All Contexts";
		}
		if (FILTER_PROVIDER.equals(filter)) {
			return "All Applications";
		}
		return super.getFilterText(filter);
	}

	private List<AppRegItem> getSelectedAppRegs() {

		final List<AppRegItem> selectedOnes = new ArrayList<AppRegItem>();
		final TreeSelection selection = (TreeSelection) treeViewer.getSelection();
		final Iterator it = selection.iterator();
		while (it.hasNext()) {
			final Object element = it.next();
			if (element instanceof AppRegItem) {
				selectedOnes.add((AppRegItem) element);
			}
		}
		return selectedOnes;
	}

	private AppRegItem getSelectedValue() {
		final TreeSelection selection = (TreeSelection) treeViewer.getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof AppRegItem)) {
			return (AppRegItem) selection.getFirstElement();
		}

		return null;
	}

	void removeButtonPressed() {
		if (getSelectedValue() == null) {
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(pageComposite), "Remove Context", "Do you really want to delete the application(s)?")) {
			return;
		}

		final List<AppRegItem> selectedAppRegs = getSelectedAppRegs();
		for (final AppRegItem appRegItem : selectedAppRegs) {
			final ApplicationRegistration app = appRegItem.getApplicationRegistration();
			getApplicationManager().unregister(app.getApplicationId());
		}
		treeViewer.refresh();
		treeViewer.expandAll();
	}

}
