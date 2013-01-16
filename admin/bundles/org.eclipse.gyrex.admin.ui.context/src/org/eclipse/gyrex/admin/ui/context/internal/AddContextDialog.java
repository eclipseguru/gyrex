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
package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

/**
 * The Class represents RAP UI non blocking status dialog to enter Context
 * values.
 */
public class AddContextDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/** The path field. */
	private final StringDialogField pathField = new StringDialogField();

	/** The name field. */
	private final StringDialogField nameField = new StringDialogField();

	/** The registry impl. */
	private final ContextRegistryImpl registryImpl;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            the parent UI element
	 * @param registryImpl
	 *            the registry impl
	 */
	public AddContextDialog(final Shell parent, final ContextRegistryImpl registryImpl) {
		super(parent);
		this.registryImpl = registryImpl;
		setTitle("New Software Repository");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		pathField.setLabelText("Path");
		nameField.setLabelText("Name");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		pathField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), pathField, nameField }, false);
		LayoutUtil.setHorizontalGrabbing(pathField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(nameField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			final ContextDefinition contextDefinition = new ContextDefinition(new Path(pathField.getText()).makeAbsolute().addTrailingSeparator());
			contextDefinition.setName(nameField.getText());
			registryImpl.saveDefinition(contextDefinition);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	/**
	 * Sets the error.
	 * 
	 * @param message
	 *            the new error
	 */
	void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, ContextUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	/**
	 * Sets the info.
	 * 
	 * @param message
	 *            the new info
	 */
	void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, ContextUiActivator.SYMBOLIC_NAME, message));
	}

	/**
	 * Sets the warning.
	 * 
	 * @param message
	 *            the new warning
	 */
	void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, ContextUiActivator.SYMBOLIC_NAME, message));
	}

	/**
	 * Validate entered values.
	 */
	void validate() {
		final String id = pathField.getText();
		if (StringUtils.isNotBlank(id) && !Path.EMPTY.isValidPath(id)) {
			setError("The entered path is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_' and '/' as separator.");
			return;
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a repository id.");
			return;
		}

		final String name = nameField.getText();
		if (StringUtils.isBlank(name)) {
			setInfo("Please enter a context name.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
