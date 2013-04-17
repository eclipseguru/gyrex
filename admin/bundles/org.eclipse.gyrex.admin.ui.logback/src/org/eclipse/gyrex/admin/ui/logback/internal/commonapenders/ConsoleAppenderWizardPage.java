/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal.commonapenders;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.ConsoleAppender;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard pager for {@link ConsoleAppender}.
 */
public class ConsoleAppenderWizardPage extends WizardPage {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final SelectionButtonDialogField useCustomPatternCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final StringDialogField patternField = new StringDialogField();

	private final ConsoleAppender appender;

	public ConsoleAppenderWizardPage(final AppenderConfigurationWizardSession session) {
		super("console");
		final Appender appender = session.getAppender();
		if (appender instanceof ConsoleAppender) {
			this.appender = (ConsoleAppender) appender;
		} else {
			this.appender = new ConsoleAppender();
			session.setAppender(this.appender);
		}
		setTitle("Console Appender");
		setDescription("Configure details for a console appender.");
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		useCustomPatternCheckBox.setLabelText("Use a custom message pattern:");
		patternField.setLabelText("");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		useCustomPatternCheckBox.setDialogFieldListener(validateListener);
		patternField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { useCustomPatternCheckBox, patternField }, false);
		LayoutUtil.setHorizontalGrabbing(patternField.getTextControl(null));

		useCustomPatternCheckBox.setAttachedDialogFields(patternField);

		if (appender != null) {
			if (isNotBlank(appender.getPattern())) {
				useCustomPatternCheckBox.setSelection(true);
				patternField.setText(appender.getPattern());
			}
		}
	}

	private String getPattern() {
		return patternField.getText();
	}

	void validate() {
		if (useCustomPatternCheckBox.isSelected()) {
			final String pattern = getPattern();
			if (isBlank(pattern)) {
				setMessage("Please enter a pattern.", INFORMATION);
				setPageComplete(false);
				return;
			}
			appender.setPattern(pattern);
		} else {
			appender.setPattern(null);
		}

		setMessage(null);
		setPageComplete(true);
	}

}
