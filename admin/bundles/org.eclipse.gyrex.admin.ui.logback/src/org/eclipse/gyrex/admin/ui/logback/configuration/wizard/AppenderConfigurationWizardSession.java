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
package org.eclipse.gyrex.admin.ui.logback.configuration.wizard;

import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * A container to maintain configuration session state.
 */
public final class AppenderConfigurationWizardSession {

	private final String appenderTypeId;
	private Appender appender;
	private IWizardPage[] pages;
	private final String appenderTypeName;

	/**
	 * Creates a new instance.
	 * 
	 * @param appenderTypeId
	 *            the appender type id
	 * @param appenderTypeName
	 *            the appender type name
	 * @noreference This constructor is not intended to be referenced by
	 *              clients.
	 */
	public AppenderConfigurationWizardSession(final String appenderTypeId, final String appenderTypeName) {
		this.appenderTypeId = appenderTypeId;
		this.appenderTypeName = appenderTypeName;
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
	 * Returns the appender.
	 * 
	 * @return the appender
	 */
	public final Appender getAppender() {
		return appender;
	}

	/**
	 * Returns the appender type id.
	 * 
	 * @return the appender type id
	 */
	public final String getAppenderTypeId() {
		return appenderTypeId;
	}

	/**
	 * Returns a human readable name of the appender type.
	 * 
	 * @return the appender type name
	 * @see AppenderProvider#getName(String)
	 */
	public String getAppenderTypeName() {
		return appenderTypeName;
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
	 * Sets the appender.
	 * 
	 * @param appender
	 *            the appender to set
	 */
	public void setAppender(final Appender appender) {
		this.appender = appender;
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
