/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.pages.overview;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.pages.OverviewPageItem;
import org.eclipse.gyrex.boot.internal.app.ServerApplication;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

public class NodeShortcuts extends OverviewPageItem {

	@Override
	public Control createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, true));
		composite.setLayoutData(AdminUiUtil.createHorzFillData());

		AdminUiUtil.createHeading(composite, "Shortcuts", 1);

		final Label desc = new Label(composite, SWT.WRAP);
		desc.setText("Some convenience shortcuts for the system.");
		desc.setLayoutData(AdminUiUtil.createHorzFillData());

		final Link restartLink = new Link(composite, SWT.NONE);
		restartLink.setText("<a>Restart local node</a>");
		restartLink.setLayoutData(AdminUiUtil.createHorzFillData());
		restartLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (MessageDialog.openConfirm(SwtUtil.getShell(restartLink), "Restart Node", "The node will be restarted. Please confirm!")) {
					ServerApplication.restart();
				}
			}
		});

		return composite;
	}

}
