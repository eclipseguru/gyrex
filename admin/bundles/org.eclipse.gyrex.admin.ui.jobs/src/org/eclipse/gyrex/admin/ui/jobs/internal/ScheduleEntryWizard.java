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

import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardAdapter;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardSession;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import org.osgi.service.prefs.BackingStoreException;

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

	public ScheduleEntryWizard(final ScheduleImpl schedule, final ScheduleEntryImpl entry) {
		this.schedule = schedule;
		this.entry = entry;
		scheduleEntryPage = new ScheduleEntryWizardPage(schedule, entry);

		// force previous and next buttons (we don't know about potential job type pages)
		setForcePreviousAndNextButtons(true);
	}

	@Override
	public void addPages() {
		addPage(scheduleEntryPage);
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
		if (page == scheduleEntryPage) {
			if (currentSession != null) {
				final IWizardPage[] sessionPages = currentSession.getPages();
				if (sessionPages.length > 0)
					return sessionPages[0];
			}
		}

		// default order
		return super.getNextPage(page);
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
			final IWizardPage[] pages = wizardAdapter != null ? wizardAdapter.createPages(session) : null;
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

		ScheduleEntryImpl entry;
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

		try {
			schedule.save();
			return true;
		} catch (final BackingStoreException e) {
			// remove entry if we created one
			// (keep schedule clean, to allow further entry id updates)
			if (entry != this.entry) {
				schedule.removeEntry(entryId);
			}

			// handle error
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to update schedule.", e), "Error Saving Schedule");
			return false;
		}
	}
}
