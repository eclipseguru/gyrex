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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.cloud.services.queue.IQueue;
import org.eclipse.gyrex.cloud.services.queue.IQueueService;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.jobs.internal.JobsActivator;
import org.eclipse.gyrex.jobs.internal.worker.JobInfo;
import org.eclipse.gyrex.jobs.manager.IJobManager;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.StringUtils;

/**
 * Admin page for managing the content of a job queue.
 */
public class ManageJobQueuePage extends AdminPageWithTree {

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_CONTEXT = 2;
	private static final int COLUMN_AGE = 3;
	private static final int COLUMN_TRIGGER = 4;

	static String toRelativeTime(final long duration) {
		if (duration < TimeUnit.MINUTES.toMillis(2))
			return "a minute";
		else if (duration < TimeUnit.HOURS.toMillis(2))
			return String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(duration));
		else
			return String.format("%d hours", TimeUnit.MILLISECONDS.toMinutes(duration));
	}

	private Label queueLabel;

	public ManageJobQueuePage() {
		super(5);
		setTitle("Job Queue Contents");
	}

	@Override
	protected void createButtons(final Composite parent) {
		// TODO Auto-generated method stub
	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new JobQueueContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		queueLabel = new Label(composite, SWT.BEGINNING);

		return composite;
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_ID:
				return "Job";
			case COLUMN_TYPE:
				return "Type";
			case COLUMN_CONTEXT:
				return "Context";
			case COLUMN_AGE:
				return "Age";
			case COLUMN_TRIGGER:
				return "Trigger";

			default:
				return null;
		}
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof JobInfo) {
			final JobInfo jobInfo = (JobInfo) element;
			switch (column) {
				case COLUMN_ID:
					return jobInfo.getJobId();
				case COLUMN_TYPE:
					return getTypeName(jobInfo);
				case COLUMN_CONTEXT:
					return String.valueOf(jobInfo.getContextPath());
				case COLUMN_AGE:
					return toRelativeTime(System.currentTimeMillis() - jobInfo.getQueueTimestamp());
				case COLUMN_TRIGGER:
					return jobInfo.getQueueTrigger();
				default:
					return null;
			}
		} else if ((column == NO_COLUMN) || (column == COLUMN_ID))
			return String.valueOf(element);
		else
			return null;
	}

	private String getQueueId() {
		final String[] args = getArguments();
		if ((args.length > 1) && StringUtils.isNotBlank(args[1])) {
			switch (args[1]) {
				case "default":
					return IJobManager.DEFAULT_QUEUE;
				case "priority":
					return IJobManager.PRIORITY_QUEUE;

				default:
					if (IdHelper.isValidId(args[1]))
						return args[1];
					break;
			}
		}
		return IJobManager.DEFAULT_QUEUE;
	}

	private IQueueService getQueueService() {
		return JobsUiActivator.getInstance().getService(IQueueService.class);
	}

	private String getTypeName(final JobInfo info) {
		final String name = JobsActivator.getInstance().getJobProviderRegistry().getName(info.getJobTypeId());
		if (StringUtils.isNotBlank(name))
			return name;
		return "unknown (" + info.getJobTypeId() + ")";
	}

	@Override
	protected Object getViewerInput() {
		final String queueId = getQueueId();
		final IQueue queue = getQueueService().getQueue(queueId, null);
		if (queue != null)
			return queue;
		return Collections.singleton(String.format("Queue '%s' not available!", queueId));
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return false;
	}

	@Override
	public void setArguments(final String[] args) {
		super.setArguments(args);
	}

	@Override
	protected void updateButtons() {
		updateQueueLabel();
	}

	private void updateQueueLabel() {
		final String queueId = getQueueId();
		final StringBuilder text = new StringBuilder();
		switch (queueId) {
			case IJobManager.DEFAULT_QUEUE:
				text.append("Default Job Queue");
				break;
			case IJobManager.PRIORITY_QUEUE:
				text.append("Priority Job Queue");
				break;

			default:
				text.append("Custom Queue: ").append(queueId);
				break;
		}
		final IQueue queue = getQueueService().getQueue(queueId, null);
		final int count = queue != null ? queue.size() : -1;
		if ((count > 1) || (count == 0)) {
			text.append(" (").append(count).append(" jobs)");
		} else if (count == 1) {
			text.append(" (").append(count).append(" job)");
		}
		queueLabel.setText(text.toString());
		queueLabel.getParent().layout();
	}

}
