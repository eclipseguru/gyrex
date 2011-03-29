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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;

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

	}
}
