/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
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

import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;
import org.eclipse.gyrex.jobs.JobState;
import org.eclipse.gyrex.jobs.internal.manager.JobHungDetectionHelper;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.internal.storage.CloudPreferncesJobStorage;
import org.eclipse.gyrex.jobs.internal.worker.WorkerEngine;
import org.eclipse.gyrex.jobs.manager.IJobManager;
import org.eclipse.gyrex.jobs.schedules.ISchedule;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

public class BackgroundTasksPage extends AdminPageWithTree {

	public static final String ID = "background-tasks";

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_TIMEZONE = 1;
	private static final int COLUMN_QUEUE = 2;

	private Button addButton;
	private Button removeButton;
	private Button enableButton;
	private Button disableButton;
	private Button showEntriesButton;

	private Label schedulesMetricLabel;
	private Label jobsRunningLabel;
	private Label jobsWaitingMetricLabel;
	private Label processingStateMetricLabel;

	public BackgroundTasksPage() {
		super(3);
		setTitle("Background Tasks");
		setTitleToolTip("Browse and manage schedules for executing background tasks.");
	}

	void addButtonPressed() {
		final AddScheduleDialog dialog = new AddScheduleDialog(SwtUtil.getShell(addButton));
		dialog.openNonBlocking(new DialogCallback() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});
	}

	@Override
	protected void createButtons(final Composite parent) {
		addButton = createButton(parent, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		removeButton = createButton(parent, "Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

		createButtonSeparator(parent);

		enableButton = createButton(parent, "Enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				enableButtonPressed();
			}
		});

		disableButton = createButton(parent, "Disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				disableButtonPressed();
			}
		});

		createButtonSeparator(parent);

		showEntriesButton = createButton(parent, "Edit Schedule");
		showEntriesButton.setEnabled(false);
		showEntriesButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				showEntriesButtonPressed();
			}
		});

		getTreeViewer().addOpenListener(new IOpenListener() {

			@Override
			public void open(final OpenEvent event) {
				final ScheduleImpl schedule = getFirstSelectedSchedule(event.getSelection());
				if (schedule != null) {
					openScheduleEntriesPage(schedule);
				}
			}
		});
	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new SchedulesContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		createMetricInfoArea(composite);

		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(composite);
			infobox.addHeading("Schedules");
			infobox.addParagraph("Background tasks in Gyrex are organized into schedules. A schedule is associated to a context and defines common properties (such as timezone) for all background tasks.");
			infobox.addParagraph("Schedules define the jobs to execute as schedule entries. Schedule entries can be triggered using cron expressions or by the successful execution of a preceding entry within the same schedule. Both a schedule as well as individual schedule entries can be enabled or disabled.");
		}

		return composite;
	}

	private void createMetricInfoArea(final Composite parent) {
		final Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		area.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).numColumns(8).create());

		schedulesMetricLabel = createMetricText(area, "Schedules");
		schedulesMetricLabel.setText("0");
		createMetricSeparator(area);
		jobsRunningLabel = createMetricText(area, "Running Jobs");
		jobsRunningLabel.setText("0");
		createMetricSeparator(area);
		jobsWaitingMetricLabel = createMetricText(area, "Waiting Jobs");
		jobsWaitingMetricLabel.setText("0");
		createMetricSeparator(area);
		processingStateMetricLabel = createMetricText(area, "Worker Engine");
		processingStateMetricLabel.setText("?");

	}

	void disableButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if ((schedule == null) || !schedule.isEnabled())
			return;

		try {
			schedule.setEnabled(false);
			ScheduleStore.flush(schedule.getStorageKey(), schedule);
			schedule.load();
		} catch (final BackingStoreException e) {
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to activate schedule.", e), "Error");
		}

		getTreeViewer().refresh(schedule, true);
		updateButtons();
	}

	void enableButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if ((schedule == null) || schedule.isEnabled())
			return;

		try {
			schedule.setEnabled(true);
			ScheduleStore.flush(schedule.getStorageKey(), schedule);
			schedule.load();
		} catch (final BackingStoreException e) {
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to activate schedule.", e), "Error");
		}

		getTreeViewer().refresh(schedule, true);
		updateButtons();
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_ID:
				return "Schedule";
			case COLUMN_TIMEZONE:
				return "Time Zone";
			case COLUMN_QUEUE:
				return "Queue";

			default:
				return StringUtils.EMPTY;
		}
	}

	@Override
	protected Image getElementImage(final Object element, final int column) {
		if ((element instanceof ISchedule) && (column == COLUMN_ID)) {
			if (((ISchedule) element).isEnabled())
				return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_SCHEDULE);
			else
				return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_SCHEDULE_DISABLED);
		}
		return null;
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if ((element instanceof ContextDefinition) && (column == 0))
			return ((ContextDefinition) element).getPath().toString();

		if (element instanceof ISchedule) {
			final ISchedule schedule = (ISchedule) element;
			switch (column) {
				case COLUMN_ID:
					return schedule.getId();
				case COLUMN_TIMEZONE:
					return schedule.getTimeZone().getID();
				case COLUMN_QUEUE:
					return getQueueName(schedule);

				default:
					break;
			}
		}
		return StringUtils.EMPTY;
	};

	ScheduleImpl getFirstSelectedSchedule(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (!selection.isEmpty() && (structuredSelection.getFirstElement() instanceof ScheduleImpl))
				return (ScheduleImpl) structuredSelection.getFirstElement();
		}

		return null;
	}

	private String getQueueName(final ISchedule schedule) {
		if (schedule.getQueueId() == null)
			return "Default Queue";
		switch (schedule.getQueueId()) {
			case IJobManager.DEFAULT_QUEUE:
				return "Default Queue";
			case IJobManager.PRIORITY_QUEUE:
				return "Priority Queue";

			default:
				return schedule.getQueueId();
		}
	}

	private ScheduleImpl getSelectedSchedule() {
		return getFirstSelectedSchedule(getTreeViewer().getSelection());
	}

	@Override
	protected Object getViewerInput() {
		return JobsUiActivator.getInstance().getService(IRuntimeContextDefinitionManager.class);
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return column == COLUMN_ID;
	}

	void openScheduleEntriesPage(final ScheduleImpl schedule) {
		getAdminUi().openPage(ScheduleEntriesPage.ID, schedule.getContextPath().toString(), schedule.getId());
	}

	@Override
	protected void refresh() {
		getTreeViewer().setInput(getViewerInput());
	}

	void removeButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Remove selected Schedule", String.format("Do you really want to delete schedule %s?", schedule.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					ScheduleStore.remove(schedule.getStorageKey(), schedule.getId());
				} catch (final BackingStoreException e) {
					e.printStackTrace();
				}

				refresh();
			}
		});
	}

	void showEntriesButtonPressed() {
		final ScheduleImpl schedule = getSelectedSchedule();
		if (schedule == null)
			return;

		openScheduleEntriesPage(schedule);
	}

	@Override
	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) getTreeViewer().getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			showEntriesButton.setEnabled(false);
		} else {
			final ScheduleImpl selectedSchedule = getSelectedSchedule();
			addButton.setEnabled(true);
			removeButton.setEnabled((selectedElementsCount == 1) && !selectedSchedule.isEnabled());
			showEntriesButton.setEnabled((selectedElementsCount == 1) && !selectedSchedule.isEnabled());

			if (selectedSchedule != null) {
				if (selectedSchedule.isEnabled()) {
					enableButton.setEnabled(false);
					disableButton.setEnabled(true);
				} else {
					enableButton.setEnabled(true);
					disableButton.setEnabled(false);
				}
			}
		}

		updateMetrics();
	}

	private void updateMetrics() {
		try {
			schedulesMetricLabel.setText(String.valueOf(ScheduleStore.getSchedules().length));
		} catch (final Exception e) {
			schedulesMetricLabel.setText("n/a");
		}

		try {
			jobsRunningLabel.setText(String.valueOf(JobHungDetectionHelper.getNumberOfActiveJobs()));
		} catch (final Exception e) {
			jobsRunningLabel.setText("n/a");
		}

		try {
			jobsWaitingMetricLabel.setText(String.valueOf(CloudPreferncesJobStorage.getAllJobStorageKeysByState(JobState.WAITING).size()));
		} catch (final Exception e) {
			jobsWaitingMetricLabel.setText("n/a");
		}

		try {
			processingStateMetricLabel.setText(WorkerEngine.isSuspended() ? "Off" : "On");
		} catch (final Exception e) {
			processingStateMetricLabel.setText("n/a");
		}
	}
}
