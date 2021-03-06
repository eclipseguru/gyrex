/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.internal.widgets.Statusbox;
import org.eclipse.gyrex.admin.ui.internal.wizards.NonBlockingWizardDialog;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.jobs.IJob;
import org.eclipse.gyrex.jobs.JobState;
import org.eclipse.gyrex.jobs.internal.JobsActivator;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.internal.schedules.SchedulingUtil;
import org.eclipse.gyrex.jobs.internal.util.ContextHashUtil;
import org.eclipse.gyrex.jobs.manager.IJobManager;
import org.eclipse.gyrex.rap.helper.SwtUtil;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;

public class ScheduleEntriesPage extends AdminPageWithTree {

	public static final String ID = "schedule-entries";

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_TYPE = 1;
	private static final int COLUMN_CRON = 2;
	private static final int COLUMN_PRECEDINGS = 3;
	private static final int COLUMN_LAST_RESULT = 4;
	private static final int COLUMN_STATUS = 5;

	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button enableButton;
	private Button disableButton;
	private Button runNowButton;

	private ScheduleImpl schedule;

	public ScheduleEntriesPage() {
		super(6);
		setTitle("Schedule Entries");
		setTitleToolTip("Edit a schedule and its entries for executing background tasks.");
	}

	void addButtonPressed() {
		editOrAddScheduleEntry(null);
	}

	@Override
	protected void createButtons(final Composite parent) {
		addButton = createButton(parent, "New...");
		addButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});
		editButton = createButton(parent, "Edit...");
		editButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editButtonPressed();
			}
		});
		removeButton = createButton(parent, "Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});

		createButtonSeparator(parent);

		enableButton = createButton(parent, "Enable");
		enableButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				enableButtonPressed();
			}
		});

		disableButton = createButton(parent, "Disable");
		disableButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				disableButtonPressed();
			}
		});

		createButtonSeparator(parent);

		runNowButton = createButton(parent, "Run Now");
		runNowButton.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				runNowButtonPressed();
			}
		});
	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new ScheduleEntriesContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());

		if (getSchedule().isEnabled()) {
			final Statusbox statusbox = new Statusbox(composite, org.eclipse.gyrex.admin.ui.internal.widgets.Statusbox.Status.Warning);
			statusbox.addLink("The schedule is enabled. You can not modify it. Please <a>disable it first</a>.", new SelectionAdapter() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent e) {
					final ScheduleImpl schedule = getSchedule();
					NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Disable Schedule", String.format("Do you want to disable schedule %s?", schedule.getId()), new DialogCallback() {
						/** serialVersionUID */
						private static final long serialVersionUID = 1L;

						@Override
						public void dialogClosed(final int returnCode) {
							if (returnCode != Window.OK)
								return;

							try {
								schedule.setEnabled(false);
								ScheduleStore.flush(schedule.getStorageKey(), schedule);
								schedule.load();
							} catch (final BackingStoreException ex) {
								Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to deactivate schedule.", ex), "Error");
							}

							BackgroundTasksPage.openSchedule(schedule, getApplicationService());
						}
					});
				}
			});
		}

		final Infobox infobox = new Infobox(composite);
		infobox.addHeading("Schedule Entries");
		infobox.addParagraph("A schedule is composed of schedule entries. They define, what and how a background task should run. They can have a cron expression and/or a dependency on other entries in the same schedule.");
		infobox.addHeading("Navigate");
		infobox.addLink("Back to <a>schedules list</a>", new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				openSchedulesPage();
			}
		});
		return composite;
	}

	void disableButtonPressed() {
		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		try {
			scheduleEntry.setEnabled(false);
			scheduleEntry.getSchedule().save();
		} catch (final BackingStoreException e) {
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to activate schedule.", e), "Error");
		}

		getTreeViewer().refresh(scheduleEntry);
		updateButtons();
	}

	void editButtonPressed() {
		editOrAddScheduleEntry(getSelectedScheduleEntry());
	}

	private void editOrAddScheduleEntry(final ScheduleEntryImpl entryToEdit) {
		if (schedule.isEnabled()) {
			NonBlockingMessageDialogs.openInformation(SwtUtil.getShell(getTreeViewer().getTree()), "Active Schedule", "The schedule is enabled and cannot be modified. Please disable it first!", null);
			return;
		}

		final NonBlockingWizardDialog dialog = new NonBlockingWizardDialog(SwtUtil.getShell(getTreeViewer().getTree()), new ScheduleEntryWizard(getSchedule(), entryToEdit));
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});
	}

	void enableButtonPressed() {
		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		try {
			scheduleEntry.setEnabled(true);
			scheduleEntry.getSchedule().save();
		} catch (final BackingStoreException e) {
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to activate schedule.", e), "Error");
		}

		getTreeViewer().refresh(scheduleEntry);
		updateButtons();
	}

	@Override
	protected String getColumnLabel(final int column) {
		switch (column) {
			case COLUMN_ID:
				return "Entry";
			case COLUMN_TYPE:
				return "Task";
			case COLUMN_CRON:
				return "Cron Exp.";
			case COLUMN_PRECEDINGS:
				return "Precedings";
			case COLUMN_LAST_RESULT:
				return "Last Run";
			case COLUMN_STATUS:
				return "Status";

			default:
				return StringUtils.EMPTY;
		}
	}

	@Override
	protected ColumnLayoutData getColumnLayoutData(final int column) {
		switch (column) {
			case COLUMN_ID:
				return new ColumnWeightData(25, 50);
			case COLUMN_TYPE:
			case COLUMN_CRON:
				return new ColumnWeightData(12, 50);
			case COLUMN_LAST_RESULT:
			case COLUMN_STATUS:
				return new ColumnWeightData(6, 30);
			default:
				return new ColumnWeightData(10, 50);
		}
	}

	@Override
	protected Image getElementImage(final Object element, final int column) {
		if ((element instanceof ScheduleEntryImpl)) {
			if (column == COLUMN_ID)
				if (((ScheduleEntryImpl) element).isEnabled())
					return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_ACTIVE);
				else
					return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_INACTIVE);
			else if (column == COLUMN_LAST_RESULT)
				return getLastResultImage((ScheduleEntryImpl) element);
		}
		return null;
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof ScheduleEntryImpl) {
			final ScheduleEntryImpl entry = (ScheduleEntryImpl) element;
			switch (column) {
				case COLUMN_ID:
					return entry.getId();
				case COLUMN_TYPE:
					return getName(entry);
				case COLUMN_CRON:
					return entry.getCronExpression();
				case COLUMN_PRECEDINGS:
					return StringUtils.join(entry.getPrecedingEntries(), ", ");
				case COLUMN_LAST_RESULT:
					if (null == getLastResultImage(entry))
						return getLastResult(entry);
					else
						return null;
				case COLUMN_STATUS:
					return getJobStatus(entry);

				default:
					return null;
			}
		}

		return null;
	}

	String getJobStatus(final ScheduleEntryImpl entry) {
		final IRuntimeContext ctx = JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(schedule.getContextPath());
		if (ctx != null) {
			final IJobManager jobManager = ctx.get(IJobManager.class);
			if (jobManager != null) {
				final IJob job = jobManager.getJob(entry.getJobId());
				if (job != null) {
					final JobState state = job.getState();
					switch (state) {
						case ABORTING:
							return "aborting";
						case RUNNING:
							return "running";
						case WAITING:
							return "waiting";
						case NONE:
							if (entry.isEnabled() && entry.getSchedule().isEnabled())
								return "sleeping";
						default:
							return "";
					}
				}
			}
		}
		return "n/a";
	}

	String getLastResult(final ScheduleEntryImpl entry) {
		final IRuntimeContext ctx = JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(schedule.getContextPath());
		if (ctx != null) {
			final IJobManager jobManager = ctx.get(IJobManager.class);
			if (jobManager != null) {
				final IJob job = jobManager.getJob(entry.getJobId());
				if (job != null) {
					final IStatus result = job.getLastResult();
					if (result != null) {
						if (result.isOK())
							return "OK";
						else if (result.matches(IStatus.CANCEL))
							return "aborted";
						else if (result.matches(IStatus.ERROR))
							return "failed";
						else if (result.matches(IStatus.WARNING))
							return "with warnings";
						else if (result.matches(IStatus.INFO))
							return "OK";
					}
				}
			}
		}
		return "n/a";
	};

	private Image getLastResultImage(final ScheduleEntryImpl entry) {
		final IRuntimeContext ctx = JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(schedule.getContextPath());
		if (ctx != null) {
			final IJobManager jobManager = ctx.get(IJobManager.class);
			if (jobManager != null) {
				final IJob job = jobManager.getJob(entry.getJobId());
				if (job != null) {
					final IStatus result = job.getLastResult();
					if (result != null) {
						if (result.isOK())
							return null;
						else if (result.matches(IStatus.CANCEL))
							return null;
						else if (result.matches(IStatus.ERROR))
							return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_ERROR_RESULT);
						else if (result.matches(IStatus.WARNING))
							return JobsUiImages.getImage(JobsUiImages.IMG_OBJ_WARN_RESULT);
						else if (result.matches(IStatus.INFO))
							return null;
					}
				}
			}
		}
		return null;
	};

	private String getName(final ScheduleEntryImpl entry) {
		final String name = JobsActivator.getInstance().getJobProviderRegistry().getName(entry.getJobTypeId());
		if (StringUtils.isNotBlank(name))
			return name;
		return "unknown (" + entry.getJobTypeId() + ")";
	}

	public ScheduleImpl getSchedule() {
		return schedule;
	}

	private ScheduleEntryImpl getSelectedScheduleEntry() {
		final IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
		if (!selection.isEmpty() && (selection.getFirstElement() instanceof ScheduleEntryImpl))
			return (ScheduleEntryImpl) selection.getFirstElement();

		return null;
	}

	@Override
	protected Object getViewerInput() {
		return schedule;
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return false;
	}

	protected void openSchedulesPage() {
		getApplicationService().openPage(BackgroundTasksPage.ID);
	}

	@Override
	protected void openSelectedElement() {
		editOrAddScheduleEntry(getSelectedScheduleEntry());
	}

	@Override
	protected void refresh() {
		try {
			getSchedule().load();
		} catch (final BackingStoreException e) {
			Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Error loading schedule.", e), "Error");
		}
		getTreeViewer().refresh();
	}

	void removeButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Remove selected Schedule entry ", String.format("Do you really want to delete schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					final ScheduleImpl schedule = scheduleEntry.getSchedule();
					schedule.removeEntry(scheduleEntry.getId());
					schedule.save();
				} catch (final Exception | LinkageError | AssertionError e) {
					Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to activate schedule.", e), "Error");
				}

				refresh();
				updateButtons();
			}
		});
	}

	void runNowButtonPressed() {
		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Run Now", String.format("Do you want to queue entry '%s' for execution now?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				try {
					SchedulingUtil.queueJob(scheduleEntry);
				} catch (final Exception ex) {
					Policy.getStatusHandler().show(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, "Unable to queue schedule entry.", ex), "Error");
				}

				getTreeViewer().refresh(scheduleEntry);
				updateButtons();
			}
		});
	}

	@Override
	public void setArguments(final String[] args) {
		super.setArguments(args);
		if (args.length >= 3) {

			final String contextPath = args[1];
			final String scheduleId = args[2];

			final String storageKey = new ContextHashUtil(new Path(contextPath)).toInternalId(scheduleId);
			try {
				final ScheduleImpl schedule = ScheduleStore.load(storageKey, ScheduleManagerImpl.getExternalId(storageKey), false);
				if (schedule != null) {
					setSchedule(schedule);
				} else
					throw new IllegalArgumentException(String.format("Schedule %s not found in context %s", scheduleId, contextPath));
			} catch (final BackingStoreException e) {
				throw new UnhandledException(e);
			}
		}
	}

	public void setSchedule(final ScheduleImpl schedule) {
		this.schedule = schedule;
		setTitle("Schedule " + schedule.getId());
	}

	@Override
	protected void updateButtons() {
		final ScheduleEntryImpl selectedScheduleEntry = getSelectedScheduleEntry();

		if (getSchedule().isEnabled()) {
			// disable most buttons when schedule is enabled
			addButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			runNowButton.setEnabled((selectedScheduleEntry != null) && selectedScheduleEntry.isEnabled());
			return;
		}

		if (selectedScheduleEntry == null) {
			addButton.setEnabled(true);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			runNowButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(true);
		editButton.setEnabled(true);
		removeButton.setEnabled(true);

		if (selectedScheduleEntry.isEnabled()) {
			enableButton.setEnabled(false);
			disableButton.setEnabled(true);
			runNowButton.setEnabled(true);
		} else {
			enableButton.setEnabled(true);
			disableButton.setEnabled(false);
			runNowButton.setEnabled(false);
		}
	}

}
