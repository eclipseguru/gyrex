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

import java.util.Collection;

import org.eclipse.gyrex.admin.ui.internal.databinding.TrueWhenListSelectionNotEmptyConverter;
import org.eclipse.gyrex.admin.ui.internal.forms.ViewerWithButtonsSectionPart;
import org.eclipse.gyrex.admin.ui.internal.helper.SwtUtil;
import org.eclipse.gyrex.http.jetty.admin.ChannelDescriptor;
import org.eclipse.gyrex.http.jetty.admin.ICertificate;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;

import org.apache.commons.lang.text.StrBuilder;

/**
 *
 */
public class CertificatsSection extends ViewerWithButtonsSectionPart {

	static class CertificatesLabelProvider extends LabelProvider {
		@Override
		public String getText(final Object element) {
			if (element instanceof ICertificate) {
				final ICertificate cert = (ICertificate) element;
				return String.format("%s (%s)", cert.getId(), cert.getInfo());
			}
			return super.getText(element);
		}
	}

	private ListViewer certificatesList;
	private Button addButton;
	private Button removeButton;
	private final DataBindingContext bindingContext;
	private IObservableValue selectedCertificateValue;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public CertificatsSection(final Composite parent, final JettyConfigurationPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		bindingContext = page.getBindingContext();
		final Section section = getSection();
		section.setText("Certificates");
		section.setDescription("Manage the SSL certificates.");
		createContent(section);
	}

	void addButtonPressed() {
		final ImportCertificateDialog dialog = new ImportCertificateDialog(SwtUtil.getShell(addButton), getJettyManager());
		if (dialog.open() == Window.OK) {
			markStale();
		}
	}

	@Override
	public void commit(final boolean onSave) {
		super.commit(onSave);
	}

	@Override
	protected void createButtons(final Composite parent) {
		addButton = createButton(parent, "Add...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				addButtonPressed();
			}
		});
		removeButton = createButton(parent, "Remove...", new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				removeButtonPressed();
			}
		});
	}

	@Override
	protected void createViewer(final Composite parent) {
		certificatesList = new ListViewer(parent, getToolkit().getBorderStyle() | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);

		final List list = certificatesList.getList();
		getToolkit().adapt(list, true, true);
		list.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).grab(true, true).create());

		certificatesList.setContentProvider(new ArrayContentProvider());
		certificatesList.setLabelProvider(new CertificatesLabelProvider());

		selectedCertificateValue = ViewersObservables.observeSingleSelection(certificatesList);
	}

	/**
	 * Returns the bindingContext.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		return bindingContext;
	}

	private IJettyManager getJettyManager() {
		return (IJettyManager) getManagedForm().getInput();
	}

	@Override
	public void initialize(final IManagedForm form) {
		super.initialize(form);

		final UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
		modelToTarget.setConverter(new TrueWhenListSelectionNotEmptyConverter());
		getBindingContext().bindValue(SWTObservables.observeEnabled(removeButton), SWTObservables.observeSelection(certificatesList.getControl()), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), modelToTarget);
	}

	@Override
	public void refresh() {
		final Object input = getManagedForm().getInput();
		if (input instanceof IJettyManager) {
			final IJettyManager jettyManager = (IJettyManager) input;
			final Collection<ICertificate> certificates = jettyManager.getCertificates();
			certificatesList.setInput(certificates);
		}
		super.refresh();
	}

	void removeButtonPressed() {
		final ICertificate certificate = (ICertificate) (null != selectedCertificateValue ? selectedCertificateValue.getValue() : null);
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
			MessageDialog.openError(SwtUtil.getShell(getSection()), "Still In Use", errorMessage.toString());
			return;
		}

		if (!MessageDialog.openQuestion(SwtUtil.getShell(getSection()), "Remove Certificate", "Do you really want to delete the certificate?")) {
			return;
		}

		jettyManager.removeCertificate(certificateId);
		markStale();
	}

	@Override
	public boolean setFormInput(final Object input) {
		if (input instanceof IJettyManager) {
			markStale();
			return true;
		}
		return super.setFormInput(input);
	}
}
