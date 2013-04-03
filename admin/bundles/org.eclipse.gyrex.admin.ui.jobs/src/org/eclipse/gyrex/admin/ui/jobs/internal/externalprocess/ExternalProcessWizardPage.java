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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

public class ExternalProcessWizardPage extends WizardPage {

	private static final Object[] NO_CHILDREN = new Object[0];
	private static final long serialVersionUID = 1L;

	private final StringDialogField commandField = new StringDialogField();
	private final ListDialogField argumentsList = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			// TODO Auto-generated method stub

		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			// TODO Auto-generated method stub

		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			// TODO Auto-generated method stub

		}
	}, new String[] { "Add...", "Remove", "", "Up", "Down", "", "Modify..." }, new LabelProvider());
	{
		argumentsList.setRemoveButtonIndex(1);
		argumentsList.setUpButtonIndex(3);
		argumentsList.setDownButtonIndex(4);
	}

	private final StringDialogField workingDirectoryField = new StringDialogField();

	private final SelectionButtonDialogField clearEnvironmentCheckBox = new SelectionButtonDialogField(SWT.CHECK);
	private final ListDialogField environmentTable = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			// TODO Auto-generated method stub

		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			// TODO Auto-generated method stub

		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			// TODO Auto-generated method stub

		}
	}, new String[] { "Add...", "Remove", "", "Modify..." }, new LabelProvider());
	{
		environmentTable.setRemoveButtonIndex(1);
		environmentTable.setTableColumns(new ColumnsDescription(new String[] { "Variable", "Value" }, true));
	}

	private final StringDialogField expectedReturnCodeField = new StringDialogField();

	private final JobConfigurationWizardSession session;

	public ExternalProcessWizardPage(final JobConfigurationWizardSession session) {
		super(ExternalProcessWizardPage.class.getSimpleName());
		this.session = session;
		setTitle("Configure External Process");
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

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { commandField, argumentsList, new Separator(), clearEnvironmentCheckBox, environmentTable, new Separator(), workingDirectoryField, expectedReturnCodeField }, true);
		LayoutUtil.setHorizontalGrabbing(commandField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(argumentsList.getListControl(null));
		LayoutUtil.setHorizontalGrabbing(environmentTable.getListControl(null));
		LayoutUtil.setHorizontalGrabbing(workingDirectoryField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(expectedReturnCodeField.getTextControl(null));
	}

	public String getCommand() {
		return StringUtils.trimToNull(commandField.getText());
	}

	void validate() {
		final String command = getCommand();
		if (StringUtils.isBlank(command)) {
			setMessage("Please enter a program to launch.");
			setPageComplete(false);
			return;
		}

		setMessage(null);
		setPageComplete(true);
	}
}
