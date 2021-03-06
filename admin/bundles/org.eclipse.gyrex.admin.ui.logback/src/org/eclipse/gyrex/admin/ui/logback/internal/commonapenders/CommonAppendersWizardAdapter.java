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
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.ConsoleAppender;
import org.eclipse.gyrex.logback.config.model.FileAppender;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adapter for {@link CommonLogbackAppenders}
 */
public class CommonAppendersWizardAdapter extends AppenderConfigurationWizardAdapter {

	@Override
	protected Appender createAppender(final String typeId) throws IllegalArgumentException {
		switch (typeId) {
			case "console":
				return new ConsoleAppender();

			case "file":
				return new FileAppender();

			default:
				throw new IllegalArgumentException("unsupported type: " + typeId);
		}
	}

	@Override
	public IWizardPage[] createPages(final AppenderConfigurationWizardSession session) {
		switch (session.getAppenderTypeId()) {
			case "console":
				return new IWizardPage[] { new ConsoleAppenderWizardPage(session), new AppenderThresholdWizardPage(session) };

			case "file":
				return new IWizardPage[] { new FileAppenderWizardPage(session), new AppenderThresholdWizardPage(session) };

			default:
				break;
		}
		return null;
	}

	@Override
	protected boolean isCompatibleAppender(final String typeId, final Appender appender) throws IllegalArgumentException {
		switch (typeId) {
			case "console":
				return appender instanceof ConsoleAppender;

			case "file":
				return appender instanceof FileAppender;

			default:
				throw new IllegalArgumentException("unsupported type: " + typeId);
		}
	}

}
