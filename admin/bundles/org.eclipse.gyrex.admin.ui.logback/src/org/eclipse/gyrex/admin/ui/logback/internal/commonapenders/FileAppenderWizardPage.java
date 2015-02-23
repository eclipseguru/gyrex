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
package org.eclipse.gyrex.admin.ui.logback.internal.commonapenders;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.logback.config.model.FileAppender;
import org.eclipse.gyrex.logback.config.model.FileAppender.RotationPolicy;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class FileAppenderWizardPage extends WizardPage {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final StringDialogField fileNameField = new StringDialogField();
	private final SelectionButtonDialogField compressField = new SelectionButtonDialogField(SWT.CHECK);
	private final SelectionButtonDialogFieldGroup rotationTypeField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "never", "daily", "weekly", "monthly", "based on size" }, 5);
	private final StringDialogField maxFileSizeField = new StringDialogField();
	private final StringDialogField maxHistoryField = new StringDialogField();
	private final StringDialogField siftingPropertyNameField = new StringDialogField();
	private final StringDialogField siftingPropertyDefaultField = new StringDialogField();

	private final FileAppender appender;

	public FileAppenderWizardPage(final AppenderConfigurationWizardSession session) {
		super("file");
		setTitle("File Appender");
		setDescription("Configure log file details like name and rotation.");

		appender = (FileAppender) checkNotNull(session.getAppender());
		setPageComplete(false); // initial status is incomplete
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		fileNameField.setLabelText("File Name:");
		rotationTypeField.setLabelText("Rotate log files:");
		compressField.setLabelText("Compress rotated logs");
		maxHistoryField.setLabelText("Number of rotated logs to keep:");
		maxFileSizeField.setLabelText("Rotate when log file is greater then:");

		siftingPropertyNameField.setLabelText("Separate log files based on MDC property:");
		siftingPropertyDefaultField.setLabelText("Default value if MDC property is not set:");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				updateEnabledFields();
				validate();
			}
		};

		fileNameField.setDialogFieldListener(validateListener);
		rotationTypeField.setDialogFieldListener(validateListener);
		maxFileSizeField.setDialogFieldListener(validateListener);
		maxHistoryField.setDialogFieldListener(validateListener);
		siftingPropertyNameField.setDialogFieldListener(validateListener);

		compressField.setSelection(true);
		updateEnabledFields();

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fileNameField, new Separator(), rotationTypeField, compressField, maxHistoryField, maxFileSizeField, new Separator(), siftingPropertyNameField, siftingPropertyDefaultField }, false);
		LayoutUtil.setHorizontalGrabbing(fileNameField.getTextControl(null));
	}

	private void updateAppender() {
		appender.setFileName(fileNameField.getText());
		if (rotationTypeField.isSelected(1) || rotationTypeField.isSelected(2) || rotationTypeField.isSelected(3)) {
			if (rotationTypeField.isSelected(1)) {
				appender.setRotationPolicy(RotationPolicy.DAILY);
			} else if (rotationTypeField.isSelected(2)) {
				appender.setRotationPolicy(RotationPolicy.WEEKLY);
			} else if (rotationTypeField.isSelected(3)) {
				appender.setRotationPolicy(RotationPolicy.MONTHLY);
			}
			appender.setMaxHistory(StringUtils.trimToNull(maxHistoryField.getText()));
		} else if (rotationTypeField.isSelected(4)) {
			appender.setRotationPolicy(RotationPolicy.SIZE);
			appender.setMaxFileSize(StringUtils.trimToNull(maxFileSizeField.getText()));
		}
		appender.setCompressRotatedLogs(compressField.isSelected());
		final String siftingMdcPropertyName = StringUtils.trimToNull(siftingPropertyNameField.getText());
		if (null != siftingMdcPropertyName) {
			appender.setSiftingMdcPropertyName(siftingMdcPropertyName);
			appender.setSiftingMdcPropertyDefaultValue(StringUtils.trimToNull(siftingPropertyDefaultField.getText()));
		}
	}

	void updateEnabledFields() {
		siftingPropertyDefaultField.setEnabled(StringUtils.isNotBlank(siftingPropertyNameField.getText()));
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
	}

	void validate() {
		final String fileName = fileNameField.getText();
		if (StringUtils.isBlank(fileName)) {
			setMessage("Please enter a file name.", INFORMATION);
			setPageComplete(false);
			return;
		}

		if (!IdHelper.isValidId(fileName)) {
			setMessage("The entered file name is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.", ERROR);
			return;
		}

		updateAppender();

		setMessage(null);
		setPageComplete(true);
	}
}
