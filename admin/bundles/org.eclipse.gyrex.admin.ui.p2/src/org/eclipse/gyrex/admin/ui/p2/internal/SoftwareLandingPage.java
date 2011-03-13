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
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Configuration page for Jobs.
 */
public class SoftwareLandingPage extends ConfigurationPage {

	private DataBindingContext bindingContext;
	boolean disposed;

	/**
	 * Creates a new instance.
	 */
	public SoftwareLandingPage() {
		setTitle("Software Provisioning");
		setTitleToolTip("Install, update and remove software.");
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final Realm realm = SWTObservables.getRealm(Display.getCurrent());
		bindingContext = new DataBindingContext(realm);

		final Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 2));
		body.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		final FormToolkit toolkit = managedForm.getToolkit();

		final Composite left = toolkit.createComposite(body);
		left.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		left.setLayout(GridLayoutFactory.fillDefaults().create());

		final PackagesSection packageSection = new PackagesSection(left, this);
		packageSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(packageSection);

		final Composite right = toolkit.createComposite(body);
		right.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		right.setLayout(GridLayoutFactory.fillDefaults().create());

		final RepositoriesSection repoSection = new RepositoriesSection(right, this);
		repoSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(repoSection);

		final InstallStateSection installSection = new InstallStateSection(right, this);
		installSection.getSection().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		managedForm.addPart(installSection);

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
}
