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
package org.eclipse.gyrex.admin.ui.jobs.internal.generic;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.jobs.internal.JobsUiActivator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

public class AddEditParameterDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField nameField = new StringDialogField();
	private final StringDialogField valueField = new StringDialogField();
	private final boolean isEdit;

	public AddEditParameterDialog(final Shell parent) {
		this(parent, null, null);
	}

	public AddEditParameterDialog(final Shell parent, final String name, final String value) {
		super(parent);
		isEdit = name != null;
		setTitle(isEdit ? "Edit Parameter" : "New Parameter");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		nameField.setText(StringUtils.trimToEmpty(name));
		valueField.setText(StringUtils.trimToEmpty(value));
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(40);
		gd.minimumWidth = convertHorizontalDLUsToPixels(200);

		nameField.setLabelText("Name:");
		valueField.setLabelText("Value:");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		nameField.setDialogFieldListener(validateListener);
		valueField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { nameField, valueField }, false);
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(valueField.getTextControl(null));
		LayoutUtil.setMargin(composite, 5);

		return composite;
	}

	public String getName() {
		return nameField.getText();
	}

	public String getValue() {
		return valueField.getText();
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK())
			return;

		super.okPressed();
	}

	@Override
	public void openNonBlocking(final DialogCallback callback) {
		super.openNonBlocking(callback);

		if (isEdit) {
			valueField.getTextControl(null).setSelection(0);
			valueField.setFocus();
		} else {
			nameField.setFocus();
		}
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
		if (StringUtils.isBlank(getName())) {
			setInfo("Please enter a name.");
			return;
		}

		if (StringUtils.isBlank(getValue())) {
			setInfo("Please enter a value.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
