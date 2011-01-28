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
import org.eclipse.gyrex.admin.ui.internal.forms.FormLayoutFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * General configuration page.
 */
public class GeneralPage extends ConfigurationPage {

	@Override
	protected void createFormContent(final IManagedForm managedForm) {
		setTitle("General Configuration");

		final Composite body = managedForm.getForm().getBody();
		body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));

		final Composite left = managedForm.getToolkit().createComposite(body);
		left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		final WelcomeSection welcomeSection = new WelcomeSection(left, this);
		managedForm.addPart(welcomeSection);

		final Composite right = managedForm.getToolkit().createComposite(body);
		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
		right.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		final PlatformStatusSection statusSection = new PlatformStatusSection(right, this);
		managedForm.addPart(statusSection);

		final NodeShortcutsSection shortcutsSection = new NodeShortcutsSection(right, this);
		managedForm.addPart(shortcutsSection);
	}
}
