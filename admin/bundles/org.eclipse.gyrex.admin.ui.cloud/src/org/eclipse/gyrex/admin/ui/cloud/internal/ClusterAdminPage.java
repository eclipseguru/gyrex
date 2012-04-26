/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
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

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
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
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Gyrex Cloud Configuration Page.
 */
public class ClusterAdminPage extends AdminPage {

	private ZooKeeperGateListener listener;

	private FilteredTree filteredTree;
	private StringDialogField nodeIdField;
	private LinkDialogField membershipStatusField;

	private Composite composite;

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

		if (null == filteredTree) {
			return;
		}

		final Display display = filteredTree.getViewer().getControl().getDisplay();
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

		refresh();
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
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		final Control connectGroup = createConnectGroup(composite);
		GridData gd = AdminUiUtil.createHorzFillData(2);
		gd.verticalIndent = 10;
		connectGroup.setLayoutData(gd);

		filteredTree = createNodeBrowser(composite);
		gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		filteredTree.setLayoutData(gd);

		return composite;
	}

	private FilteredTree createNodeBrowser(final Composite parent) {
		final FilteredTree filteredTree = new FilteredTree(parent, SWT.NONE, new NodePatternFilter(), true);

		final TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new NodeBrowserContentProvider());

		return filteredTree;
	}

	@Override
	public void deactivate() {
		super.deactivate();
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

	private ICloudManager getCloudManager() {
		return CloudUiActivator.getInstance().getCloudManager();
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

		filteredTree.getViewer().refresh();
	}

	void showConnectDialog() {
		final ConnectToCloudDialog connectToCloudDialog = new ConnectToCloudDialog(getCloudManager(), SwtUtil.getShell(membershipStatusField.getLabelControl(null)));
		if (connectToCloudDialog.open() == Window.OK) {
			refresh();
		}
	}
}
