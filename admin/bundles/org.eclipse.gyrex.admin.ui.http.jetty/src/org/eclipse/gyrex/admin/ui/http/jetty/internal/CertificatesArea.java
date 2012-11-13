/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.admin.ui.internal.widgets.Infobox;
import org.eclipse.gyrex.admin.ui.internal.widgets.NonBlockingMessageDialogs;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.ICertificate;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.apache.commons.lang.text.StrBuilder;

/**
 * 
 */
public class CertificatesArea {

	static class CertificatesLabelProvider extends LabelProvider {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public String getText(final Object element) {
			if (element instanceof ICertificate) {
				final ICertificate cert = (ICertificate) element;
				return String.format("%s (%s)", cert.getId(), cert.getInfo());
			}
			return super.getText(element);
		}
	}

	private ISelectionChangedListener updateButtonsListener;
	private Button addButton;
	private Button removeButton;
	private ListViewer certificatesList;
	private Composite pageComposite;

	public void activate() {

		if (certificatesList != null) {
			certificatesList.setInput(getJettyManager());
			updateButtonsListener = new ISelectionChangedListener() {

				@Override
				public void selectionChanged(final SelectionChangedEvent event) {
					updateButtons();
				}
			};
			certificatesList.addSelectionChangedListener(updateButtonsListener);
		} else {
		}

	}

	void addButtonPressed() {
		final ImportCertificateDialog dialog = new ImportCertificateDialog(SwtUtil.getShell(addButton), getJettyManager());
		dialog.openNonBlocking(new DialogCallback() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode == Window.OK) {
					refresh();
				}
			}
		});

	}

	private Button createButton(final Composite buttons, final String buttonLabel) {
		final Button b = new Button(buttons, SWT.NONE);
		b.setText(buttonLabel);
		b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return b;
	}

	public void createChannelsControls(final Composite parent) {
		pageComposite = parent;

		final Infobox infobox = new Infobox(pageComposite);
		infobox.setLayoutData(AdminUiUtil.createHorzFillData());
		infobox.addHeading("Certificates");
		infobox.addParagraph("In this section you can define SSL Certificates, which can be assigned to Jetty channels to support SSL encryption.");

		final Composite description = new Composite(pageComposite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		description.setLayoutData(gd);
		description.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		certificatesList = new ListViewer(description, SWT.SINGLE | SWT.BORDER);
		final List list = certificatesList.getList();
		list.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		certificatesList.setContentProvider(new ArrayContentProvider());
		certificatesList.setLabelProvider(new CertificatesLabelProvider());
		certificatesList.setContentProvider(new CertificatesContentProvider());

		final Composite buttons = new Composite(description, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		buttons.setLayout(new GridLayout());

		addButton = createButton(buttons, "Add");
		addButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				addButtonPressed();
			}
		});

		removeButton = createButton(buttons, "Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				removeButtonPressed();
			}
		});

	}

	public void deactivate() {

		// remove data inputs form controls
		if (certificatesList != null) {
			if (updateButtonsListener != null) {
				certificatesList.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (!certificatesList.getList().isDisposed()) {
				certificatesList.setInput(null);
			}
		}

	}

	private IJettyManager getJettyManager() {
		return JettyConfigActivator.getInstance().getJettyManager();
	}

	private ICertificate getSelectedCertificate() {
		final IStructuredSelection selection = (IStructuredSelection) certificatesList.getSelection();
		if (!selection.isEmpty() && selection.getFirstElement() instanceof ICertificate) {
			return (ICertificate) selection.getFirstElement();
		}

		return null;
	}

	public void refresh() {
		certificatesList.refresh();
		updateButtons();
	}

	void removeButtonPressed() {

		final ICertificate certificate = getSelectedCertificate();
		if (certificate == null) {
			return;
		}

		// we also validate that the certificate is not used anymore
		final String certificateId = certificate.getId();
		final IJettyManager jettyManager = getJettyManager();
		final Collection<ChannelDescriptor> channels = jettyManager.getChannelsUsingCertificate(certificateId);
		if (!channels.isEmpty()) {
			final StrBuilder errorMessage = new StrBuilder();
			errorMessage.appendln("Certificate is still in use and cannot be removed.");
			errorMessage.appendln("");
			errorMessage.appendln("Used by:");
			for (final ChannelDescriptor channelDescriptor : channels) {
				errorMessage.append("  * ").appendln(channelDescriptor.getId());
			}
			NonBlockingMessageDialogs.openError(SwtUtil.getShell(pageComposite), "Still In Use", errorMessage.toString(), new DialogCallback() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public void dialogClosed(final int returnCode) {
					if (returnCode != Window.OK) {
						return;
					}

				}
			});
			return;
		}

		NonBlockingMessageDialogs.openQuestion(SwtUtil.getShell(pageComposite), "Remove selected Certificate", String.format("Do you really want to delete certificate %s?", certificate.getId()), new DialogCallback() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void dialogClosed(final int returnCode) {
				if (returnCode != Window.OK) {
					return;
				}

				getJettyManager().removeCertificate(certificate.getId());
				refresh();
			}
		});
	}

	void updateButtons() {
		final int selectedElementsCount = ((IStructuredSelection) certificatesList.getSelection()).size();
		if (selectedElementsCount == 0) {
			addButton.setEnabled(true);
			removeButton.setEnabled(false);
			return;
		}

		addButton.setEnabled(selectedElementsCount == 1);
		removeButton.setEnabled(selectedElementsCount == 1);
	}

}
