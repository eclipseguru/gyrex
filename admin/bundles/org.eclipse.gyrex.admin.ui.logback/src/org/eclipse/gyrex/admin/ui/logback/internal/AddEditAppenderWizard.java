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
package org.eclipse.gyrex.admin.ui.logback.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardAdapter;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.LogbackConfig;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wizard for updating/creating appenders.
 */
public class AddEditAppenderWizard extends Wizard {

	private static final Logger LOG = LoggerFactory.getLogger(AddEditAppenderWizard.class);

	private static final IWizardPage[] NO_PAGES = new IWizardPage[0];
	private final Map<String, AppenderConfigurationWizardSession> sessionsByAppenderTypeId = new HashMap<String, AppenderConfigurationWizardSession>(2);

	private final LogbackConfig logbackConfig;
	private final Appender existingAppender;

	private final AppenderWizardPage appenderTypeWizardPage;

	private AppenderConfigurationWizardSession currentSession;

	public AddEditAppenderWizard(final LogbackConfig logbackConfig, final Appender existingAppender) {
		this.logbackConfig = logbackConfig;
		this.existingAppender = existingAppender;

		appenderTypeWizardPage = new AppenderWizardPage();
		if (existingAppender != null) {
			appenderTypeWizardPage.initializeFromExistingAppender(existingAppender);
		}

		// force previous and next buttons (we don't know about potential appender type pages)
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(appenderTypeWizardPage);
	}

	@Override
	public boolean canFinish() {
		return appenderTypeWizardPage.isPageComplete() && (currentSession != null) && currentSession.canFinish();
	}

	void clearCurrentAppenderConfigurationSession() {
		currentSession = null;
		getContainer().showPage(appenderTypeWizardPage);
	}

	private AppenderConfigurationWizardSession findExistingSessionOrCreateNew(final String id, final String name) {
		if (!sessionsByAppenderTypeId.containsKey(id)) {
			final AppenderConfigurationWizardSession session = new AppenderConfigurationWizardSession(id, name);
			sessionsByAppenderTypeId.put(id, session);
		}

		final AppenderConfigurationWizardSession session = sessionsByAppenderTypeId.get(id);
		return session;
	}

	public Appender getAppender() {
		checkState(currentSession != null, "No appender type selected!");
		final Appender appender = currentSession.getAppender();
		checkState(appender != null, "No appender initialized for appender type '%s'!", currentSession.getAppenderTypeId());
		return appender;
	}

	public LogbackConfig getLogbackConfig() {
		return logbackConfig;
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: appenderTypeWizardPage (if available)
		//    2-..: appender type specific pages
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;
		if (page == appenderTypeWizardPage) {
			if (sessionPages.length > 0) {
				return sessionPages[0];
			} else {
				return null;
			}
		}

		// find the current appender type page
		for (int i = 0; i < sessionPages.length; i++) {
			if (page == sessionPages[i]) {
				if ((i + 1) < sessionPages.length) {
					return sessionPages[i + 1];
				} else {
					return null;
				}
			}
		}

		return null;
	}

	@Override
	public IWizardPage getPreviousPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: appenderTypeWizardPage
		//    2-..: appender type specific pages
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;

		if (page == appenderTypeWizardPage) {
			return null;
		}

		// find the current appender type page
		for (int i = sessionPages.length - 1; i >= 0; i--) {
			if (page == sessionPages[i]) {
				if ((i - 1) >= 0) {
					return sessionPages[i - 1];
				} else {
					return appenderTypeWizardPage;
				}
			}
		}

		return null;
	}

	void initializeCurrentAppenderConfigurationSession(final String id, final String name, final AppenderConfigurationWizardAdapter wizardAdapter) {
		checkState(wizardAdapter != null, "No adapter available for editing appenders of type '%s'!", id);

		final AppenderConfigurationWizardSession session = findExistingSessionOrCreateNew(id, name);
		if (session == currentSession) {
			// nothing changed
			return;
		}

		if (session.getAppender() == null) {
			wizardAdapter.initializeAppender(session, existingAppender);
		}

		// lazy initialize pages
		if (null == session.getPages()) {
			final IWizardPage[] pages = wizardAdapter != null ? wizardAdapter.createPages(session) : NO_PAGES;
			if (pages != null) {
				session.setPages(pages);
				for (final IWizardPage page : pages) {
					addPage(page);
				}
			} else {
				LOG.debug("No pages returned for appender type {} (adapter {})", id, wizardAdapter);
				session.setPages(NO_PAGES);
			}
		}

		currentSession = session;
		if (null != getContainer().getCurrentPage()) {
			getContainer().updateButtons();
		}
	}

	@Override
	public boolean performFinish() {
		try {
			getAppender().setName(appenderTypeWizardPage.getAppenderName());
			// we rely on all pages applying any valid values immediately;
			// thus, no further action here
			return true;
		} catch (final Exception | LinkageError | AssertionError e) {
			// handle error
			LOG.debug("Error writing appender. ", e);
			NonBlockingMessageDialogs.openError(getShell(), "Error Updating Appender", "Unable to update appender. " + e.getMessage(), null);
			return false;
		}
	}

}
