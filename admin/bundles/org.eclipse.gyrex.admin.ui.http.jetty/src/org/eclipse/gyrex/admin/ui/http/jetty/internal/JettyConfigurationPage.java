/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;
import org.eclipse.gyrex.http.jetty.admin.IJettyManager;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Simple configuration page for the default ports where the built-in local
 * jetty server runs.
 */
public class JettyConfigurationPage extends ConfigurationPage {

	private static final String INPUT = "input";

	private DataBindingContext bindingContext;
	boolean disposed;

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		setTitle("Jetty Configuration");

		final Realm realm = SWTObservables.getRealm(Display.getCurrent());
		bindingContext = new DataBindingContext(realm);

		final Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
		body.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		final FormToolkit toolkit = managedForm.getToolkit();

		final Composite left = toolkit.createComposite(body);
		left.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		left.setLayout(GridLayoutFactory.fillDefaults().create());

		final ChannelsSection channelsSection = new ChannelsSection(left, this);
		channelsSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(channelsSection);

		final Composite right = toolkit.createComposite(body);
		right.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		right.setLayout(GridLayoutFactory.fillDefaults().create());

		final CertificatsSection certificatesSection = new CertificatsSection(right, this);
		certificatesSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(certificatesSection);

		initializeInput();

//		FormLayoutFactory.visualizeLayoutArea(body, SWT.COLOR_CYAN);
//		FormLayoutFactory.visualizeLayoutArea(left, SWT.COLOR_DARK_GREEN);
//		FormLayoutFactory.visualizeLayoutArea(right, SWT.COLOR_DARK_GREEN);
	}

	@Override
	public void dispose() {
		disposed = true;
		bindingContext.dispose();
		bindingContext = null;
		super.dispose();
	}

	/**
	 * Returns the binding context.
	 * 
	 * @return the bindingContext
	 */
	public DataBindingContext getBindingContext() {
		if (disposed) {
			throw new IllegalStateException("disposed");
		}
		return bindingContext;
	}

	private void initializeInput() {
		final IManagedForm managedForm = getManagedForm();
		try {
			final IJettyManager manager = JettyConfigActivator.getInstance().getJettyManager();
			managedForm.setInput(manager);
			managedForm.getMessageManager().removeMessage(INPUT);
		} catch (final IllegalStateException e) {
			managedForm.getMessageManager().addMessage(INPUT, "The Jetty component could not be initialized. " + e.getMessage(), e, IMessageProvider.ERROR);
			final Job job = new Job("Load input") {

				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					initializeInput();
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule(800l);
		}
	}
}
