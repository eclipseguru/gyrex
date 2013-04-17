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
package org.eclipse.gyrex.admin.ui.logback.internal;

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
	private final Appender appender;

	private final AppenderWizardPage appenderTypeWizardPage;

	private AppenderConfigurationWizardSession currentSession;

	public AddEditAppenderWizard(final LogbackConfig logbackConfig, final Appender appender) {
		this.logbackConfig = logbackConfig;
		this.appender = appender;

		appenderTypeWizardPage = new AppenderWizardPage(appender);

		// force previous and next buttons (we don't know about potential job type pages)
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

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: appenderTypeWizardPage (if available)
		//    2-..: job type specific pages
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;
		if (page == appenderTypeWizardPage) {
			if (sessionPages.length > 0)
				// show first job type page after scheduleEntryPage
				return sessionPages[0];
			else
				// no next page
				return null;
		}

		// find the current job type page
		for (int i = 0; i < sessionPages.length; i++) {
			if (page == sessionPages[i]) {
				if ((i + 1) < sessionPages.length)
					// show next job type page
					return sessionPages[i + 1];
				else
					// no next page
					return null;
			}
		}

		// no next page
		return null;
	}

	@Override
	public IWizardPage getPreviousPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: appenderTypeWizardPage
		//    2-..: job type specific pages
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;

		if (page == appenderTypeWizardPage)
			// no previous page for the first page
			return null;

		// find the current job type page
		for (int i = sessionPages.length - 1; i >= 0; i--) {
			if (page == sessionPages[i]) {
				if ((i - 1) >= 0)
					// show previous job type page
					return sessionPages[i - 1];
				else
					// previous page is appenderTypeWizardPage if not in edit mode
					return isEditMode() ? null : appenderTypeWizardPage;
			}
		}

		// no previous page
		return null;
	}

	public LogbackConfig getSchedule() {
		return logbackConfig;
	}

	public Appender getScheduleEntry() {
		return appender;
	}

	void initializeCurrentAppenderConfigurationSession(final String id, final String name, final AppenderConfigurationWizardAdapter wizardAdapter) {
		if (!sessionsByAppenderTypeId.containsKey(id)) {
			final AppenderConfigurationWizardSession session = new AppenderConfigurationWizardSession(id, name);
			session.setAppender(appender);
			sessionsByAppenderTypeId.put(id, session);
		}

		final AppenderConfigurationWizardSession session = sessionsByAppenderTypeId.get(id);

		if (session == currentSession)
			// nothing changed
			return;

		// lazy initialize pages
		if (null == session.getPages()) {
			final IWizardPage[] pages = wizardAdapter != null ? wizardAdapter.createPages(session) : new IWizardPage[0];
			if (pages != null) {
				session.setPages(pages);
				for (final IWizardPage page : pages) {
					addPage(page);
				}
			} else {
				LOG.debug("No pages returned for job type {} (adapter {})", id, wizardAdapter);
				session.setPages(NO_PAGES);
			}

		}

		currentSession = session;
		if (null != getContainer().getCurrentPage()) {
			getContainer().updateButtons();
		}
	}

	public final boolean isEditMode() {
		return appender != null;
	}

	@Override
	public boolean performFinish() {
		try {
			return true;
		} catch (final Exception | LinkageError | AssertionError e) {
			// handle error
			LOG.debug("Error adding appender. ", e);
			NonBlockingMessageDialogs.openError(getShell(), "Error Updating Appender", "Unable to update appender. " + e.getMessage(), null);
			return false;
		}
	}

}
