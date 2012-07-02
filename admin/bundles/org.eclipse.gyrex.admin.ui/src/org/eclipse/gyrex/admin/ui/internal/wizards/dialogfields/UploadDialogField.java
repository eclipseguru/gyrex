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
package org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.gyrex.admin.ui.internal.upload.FileUploadEvent;
import org.eclipse.gyrex.admin.ui.internal.upload.FileUploadHandler;
import org.eclipse.gyrex.admin.ui.internal.upload.FileUploadListener;
import org.eclipse.gyrex.admin.ui.internal.upload.FileUploadReceiver;
import org.eclipse.gyrex.admin.ui.internal.upload.IFileUploadDetails;

import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

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
	private Text fileText;
	private FileUpload uploadControl;

	private boolean uploadInProgress;
	private String fileName = "";

	public UploadDialogField() {
		super();
		uploadButtonLabel = "Browse..."; //$NON-NLS-1$
	}

	@Override
	public Control[] doFillIntoGrid(final Composite parent, final int nColumns) {
		assertEnoughColumns(nColumns);

		final Label label = getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		final Text text = getFileTextControl(parent);
		text.setLayoutData(StringDialogField.gridDataForText(nColumns - 2));
		final FileUpload upload = getUploadControl(parent);
		upload.setLayoutData(gridDataForUpload(1));

		return new Control[] { label, text, upload };
	}

	private void doModifyFileName() {
		if (isOkToUse(uploadControl)) {
			fileName = StringUtils.trimToEmpty(uploadControl.getFileName());
			fileText.setText(fileName);
		}
		dialogFieldChanged();
	}

	/**
	 * Returns the name of the selected file.
	 */
	public String getFileName() {
		return fileName;
	}

	public Text getFileTextControl(final Composite parent) {
		if (fileText == null) {
			assertCompositeNotNull(parent);

			fileText = new Text(parent, SWT.BORDER | SWT.SINGLE);
			fileText.setText(fileName);
			fileText.setToolTipText("Selected file");
			fileText.setEditable(false);
			fileText.setFont(parent.getFont());
			fileText.setEnabled(isEnabled());
		}
		return fileText;
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}

	/**
	 * Creates or returns the created text control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has
	 *            already been created.
	 * @return the text control
	 */
	public FileUpload getUploadControl(final Composite parent) {
		if (uploadControl == null) {
			assertCompositeNotNull(parent);

			uploadControl = new FileUpload(parent, SWT.NONE);

			uploadControl.setText(uploadButtonLabel);
			uploadControl.setToolTipText("Select a file");
			uploadControl.setFont(parent.getFont());
			uploadControl.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					doModifyFileName();
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

	public void startUpload(final IUploadAdapter receiver) {
		uploadInProgress = true;
		updateEnableState();

		// activate background updates
		UICallBack.activate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));

		final FileUploadHandler handler = new FileUploadHandler(new FileUploadReceiver() {
			@Override
			public void receive(final InputStream dataStream, final IFileUploadDetails details) throws IOException {
				receiver.receive(dataStream, details.getFileName(), details.getContentType(), details.getContentLength());
			}
		});

		uploadControl.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent event) {
				handler.dispose();
				UICallBack.deactivate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));
			}
		});

		final Display display = uploadControl.getDisplay();
		final String url = handler.getUploadUrl();
		handler.addUploadListener(new FileUploadListener() {

			@Override
			public void uploadFailed(final FileUploadEvent event) {
				handler.dispose();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						fileText.setText(String.format("ERROR: %s", ExceptionUtils.getRootCauseMessage(event.getException())));
						uploadInProgress = false;
						setUploadButtonLabel(uploadButtonLabel);
						updateEnableState();
						UICallBack.deactivate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));
					}
				});
			}

			@Override
			public void uploadFinished(final FileUploadEvent event) {
				handler.dispose();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isOkToUse(fileText)) {
							fileText.setText(getFileName());
						}
						uploadInProgress = false;
						setUploadButtonLabel(uploadButtonLabel);
						updateEnableState();
						UICallBack.deactivate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));
					}
				});
			}

			@Override
			public void uploadProgress(final FileUploadEvent event) {
				handler.dispose();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						fileText.setText(fileText.getText() + ".");
					}
				});
			}
		});

		// start upload
		uploadControl.setText("Uploading...");
		uploadControl.submit(url);
	}

	@Override
	protected void updateEnableState() {
		super.updateEnableState();

		if (isOkToUse(fileText)) {
			fileText.setEnabled(isEnabled());
		}
		if (isOkToUse(uploadControl)) {
			uploadControl.setEnabled(isEnabled() && !uploadInProgress);
		}
	}
}
