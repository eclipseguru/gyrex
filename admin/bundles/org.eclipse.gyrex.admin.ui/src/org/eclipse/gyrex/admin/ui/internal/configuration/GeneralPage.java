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
package org.eclipse.gyrex.admin.ui.internal.configuration;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * General configuration page.
 */
public class GeneralPage extends ConfigurationPage {

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		setTitle("General Configuration");

		final Composite body = managedForm.getForm().getBody();
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		body.setLayout(layout);

		final WelcomeSection welcomeSection = new WelcomeSection(body, this);
		managedForm.addPart(welcomeSection);

		final PlatformStatusSection statusSection = new PlatformStatusSection(body, this);
		managedForm.addPart(statusSection);
	}
}
