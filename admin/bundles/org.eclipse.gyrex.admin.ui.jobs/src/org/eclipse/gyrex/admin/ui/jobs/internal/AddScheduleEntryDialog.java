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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.TimeZone;

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
import org.eclipse.gyrex.jobs.schedules.manager.IScheduleManager;
import org.eclipse.gyrex.jobs.schedules.manager.IScheduleWorkingCopy;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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

	/** serialVersionUID */
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
			// TODO Auto-generated method stub
		}

		@Override
		public void doubleClicked(final TreeListDialogField field) {
			// no-op
		}

		@Override
		public Object[] getChildren(final TreeListDialogField field, final Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object getParent(final TreeListDialogField field, final Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasChildren(final TreeListDialogField field, final Object element) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void keyPressed(final TreeListDialogField field, final KeyEvent event) {
			// TODO Auto-generated method stub

		}

		@Override
		public void selectionChanged(final TreeListDialogField field) {
			// TODO Auto-generated method stub

		}
	}, new String[] { "Add...", "Remove" }, null);

	public AddScheduleEntryDialog(final Shell parent) {
		super(parent);
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
		scheduleCheckBox.setLabelText("Execute at specific times:");
		cronExpressionField.setLabelText("Cron Expression");
		dependsCheckBox.setLabelText("Execute after other tasks:");

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

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, jobTypeField, cronExpressionField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(jobTypeField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(cronExpressionField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
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
		// TODO Auto-generated method stub

	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, JobsUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, JobsUiActivator.SYMBOLIC_NAME, message));
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

		final String context = jobTypeField.getText();
		if (StringUtils.isNotBlank(context)) {
			try {
				new URI(context);
			} catch (final URISyntaxException e) {
				setError("The entered URL. Please use valid URI syntax. " + e.getMessage());
				return;
			}
		}

		final String timeZone = cronExpressionField.getText();
		if (StringUtils.isNotBlank(timeZone)) {
			// TODO validate timezone
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a schedule id.");
			return;
		}

		if (StringUtils.isBlank(context)) {
			setInfo("Please enter a context path.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
