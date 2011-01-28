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

import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 *
 */
public class NodeDetails extends AbstractFormPart implements IDetailsPage {

	private INodeDescriptor nodeDescriptor;

	@Override
	public void createContents(final Composite parent) {
		parent.setLayout(FormLayoutFactory.createDetailsGridLayout(false, 1));
		final FormToolkit toolkit = getManagedForm().getToolkit();
		final Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
		section.setText("Node Details");
		section.setDescription("Set the properties of the selected node.");
		section.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
	}

	@Override
	public void refresh() {
		if (null != nodeDescriptor) {

		} else {

		}

		super.refresh();
	}

	@Override
	public void selectionChanged(final IFormPart part, final ISelection selection) {
		final IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			nodeDescriptor = (INodeDescriptor) ssel.getFirstElement();
		} else {
			nodeDescriptor = null;
		}
		refresh();
	}

}
