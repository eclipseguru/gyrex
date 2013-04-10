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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IListAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.ListDialogField.ColumnsDescription;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardSession;
import org.eclipse.gyrex.admin.ui.jobs.internal.externalprocess.AddEditEnvironmentVariableDialog;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang.StringUtils;

public class GenericJobParameterPage extends WizardPage {

	private static final long serialVersionUID = 1L;

	final ListDialogField parameterTable = new ListDialogField(new IListAdapter() {

		@Override
		public void customButtonPressed(final ListDialogField field, final int index) {
			switch (index) {
				case 0:
					openAddParameterDialog();
					break;
				case 1:
					openEditParameterDialog();
					break;
				default:
					// nothing
					break;
			}
		}

		@Override
		public void doubleClicked(final ListDialogField field) {
			openEditParameterDialog();
		}

		@Override
		public void selectionChanged(final ListDialogField field) {
			field.enableButton(2, field.getSelectedElements().size() == 1);
		}
	}, new String[] { "New...", "Edit...", "Remove" }, new ColumnLabelProvider() {
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(final Object element) {
			if (element instanceof Parameter)
				return ((Parameter) element).getName();

			return element == null ? "" : element.toString();//$NON-NLS-1$
		}

		@Override
		public void update(final ViewerCell cell) {
			final Parameter element = (Parameter) cell.getElement();
			switch (cell.getColumnIndex()) {
				case 0:
					cell.setText(element.getName());
					break;

				case 1:
					cell.setText(element.getValue());
					break;

				default:
					break;
			}
		}
	});
	{
		parameterTable.setRemoveButtonIndex(2);
		parameterTable.setTableColumns(new ColumnsDescription(new String[] { "Parameter", "Value" }, true));
	}

	private final JobConfigurationWizardSession session;

	public GenericJobParameterPage(final JobConfigurationWizardSession session) {
		super(GenericJobParameterPage.class.getSimpleName());
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

		parameterTable.setLabelText("Job Parameter:");
		parameterTable.setViewerComparator(new ViewerComparator());

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		parameterTable.setDialogFieldListener(validateListener);

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { parameterTable }, true);
		LayoutUtil.setHorizontalGrabbing(parameterTable.getListControl(null));

		// initialize from existing parameter
		readParameterFromSession();
	}

	void openAddParameterDialog() {
		final AddEditEnvironmentVariableDialog dialog = new AddEditEnvironmentVariableDialog(getShell());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getName())) {
					parameterTable.addElement(new Parameter(dialog.getName(), dialog.getValue()));
				}
			}
		});
	}

	void openEditParameterDialog() {
		final Parameter v = (Parameter) parameterTable.getSelectedElements().iterator().next();
		final AddEditEnvironmentVariableDialog dialog = new AddEditEnvironmentVariableDialog(getShell(), v.getName(), v.getValue());
		dialog.openNonBlocking(new DialogCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if ((returnCode == Window.OK) && StringUtils.isNotBlank(dialog.getName())) {
					v.setName(dialog.getName());
					v.setValue(dialog.getValue());
					parameterTable.refresh();
				}
			}
		});
	}

	private void readParameterFromSession() {
		parameterTable.removeAllElements();
		for (final Entry<String, String> e : session.getParameter().entrySet()) {
			parameterTable.addElement(new Parameter(e.getKey(), e.getValue()));
		}
		parameterTable.selectElements(null);
	}

	private void saveParameterToSession() {
		final Map<String, String> result = new LinkedHashMap<>();
		for (final Object variable : parameterTable.getElements()) {
			final Parameter v = (Parameter) variable;
			result.put(v.getName(), v.getValue());
		}

		session.setParameter(result);
	}

	void validate() {
		saveParameterToSession();

		setMessage(null);
		setPageComplete(true);
	}
}
