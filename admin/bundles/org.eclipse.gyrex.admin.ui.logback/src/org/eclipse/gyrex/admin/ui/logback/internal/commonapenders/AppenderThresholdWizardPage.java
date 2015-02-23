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

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardSession;
import org.eclipse.gyrex.logback.config.model.Appender;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.qos.logback.classic.Level;

/**
 * Wizard page for configuring a threshold on any {@link Appender}.
 */
public class AppenderThresholdWizardPage extends WizardPage {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static final int IDX_NONE = 0;
	private static final int IDX_DEBUG = 1;
	private static final int IDX_INFO = 2;
	private static final int IDX_WARN = 3;
	private static final int IDX_ERROR = 4;

	private final SelectionButtonDialogFieldGroup thresholdField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "No filter", "DEBUG", "INFO", "WARN", "ERROR" }, 1);
	private final AppenderConfigurationWizardSession session;

	public AppenderThresholdWizardPage(final AppenderConfigurationWizardSession session) {
		super("threshold");
		this.session = session;
		setTitle("Appender Threshold");
		setDescription("Configure an appender threshold.");
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		thresholdField.setLabelText("Only log events with a severity equal to or higher than:");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		thresholdField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { thresholdField }, true);

		final GridLayout layout = (GridLayout) thresholdField.getSelectionButtonsGroup(null).getLayout();
		layout.marginLeft = 20;

		if (session.getAppender() != null) {

		}
	}

	@Override
	public void setVisible(final boolean visible) {
		final Level level = session.getAppender().getThreshold();
		if (level != null) {
			switch (level.levelInt) {
				case Level.DEBUG_INT:
					thresholdField.setSelection(IDX_DEBUG, true);
					break;
				case Level.INFO_INT:
					thresholdField.setSelection(IDX_INFO, true);
					break;
				case Level.WARN_INT:
					thresholdField.setSelection(IDX_WARN, true);
					break;
				case Level.ERROR_INT:
					thresholdField.setSelection(IDX_ERROR, true);
					break;
				case Level.OFF_INT:
				default:
					thresholdField.setSelection(IDX_NONE, true);
					break;
			}
		} else {
			thresholdField.setSelection(IDX_NONE, true);
		}

		super.setVisible(visible);
	}

	void validate() {
		final Appender appender = session.getAppender();
		if (appender == null) {
			setMessage("Please select an appender type first!");
			return;
		}

		if (thresholdField.isSelected(IDX_DEBUG)) {
			appender.setThreshold(Level.DEBUG);
		} else if (thresholdField.isSelected(IDX_INFO)) {
			appender.setThreshold(Level.INFO);
		} else if (thresholdField.isSelected(IDX_WARN)) {
			appender.setThreshold(Level.WARN);
		} else if (thresholdField.isSelected(IDX_ERROR)) {
			appender.setThreshold(Level.ERROR);
		} else {
			appender.setThreshold(null);
		}

		setMessage(null);
		setPageComplete(true);
	}

}
