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

import org.eclipse.gyrex.admin.ui.cloud.internal.NodeBrowserContentProvider.NodeItem;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LinkDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeConfigurer;
import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

/**
 * Gyrex Cloud Configuration Page.
 */
public class ClusterAdminPage extends ZooKeeperBasedAdminPage {

	private StringDialogField nodeIdField;
	private LinkDialogField membershipStatusField;

	private Button approveButton;
	private Button retireButton;
	private Button editButton;
	private Button manageButton;

	public static final int ID = 0;
	public static final int LOCATION = 1;
	public static final int TAGS = 2;
	public static final int STATUS = 3;

	/**
	 * Creates a new instance.
	 */
	public ClusterAdminPage() {
		super(4);
		setTitle("Cluster Configuration");
		setTitleToolTip("Configure the cluster of nodes in the system.");
	}

	void approveSelectedNodes() {
		final ICloudManager cloudManager = getCloudManager();
		final MultiStatus result = new MultiStatus(CloudUiActivator.SYMBOLIC_NAME, 0, "Some nodes could not be approved.", null);
		for (final Iterator stream = ((IStructuredSelection) getTreeViewer().getSelection()).iterator(); stream.hasNext();) {
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

	@Override
	protected void createButtons(final Composite parent) {
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
				openSelectedElement();
			}
		});
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
	protected ITreeContentProvider createContentProvider() {
		return new NodeBrowserContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		return createConnectGroup(parent);
	}

	@Override
	protected PatternFilter createPatternFilter() {
		return new NodePatternFilter();
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
		final Object firstElement = ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
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

	ICloudManager getCloudManager() {
		return CloudUiActivator.getInstance().getCloudManager();
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case ID:
				return "Node";
			case LOCATION:
				return "Location";
			case TAGS:
				return "Tags";
			case STATUS:
				return "Status";
			default:
				return String.valueOf(column);
		}
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof NodeItem) {
			final NodeItem nodeItem = (NodeItem) element;
			switch (column) {
				case ID:
					return nodeItem.getDescriptor().getId();
				case LOCATION:
					return nodeItem.getDescriptor().getLocation();
				case STATUS:
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
				case TAGS:
					return StringUtils.join(((NodeItem) element).getDescriptor().getTags(), ", ");
				default:
					break;
			}
		}
		return String.valueOf(element);
	}

	@Override
	protected String getElementTextForSorting(final Object element, final int column) {
		if (element instanceof NodeItem) {
			final NodeItem node = (NodeItem) element;
			switch (column) {
				case STATUS:
					return node.isApproved() ? node.isOnline() ? "A1" : "A2" : node.isOnline() ? "P1" : "P2";
				default:
					// fall-through
					break;
			}
		}
		return super.getElementTextForSorting(element, column);
	}

	@Override
	protected Object getViewerInput() {
		return getCloudManager();
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		switch (column) {
			case ID:
			case LOCATION:
			case STATUS:
				return true;
			case TAGS:
			default:
				return false;
		}
	}

	@Override
	protected void openSelectedElement() {
		final Object firstElement = ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
		if (!(firstElement instanceof NodeItem))
			return;

		getAdminUi().openPage(NodeAdminPage.ID, new String[] { ((NodeItem) firstElement).getDescriptor().getId() });
	}

	@Override
	protected void refresh() {
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
	}

	void retireSelectedNodes() {
		final ICloudManager cloudManager = getCloudManager();
		final MultiStatus result = new MultiStatus(CloudUiActivator.SYMBOLIC_NAME, 0, "Some nodes could not be retired.", null);
		for (final Iterator stream = ((IStructuredSelection) getTreeViewer().getSelection()).iterator(); stream.hasNext();) {
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

	@Override
	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) getTreeViewer().getSelection()).size();
		if (selectedElementsCount == 0) {
			approveButton.setEnabled(false);
			retireButton.setEnabled(false);
			editButton.setEnabled(false);
			manageButton.setEnabled(false);
			return;
		}

		boolean hasApprovedNodes = false;
		boolean hasPendingNodes = false;
		for (final Iterator stream = ((IStructuredSelection) getTreeViewer().getSelection()).iterator(); stream.hasNext();) {
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
