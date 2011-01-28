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
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 *
 */
public class PendingNodesSection extends NodeListSection {

	private Button approveButton;

	public PendingNodesSection(final Composite parent, final CloudConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), ExpandableComposite.TITLE_BAR, page.getBindingContext());
		final Section section = getSection();
		section.setText("Pending Nodes");
		createContent(section);
	}

	void approveButtonPressed() {
		final INodeDescriptor descriptor = (INodeDescriptor) (null != selectedNodeValue ? selectedNodeValue.getValue() : null);
		if (descriptor == null) {
			return;
		}

		if (MessageDialog.openConfirm(SwtUtil.getShell(approveButton), "Approve Node", "The selected node will be approved. Please confirm!")) {
			getCloudManager().approveNode(descriptor.getId());
		}
	}

	@Override
	protected void createButtons(final Composite parent) {
		approveButton = createButton(parent, "Approve", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				approveButtonPressed();
			}
		});
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		getBindingContext().bindValue(SWTObservables.observeEnabled(approveButton), SWTObservables.observeSelection(nodesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
	}

	@Override
	protected Collection<INodeDescriptor> loadNodes(final ICloudManager cloudManager) {
		return cloudManager.getPendingNodes();
	}

}
