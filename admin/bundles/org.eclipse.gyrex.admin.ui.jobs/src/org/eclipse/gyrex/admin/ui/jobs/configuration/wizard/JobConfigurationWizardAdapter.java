/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.configuration.wizard;

import org.eclipse.gyrex.jobs.provider.JobProvider;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adapter to allow {@link JobProvider job providers} to participate in the
 * wizard driven job configuration user interface.
 */
public abstract class JobConfigurationWizardAdapter {

	/**
	 * Creates and returns the job configuration specific wizard pages for the
	 * specified session.
	 * 
	 * @param session
	 */
	public abstract IWizardPage[] createPages(JobConfigurationWizardSession session);

}
