/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;

/**
 *
 */
public class HttpApplicationPage extends ConfigurationPage {

	private ApplicationsSection appSection;

	/**
	 * Creates a new instance.
	 */
	public HttpApplicationPage() {
		setTitle("Web Applications");
		setTitleToolTip("Define, configure and mount applications.");
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		final Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormGridLayout(true, 1));
		body.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		appSection = new ApplicationsSection(body, this);
		appSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(appSection);

//		FormLayoutFactory.visualizeLayoutArea(body, SWT.COLOR_CYAN);
//		FormLayoutFactory.visualizeLayoutArea(left, SWT.COLOR_DARK_GREEN);
//		FormLayoutFactory.visualizeLayoutArea(right, SWT.COLOR_DARK_GREEN);
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		if (null != appSection) {
			return appSection.getSelectionProvider();
		}
		return null;
	}
}
