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

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DescriptionDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.ConsoleAppender;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.qos.logback.core.joran.spi.ConsoleTarget;

/**
 * Wizard page for {@link ConsoleAppender}.
 */
public class ConsoleAppenderWizardPage extends WizardPage {

	private static final int INDEX_STD_ERR = 1;

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private final StringDialogField patternField = new StringDialogField();
	private final DescriptionDialogField patternDescriptionField = new DescriptionDialogField();

	SelectionButtonDialogFieldGroup targetGroup = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "STDOUT", "STDERR" }, 2);

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

		targetGroup.setLabelText("Output to:");

		patternField.setLabelText("Pattern:");
		patternDescriptionField.setText("<small>Please have a look at <a href=\"\"></a> for possible patterns or leave empty for a system default.</small>");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		patternDescriptionField.setDialogFieldListener(validateListener);
		patternField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { targetGroup, new Separator(), patternField, patternDescriptionField }, false);
		LayoutUtil.setHorizontalGrabbing(targetGroup.getSelectionButtonsGroup(null));
		LayoutUtil.setHorizontalGrabbing(patternField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(patternDescriptionField.getDescriptionControl(null));

		if (appender != null) {
			if (isNotBlank(appender.getPattern())) {
				patternField.setText(appender.getPattern());
			}
			if (appender.getTarget() == ConsoleTarget.SystemErr) {
				targetGroup.setSelection(INDEX_STD_ERR, true);
			}
		}
	}

	public String getPattern() {
		return patternField.getText();
	}

	private ConsoleTarget getTarget() {
		return targetGroup.isSelected(INDEX_STD_ERR) ? ConsoleTarget.SystemErr : ConsoleTarget.SystemOut;
	}

	void validate() {
		final String pattern = getPattern();
		appender.setPattern(isBlank(pattern) ? null : pattern);
		appender.setTarget(getTarget());

		setMessage(null);
		setPageComplete(true);
	}

}
