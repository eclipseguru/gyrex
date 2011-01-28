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

import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutDataFactory;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeConfigurer;
import org.eclipse.gyrex.cloud.environment.INodeEnvironment;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class NodeConnectionSection extends SectionPart {

	private final CloudConfigurationPage page;
	private Text connectStringText;
	private Button cloudMemberButton;
	private Text nodeIdText;
	private Button connectButton;

	/**
	 * Creates a new instance.
	 * 
	 * @param page
	 * @param parent
	 */
	public NodeConnectionSection(final CloudConfigurationPage page, final Composite parent) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		this.page = page;
		createContent(getSection(), page.getManagedForm().getToolkit());
	}

	@Override
	public void commit(final boolean onSave) {
		// reset dirty
		super.commit(onSave);
	}

	void connectButtonPressed() {
		final ICloudManager cloudManager = getCloudManager();
		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(cloudManager.getLocalInfo().getNodeId());
		IStatus status;
		if (cloudMemberButton.getSelection()) {
			status = nodeConfigurer.configureConnection(connectStringText.getText());
		} else {
			status = nodeConfigurer.configureConnection(null);
		}
		if (!status.isOK()) {
			Policy.getStatusHandler().show(status, "Error Configuring Node");
		}
	}

	private void createContent(final Section section, final FormToolkit toolkit) {
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));

		section.setText("Node Information");
		section.setDescription("This section describes the general information about the local node.");

		final Composite client = toolkit.createComposite(section);
		section.setClient(client);
		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 3));
		FormLayoutDataFactory.applyDefaults(client, 1);

		final Label idLabel = toolkit.createLabel(client, "Node Id");
		nodeIdText = toolkit.createText(client, StringUtils.EMPTY, SWT.SINGLE | SWT.READ_ONLY);
		FormLayoutDataFactory.applyDefaults(idLabel, 1);
		FormLayoutDataFactory.applyDefaults(nodeIdText, 2);

		final Label modeLabel = toolkit.createLabel(client, "Membership");
		cloudMemberButton = toolkit.createButton(client, "The local node is a member of a cloud.", SWT.CHECK);
		FormLayoutDataFactory.applyDefaults(modeLabel, 1);
		FormLayoutDataFactory.applyDefaults(cloudMemberButton, 2);

		final Label cloudLabel = toolkit.createLabel(client, "Connection String:");
		connectStringText = toolkit.createText(client, StringUtils.EMPTY);
		connectButton = toolkit.createButton(client, "Connect", SWT.PUSH);
		FormLayoutDataFactory.applyDefaults(cloudLabel, 1);
		FormLayoutDataFactory.applyDefaults(connectStringText, 1);

		connectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				connectButtonPressed();
			};
		});

		final DataBindingContext bindingContext = page.getBindingContext();
		final ISWTObservableValue cloudMemberSelectionValue = SWTObservables.observeSelection(cloudMemberButton);
		bindingContext.bindValue(SWTObservables.observeEnabled(connectStringText), cloudMemberSelectionValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
		bindingContext.bindValue(SWTObservables.observeEnabled(connectButton), cloudMemberSelectionValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
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

		nodeIdText.setText(localInfo.getNodeId());
		cloudMemberButton.setSelection(!localInfo.inStandaloneMode());

		final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(localInfo.getNodeId());
		connectStringText.setText(StringUtils.trimToEmpty(nodeConfigurer.getConnectionString()));

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
}
