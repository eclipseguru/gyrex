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

import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adapter to allow {@link AppenderProvider appender providers} to participate
 * in the wizard driven appender configuration user interface.
 */
public abstract class AppenderConfigurationWizardAdapter {

	/**
	 * Creates and returns the appender configuration specific wizard pages for
	 * the specified session.
	 * 
	 * @param session
	 */
	public abstract IWizardPage[] createPages(AppenderConfigurationWizardSession session);

}
