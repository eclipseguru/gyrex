/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

/**
 * DialogField using RWT Upload widget
 */
public class UploadDialogField extends DialogField {

	protected static GridData gridDataForUpload(final int span) {
		final GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = span;
		return gd;
	}

	private String uploadButtonLabel;
	private Button uploadControl;
	private final IUploadAdapter uploadAdapter;

	public UploadDialogField(final IUploadAdapter uploadAdapter) {
		super();
		this.uploadAdapter = uploadAdapter;
		uploadButtonLabel = "Upload..."; //$NON-NLS-1$
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Button upload = getUploadControl(parent);
		upload.setLayoutData(gridDataForUpload(nColumns - 1));

		return new Control[] { label, upload };
	}

	void doOpenUploadDialog() {
		final FileDialog fileDialog = new FileDialog(SwtUtil.getShell(uploadControl), SWT.TITLE | SWT.MULTI);
		fileDialog.setText("Upload Files");
		fileDialog.setAutoUpload(true);
		fileDialog.open();
		uploadAdapter.uploadFinished(fileDialog.getFileNames());
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the text control
	 */
	public Button getUploadControl(final Composite parent) {
		if (uploadControl == null) {
			assertCompositeNotNull(parent);

			uploadControl = new Button(parent, SWT.PUSH);
			uploadControl.setText(uploadButtonLabel);
			uploadControl.setFont(parent.getFont());
			uploadControl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					doOpenUploadDialog();
				}
			});
		}
		return uploadControl;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(uploadControl)) {
		}
	}

	@Override
	public boolean setFocus() {
		if (isOkToUse(uploadControl)) {
			uploadControl.setFocus();
		}
		return true;
	}

	/**
	 * Sets the uploadButtonLabel.
	 * 
	 * @param uploadButtonLabel
	 *            the uploadButtonLabel to set
	 */
	public void setUploadButtonLabel(final String uploadButtonLabel) {
		this.uploadButtonLabel = uploadButtonLabel;
		if (isOkToUse(uploadControl)) {
			uploadControl.setText(uploadButtonLabel);
		}
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(uploadControl)) {
			uploadControl.setEnabled(isEnabled());
		}
	}

}
