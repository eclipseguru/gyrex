/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *      Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.jetty.internal;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Simple configuration page for the default ports where the built-in local
 * jetty server runs.
 */
public class JettyConfigurationPage extends AdminPage {

	boolean disposed;
	private Composite pageComposite;
	private ChannelsArea channelsArea;
	private CertificatesArea certificatesArea;

	public JettyConfigurationPage() {
		setTitle("Jetty Configuration");
		setTitleToolTip("Manage your jetty configuration");
//		setFilters(Arrays.asList(FILTER_CONTEXT, FILTER_PROVIDER));
	}

	@Override
	public void activate() {
		super.activate();

		if (channelsArea != null) {
			channelsArea.activate();
		}

		if (certificatesArea != null) {
			certificatesArea.activate();
		}
	}

	@Override
	public Control createControl(final Composite parent) {
		pageComposite = new Composite(parent, SWT.NONE);
		pageComposite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		channelsArea = new ChannelsArea();
		channelsArea.createChannelsControls(pageComposite);

		new Label(pageComposite, SWT.HORIZONTAL);

		certificatesArea = new CertificatesArea();
		certificatesArea.createChannelsControls(pageComposite);

		return pageComposite;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (channelsArea != null) {
			channelsArea.deactivate();
		}

		if (certificatesArea != null) {
			certificatesArea.deactivate();
		}

	}

}
