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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * A container to maintain configuration session state.
 */
public final class JobConfigurationWizardSession {

	private final String jobTypeId;
	private final Map<String, String> parameter;
	private IWizardPage[] pages;

	/**
	 * Creates a new instance.
	 * 
	 * @param jobTypeId
	 *            the job type id
	 * @noreference This constructor is not intended to be referenced by
	 *              clients.
	 */
	public JobConfigurationWizardSession(final String jobTypeId) {
		this.jobTypeId = jobTypeId;
		parameter = new HashMap<>();
	}

	public boolean canFinish() {
		// never finish if pages haven't been initialized
		if (pages == null)
			return false;

		// do not finish if one of the pages is not complete
		for (final IWizardPage page : pages) {
			if (!page.isPageComplete())
				return false;
		}

		// all good
		return true;
	}

	/**
	 * Returns the job type id.
	 * 
	 * @return the job type id
	 */
	public final String getJobTypeId() {
		return jobTypeId;
	}

	/**
	 * Returns the pages.
	 * 
	 * @return the pages
	 */
	public final IWizardPage[] getPages() {
		return pages;
	}

	/**
	 * Returns the underlying parameter map.
	 * 
	 * @return the parameter map
	 */
	public final Map<String, String> getParameter() {
		return parameter;
	}

	/**
	 * Sets the pages.
	 * 
	 * @param pages
	 *            the pages to set
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void setPages(final IWizardPage[] pages) {
		this.pages = pages;
	}
}
