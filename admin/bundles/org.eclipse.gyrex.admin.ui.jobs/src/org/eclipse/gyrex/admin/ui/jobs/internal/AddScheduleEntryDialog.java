/**
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Mihm - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.gyrex.admin.ui.internal.widgets.ElementListSelectionDialog;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.TreeListDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.schedules.IScheduleEntry;
import org.eclipse.gyrex.jobs.schedules.manager.IScheduleManager;
import org.eclipse.gyrex.jobs.schedules.manager.IScheduleWorkingCopy;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

public class AddScheduleEntryDialog extends NonBlockingStatusDialog {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final long serialVersionUID = 1L;
	private final StringDialogField idField = new StringDialogField();
	private final StringButtonDialogField jobTypeField = new StringButtonDialogField(new IStringButtonAdapter() {

		@Override
		public void changeControlPressed(final DialogField field) {
			openJobTypeSelectionDialog();
		}
	}) {

		@Override
		protected Text createTextControl(final Composite parent) {
			return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		}

	};

	private final SelectionButtonDialogField scheduleCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final StringDialogField cronExpressionField = new StringDialogField();
	private final SelectionButtonDialogField dependsCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final TreeListDialogField preceedingEntriesTree = new TreeListDialogField(new ITreeListAdapter() {

		@Override
		public void customButtonPressed(final TreeListDialogField field, final int index) {
			if (index == 0) {
				final ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
				dialog.setTitle("Select Entry");
				dialog.setMessage("&Select an entry that will trigger a run:");
				final List<IScheduleEntry> entries = new ArrayList<>(getSchedule().getEntries());
				entries.remove(getEntry());
				if (!entries.isEmpty()) {
					dialog.setElements(entries.toArray());
					dialog.openNonBlocking(new DialogCallback() {
						private static final long serialVersionUID = 1L;

						@Override
						public void dialogClosed(final int returnCode) {
							if (returnCode == Window.OK) {
								final Object firstResult = dialog.getFirstResult();
								if (firstResult instanceof ScheduleEntryImpl) {
									field.addElement(firstResult);
								}
							}
						}
					});
				} else {
					NonBlockingMessageDialogs.openInformation(getShell(), "No Entries", "Sorry but there are no other entries in this schedule defined.\n\nIn order to build task chains you need to define at least two or more entries within the same schedule.", null);
				}
			}
		}

		@Override
		public void doubleClicked(final TreeListDialogField field) {
			// no-op
		}

		@Override
		public Object[] getChildren(final TreeListDialogField field, final Object element) {
			if (element instanceof ScheduleEntryImpl) {
				final ScheduleEntryImpl entry = (ScheduleEntryImpl) element;
				final Collection<String> precedingEntries = entry.getPrecedingEntries();
				if (!precedingEntries.isEmpty()) {
					final List<Object> result = new ArrayList<>(precedingEntries.size());
					for (final String entryId : precedingEntries) {
						try {
							result.add(getSchedule().getEntry(entryId));
						} catch (final Exception e) {
							result.add(entryId + " (" + e.getMessage() + ")");
						}
					}
					return result.toArray();
				}

			}
			return NO_CHILDREN;
		}

		@Override
		public Object getParent(final TreeListDialogField field, final Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(final TreeListDialogField field, final Object element) {
			return (element instanceof ScheduleEntryImpl) && !((ScheduleEntryImpl) element).getPrecedingEntries().isEmpty();
		}

		@Override
		public void keyPressed(final TreeListDialogField field, final KeyEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void selectionChanged(final TreeListDialogField field) {
			// TODO Auto-generated method stub

		}
	}, new String[] { "Add...", "Remove" }, new LabelProvider());
	{
		preceedingEntriesTree.setRemoveButtonIndex(1);
	}

	private JobType jobType;
	private final ScheduleImpl schedule;
	private ScheduleEntryImpl entry;

	public AddScheduleEntryDialog(final Shell parent, final ScheduleImpl schedule) {
		super(parent);
		this.schedule = schedule;
		setTitle("New Schedule Entry");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		jobTypeField.setLabelText("Task");
		jobTypeField.setButtonLabel("Browse...");
		scheduleCheckBox.setLabelText("Run at specific times (cron expression):");
		cronExpressionField.setLabelText("");
		dependsCheckBox.setLabelText("Run whenever one of the folloing entries run successfully:");
		preceedingEntriesTree.setLabelText("");

		scheduleCheckBox.attachDialogField(cronExpressionField);
		dependsCheckBox.attachDialogField(preceedingEntriesTree);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		jobTypeField.setDialogFieldListener(validateListener);
		cronExpressionField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, jobTypeField, new Separator(), scheduleCheckBox, cronExpressionField, new Separator(), dependsCheckBox, preceedingEntriesTree }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(jobTypeField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(cronExpressionField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	public ScheduleEntryImpl getEntry() {
		return entry;
	}

	public ScheduleImpl getSchedule() {
		return schedule;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK())
			return;

		try {

			final String contextPath = jobTypeField.getText();

			final IRuntimeContext context = JobsUiActivator.getInstance().getService(IRuntimeContextRegistry.class).get(new Path(contextPath).makeAbsolute().addTrailingSeparator());
			if (context != null) {
				final IScheduleManager scheduleManager = context.get(IScheduleManager.class);
				final IScheduleWorkingCopy schedule = scheduleManager.createSchedule(idField.getText());

				if (StringUtils.isNotBlank(cronExpressionField.getText())) {
					schedule.setTimeZone(TimeZone.getTimeZone(cronExpressionField.getText()));
				}

				scheduleManager.updateSchedule(schedule);

			} else {
				//context not found
				setError("Entered context path is not valid!");
			}
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	void openJobTypeSelectionDialog() {
		final JobTypeSelectionDialog dialog = new JobTypeSelectionDialog(getShell());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					final JobType jobType = (JobType) dialog.getFirstResult();
					setJobType(jobType);
				}

			}
		});

	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, JobsUiActivator.SYMBOLIC_NAME, message));
	}

	public void setJobType(final JobType jobType) {
		this.jobType = jobType;
		jobTypeField.setText(jobType == null ? "" : jobType.getName());
		validate();
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, JobsUiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered schedule id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		final String timeZone = cronExpressionField.getText();
		if (StringUtils.isNotBlank(timeZone)) {
			// TODO validate timezone
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a schedule id.");
			return;
		}

		if ((jobType == null) || StringUtils.isBlank(jobTypeField.getText())) {
			setInfo("Please select a job type.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
