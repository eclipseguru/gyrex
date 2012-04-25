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

import org.eclipse.gyrex.cloud.admin.ICloudManager;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

/**
 *
 */
public class OnlineNodesSection extends NodeListSection {

	public OnlineNodesSection(final Composite parent, final ClusterAdminPage page) {
		super(parent, page.getManagedForm().getToolkit(), ExpandableComposite.TITLE_BAR, page.getBindingContext());
		final Section section = getSection();
		section.setText("Online Nodes");
		createContent(section);
	}

	@Override
	protected void createButtonPanel(final Composite parent) {
		// no buttons
	}

	@Override
	protected Collection loadNodes(final ICloudManager cloudManager) {
		return cloudManager.getOnlineNodes();
	}
}
