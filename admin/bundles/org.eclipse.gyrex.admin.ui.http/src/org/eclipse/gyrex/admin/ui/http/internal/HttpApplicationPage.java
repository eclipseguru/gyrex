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
import java.util.Iterator;
import java.util.List;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserComparator.SortIndex;
import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.ApplicationItem;
import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.GroupNode;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreeNodeContentProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.widgets.DialogCallback;
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
import org.eclipse.ui.dialogs.PatternFilter;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
@SuppressWarnings("restriction")
public class HttpApplicationPage extends FilteredAdminPage {

	private final class ApplicationBrowserSortListener extends SelectionAdapter {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
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
	private Button removeButton;
	private Button activateButton;
	private Button deactivateButton;

	private ISelectionChangedListener updateButtonsListener;

	/**
	 * Creates a new instance.
	 */
	public HttpApplicationPage() {
		setTitle("Manage Web Applications");
		setTitleToolTip("Define, configure and mount applications.");
//		setFilters(Arrays.asList(FILTER_CONTEXT, FILTER_PROVIDER));
	}

	@Override
	public void activate() {
		super.activate();
		// TODO Auto-generated method stub

		if (treeViewer != null) {
			treeViewer.setInput(getApplicationManager());
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			treeViewer.addSelectionChangedListener(updateButtonsListener);
			treeViewer.getControl().getDisplay();
			treeViewer.expandAll();
		} else {
		}

	}

	void activateSelectedApplications() {
		final List<ApplicationItem> selectedAppRegs = getSelectedAppRegs();
		for (final ApplicationItem appRegItem : selectedAppRegs) {
			final ApplicationRegistration app = appRegItem.getApplicationRegistration();
			getApplicationManager().activate(app.getApplicationId());
		}
		refresh();
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

	private FilteredTree createApplicationBrowser(final Composite parent) {
		final FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, new ApplicationPatternFilter(), true);

		treeViewer = filteredTree.getViewer();
		treeViewer.getTree().setHeaderVisible(true);
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 40));
		layout.addColumnData(new ColumnWeightData(30, 20));
		layout.addColumnData(new ColumnWeightData(20, 20));
		treeViewer.getTree().setLayout(layout);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(new ApplicationBrowserContentProvider());
		final ApplicationBrowserComparator comparator = new ApplicationBrowserComparator();
		treeViewer.setComparator(comparator);
		treeViewer.addOpenListener(new IOpenListener() {

			@Override
			public void open(final OpenEvent event) {
				editSelectedApplication();
			}
		});

		final Image activeApplication = HttpUiActivator.getImageDescriptor("icons/obj/app_active.gif").createImage(parent.getDisplay());
		final Image inactiveApplication = HttpUiActivator.getImageDescriptor("icons/obj/app_inactive.gif").createImage(parent.getDisplay());

		final TreeViewerColumn idColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		idColumn.getColumn().setText("Instance ID");
		idColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.ID, idColumn));
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dispose() {
				super.dispose();
				activeApplication.dispose();
				inactiveApplication.dispose();
			}

			@Override
			public Image getImage(final Object element) {
				if (element instanceof ApplicationItem) {
					final ApplicationItem appItem = (ApplicationItem) element;
					if (appItem.isActive()) {
						return activeApplication;
					} else {
						return inactiveApplication;
					}
				}
				return super.getImage(element);
			}

			@Override
			public String getText(final Object element) {
				if (element instanceof ApplicationItem) {
					return ((ApplicationItem) element).getApplicationId();
				} else if (element instanceof GroupNode) {
					final Object value = ((GroupNode) element).getValue();
					final LabelAdapter adapter = AdapterUtil.getAdapter(value, LabelAdapter.class);
					if (null != adapter) {
						return adapter.getLabel(value);
					}
					return String.valueOf(value);
				}
				return String.valueOf(element);
			}
		});
		treeViewer.getTree().setSortColumn(idColumn.getColumn());
		treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);

		final TreeViewerColumn providerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		providerColumn.getColumn().setText("Type");
		providerColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.PROVIDER_ID, providerColumn));
		providerColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof ApplicationItem) {
					return HttpUiAdapter.getLabel(((ApplicationItem) element).getApplicationProviderRegistration());
				}
				return null;
			}
		});

		final TreeViewerColumn mountsColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		mountsColumn.getColumn().setText("Mounts");
		mountsColumn.getColumn().addSelectionListener(new ApplicationBrowserSortListener(comparator, SortIndex.MOUNTS, mountsColumn));
		mountsColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof ApplicationItem) {
					return StringUtils.join(((ApplicationItem) element).getMounts(), ", ");
				}
				return null;
			}
		});

		return filteredTree;
	}

	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(pageComposite);
			infobox.setLayoutData(AdminUiUtil.createHorzFillData());
			infobox.addHeading("Web Applications in Gyrex.");
			infobox.addParagraph("In OSGi the HttpService is a common way of making Servlets and resources available. Out of the box (in development only) an HttpService is also available in Gyrex. However, that approache does not scale very well in a multi-tenant environment. Therefore, Gyrex allows to develop and integrate multiple kind of web applications. The OSGi HttpService is just one available example of a web application. It's possible to <a href=\"http://wiki.eclipse.org/Gyrex/Developer_Guide/Web_Applications\">develop your own applications</a>.");
			infobox.addParagraph("In order to make a new application accessible an instance need to be defined first and then it needs to be mounted to an URL.");
		}

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
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
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		editButton = createButton(buttons, "Edit");
		editButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				editSelectedApplication();
			}
		});

		removeButton = createButton(buttons, "Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeSelectedApplication();
			}
		});

		final Label label = new Label(buttons, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		activateButton = createButton(buttons, "Activate");
		activateButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				activateSelectedApplications();
			}
		});

		deactivateButton = createButton(buttons, "Deactivate");
		deactivateButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				deactivateSelectedApplications();
			}
		});

		updateButtons();

		return pageComposite;
	}

	@Override
	protected Control createFilterControl(final String filter, final Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(GridLayoutFactory.fillDefaults().create());

		final FilteredTree filteredTree = new FilteredTree(content, SWT.FULL_SELECTION | SWT.SINGLE, new PatternFilter(), true);
		filteredTree.setData(WidgetUtil.CUSTOM_VARIANT, "filter-tree");
		filteredTree.getFilterControl().setData(WidgetUtil.CUSTOM_VARIANT, "filter-tree");

		final TreeViewer viewer = filteredTree.getViewer();
		viewer.getControl().setData(WidgetUtil.CUSTOM_VARIANT, "filter-tree");

		((GridData) viewer.getControl().getLayoutData()).minimumHeight = 200;
		((GridData) viewer.getControl().getLayoutData()).minimumWidth = 300;

		viewer.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				parent.getShell().pack(true);
			}

			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				parent.getShell().pack(true);
			}
		});

		viewer.setContentProvider(new TreeNodeContentProvider());
		viewer.setLabelProvider(new LabelProvider() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof TreeNode) {
					final Object value = ((TreeNode) element).getValue();
					if (value instanceof ContextDefinition) {
						final ContextDefinition contextDefinition = (ContextDefinition) value;
						if (StringUtils.isNotBlank(contextDefinition.getName())) {
							return contextDefinition.getName();
						}
						return contextDefinition.getPath().toString();
					}
					return super.getText(value);
				}
				return super.getText(element);
			}
		});
		viewer.setComparator(new ViewerComparator());

		if (FILTER_CONTEXT.equals(filter)) {
			viewer.setInput(RuntimeContextTree.getTree());
		} else {
			final TreeNode root = new TreeNode("Root");
			root.setChildren(new TreeNode[] { new TreeNode("One"), new TreeNode("Two") });
			viewer.setInput(new TreeNode[] { new TreeNode("One"), new TreeNode("Two") });
		}

		return content;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		// remove data inputs form controls
		if (treeViewer != null) {
			if (updateButtonsListener != null) {
				treeViewer.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (!treeViewer.getTree().isDisposed()) {
				treeViewer.setInput(null);
			}
		}

	}

	void deactivateSelectedApplications() {
		final List<ApplicationItem> selectedAppRegs = getSelectedAppRegs();
		for (final ApplicationItem appRegItem : selectedAppRegs) {
			final ApplicationRegistration app = appRegItem.getApplicationRegistration();
			getApplicationManager().deactivate(app.getApplicationId());
		}
		refresh();
	}

	void editSelectedApplication() {

		if (getSelectedValue() == null) {
			return;
		}
		final ApplicationRegistration app = getSelectedValue().getApplicationRegistration();

		final EditApplicationDialog dialog = new EditApplicationDialog(SwtUtil.getShell(editButton), getApplicationManager(), app);
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

	/**
	 * @return
	 */
	private ApplicationManager getApplicationManager() {
		return HttpUiActivator.getAppManager();
	}

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

	private List<ApplicationItem> getSelectedAppRegs() {

		final List<ApplicationItem> selectedOnes = new ArrayList<ApplicationItem>();
		final TreeSelection selection = (TreeSelection) treeViewer.getSelection();
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
		final TreeSelection selection = (TreeSelection) treeViewer.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof ApplicationItem) {
			return (ApplicationItem) selection.getFirstElement();
		}

		return null;
	}

	void refresh() {
		treeViewer.refresh();
		treeViewer.expandAll();
	}

	void removeSelectedApplication() {
		final ApplicationItem applicationItem = getSelectedValue();
		if (applicationItem == null) {
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(pageComposite), "Remove Application", String.format("Do you really want to delete instance %s?", applicationItem.getApplicationId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				getApplicationManager().unregister(applicationItem.getApplicationId());
				refresh();
			}
		});
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) treeViewer.getSelection()).size();
		if (selectedElementsCount == 0) {
			activateButton.setEnabled(false);
			deactivateButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			return;
		}

		boolean hasActiveApps = false;
		boolean hasInactiveApps = false;
		for (final Iterator stream = ((IStructuredSelection) treeViewer.getSelection()).iterator(); stream.hasNext();) {
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
