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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingStatusDialog;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.DialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.LayoutUtil;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.Separator;
import org.eclipse.gyrex.admin.ui.internal.wizards.dialogfields.StringDialogField;
import org.eclipse.gyrex.common.identifiers.IdHelper;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.ICertificate;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class AddChannelDialog extends NonBlockingStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private final StringDialogField idField = new StringDialogField();
	private final StringDialogField portField = new StringDialogField();
	private final SelectionButtonDialogField secureField = new SelectionButtonDialogField(SWT.CHECK);
	private final StringDialogField certificateIdField = new StringDialogField();
	private final StringDialogField secureChannelIdField = new StringDialogField();
	private final StringDialogField nodeFilterField = new StringDialogField();

	private final IJettyManager jettyManager;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 */
	public AddChannelDialog(final Shell parent, final IJettyManager jettyManager) {
		super(parent);
		this.jettyManager = jettyManager;
		setTitle("New Connector");
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
		portField.setLabelText("Port");
		secureField.setLabelText("SSL");
		certificateIdField.setLabelText("Certificate");
		secureChannelIdField.setLabelText("Secure Connector");
		nodeFilterField.setLabelText("Node Filter");

		final IDialogFieldListener validateListener = new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(final DialogField field) {
				validate();
			}
		};

		idField.setDialogFieldListener(validateListener);
		portField.setDialogFieldListener(validateListener);
		secureField.setDialogFieldListener(validateListener);
		certificateIdField.setDialogFieldListener(validateListener);
		secureChannelIdField.setDialogFieldListener(validateListener);
		nodeFilterField.setDialogFieldListener(validateListener);

		certificateIdField.setContentProposalProcessor(new IContentProposalProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public IContentProposal[] getProposals(final String contents, final int position) {
				final List<IContentProposal> resultList = new ArrayList<IContentProposal>();

				final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));

				final Collection<ICertificate> certificates = jettyManager.getCertificates();
				for (final ICertificate certificate : certificates) {
					if (null == patternString || StringUtils.contains(certificate.getId(), patternString)) {
						resultList.add(new ContentProposal(certificate.getId(), certificate.getInfo()));
					}
				}

				return resultList.toArray(new IContentProposal[resultList.size()]);
			}
		});

		secureChannelIdField.setContentProposalProcessor(new IContentProposalProvider() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public IContentProposal[] getProposals(final String contents, final int position) {
				final List<IContentProposal> resultList = new ArrayList<IContentProposal>();

				final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));

				final Collection<ChannelDescriptor> channels = jettyManager.getChannels();
				for (final ChannelDescriptor channel : channels) {
					if (channel.isSecure() && (null == patternString || StringUtils.contains(channel.getId(), patternString))) {
						resultList.add(new ContentProposal(channel.getId(), String.format("%s (%d)", channel.getId(), channel.toString())));
					}
				}

				return resultList.toArray(new IContentProposal[resultList.size()]);
			}
		});

		secureField.setAttachedDialogFields(certificateIdField);

		final Infobox infobox = new Infobox(composite);
		infobox.setLayoutData(AdminUiUtil.createHorzFillData());
		infobox.addHeading("Create a new connector!");
		infobox.addParagraph("Please fill in id and port to create a connector!\nIf the connector should support encryption, please choose the certificate to be used. You need some more documentation on the usage of the fields SecureConnector and nodefilter.");

		LayoutUtil.doDefaultLayout(composite, new DialogField[] { new Separator(), idField, portField, new Separator(), secureField, certificateIdField, new Separator(), secureChannelIdField, new Separator(), nodeFilterField }, false);
		LayoutUtil.setHorizontalGrabbing(idField.getTextControl(null));

		final GridLayout masterLayout = (GridLayout) composite.getLayout();
		masterLayout.marginWidth = 5;
		masterLayout.marginHeight = 5;

		LayoutUtil.setHorizontalSpan(infobox, masterLayout.numColumns);

		return composite;
	}

	private boolean isWithinRange(final int port, final int lower, final int higher) {
		return port >= lower && port <= higher;
	}

	@Override
	protected void okPressed() {
		validate();
		if (!getStatus().isOK()) {
			return;
		}

		try {
			final ChannelDescriptor channelDescriptor = new ChannelDescriptor();
			channelDescriptor.setId(idField.getText());
			channelDescriptor.setPort(NumberUtils.toInt(portField.getText()));
			channelDescriptor.setSecure(secureField.isSelected());
			channelDescriptor.setCertificateId(StringUtils.trimToNull(certificateIdField.getText()));
			channelDescriptor.setSecureChannelId(StringUtils.trimToNull(secureChannelIdField.getText()));
			channelDescriptor.setNodeFilter(StringUtils.trimToNull(nodeFilterField.getText()));
			jettyManager.saveChannel(channelDescriptor);
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

	private void setWarning(final String message) {
		updateStatus(new Status(IStatus.WARNING, JettyConfigActivator.SYMBOLIC_NAME, message));
	}

	void validate() {
		final String id = idField.getText();
		if (StringUtils.isNotBlank(id) && !IdHelper.isValidId(id)) {
			setError("The entered connector id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
			return;
		}

		final String port = portField.getText();
		if (StringUtils.isNotBlank(port)) {
			if (!NumberUtils.isDigits(port)) {
				setError("The entered port is invalid. Please use only digits.");
				return;
			}

			if (!isWithinRange(NumberUtils.toInt(port), 1, 65535)) {
				setError("The entered port is invalid. Port must be within 1 and 65535.");
				return;
			}
		}

		final boolean secure = secureField.isSelected();
		final String certificateId = certificateIdField.getText();
		final String secureChannelId = secureChannelIdField.getText();
		if (secure && StringUtils.isNotBlank(certificateId)) {
			if (!IdHelper.isValidId(certificateId)) {
				setError("The entered certificate id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
				return;
			}
			final ICertificate certificate = jettyManager.getCertificate(certificateId);
			if (certificate == null) {
				setError("The entered certificate id is unknown.");
				return;
			}
		} else if (!secure && StringUtils.isNotBlank(secureChannelId)) {
			if (!IdHelper.isValidId(secureChannelId)) {
				setError("The entered secure connector id is invalid. It may only contain ASCII chars a-z, 0-9, '.', '-' and/or '_'.");
				return;
			}
			final ChannelDescriptor channel = jettyManager.getChannel(secureChannelId);
			if (channel == null) {
				setError("The entered secure connector is unknown.");
				return;
			}
			if (channel.isSecure()) {
				setError("The entered secure connector is not a SSL connector.");
				return;
			}
		}

		final String nodeFilter = nodeFilterField.getText();
		if (StringUtils.isNotBlank(nodeFilter)) {
			try {
				FrameworkUtil.createFilter(nodeFilter);
			} catch (final InvalidSyntaxException e) {
				setError("The entered node filter is invalid. Please use valid LDAP filter syntax. " + e.getMessage());
				return;
			}
		}

		if (StringUtils.isBlank(id)) {
			setInfo("Please enter a connector id.");
			return;
		}

		if (StringUtils.isBlank(port)) {
			setInfo("Please enter a port.");
			return;
		}

		if (secure && StringUtils.isBlank(certificateId)) {
			setInfo("Please enter a certificate id.");
			return;
		}

		if (isWithinRange(NumberUtils.toInt(port), 1, 1024)) {
			setWarning("Ports within 1 and 1024 may require special system privileges.");
			return;
		}

		updateStatus(Status.OK_STATUS);
	}
}
