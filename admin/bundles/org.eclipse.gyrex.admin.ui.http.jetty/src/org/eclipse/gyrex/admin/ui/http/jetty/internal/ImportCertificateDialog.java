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
 *     Andreas Mihm	- rework new admin ui
 */
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.UUID;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class ImportCertificateDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private static final String[] POSSIBLE_PKCS12_EXTENSIONS = new String[] { ".p12", ".pkcs12" };

	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField keyStorePasswordField = new StringDialogField();
	private final StringDialogField keyPasswordField = new StringDialogField();
	private final SelectionButtonDialogFieldGroup keystoreTypeField = new SelectionButtonDialogFieldGroup(SWT.RADIO, new String[] { "JKS", "PKCS12" }, 2);
	private final UploadDialogField keystoreUploadField = new UploadDialogField();

	private Throwable importError;
	private String keystoreFileName;
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
		gd.widthHint = convertHorizontalDLUsToPixels(400);

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

		final Infobox infobox = new Infobox(composite);
		infobox.setLayoutData(AdminUiUtil.createHorzFillData());
		infobox.addHeading("Import a keystore!");
		infobox.addParagraph("Please fill in id, keytore type and passwords before uploading the keystore!\nAfter the upload the key store is validated. If the validation was sucessful,\nyou can import the key store into gyrex by pressing OK.");

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, new Separator(), keyStorePasswordField, keyPasswordField, new Separator(), keystoreTypeField, keystoreUploadField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));
		LayoutUtil.setHorizontalGrabbing(keystoreUploadField.getFileTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(infobox, masterLayout.numColumns);

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
//		// activate background updates
//		UICallBack.activate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));
//
		// start and wait for update
		final Display display = getShell().getDisplay();
		final String fileName = keystoreUploadField.getFileName();
		if (StringUtils.isNotBlank(fileName) && (keystoreBytes == null || keystoreFileName == null || !StringUtils.equals(keystoreFileName, fileName))) {
			updateButtonsEnableState(new Status(IStatus.ERROR, JettyConfigActivator.SYMBOLIC_NAME, "Upload in progress!")); // deactivate buttons
			keystoreUploadField.startUpload(new IUploadAdapter() {
				@Override
				public void receive(final InputStream stream, final String fileName, final String contentType, final long contentLength) {
					InputStream in = null;
					try {
						in = stream instanceof FileInputStream ? new BufferedInputStream(stream) : stream;
						importKeystore(in);
						keystoreFileName = fileName;
						importError = null;
					} catch (final Exception e) {
						importError = e;
						keystoreBytes = null;
						keystoreFileName = null;
						generatedKeyPassword = null;
						generatedKeystorePassword = null;
					} finally {
						IOUtils.closeQuietly(in);
					}
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							okPressedAndFileReceived();
						}
					});
				}
			});
		} else {
			// file is already uploaded
			okPressedAndFileReceived();
		}
	}

	void okPressedAndFileReceived() {
		// deactivate background updates
//		UICallBack.deactivate(getClass().getName() + "#" + Integer.toHexString(System.identityHashCode(this)));

		if (importError != null) {
			setError("The uploaded keystore could not be imported.\n" + importError.getMessage());
			return;
		}

		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			jettyManager.addCertificate(idField.getText(), keystoreBytes, generatedKeystorePassword, generatedKeyPassword);
		} catch (final Exception e) {
			setError(String.format("Error adding certificate: %s", e.getMessage()));
			return;
		}

		// close dialog
		super.okPressed();
	}

	private void setError(final String message) {
		updateStatus(new Status(IStatus.ERROR, JettyConfigActivator.SYMBOLIC_NAME, message));
		getShell().pack(true);
	}

	private void setInfo(final String message) {
		updateStatus(new Status(IStatus.INFO, JettyConfigActivator.SYMBOLIC_NAME, message));
	}

	private void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, JettyConfigActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a certificate id.");
			return;
		}

		if (!keystoreTypeField.isSelected(0) && !keystoreTypeField.isSelected(1)) {
			setInfo("Please select a keystore type.");
			return;
		}

		if (StringUtils.isBlank(keystoreUploadField.getFileName())) {
			setInfo("Please select a keystore to upload.");
			return;
		}

		if (StringUtils.endsWithAny(keystoreUploadField.getFileName().toLowerCase(), POSSIBLE_PKCS12_EXTENSIONS) && !keystoreTypeField.isSelected(1)) {
			setWarning("The selected file might be a PKCS12 keystore. Please verify the correct keystore type is selected!");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
