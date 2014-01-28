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
package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

public class EditPropertyDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField keyField = new StringDialogField();
	private final StringDialogField valueField = new StringDialogField();
	private final String key;
	private final String value;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public EditPropertyDialog(final Shell parent, final String key, final String value) {
		super(parent);
		this.key = key;
		this.value = value;
		setTitle(null == key ? "New Property" : "Edit Property");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(80);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		keyField.setLabelText("Name");
		valueField.setLabelText("Value");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		keyField.setDialogFieldListener(validateListener);
		valueField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), keyField, valueField }, false);
		LayoutUtil.setHorizontalGrabbing(keyField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(valueField.getTextControl(null));

		if (null != key) {
			keyField.setText(key);
			keyField.setEnabled(false);
		} else {
			keyField.setFocus();
		}
		if (null != value) {
			valueField.setText(value);
			valueField.getTextControl(null).selectAll();
			valueField.getTextControl(null).setFocus();
		}

		return composite;
	}

	public String getKey() {
		return keyField.getText();
	}

	public String getValue() {
		return valueField.getText();
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}
		super.okPressed();
	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, HttpUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, HttpUiActivator.SYMBOLIC_NAME, message));
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, HttpUiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		if (null == key) {
			final String key = getKey();
			if (StringUtils.isBlank(key)) {
				setInfo("Please enter a property name.");
				return;
			}
		}

		updateStatus(Status.OK_STATUS);
	}
}
