/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.cloud.internal;

import java.util.Iterator;

import org.eclipse.gyrex.admin.ui.cloud.internal.NodeBrowserComparator.SortIndex;
import org.eclipse.gyrex.admin.ui.cloud.internal.NodeBrowserContentProvider.NodeItem;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.FilteredTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LinkDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeConfigurer;
import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGateListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

/**
 * Gyrex Cloud Configuration Page.
 */
public class ClusterAdminPage extends AdminPage {

	private final class NodeBrowserSortListener extends SelectionAdapter {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private final NodeBrowserComparator comparator;
		private final TreeViewerColumn column;
		private final SortIndex sortIndex;

		private NodeBrowserSortListener(final NodeBrowserComparator comparator, final SortIndex sortIndex, final TreeViewerColumn column) {
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
		}
	}

	private ZooKeeperGateListener listener;

	private StringDialogField nodeIdField;
	private LinkDialogField membershipStatusField;

	private Composite composite;
	private TreeViewer treeViewer;

	private Button approveButton;
	private Button retireButton;
	private Button editButton;
	private Button manageButton;

	private ISelectionChangedListener updateButtonsListener;

	/**
	 * Creates a new instance.
	 */
	public ClusterAdminPage() {
		setTitle("Cluster Configuration");
		setTitleToolTip("Configure the cluster of nodes in the system.");
	}

	@Override
	public void activate() {
		super.activate();

		final Display display;
		if (treeViewer != null) {
			treeViewer.setInput(getCloudManager());
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			treeViewer.addSelectionChangedListener(updateButtonsListener);
			display = treeViewer.getControl().getDisplay();
		} else {
			display = null;
		}

		if ((listener == null) && (display != null) && !display.isDisposed()) {
			listener = new ZooKeeperGateListener() {

				private void asyncRefresh() {
					if (!display.isDisposed()) {
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								refresh();
							}
						});
					}
				}

				@Override
				public void gateDown(final ZooKeeperGate gate) {
					asyncRefresh();
				}

				@Override
				public void gateRecovering(final ZooKeeperGate gate) {
					asyncRefresh();
				}

				@Override
				public void gateUp(final ZooKeeperGate gate) {
					asyncRefresh();
				}
			};
			ZooKeeperGate.addConnectionMonitor(listener);
		}

		refresh();
	}

	void approveSelectedNodes() {
		final ICloudManager cloudManager = getCloudManager();
		final MultiStatus result = new MultiStatus(CloudUiActivator.SYMBOLIC_NAME, 0, "Some nodes could not be approved.", null);
		for (final Iterator stream = ((IStructuredSelection) treeViewer.getSelection()).iterator(); stream.hasNext();) {
			final Object object = stream.next();
			if (object instanceof NodeItem) {
				final NodeItem nodeItem = (NodeItem) object;
				if (!nodeItem.isApproved()) {
					final IStatus status = cloudManager.approveNode(nodeItem.getDescriptor().getId());
					if (!status.isOK()) {
						result.add(status);
					}
				}
			}
		}
		if (!result.isOK()) {
			Policy.getStatusHandler().show(result, "Error");
		}
	}

	private void createButtons(final Composite parent) {
		approveButton = new Button(parent, SWT.PUSH);
		approveButton.setText("Approve");
		approveButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				approveSelectedNodes();
			}
		});

		retireButton = new Button(parent, SWT.PUSH);
		retireButton.setText("Retire");
		retireButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				retireSelectedNodes();
			}
		});

		createButtonSeparator(parent);

		editButton = new Button(parent, SWT.PUSH);
		editButton.setText("Edit..");
		editButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editSelectedNode();
			}
		});

		manageButton = new Button(parent, SWT.PUSH);
		manageButton.setText("Manage");
		manageButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				manageSelectedNode();
			}
		});
	}

	private Label createButtonSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setVisible(false);
		final RowData gd = new RowData();
		gd.height = 4;
		separator.setLayoutData(gd);
		return separator;
	}

	private Control createConnectGroup(final Composite parent) {
		final Composite connectGroup = new Composite(parent, SWT.NONE);
//		connectGroup.setText("Connection");

		final GridLayout innerLayout = new GridLayout();
		innerLayout.numColumns = 2;
		innerLayout.marginHeight = innerLayout.marginWidth = 0;
		connectGroup.setLayout(innerLayout);

		nodeIdField = new StringDialogField() {
			@Override
			protected Text createTextControl(final Composite parent) {
				return new Text(parent, SWT.SINGLE | SWT.READ_ONLY);
			}
		};
		nodeIdField.setLabelText("Node Id:");

		membershipStatusField = new LinkDialogField();
		membershipStatusField.setLabelText("Status:");

		LayoutUtil.doDefaultLayout(connectGroup, new DialogField[] { nodeIdField, membershipStatusField }, false);
		LayoutUtil.setHorizontalGrabbing(nodeIdField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(membershipStatusField.getLinkControl(null));

		// fix off-by-one issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=377605
		// TODO: doesn't work for Text :(
//		LayoutUtil.setHeightHint(nodeIdField.getLabelControl(null), heightHint);
//		LayoutUtil.setHeightHint(nodeIdField.getTextControl(null), heightHint);
		final int heightHint = membershipStatusField.getLabelControl(null).computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		LayoutUtil.setHeightHint(membershipStatusField.getLabelControl(null), heightHint);
		LayoutUtil.setHeightHint(membershipStatusField.getLinkControl(null), heightHint);

		membershipStatusField.getLinkControl(null).addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if ("#connect".equals(e.text)) {
					showConnectDialog();
				} else if ("#disconnect".equals(e.text)) {
					disconnectNode();
				}
			}
		});
		return connectGroup;
	}

	@Override
	public Control createControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		final Control connectGroup = createConnectGroup(composite);
		GridData gd = AdminUiUtil.createHorzFillData();
		gd.verticalIndent = 10;
		connectGroup.setLayoutData(gd);

		final Composite description = new Composite(composite, SWT.NONE);
		gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		final Control filteredTree = createNodeBrowser(description);
		filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new RowLayout(SWT.VERTICAL));
		createButtons(buttons);

		return composite;
	}

	private FilteredTree createNodeBrowser(final Composite parent) {
		final FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, new NodePatternFilter(), true);

		treeViewer = filteredTree.getViewer();
		treeViewer.getTree().setHeaderVisible(true);
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(60, 50));
		layout.addColumnData(new ColumnWeightData(30, 50));
		treeViewer.getTree().setLayout(layout);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(new NodeBrowserContentProvider());
		final NodeBrowserComparator comparator = new NodeBrowserComparator();
		treeViewer.setComparator(comparator);
		treeViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(final OpenEvent event) {
				manageSelectedNode();
			}
		});

		final TreeViewerColumn idColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		idColumn.getColumn().setText("Node ID");
		idColumn.getColumn().addSelectionListener(new NodeBrowserSortListener(comparator, SortIndex.ID, idColumn));
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof NodeItem)
					return ((NodeItem) element).getDescriptor().getId();
				return String.valueOf(element);
			}
		});
		treeViewer.getTree().setSortColumn(idColumn.getColumn());
		treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);

		final TreeViewerColumn locationColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		locationColumn.getColumn().setText("Location");
		locationColumn.getColumn().addSelectionListener(new NodeBrowserSortListener(comparator, SortIndex.LOCATION, locationColumn));
		locationColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof NodeItem)
					return ((NodeItem) element).getDescriptor().getLocation();
				return null;
			}
		});

		final TreeViewerColumn tagsColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		tagsColumn.getColumn().setText("Tags");
		tagsColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof NodeItem)
					return StringUtils.join(((NodeItem) element).getDescriptor().getTags(), ", ");
				return null;
			}
		});

		final TreeViewerColumn statusColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
		statusColumn.getColumn().setText("Status");
		statusColumn.getColumn().addSelectionListener(new NodeBrowserSortListener(comparator, SortIndex.STATUS, statusColumn));
		statusColumn.setLabelProvider(new ColumnLabelProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public String getText(final Object element) {
				if (element instanceof NodeItem) {
					final NodeItem nodeItem = (NodeItem) element;
					final StrBuilder status = new StrBuilder();
					if (nodeItem.isApproved()) {
						status.append("approved");
					} else {
						status.append("pending");
					}
					if (nodeItem.isOnline()) {
						status.appendSeparator(", ").append("online");
					}
					return status.toString();
				}
				return null;
			}
		});

		return filteredTree;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (treeViewer != null) {
			if (updateButtonsListener != null) {
				treeViewer.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (treeViewer.getTree().isDisposed()) {
				treeViewer.setInput(null);
			}
		}

		if (listener != null) {
			ZooKeeperGate.removeConnectionMonitor(listener);
			listener = null;
		}
	}

	void disconnectNode() {
		final ICloudManager cloudManager = getCloudManager();
		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(cloudManager.getLocalInfo().getNodeId());

		final IStatus status = nodeConfigurer.configureConnection(null);
		if (!status.isOK()) {
			Policy.getStatusHandler().show(status, "Error Disconnecting Node");
			return;
		}

		refresh();
	}

	void editSelectedNode() {
		final Object firstElement = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
		if (!(firstElement instanceof NodeItem))
			return;
		final NonBlockingStatusDialog dialog = new EditNodeDialog(SwtUtil.getShell(membershipStatusField.getLabelControl(null)), getCloudManager(), ((NodeItem) firstElement).getDescriptor());
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

	private ICloudManager getCloudManager() {
		return CloudUiActivator.getInstance().getCloudManager();
	}

	void manageSelectedNode() {
		final Object firstElement = ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
		if (!(firstElement instanceof NodeItem))
			return;

		getAdminUi().openPage(NodeAdminPage.ID, new String[] { ((NodeItem) firstElement).getDescriptor().getId() });
	}

	void refresh() {
		final ICloudManager cloudManager = getCloudManager();
		final INodeEnvironment localInfo = cloudManager.getLocalInfo();
		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(localInfo.getNodeId());

		nodeIdField.setText(localInfo.getNodeId());
		if (localInfo.inStandaloneMode()) {
			membershipStatusField.setText("The node operates standalone using an embedded ZooKeeper server. <a href=\"#connect\">Connect</a> it now.");
		} else {
			String serverInfo;
			try {
				serverInfo = ZooKeeperGate.get().getConnectedServerInfo();
			} catch (final Exception ignored) {
				serverInfo = null;
			}
			final String connectString = StringUtils.trimToEmpty(nodeConfigurer.getConnectionString());
			if (null != serverInfo) {
				membershipStatusField.setText(String.format("The node is connected to %s (using connect string '%s'). <a href=\"#disconnect\">Disconnect it.</a>", serverInfo, StringEscapeUtils.escapeXml(connectString)));
			} else {
				membershipStatusField.setText(String.format("The node is currently not connected (using connect string '%s'). <a href=\"#disconnect\">Disconnect it.</a>", StringEscapeUtils.escapeXml(connectString)));
			}
		}

		treeViewer.refresh();
		updateButtons();
	}

	void retireSelectedNodes() {
		final ICloudManager cloudManager = getCloudManager();
		final MultiStatus result = new MultiStatus(CloudUiActivator.SYMBOLIC_NAME, 0, "Some nodes could not be retired.", null);
		for (final Iterator stream = ((IStructuredSelection) treeViewer.getSelection()).iterator(); stream.hasNext();) {
			final Object object = stream.next();
			if (object instanceof NodeItem) {
				final NodeItem nodeItem = (NodeItem) object;
				if (nodeItem.isApproved()) {
					final IStatus status = cloudManager.retireNode(nodeItem.getDescriptor().getId());
					if (!status.isOK()) {
						result.add(status);
					}
				}
			}
		}
		if (!result.isOK()) {
			Policy.getStatusHandler().show(result, "Error");
		}
	}

	void showConnectDialog() {
		final NonBlockingStatusDialog dialog = new ConnectToCloudDialog(getCloudManager(), SwtUtil.getShell(membershipStatusField.getLabelControl(null)));
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

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) treeViewer.getSelection()).size();
		if (selectedElementsCount == 0) {
			approveButton.setEnabled(false);
			retireButton.setEnabled(false);
			editButton.setEnabled(false);
			manageButton.setEnabled(false);
			return;
		}

		boolean hasApprovedNodes = false;
		boolean hasPendingNodes = false;
		for (final Iterator stream = ((IStructuredSelection) treeViewer.getSelection()).iterator(); stream.hasNext();) {
			final Object object = stream.next();
			if (object instanceof NodeItem) {
				final NodeItem nodeItem = (NodeItem) object;
				hasApprovedNodes |= nodeItem.isApproved();
				hasPendingNodes |= !nodeItem.isApproved();
			}
			if (hasPendingNodes && hasApprovedNodes) {
				break;
			}
		}

		approveButton.setEnabled(hasPendingNodes);
		retireButton.setEnabled(hasApprovedNodes);
		editButton.setEnabled(selectedElementsCount == 1);
		manageButton.setEnabled(selectedElementsCount == 1);
	}
}
