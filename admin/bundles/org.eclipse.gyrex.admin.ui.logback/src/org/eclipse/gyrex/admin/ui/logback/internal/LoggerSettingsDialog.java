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
 *     Peter Grube        - rework to Admin UI
 */
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import ch.qos.logback.classic.Level;

public class LoggerSettingsDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField nameField = new StringDialogField();
	private final SelectionButtonDialogField inheritAppendersField = new SelectionButtonDialogField(SWT.CHECK);
	private final SelectionButtonDialogFieldGroup levelField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "ALL", "DEBUG", "INFO", "WARN", "ERROR", "OFF" }, 6);
	private final ListDialogField appenderRefsField = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			if (index == 0) {
				addAppenderRefButtonPressed();
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
		}

		@Override
		public void selectionChanged(final ListDialogField field) {
		}
	}, new String[] { "Add...", "Remove" }, new LogbackLabelProvider());
	private final boolean isRoot;

	private final String originalName;
	private final Level originalLevel;
	private final boolean originalInherit;
	private final Collection<String> originalAppenderRefs;
	private final Collection<Appender> allAvailableAppenders;

	private Logger logger;

	public LoggerSettingsDialog(final Shell parent, final Collection<Appender> allAvailableAppenders) {
		this(parent, null, null, true, null, allAvailableAppenders, false);
	}

	public LoggerSettingsDialog(final Shell parent, final Level defaultLevel, final Collection<String> defaultAppenderRefs, final Collection<Appender> allAvailableAppenders) {
		this(parent, null, defaultLevel, false, defaultAppenderRefs, allAvailableAppenders, true);
	}

	public LoggerSettingsDialog(final Shell parent, final String name, final Level level, final boolean inheritOtherAppenders, final Collection<String> originalAppenderRefs, final Collection<Appender> allAvailableAppenders) {
		this(parent, name, level, inheritOtherAppenders, originalAppenderRefs, allAvailableAppenders, false);
	}

	private LoggerSettingsDialog(final Shell parent, final String originalName, final Level originalLevel, final boolean originalInherit, final Collection<String> originalAppenderRefs, final Collection<Appender> allAvailableAppenders, final boolean isRoot) {
		super(parent);
		this.originalName = originalName;
		this.originalLevel = originalLevel;
		this.originalInherit = originalInherit;
		this.originalAppenderRefs = originalAppenderRefs;
		this.allAvailableAppenders = allAvailableAppenders;
		this.isRoot = isRoot;
		if (isRoot) {
			setTitle("Default Logger Settings");
		} else {
			setTitle(null != originalLevel ? "Edit Logger" : "New Logger");
		}
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	void addAppenderRefButtonPressed() {
		final SelectAppenderDialog dialog = new SelectAppenderDialog(getShell(), allAvailableAppenders);
		dialog.openNonBlocking(new DialogCallback() {
			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					final Appender appender = (Appender) dialog.getFirstResult();
					if (null != appender) {
						if (appenderRefsField.getElements().contains(appender.getName())) {
							return;
						}
						appenderRefsField.addElement(appender.getName());
					}
				}
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		nameField.setLabelText("Name:");
		levelField.setLabelText("Level:");
		inheritAppendersField.setLabelText("Inherit appenders from parent loggers.");
		inheritAppendersField.setSelection(true);
		appenderRefsField.setLabelText("Appenders:");
		appenderRefsField.setRemoveButtonIndex(1);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		nameField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), nameField, levelField, inheritAppendersField, appenderRefsField }, true);
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(levelField.getSelectionButtonsGroup(null));
		LayoutUtil.setHorizontalGrabbing(appenderRefsField.getListControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		if (isRoot) {
			nameField.setEnabled(false);
			nameField.setText("DEFAULT");
			inheritAppendersField.setEnabled(false);
			inheritAppendersField.setSelection(false);
		} else {
			if (null != originalName) {
				nameField.setText(originalName);
			}
			inheritAppendersField.setSelection(originalInherit);
		}

		if (null != originalLevel) {
			levelField.setSelection(0, false);
			levelField.setSelection(1, false);
			levelField.setSelection(2, false);
			levelField.setSelection(3, false);
			levelField.setSelection(4, false);
			levelField.setSelection(5, false);
			levelField.setSelection(6, false);
			switch (originalLevel.levelInt) {
				case Level.ALL_INT:
					levelField.setSelection(0, true);
					break;
				case Level.DEBUG_INT:
					levelField.setSelection(1, true);
					break;
				case Level.INFO_INT:
					levelField.setSelection(2, true);
					break;
				case Level.WARN_INT:
					levelField.setSelection(3, true);
					break;
				case Level.ERROR_INT:
					levelField.setSelection(4, true);
					break;
				case Level.OFF_INT:
					levelField.setSelection(5, true);
					break;
				default:
					break;
			}
		}

		if (null != originalAppenderRefs) {
			appenderRefsField.setElements(new ArrayList<String>(originalAppenderRefs));
		}

		return composite;
	}

	/**
	 * Returns the logger.
	 * 
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			logger = new Logger();
			logger.setName(nameField.getText());
			if (levelField.isSelected(0)) {
				logger.setLevel(Level.ALL);
			} else if (levelField.isSelected(1)) {
				logger.setLevel(Level.DEBUG);
			} else if (levelField.isSelected(2)) {
				logger.setLevel(Level.INFO);
			} else if (levelField.isSelected(3)) {
				logger.setLevel(Level.WARN);
			} else if (levelField.isSelected(4)) {
				logger.setLevel(Level.ERROR);
			} else if (levelField.isSelected(5)) {
				logger.setLevel(Level.OFF);
			}
			logger.setInheritOtherAppenders(inheritAppendersField.isSelected());
			final List elements = appenderRefsField.getElements();
			final List<String> appenderRefs = new ArrayList<String>();
			for (final Object appenderRef : elements) {
				if (appenderRef instanceof String && !appenderRefs.contains(appenderRef)) {
					appenderRefs.add((String) appenderRef);
				}
			}
			logger.setAppenderReferences(appenderRefs);
		} catch (final Exception e) {
			logger = null;
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

	void validate() {
		final String name = nameField.getText();
		if (StringUtils.isBlank(name)) {
			setInfo("Please enter a name.");
			return;
		}

		if (!IdHelper.isValidId(name)) {
			setError("The entered appender name is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		if (!levelField.isAnySelected()) {
			setInfo("Please select a log level.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
