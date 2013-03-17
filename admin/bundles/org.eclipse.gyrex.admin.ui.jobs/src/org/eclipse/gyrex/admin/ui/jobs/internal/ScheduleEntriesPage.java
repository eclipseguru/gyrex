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

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.jobs.IJob;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleManagerImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleStore;
import org.eclipse.gyrex.jobs.internal.util.ContextHashUtil;
import org.eclipse.gyrex.jobs.manager.IJobManager;
import org.eclipse.gyrex.server.Platform;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;

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

	private Button addButton;
	private Button removeButton;
	private Button enableButton;
	private Button disableButton;

	private ScheduleImpl schedule;

	public ScheduleEntriesPage() {
		super(5);
		setTitle("Schedule Entries");
		setTitleToolTip("Edit a schedule and its entries for executing background tasks.");
	}

	void addButtonPressed() {
		final AddScheduleDialog dialog = new AddScheduleDialog(SwtUtil.getShell(getTreeViewer().getTree()));
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
		addButton = createButton(parent, "Add..");
		removeButton = createButton(parent, "Remove");
		createButtonSeparator(parent);
		enableButton = createButton(parent, "Enable");
		disableButton = createButton(parent, "Disable");

	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new ScheduleEntriesContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		if (Platform.inDevelopmentMode()) {
			final Infobox infobox = new Infobox(parent);
			infobox.addHeading("Schedule Entries");
			infobox.addParagraph("A schedule is composed of schedule entries. They define, what and how a background task should run. They can have a cron expression and/or a dependency on other entries in the same schedule.");
			infobox.addLink("Back to <a>schedules list</a>", new SelectionAdapter() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(final SelectionEvent e) {
					openSchedulesPage();
				}
			});
			return infobox;
		}

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(AdminUiUtil.createGridLayout(1, false, true, false));
		composite.setLayoutData(AdminUiUtil.createFillData());

		final Link link = new Link(composite, SWT.WRAP);
		link.setText("Back to <a>schedules list</a>");
		link.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		link.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				openSchedulesPage();
			}
		});

		return composite;
	}

	/**
	 * @param composite
	 */
	void disableButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Disable selected Schedule Entry ", String.format("Do you really want to disable schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				//scheduleEntry.setEnabled(false);
				// TODO set disabled

				refresh();
			}
		});
	}

	void enableButtonPressed() {

		final ScheduleEntryImpl scheduleEntry = getSelectedScheduleEntry();
		if (scheduleEntry == null)
			return;

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(getTreeViewer().getTree()), "Enable selected Schedule Entry ", String.format("Do you really want to enable schedule entry %s?", scheduleEntry.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK)
					return;

				//scheduleEntry.setEnabled(true);
				// TODO set enabled

				refresh();
			}
		});
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

			default:
				return StringUtils.EMPTY;
		}
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof ScheduleEntryImpl) {
			final ScheduleEntryImpl entry = (ScheduleEntryImpl) element;
			switch (column) {
				case COLUMN_ID:
					return entry.getId();
				case COLUMN_TYPE:
					return entry.getJobTypeId();
				case COLUMN_CRON:
					return entry.getCronExpression();
				case COLUMN_PRECEDINGS:
					return StringUtils.join(entry.getPrecedingEntries(), ", ");
				case COLUMN_LAST_RESULT:
					return getLastResult(entry);

				default:
					return null;
			}
		}

		return null;
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
		getAdminUi().openPage(BackgroundTasksPage.ID);
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

				//ScheduleStore.(scheduleEntry.getStorageKey(), scheduleEntry.getId());
				// TODO remove schedule entry

				refresh();
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
		setTitle("Schedule Entries of " + schedule.getId());

	}

	@Override
	protected void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) getTreeViewer().getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			enableButton.setEnabled(false);
			disableButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(true);
		removeButton.setEnabled(selectedElementsCount == 1);

		final ScheduleEntryImpl selectedScheduleEntry = getSelectedScheduleEntry();
		if (selectedScheduleEntry != null) {
			if (selectedScheduleEntry.isEnabled()) {
				enableButton.setEnabled(false);
				disableButton.setEnabled(true);
			} else {
				enableButton.setEnabled(true);
				disableButton.setEnabled(false);
			}
		}
	}

}
