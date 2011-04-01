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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Configuration page for Jobs.
 */
public class JobsConfigurationPage extends ConfigurationPage {

	/**
	 * Creates a new instance.
	 */
	public JobsConfigurationPage() {
		setTitle("Job Configuration");
		setTitleToolTip("Schedule and manage jobs.");
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
		body.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		final FormToolkit toolkit = managedForm.getToolkit();

		final Composite left = toolkit.createComposite(body);
		left.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		left.setLayout(GridLayoutFactory.fillDefaults().create());

		final SchedulesSection schedulesSection = new SchedulesSection(left, this);
		schedulesSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(schedulesSection);

		final Composite right = toolkit.createComposite(body);
		right.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		right.setLayout(GridLayoutFactory.fillDefaults().create());

		final WaitingJobsSection waitingSection = new WaitingJobsSection(right, this);
		waitingSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		managedForm.addPart(waitingSection);

		final RunningJobsSection runningSection = new RunningJobsSection(right, this);
		runningSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		managedForm.addPart(runningSection);

		final JobsLogSection logSection = new JobsLogSection(right, this);
		logSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(logSection);

	}
}
