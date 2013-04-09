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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardAdapter;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardSession;
import org.eclipse.gyrex.admin.ui.jobs.internal.generic.GenericJobParameterPage;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.manager.IJobManager;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wizard for adding/updating a schedule entry.
 */
public class ScheduleEntryWizard extends Wizard {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduleEntryWizard.class);

	private static final IWizardPage[] NO_PAGES = new IWizardPage[0];
	private final Map<String, JobConfigurationWizardSession> sessionsByJobTypeId = new HashMap<String, JobConfigurationWizardSession>(2);

	private final ScheduleImpl schedule;
	private final ScheduleEntryImpl entry;
	private final ScheduleEntryWizardPage scheduleEntryPage;

	private JobConfigurationWizardSession currentSession;

	private final ExclusiveLockWizardPage exclusiveLockPage;

	public ScheduleEntryWizard(final ScheduleImpl schedule, final ScheduleEntryImpl entry) {
		this.schedule = schedule;
		this.entry = entry;
		scheduleEntryPage = new ScheduleEntryWizardPage(schedule, entry);
		exclusiveLockPage = new ExclusiveLockWizardPage(schedule, entry);

		// force previous and next buttons (we don't know about potential job type pages)
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(scheduleEntryPage);
		addPage(exclusiveLockPage);
	}

	@Override
	public boolean canFinish() {
		return scheduleEntryPage.isPageComplete() && (currentSession != null) && currentSession.canFinish();
	}

	void clearCurrentJobConfigurationSession() {
		currentSession = null;
		getContainer().showPage(scheduleEntryPage);
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: scheduleEntryPage
		//    2-..: job type specific pages
		//    last: exclusiveLockPage
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;
		if (page == scheduleEntryPage) {
			if (sessionPages.length > 0)
				// show first job type page after scheduleEntryPage
				return sessionPages[0];
			else
				// or go to exclusiveLockPage right away if there are no job type pages
				return exclusiveLockPage;
		}

		if (page == exclusiveLockPage)
			// no next page for the first page
			return null;

		// find the current job type page
		for (int i = 0; i < sessionPages.length; i++) {
			if (page == sessionPages[i]) {
				if ((i + 1) < sessionPages.length)
					// show next job type page
					return sessionPages[i + 1];
				else
					// no next job type page; go to exclusiveLockPage
					return exclusiveLockPage;
			}
		}
		// no next page
		return null;
	}

	@Override
	public IWizardPage getPreviousPage(final IWizardPage page) {
		// REMINDER: this logic is inverted in #getPreviousPage
		// the flow is as follows
		//   first: scheduleEntryPage
		//    2-..: job type specific pages
		//    last: exclusiveLockPage
		final IWizardPage[] sessionPages = null != currentSession ? currentSession.getPages() : NO_PAGES;
		if (page == exclusiveLockPage) {
			if (sessionPages.length > 0)
				// show last job type page before exclusiveLockPage
				return sessionPages[sessionPages.length - 1];
			else
				// or go to scheduleEntryPage right away if there are no job type pages
				return scheduleEntryPage;
		}

		if (page == scheduleEntryPage)
			// no previous page for the first page
			return null;

		// find the current job type page
		for (int i = sessionPages.length - 1; i >= 0; i--) {
			if (page == sessionPages[i]) {
				if ((i - 1) >= 0)
					// show previous job type page
					return sessionPages[i - 1];
				else
					// no previous job type page; go to scheduleEntryPage
					return scheduleEntryPage;
			}
		}

		// no previous page
		return null;
	}

	public ScheduleImpl getSchedule() {
		return schedule;
	}

	public ScheduleEntryImpl getScheduleEntry() {
		return entry;
	}

	void initializeCurrentJobConfigurationSession(final String id, final JobConfigurationWizardAdapter wizardAdapter) {
		if (!sessionsByJobTypeId.containsKey(id)) {
			final JobConfigurationWizardSession session = new JobConfigurationWizardSession(id);
			if (entry != null) {
				// clone job parameter into new session
				// (subsequent modifications MUST NOT be reflected into the entry or other sessions)
				session.getParameter().putAll(entry.getJobParameter());
			}
			sessionsByJobTypeId.put(id, session);
		}

		final JobConfigurationWizardSession session = sessionsByJobTypeId.get(id);

		if (session == currentSession)
			// nothing changed
			return;

		// lazy initialize pages
		if (null == session.getPages()) {
			final IWizardPage[] pages = wizardAdapter != null ? wizardAdapter.createPages(session) : new IWizardPage[] { new GenericJobParameterPage(session) };
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

	@Override
	public boolean performFinish() {
		final String entryId = scheduleEntryPage.getEntryId();

		ScheduleEntryImpl entry = null;
		try {
			if (this.entry == null) {
				entry = schedule.createEntry(entryId);
				entry.setJobTypeId(scheduleEntryPage.getJobTypeId());
			} else {
				entry = this.entry;
			}

			if (scheduleEntryPage.isScheduleUsingCronExpression()) {
				entry.setCronExpression(scheduleEntryPage.getCronExpression());
			} else {
				entry.setCronExpression(null);
			}

			if (scheduleEntryPage.isScheduleUsingPreceedingEntries()) {
				entry.setPrecedingEntries(scheduleEntryPage.getPreceedingEntryIds());
			} else {
				entry.setPrecedingEntries();
			}

			// get parameter from current session (as copy)
			final Map<String, String> parameter = new HashMap<>(currentSession.getParameter());

			// add lock id
			if (IdHelper.isValidId(exclusiveLockPage.getLockId())) {
				parameter.put(IJobManager.LOCK_ID, exclusiveLockPage.getLockId());
			}

			// set parameter
			entry.setJobParameter(parameter);

			// save schedule
			schedule.save();
			return true;
		} catch (final Exception | LinkageError | AssertionError e) {
			// remove entry if we created one
			// (keep schedule clean, to allow further entry id updates)
			if (entry != this.entry) {
				schedule.removeEntry(entryId);
			}

			// handle error
			LOG.debug("Error saving schedule. ", e);
			NonBlockingMessageDialogs.openError(getShell(), "Error Saving Schedule", "Unable to save schedule. " + e.getMessage(), null);
//			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to update schedule.", e), "Error Saving Schedule");
			return false;
		}
	}

}
