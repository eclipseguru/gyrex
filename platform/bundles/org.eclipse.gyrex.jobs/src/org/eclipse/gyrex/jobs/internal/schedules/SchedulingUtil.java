/*******************************************************************************
 * Copyright (c) 2014 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.jobs.internal.schedules;

import java.util.Map;

import org.eclipse.gyrex.cloud.services.queue.IQueue;
import org.eclipse.gyrex.cloud.services.queue.IQueueService;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.jobs.JobState;
import org.eclipse.gyrex.jobs.internal.JobsActivator;
import org.eclipse.gyrex.jobs.internal.manager.JobImpl;
import org.eclipse.gyrex.jobs.internal.manager.JobManagerImpl;
import org.eclipse.gyrex.jobs.internal.scheduler.SchedulingJob;
import org.eclipse.gyrex.jobs.manager.IJobManager;
import org.eclipse.gyrex.jobs.schedules.IScheduleEntry;

import org.eclipse.core.runtime.IPath;

import org.apache.commons.lang.StringUtils;

/**
 * A helper for scheduling jobs based on schedule entries.
 */
public class SchedulingUtil {

	public static String createScheduleInfo(final String scheduleId, final String scheduleEntryId, final String scheduleEntriesToTriggerAfterRun) {
		if (StringUtils.isBlank(scheduleEntriesToTriggerAfterRun))
			return scheduleId + SchedulingJob.SEPARATOR_CHAR + scheduleEntryId;
		else
			return scheduleId + SchedulingJob.SEPARATOR_CHAR + scheduleEntryId + SchedulingJob.SEPARATOR_CHAR + scheduleEntriesToTriggerAfterRun;
	}

	public static String getScheduleEntriesToTriggerAfterRun(final IScheduleEntry entry, final ScheduleImpl schedule) {
		return StringUtils.join(schedule.getEntriesToTriggerAfter(entry.getId()), SchedulingJob.SEPARATOR_CHAR);
	}

	public static void queueJob(final ScheduleEntryImpl entry) {
		final IPath contextPath = entry.getSchedule().getContextPath();
		final String scheduleId = entry.getSchedule().getId();
		final String scheduleEntryId = entry.getId();
		final String scheduleEntriesToTriggerAfterRun = SchedulingUtil.getScheduleEntriesToTriggerAfterRun(entry, entry.getSchedule());
		final String queueId = StringUtils.isNotBlank(entry.getQueueId()) ? entry.getQueueId() : entry.getSchedule().getQueueId();
		final String jobId = entry.getJobId();
		final String jobTypeId = entry.getJobTypeId();
		final Map<String, String> jobParameter = entry.getJobParameter();

		SchedulingUtil.queueJob(jobId, jobTypeId, contextPath, scheduleId, scheduleEntryId, scheduleEntriesToTriggerAfterRun, queueId, jobParameter);
	}

	public static void queueJob(final String jobId, final String jobTypeId, final IPath contextPath, final String scheduleId, final String scheduleEntryId, final String scheduleEntriesToTriggerAfterRun, final String queueId, final Map<String, String> parameter) {
		// get context
		final IRuntimeContext runtimeContext = JobsActivator.getInstance().getService(IRuntimeContextRegistry.class).get(contextPath);
		if (null == runtimeContext) {
			SchedulingJob.LOG.error("Unable to find context (using path {}) for job {}.", contextPath, jobId);
			return;
		}

		// get job manager
		final IJobManager jobManager = runtimeContext.get(IJobManager.class);
		if (!(jobManager instanceof JobManagerImpl)) {
			SchedulingJob.LOG.error("Invalid job manager ({}). Please verify the system is setup properly.", jobManager);
			return;
		}
		final JobManagerImpl jobManagerImpl = (JobManagerImpl) jobManager;

		// check that job state is NONE (and it's not stuck) if one exists
		final JobImpl job = jobManagerImpl.getJob(jobId);
		if ((job != null) && (job.getState() != JobState.NONE) && !jobManagerImpl.isStuck(job)) {
			SchedulingJob.LOG.warn("Job {} (type {}) cannot be queued because it is already active in the system (current state {}).", new Object[] { job.getId(), job.getTypeId(), job.getState() });
			return;
		}

		// check that queue exists
		final IQueueService queueService = JobsActivator.getInstance().getQueueService();
		IQueue queue = queueService.getQueue(null != queueId ? queueId : IJobManager.DEFAULT_QUEUE, null);
		if (queue == null) {
			queue = queueService.createQueue(null != queueId ? queueId : IJobManager.DEFAULT_QUEUE, null);
		}

		// queue job (create it if necessary)
		jobManagerImpl.queueJob(jobTypeId, jobId, parameter, queue.getId(), String.format("Schedule '%s' entry '%s'.", scheduleId, scheduleEntryId), SchedulingUtil.createScheduleInfo(scheduleId, scheduleEntryId, scheduleEntriesToTriggerAfterRun));
	}

	private SchedulingUtil() {
		// empty
	}

}
