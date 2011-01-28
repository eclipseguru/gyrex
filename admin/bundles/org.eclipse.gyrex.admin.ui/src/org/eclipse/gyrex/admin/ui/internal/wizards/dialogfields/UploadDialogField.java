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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rwt.widgets.Upload;
import org.eclipse.rwt.widgets.UploadAdapter;
import org.eclipse.rwt.widgets.UploadEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

	private String uploadPath;
	private String browseButtonLabel;
	private String uploadButtonLabel;
	private Upload uploadControl;
	private ModifyListener modifyListener;
	private final IUploadAdapter uploadAdapter;

	public UploadDialogField(final IUploadAdapter uploadAdapter) {
		super();
		this.uploadAdapter = uploadAdapter;
		browseButtonLabel = "Browse..."; //$NON-NLS-1$
		uploadButtonLabel = "Upload";
		uploadPath = "";
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Upload upload = getUploadControl(parent);
		upload.setLayoutData(gridDataForUpload(nColumns - 1));

		return new Control[] { label, upload };
	}

	private void doModifyUploadPath() {
		if (isOkToUse(uploadControl)) {
			uploadPath = uploadControl.getPath();
		}
		dialogFieldChanged();
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
	public Upload getUploadControl(final Composite parent) {
		if (uploadControl == null) {
			assertCompositeNotNull(parent);
			modifyListener = new ModifyListener() {
				public void modifyText(final ModifyEvent e) {
					doModifyUploadPath();
				}
			};

			uploadControl = new Upload(parent, SWT.BORDER, Upload.SHOW_PROGRESS | Upload.SHOW_UPLOAD_BUTTON);
			uploadControl.setBrowseButtonText(browseButtonLabel);
			uploadControl.setUploadButtonText(uploadButtonLabel);
			uploadControl.setFont(parent.getFont());
			uploadControl.addModifyListener(modifyListener);
			uploadControl.addUploadListener(new UploadAdapter() {
				@Override
				public void uploadException(final UploadEvent uploadEvent) {
					final Exception exc = uploadEvent.getUploadException();
					if (exc != null) {
						MessageDialog.openError(SwtUtil.getShell(parent), "Upload Error", exc.getMessage());
					}
				}

				@Override
				public void uploadFinished(final UploadEvent uploadEvent) {
					uploadAdapter.uploadFinished(uploadControl.getUploadItem());
				}
			});

			uploadControl.setEnabled(isEnabled());
		}
		return uploadControl;
	}

	/**
	 * Returns the uploadPath.
	 * 
	 * @return the uploadPath
	 */
	public String getUploadPath() {
		return uploadPath;
	}

	@Override
	public void refresh() {
		super.refresh();
		if (isOkToUse(uploadControl)) {
		}
	}

	/**
	 * Sets the browseButtonLabel.
	 * 
	 * @param browseButtonLabel
	 *            the browseButtonLabel to set
	 */
	public void setBrowseButtonLabel(final String browseButtonLabel) {
		this.browseButtonLabel = browseButtonLabel;
		if (isOkToUse(uploadControl)) {
			uploadControl.setBrowseButtonText(browseButtonLabel);
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
			uploadControl.setUploadButtonText(uploadButtonLabel);
		}
	}

	/*
	 * @see DialogField#updateEnableState
	 */
	@Override
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(uploadControl)) {
			uploadControl.setEnabled(isEnabled());
		}
	}

}
