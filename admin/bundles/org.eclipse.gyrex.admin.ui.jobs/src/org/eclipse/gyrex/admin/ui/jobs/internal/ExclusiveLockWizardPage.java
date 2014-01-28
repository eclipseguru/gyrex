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

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DescriptionDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleEntryImpl;
import org.eclipse.gyrex.jobs.internal.schedules.ScheduleImpl;
import org.eclipse.gyrex.jobs.manager.IJobManager;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

public class ExclusiveLockWizardPage extends WizardPage {

	private static final long serialVersionUID = 1L;

	private final SelectionButtonDialogField requireLockCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final StringDialogField lockNameField = new StringDialogField();
	private final DescriptionDialogField lockDescriptionField = new DescriptionDialogField();

	private final ScheduleImpl schedule;
	private final ScheduleEntryImpl entry;

	public ExclusiveLockWizardPage(final ScheduleImpl schedule, final ScheduleEntryImpl entry) {
		super(ExclusiveLockWizardPage.class.getSimpleName());
		setTitle("Exclusive Lock");
		setDescription("Configure an exclusive lock for the job execution.");
		setPageComplete(false);
		this.schedule = schedule;
		this.entry = entry;
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		requireLockCheckBox.setLabelText("Require an exclusive lock before starting the task:");
		lockNameField.setLabelText("Lock Id:");
		lockDescriptionField.setText("<small>The lock will be acquired before the task is started. If the lock is already acquired elsewhere the task will be discarded. This allows to prevent different tasks from running in parallel.</small>");

		requireLockCheckBox.setAttachedDialogFields(lockNameField, lockDescriptionField);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		lockNameField.setDialogFieldListener(validateListener);
		requireLockCheckBox.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { requireLockCheckBox, lockNameField, lockDescriptionField }, false);
		LayoutUtil.setHorizontalGrabbing(lockNameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(lockDescriptionField.getDescriptionControl(null));
		LayoutUtil.setWidthHint(lockDescriptionField.getDescriptionControl(null), convertWidthInCharsToPixels(40));

		if (null != getEntry()) {
			final String lockId = getEntry().getJobParameter().get(IJobManager.LOCK_ID);
			if (StringUtils.isNotBlank(lockId)) {
				requireLockCheckBox.setSelection(true);
				lockNameField.setText(lockId);
			}
		}
	}

	public ScheduleEntryImpl getEntry() {
		return entry;
	}

	public String getLockId() {
		if (requireLockCheckBox.isSelected())
			return lockNameField.getText();
		return null;
	}

	public ScheduleImpl getSchedule() {
		return schedule;
	}

	void validate() {
		final String id = getLockId();
		if (StringUtils.isNotBlank(id)) {
			if (!IdHelper.isValidId(id)) {
				setMessage("The entered lock id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.", IMessageProvider.ERROR);
				setPageComplete(false);
				return;
			}
		}

		if (StringUtils.isBlank(id) && requireLockCheckBox.isSelected()) {
			setMessage("Please enter an lock identifier.", INFORMATION);
			setPageComplete(false);
			return;
		}

		setMessage(null);
		setPageComplete(true);
	}
}
