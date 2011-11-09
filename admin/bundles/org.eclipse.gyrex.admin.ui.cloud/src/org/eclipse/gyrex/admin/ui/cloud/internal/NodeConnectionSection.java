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

import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.FormTextDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeConfigurer;
import org.eclipse.gyrex.cloud.environment.INodeEnvironment;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class NodeConnectionSection extends SectionPart {

	private StringDialogField nodeIdField;
	private FormTextDialogField membershipStatusField;

	/**
	 * Creates a new instance.
	 * 
	 * @param page
	 * @param parent
	 */
	public NodeConnectionSection(final CloudConfigurationPage page, final Composite parent) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		createContent(getSection(), page.getManagedForm().getToolkit());
	}

	@Override
	public void commit(final boolean onSave) {
		// reset dirty
		super.commit(onSave);
	}

	private void createContent(final Section section, final FormToolkit toolkit) {
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));

		section.setText("Node Information");
		section.setDescription("This section describes the general information about the local node.");

		final Composite client = toolkit.createComposite(section);
		section.setClient(client);
//		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 3));
//		FormLayoutDataFactory.applyDefaults(client, 1);

		nodeIdField = new StringDialogField() {
			@Override
			protected Text createTextControl(final Composite parent) {
				return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			}
		};
		nodeIdField.setLabelText("Node Id");

		membershipStatusField = new FormTextDialogField();
		membershipStatusField.setLabelText("Membership");

		LayoutUtil.doDefaultLayout(client, new DialogField[] { nodeIdField, membershipStatusField }, false);
		LayoutUtil.setHorizontalGrabbing(nodeIdField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(membershipStatusField.getTextControl(null));

		membershipStatusField.adaptToForm(toolkit);
		membershipStatusField.getTextControl(null).addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(final HyperlinkEvent e) {
				if ("#connect".equals(e.getHref())) {
					showConnectDialog();
				} else if ("#disconnect".equals(e.getHref())) {
					disconnectNode();
				}
			}
		});
	}

	void disconnectNode() {
		final ICloudManager cloudManager = getCloudManager();
		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(cloudManager.getLocalInfo().getNodeId());

		final IStatus status = nodeConfigurer.configureConnection(null);
		if (!status.isOK()) {
			Policy.getStatusHandler().show(status, "Error Disconnecting Node");
			return;
		}

		markStale();
	}

	protected ICloudManager getCloudManager() {
		return (ICloudManager) getManagedForm().getInput();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);
	}

	@Override
	public void refresh() {
		final ICloudManager cloudManager = getCloudManager();
		final INodeEnvironment localInfo = cloudManager.getLocalInfo();
		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(localInfo.getNodeId());

		nodeIdField.setText(localInfo.getNodeId());
		if (localInfo.inStandaloneMode()) {
			membershipStatusField.setText("<form><p>The node operates standalone. <a href=\"#connect\">Connect</a> it now.</p></form>", true, false);
		} else {
			membershipStatusField.setText(String.format("<form><p>The node is connected to '%s'. <a href=\"#disconnect\">Disconnect it.</a></p></form>", StringEscapeUtils.escapeXml(StringUtils.trimToEmpty(nodeConfigurer.getConnectionString()))), true, false);
		}

		// call super
		super.refresh();
	}

	@Override
	public boolean setFormInput(final Object input) {
		if (input instanceof ICloudManager) {
			markStale();
			return true;
		}
		return super.setFormInput(input);
	}

	void showConnectDialog() {
		final ConnectToCloudDialog connectToCloudDialog = new ConnectToCloudDialog(getCloudManager(), SwtUtil.getShell(membershipStatusField.getTextControl(null)));
		if (connectToCloudDialog.open() == Window.OK) {
			markStale();
		}
	}
}
