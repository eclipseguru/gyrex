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
package org.eclipse.gyrex.admin.ui.jobs.internal.externalprocess;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField.ColumnsDescription;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardSession;
import org.eclipse.gyrex.jobs.internal.externalprocess.ExternalProcessJobParameter;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class ExternalProcessWizardPage extends WizardPage {

	private static final long serialVersionUID = 1L;

	final StringDialogField commandField = new StringDialogField();
	final ListDialogField argumentsList = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			switch (index) {
				case 0:
					openAddArgumentDialog();
					break;
				case 1:
					openEditArgumentDialog();
					break;
				default:
					// nothing
					break;
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			openEditArgumentDialog();
		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			field.enableButton(1, field.getSelectedElements().size() == 1);
		}
	}, new String[] { "New...", "Edit...", "Remove", null, "Up", "Down", }, new LabelProvider());
	{
		argumentsList.setRemoveButtonIndex(2);
		argumentsList.setUpButtonIndex(4);
		argumentsList.setDownButtonIndex(5);
	}

	final StringDialogField workingDirectoryField = new StringDialogField();

	final SelectionButtonDialogField clearEnvironmentCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	final ListDialogField environmentTable = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			switch (index) {
				case 0:
					openAddVariableDialog();
					break;
				case 1:
					openSelectVariableDialog();
					break;
				case 2:
					openEditVariableDialog();
					break;
				default:
					// nothing
					break;
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			openEditVariableDialog();
		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			field.enableButton(2, field.getSelectedElements().size() == 1);
		}
	}, new String[] { "New...", "Select...", "Edit...", "Remove" }, new ColumnLabelProvider() {
		private static final long serialVersionUID = 1L;

		@Override
		public void update(final ViewerCell cell) {
			final Variable element = (Variable) cell.getElement();
			switch (cell.getColumnIndex()) {
				case 0:
					cell.setText(element.getName());
					break;

				case 1:
					if (StringUtils.equals(element.getValue(), ExternalProcessJobParameter.ENV_VALUE_INHERIT)) {
						cell.setText(System.getenv(element.getName()) + " (inherited)");
						cell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
					} else {
						cell.setText(element.getValue());
						cell.setForeground(null);
					}
					break;
				default:
					break;
			}
		}
	});
	{
		environmentTable.setRemoveButtonIndex(3);
		environmentTable.setTableColumns(new ColumnsDescription(new String[] { "Variable", "Value" }, true));
	}

	final StringDialogField expectedReturnCodeField = new StringDialogField();

	private final JobConfigurationWizardSession session;

	public ExternalProcessWizardPage(final JobConfigurationWizardSession session) {
		super(ExternalProcessWizardPage.class.getSimpleName());
		this.session = session;
		setTitle("External Process");
		setDescription("Provide details for the external process to launch.");
		setPageComplete(false);
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setLayoutData(GridDataFactory.fillDefaults().minSize(convertVerticalDLUsToPixels(200), convertHorizontalDLUsToPixels(400)).create());
		setControl(composite);

		commandField.setLabelText("Program:");
		argumentsList.setLabelText("Arguments:");

		clearEnvironmentCheckBox.setLabelText("Clear native environment before launching.");
		environmentTable.setLabelText("Environment variable to set:");

		workingDirectoryField.setLabelText("Working Directory:");
		expectedReturnCodeField.setLabelText("Return Code:");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		commandField.setDialogFieldListener(validateListener);
		argumentsList.setDialogFieldListener(validateListener);
		clearEnvironmentCheckBox.setDialogFieldListener(validateListener);
		environmentTable.setDialogFieldListener(validateListener);
		workingDirectoryField.setDialogFieldListener(validateListener);
		expectedReturnCodeField.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { commandField, argumentsList, new Separator(), clearEnvironmentCheckBox, environmentTable, new Separator(), workingDirectoryField, expectedReturnCodeField }, false);
		LayoutUtil.setHorizontalGrabbing(commandField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(argumentsList.getListControl(null));
		LayoutUtil.setHorizontalGrabbing(environmentTable.getListControl(null));
		LayoutUtil.setHorizontalGrabbing(workingDirectoryField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(expectedReturnCodeField.getTextControl(null));

		// initialize from existing parameter
		readParameterFromSession();
	}

	public String getCommand() {
		return StringUtils.trimToNull(commandField.getText());
	}

	private String getWorkingDir() {
		return workingDirectoryField.getText();
	}

	void openAddArgumentDialog() {
		final AddEditArgumentDialog dialog = new AddEditArgumentDialog(getShell());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getValue())) {
					argumentsList.addElement(new Argument(dialog.getValue()));
				}
			}
		});
	}

	void openAddVariableDialog() {
		final AddEditEnvironmentVariableDialog dialog = new AddEditEnvironmentVariableDialog(getShell());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getName())) {
					environmentTable.addElement(new Variable(dialog.getName(), dialog.getValue()));
				}
			}
		});
	}

	void openEditArgumentDialog() {
		final Argument a = (Argument) argumentsList.getSelectedElements().iterator().next();
		final AddEditArgumentDialog dialog = new AddEditArgumentDialog(getShell(), a.getValue());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getValue())) {
					a.setValue(dialog.getValue());
					argumentsList.refresh();
				}
			}
		});
	}

	void openEditVariableDialog() {
		final Variable v = (Variable) environmentTable.getSelectedElements().iterator().next();
		final AddEditEnvironmentVariableDialog dialog = new AddEditEnvironmentVariableDialog(getShell(), v.getName(), v.getValue());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getName())) {
					v.setName(dialog.getName());
					v.setValue(dialog.getValue());
					environmentTable.refresh();
				}
			}
		});
	}

	void openSelectVariableDialog() {
		final SelectEnvironmentVariableDialog dialog = new SelectEnvironmentVariableDialog(getShell());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					for (final String name : dialog.getVariables()) {
						environmentTable.addElement(new Variable(name, ExternalProcessJobParameter.ENV_VALUE_INHERIT));
					}
					environmentTable.refresh();
				}
			}
		});
	}

	private void readParameterFromSession() {
		final ExternalProcessJobParameter p = ExternalProcessJobParameter.fromParameter(session.getParameter(), false);
		if (StringUtils.isNotBlank(p.getCommand())) {
			commandField.setText(StringUtils.trimToEmpty(p.getCommand()));
		}

		if (p.getArguments() != null) {
			argumentsList.removeAllElements();
			for (final String value : p.getArguments()) {
				argumentsList.addElement(new Argument(value));
			}
			argumentsList.selectElements(null);
		}

		if (p.getExpectedReturnCode() != null) {
			expectedReturnCodeField.setText(String.valueOf(p.getExpectedReturnCode().intValue()));
		}

		if (p.getClearEnvironment() != null) {
			clearEnvironmentCheckBox.setSelection(p.getClearEnvironment().booleanValue());
		}

		if (p.getEnvironment() != null) {
			environmentTable.removeAllElements();
			for (final Entry<String, String> e : p.getEnvironment().entrySet()) {
				environmentTable.addElement(new Variable(e.getKey(), e.getValue()));
			}
			environmentTable.selectElements(null);
		}

		if (StringUtils.isNotBlank(p.getWorkingDir())) {
			workingDirectoryField.setText(StringUtils.trimToEmpty(p.getWorkingDir()));
		}
	}

	private void saveParameterToSession() {
		final ExternalProcessJobParameter p = new ExternalProcessJobParameter();

		p.setCommand(StringUtils.trimToNull(getCommand()));
		p.setWorkingDir(StringUtils.trimToNull(getWorkingDir()));
		p.setClearEnvironment(clearEnvironmentCheckBox.isSelected());

		if (StringUtils.isBlank(expectedReturnCodeField.getText())) {
			p.setExpectedReturnCode(null);
		} else {
			p.setExpectedReturnCode(NumberUtils.toInt(expectedReturnCodeField.getText()));
		}

		final List<Object> arguments = argumentsList.getElements();
		if (!arguments.isEmpty()) {
			final List<String> result = new ArrayList<>(arguments.size());
			for (final Object object : arguments) {
				result.add(((Argument) object).getValue());
			}
			p.setArguments(result);
		} else {
			p.setArguments(null);
		}

		final List<Object> variables = environmentTable.getElements();
		if (!variables.isEmpty()) {
			final Map<String, String> result = new LinkedHashMap<>();
			for (final Object variable : variables) {
				final Variable v = (Variable) variable;
				result.put(v.getName(), v.getValue());
			}
			p.setEnvironment(result);
		} else {
			p.setEnvironment(null);
		}

		session.setParameter(p.toParameter());
	}

	void validate() {
		final String command = getCommand();
		if (StringUtils.isBlank(command)) {
			setMessage("Please enter a program to launch.");
			setPageComplete(false);
			return;
		}

		final String returnCode = expectedReturnCodeField.getText();
		if (StringUtils.isNotBlank(returnCode)) {
			try {
				Integer.parseInt(returnCode);
			} catch (final NumberFormatException e) {
				setMessage("The entered return code must be an integer number.", IMessageProvider.ERROR);
				setPageComplete(false);
				return;
			}
		}

		saveParameterToSession();

		setMessage(null);
		setPageComplete(true);
	}
}
