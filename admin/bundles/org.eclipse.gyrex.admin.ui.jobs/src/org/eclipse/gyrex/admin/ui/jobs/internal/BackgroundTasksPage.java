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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
public class BackgroundTasksPage extends FilteredAdminPage {

	public static final String ID = "background-tasks";

	private SchedulesSection schedulesSection;
	private Composite pageComposite;

	/**
	 * Creates a new instance.
	 */
	public BackgroundTasksPage() {
		setTitle("Background Tasks");
		setTitleToolTip("Configure schedules for executing background tasks.");
	}

	@Override
	public void activate() {
		super.activate();

		if (schedulesSection != null) {
			schedulesSection.activate();
		}

	}

	@Override
	public Control createControl(final Composite parent) {

		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(pageComposite);
			final GridData gd = AdminUiUtil.createHorzFillData();
			infobox.setLayoutData(gd);
			infobox.addHeading("Schedules.");
			infobox.addParagraph("Background tasks in Gyrex are organized into schedules. A schedule is associated to a context and defines common properties (such as timezone) for all background tasks.");
			infobox.addParagraph("Gyrex schedule are bound to a context path e.g. an application context and they group all the schedules together, which belopng to this application context. Gyrex scheduler can be enabled and disabled to be able to switch the background tasks for a specific application context on and off.");
		}

		schedulesSection = new SchedulesSection(this);
		schedulesSection.createSchedulesControls(pageComposite);

		return pageComposite;
	}
}
