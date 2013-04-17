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
package org.eclipse.gyrex.admin.ui.logback.internal.commonapenders;

import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardAdapter;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.logback.config.internal.CommonLogbackAppenders;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adapter for {@link CommonLogbackAppenders}
 */
public class CommonApendersWizardAdapter extends AppenderConfigurationWizardAdapter {

	@Override
	public IWizardPage[] createPages(final AppenderConfigurationWizardSession session) {
		switch (session.getAppenderTypeId()) {
			case "console":
				return new IWizardPage[] { new ConsoleAppenderWizardPage(session) };

			case "file":
				return new IWizardPage[] { new FileAppenderWizardPage(session) };

			default:
				break;
		}
		return null;
	}

}
