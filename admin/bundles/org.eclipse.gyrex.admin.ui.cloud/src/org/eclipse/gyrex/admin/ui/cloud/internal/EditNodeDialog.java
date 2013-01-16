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
package org.eclipse.gyrex.admin.ui.cloud.internal;

import java.util.HashSet;

import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeConfigurer;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;

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

public class EditNodeDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField nameField = new StringDialogField();
	private final StringDialogField locationField = new StringDialogField();
	private final StringDialogField tagsField = new StringDialogField();

	private final ICloudManager cloudManager;
	private final INodeDescriptor nodeDescriptor;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public EditNodeDialog(final Shell parent, final ICloudManager cloudManager, final INodeDescriptor nodeDescriptor) {
		super(parent);
		this.cloudManager = cloudManager;
		this.nodeDescriptor = nodeDescriptor;
		setTitle("Edit Node Info");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);

		idField.setText(nodeDescriptor.getId());
		nameField.setText(nodeDescriptor.getName());
		locationField.setText(nodeDescriptor.getLocation());
		tagsField.setText(StringUtils.join(nodeDescriptor.getTags(), ", "));
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		nameField.setLabelText("Name");
		locationField.setLabelText("Location");
		tagsField.setLabelText("Tags");

		idField.setEnabled(false);

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		nameField.setDialogFieldListener(validateListener);
		locationField.setDialogFieldListener(validateListener);
		tagsField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, nameField, locationField, tagsField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			final INodeConfigurer nodeConfigurer = cloudManager.getNodeConfigurer(nodeDescriptor.getId());
			nodeConfigurer.setName(nameField.getText());
			nodeConfigurer.setLocation(locationField.getText());
			final String[] tags = StringUtils.split(tagsField.getText(), ',');
			final HashSet<String> cleanedTags = new HashSet<String>();
			for (String tag : tags) {
				tag = StringUtils.trimToNull(tag);
				if (tag != null) {
					cleanedTags.add(tag);
				}
			}
			nodeConfigurer.setTags(cleanedTags);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	protected void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, CloudUiActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	protected void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, CloudUiActivator.SYMBOLIC_NAME, message));
	}

	protected void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, CloudUiActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		updateStatus(Status.OK_STATUS);
	}
}
