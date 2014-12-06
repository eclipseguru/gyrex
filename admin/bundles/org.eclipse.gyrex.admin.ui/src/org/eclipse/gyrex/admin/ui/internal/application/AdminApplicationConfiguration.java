/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import java.util.Map;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.AdminPageRegistry;
import org.eclipse.gyrex.rap.application.RapApplicationConfiguration;
import org.eclipse.gyrex.rap.application.RapApplicationEntryPoint;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;

import org.osgi.framework.Bundle;

public class AdminApplicationConfiguration extends RapApplicationConfiguration implements ApplicationConfiguration {

	private static final String IMG_ECLIPSE_ICO = "img/gyrex/eclipse.ico";

	@Override
	public void configure(final Application application) {
		super.configure(application);
		application.addResource(IMG_ECLIPSE_ICO, new BundleResourceLoader(getBundle()));
		application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/admin.css", new BundleResourceLoader(getBundle()));
	}

	@Override
	protected RapApplicationEntryPoint createEntryPoint() {
		final AdminApplication adminApplication = new AdminApplication();
		adminApplication.setPageProvider(AdminPageRegistry.getInstance());
		return adminApplication;
	}

	private Bundle getBundle() {
		return AdminUiActivator.getInstance().getBundle();
	}

	@Override
	protected String getEntryPointPath() {
		return "/admin";
	}

	@Override
	protected Map<String, String> getEntryPointProperties() {
		final Map<String, String> properties = super.getEntryPointProperties();
		properties.put(WebClient.FAVICON, IMG_ECLIPSE_ICO);
		return properties;
	}

}
