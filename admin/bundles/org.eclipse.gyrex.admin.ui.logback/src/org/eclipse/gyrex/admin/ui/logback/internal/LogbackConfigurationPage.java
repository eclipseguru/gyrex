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
 *     Peter Grube        - rework to Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class LogbackConfigurationPage extends FilteredAdminPage {

	private Composite pageComposite;

	/**
	 * Creates a new instance.
	 */
	public LogbackConfigurationPage() {
		setTitle("Logback Configuration");
		setTitleToolTip("Configure and assign Logback appenders and loggers.");
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(pageComposite);
			infobox.setLayoutData(AdminUiUtil.createHorzFillData());
			infobox.addHeading("Logbacks in Gyrex");
			infobox.addParagraph("Configure your Logbacks.");
		}

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		final LogbackSection logbackSection = new LogbackSection(description, this);

		return pageComposite;

	}

}
