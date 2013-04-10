/**
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.ConsoleAppender;
import org.eclipse.gyrex.logback.config.model.FileAppender;
import org.eclipse.gyrex.logback.config.model.FileAppender.RotationPolicy;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import ch.qos.logback.classic.Level;

public class AddAppenderDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final SelectionButtonDialogFieldGroup typeField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "Console", "File" }, 2);
	private final StringDialogField nameField = new StringDialogField();
	private final StringDialogField fileNameField = new StringDialogField();
	private final SelectionButtonDialogField compressField = new SelectionButtonDialogField(SWT.CHECK);
	private final SelectionButtonDialogFieldGroup rotationTypeField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "never", "daily", "weekly", "monthly", "based on size" }, 5);
	private final StringDialogField maxFileSizeField = new StringDialogField();
	private final StringDialogField maxHistoryField = new StringDialogField();
	private final SelectionButtonDialogFieldGroup thresholdField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "No filter", "DEBUG", "INFO", "WARN", "ERROR" }, 5);
	private final StringDialogField siftingPropertyNameField = new StringDialogField();
	private final StringDialogField siftingPropertyDefaultField = new StringDialogField();

	private Appender appender;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public AddAppenderDialog(final Shell parent) {
		super(parent);
		setTitle("New Appender");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		typeField.setLabelText("Type");
		nameField.setLabelText("Appender Name");
		fileNameField.setLabelText("File Name");
		rotationTypeField.setLabelText("Rotate log files");
		compressField.setLabelText("Compress rotated logs");
		maxHistoryField.setLabelText("Number of rotated logs to keep");
		maxFileSizeField.setLabelText("Rotate when log file is greater then");
		thresholdField.setLabelText("Filter log event below");

		siftingPropertyNameField.setLabelText("Separate log files based on MDC property:");
		siftingPropertyDefaultField.setLabelText("Default value if MDC property is not set:");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				updateEnabledFields();
				validate();
			}
		};

		typeField.setDialogFieldListener(validateListener);
		nameField.setDialogFieldListener(validateListener);
		fileNameField.setDialogFieldListener(validateListener);
		rotationTypeField.setDialogFieldListener(validateListener);
		maxFileSizeField.setDialogFieldListener(validateListener);
		maxHistoryField.setDialogFieldListener(validateListener);
		siftingPropertyNameField.setDialogFieldListener(validateListener);

		compressField.setSelection(true);
		updateEnabledFields();

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), typeField, nameField, thresholdField, new Separator(), fileNameField, new Separator(), rotationTypeField, compressField, maxHistoryField, maxFileSizeField, new Separator(), siftingPropertyNameField, siftingPropertyDefaultField }, true);
		LayoutUtil.setHorizontalGrabbing(fileNameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	/**
	 * Returns the appender.
	 * 
	 * @return the appender
	 */
	public Appender getAppender() {
		return appender;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK())
			return;

		try {
			if (typeField.isSelected(0)) {
				appender = new ConsoleAppender();
			} else {
				final FileAppender fileAppender = new FileAppender();
				appender = fileAppender;
				fileAppender.setFileName(fileNameField.getText());
				if (rotationTypeField.isSelected(1) || rotationTypeField.isSelected(2) || rotationTypeField.isSelected(3)) {
					if (rotationTypeField.isSelected(1)) {
						fileAppender.setRotationPolicy(RotationPolicy.DAILY);
					} else if (rotationTypeField.isSelected(2)) {
						fileAppender.setRotationPolicy(RotationPolicy.WEEKLY);
					} else if (rotationTypeField.isSelected(3)) {
						fileAppender.setRotationPolicy(RotationPolicy.MONTHLY);
					}
					fileAppender.setMaxHistory(StringUtils.trimToNull(maxHistoryField.getText()));
				} else if (rotationTypeField.isSelected(4)) {
					fileAppender.setRotationPolicy(RotationPolicy.SIZE);
					fileAppender.setMaxFileSize(StringUtils.trimToNull(maxFileSizeField.getText()));
				}
				fileAppender.setCompressRotatedLogs(compressField.isSelected());
				final String siftingMdcPropertyName = StringUtils.trimToNull(siftingPropertyNameField.getText());
				if (null != siftingMdcPropertyName) {
					fileAppender.setSiftingMdcPropertyName(siftingMdcPropertyName);
					fileAppender.setSiftingMdcPropertyDefaultValue(StringUtils.trimToNull(siftingPropertyDefaultField.getText()));
				}
			}
			if (thresholdField.isSelected(1)) {
				appender.setThreshold(Level.DEBUG);
			} else if (thresholdField.isSelected(2)) {
				appender.setThreshold(Level.INFO);
			} else if (thresholdField.isSelected(3)) {
				appender.setThreshold(Level.WARN);
			} else if (thresholdField.isSelected(4)) {
				appender.setThreshold(Level.ERROR);
			}
			appender.setName(nameField.getText());
		} catch (final Exception e) {
			appender = null;
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, LogbackUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, LogbackUiActivator.SYMBOLIC_NAME, message));
	}

	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, LogbackUiActivator.SYMBOLIC_NAME, message));
	}

	void updateEnabledFields() {
		if (typeField.isSelected(1)) {
			fileNameField.setEnabled(true);
			siftingPropertyNameField.setEnabled(true);
			siftingPropertyDefaultField.setEnabled(StringUtils.isNotBlank(siftingPropertyNameField.getText()));
			rotationTypeField.setEnabled(true);
			if (rotationTypeField.isSelected(1) || rotationTypeField.isSelected(2) || rotationTypeField.isSelected(3)) {
				maxFileSizeField.setEnabled(false);
				maxHistoryField.setEnabled(true);
				compressField.setEnabled(true);
			} else if (rotationTypeField.isSelected(4)) {
				maxFileSizeField.setEnabled(true);
				maxHistoryField.setEnabled(false);
				compressField.setEnabled(true);
			} else {
				maxFileSizeField.setEnabled(false);
				maxHistoryField.setEnabled(false);
				compressField.setEnabled(false);
			}
		} else {
			fileNameField.setEnabled(false);
			rotationTypeField.setEnabled(false);
			maxFileSizeField.setEnabled(false);
			maxHistoryField.setEnabled(false);
			compressField.setEnabled(false);
			siftingPropertyNameField.setEnabled(false);
			siftingPropertyDefaultField.setEnabled(false);
		}
	}

	void validate() {
		if (!(typeField.isSelected(0) || typeField.isSelected(1))) {
			setInfo("Please select an appender type.");
			return;
		}

		final String name = nameField.getText();
		if (StringUtils.isBlank(name)) {
			setInfo("Please enter an appender name.");
			return;
		}

		if (!IdHelper.isValidId(name)) {
			setError("The entered appender name is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		if (typeField.isSelected(1)) {
			final String fileName = fileNameField.getText();
			if (StringUtils.isBlank(fileName)) {
				setInfo("Please enter a file name.");
				return;
			}

			if (!IdHelper.isValidId(fileName)) {
				setError("The entered file name is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
				return;
			}

		}

		updateStatus(Status.OK_STATUS);
	}
}
