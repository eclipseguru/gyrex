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
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.UUID;

import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IUploadAdapter;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.UploadDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.rwt.widgets.UploadItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 */
public class ImportCertificateDialog extends StatusDialog {

	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField keyStorePasswordField = new StringDialogField();
	private final StringDialogField keyPasswordField = new StringDialogField();
	private final SelectionButtonDialogFieldGroup keystoreTypeField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "JKS", "PKCS12" }, 2);
	private final UploadDialogField keystoreUploadField = new UploadDialogField(new IUploadAdapter() {

		@Override
		public void uploadFinished(final UploadItem uploadItem) {
			InputStream in = null;
			try {
				in = uploadItem.getFileInputStream();
				importKeystore(in);
				importError = null;
			} catch (final Exception e) {
				final Throwable rootCause = ExceptionUtils.getRootCause(e);
				importError = rootCause != null ? rootCause : e;
				keystoreBytes = null;
				generatedKeyPassword = null;
				generatedKeystorePassword = null;
			} finally {
				IOUtils.closeQuietly(in);
			}
			validate();
		}
	});

	private Throwable importError;
	private byte[] keystoreBytes;
	private char[] generatedKeystorePassword;
	private char[] generatedKeyPassword;
	private final IJettyManager jettyManager;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public ImportCertificateDialog(final Shell parent, final IJettyManager jettyManager) {
		super(parent);
		this.jettyManager = jettyManager;
		setTitle("New Certificate");
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		final GridData gd = (GridData) composite.getLayoutData();
		gd.minimumHeight = convertVerticalDLUsToPixels(200);
		gd.minimumWidth = convertHorizontalDLUsToPixels(400);

		idField.setLabelText("Id");
		keystoreTypeField.setLabelText("Keystore Type");
		keyStorePasswordField.setLabelText("Keystore Password");
		keyPasswordField.setLabelText("Key Password");
		keystoreUploadField.setLabelText("Keystore");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		keystoreTypeField.setDialogFieldListener(validateListener);
		keyStorePasswordField.setDialogFieldListener(validateListener);
		keyPasswordField.setDialogFieldListener(validateListener);
		keystoreUploadField.setDialogFieldListener(validateListener);

		final Text warning = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
		warning.setText("Warning: this dialog is ugly. Please help us improve the UI. Any mockups and/or patches are very much appreciated!");
		warning.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, new Separator(), keystoreUploadField, keystoreTypeField, keyStorePasswordField, keyPasswordField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(warning, masterLayout.numColumns);

		return composite;
	}

	void importKeystore(final InputStream in) throws Exception {
		KeyStore tempKs;
		if (keystoreTypeField.isSelected(0)) {
			tempKs = KeyStore.getInstance("JKS");
		} else if (keystoreTypeField.isSelected(1)) {
			tempKs = KeyStore.getInstance("PKCS12");
		} else {
			throw new IllegalArgumentException("Please select a keystore type before uploading a keystore and retry.");
		}

		final String keystorePassword = keyStorePasswordField.getText();
		final String keyPassword = keyPasswordField.getText();

		// load keystore
		tempKs.load(new BufferedInputStream(in), null != keystorePassword ? keystorePassword.toCharArray() : null);

		// initialize new JKS store
		final KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null);

		generatedKeystorePassword = UUID.randomUUID().toString().toCharArray();
		generatedKeyPassword = UUID.randomUUID().toString().toCharArray();

		// verify and copy into new store
		final Enumeration aliases = tempKs.aliases();
		while (aliases.hasMoreElements()) {
			final String alias = (String) aliases.nextElement();
			if (tempKs.isKeyEntry(alias)) {
				final Key key = tempKs.getKey(alias, null != keyPassword ? keyPassword.toCharArray() : null != keystorePassword ? keystorePassword.toCharArray() : null);
				Certificate[] chain = tempKs.getCertificateChain(alias);
				if (null == chain) {
					final Certificate certificate = tempKs.getCertificate(alias);
					if (null == certificate) {
						// skip to next
						continue;
					}
					chain = new Certificate[] { certificate };
				}
				ks.setKeyEntry("jetty", key, generatedKeyPassword, chain);
				break;
			}
		}

		if (!ks.aliases().hasMoreElements()) {
			throw new IllegalArgumentException("The uploaded keystore does not have a valid key + certificate chain entry. Please use a different keystore and retry.");
		}

		// write into bytes
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		ks.store(out, generatedKeystorePassword);

		keystoreBytes = out.toByteArray();
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			jettyManager.addCertificate(idField.getText(), keystoreBytes, generatedKeystorePassword, generatedKeyPassword);
		} catch (final Exception e) {
			setError(e.getMessage());
			return;
		}

		super.okPressed();
	}

	private void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, JettyConfigActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	private void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, JettyConfigActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		if (importError != null) {
			setError("The uploaded keystore could not be imported.\n" + importError.getMessage());
			return;
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a certificate id.");
			return;
		}

		if (StringUtils.isBlank(keystoreUploadField.getUploadPath())) {
			setInfo("Please select a keystore.");
			return;
		}

		if (!keystoreTypeField.isSelected(0) && !keystoreTypeField.isSelected(1)) {
			setInfo("Please select a keystore type.");
			return;
		}

		if (keystoreBytes == null) {
			setInfo("Please upload the selected keystore.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
