/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal.externalprocess;

import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardAdapter;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardSession;

import org.eclipse.jface.wizard.IWizardPage;

public class ExternalProcessJobConfigurationWizardAdapter extends JobConfigurationWizardAdapter {

	@Override
	public IWizardPage[] createPages(final JobConfigurationWizardSession session) {
		return new IWizardPage[] { new ExternalProcessWizardPage(session) };
	}

}
