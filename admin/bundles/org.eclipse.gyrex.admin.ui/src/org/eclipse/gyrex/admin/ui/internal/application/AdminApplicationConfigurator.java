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

import java.lang.reflect.Constructor;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.rap.ui.internal.servlet.WorkbenchApplicationConfigurator;
import org.eclipse.rwt.application.Application;
import org.eclipse.rwt.application.ApplicationConfiguration;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.apache.commons.lang.UnhandledException;

@SuppressWarnings("restriction")
public class AdminApplicationConfigurator implements ApplicationConfiguration {
//	public static final String THEME_ID_RAP_DEFAULT = "org.eclipse.rap.rwt.theme.Default";
//	public static final String EP_ADMIN = "admin";

	/** FILTER */
	public static final String FILTER = "(service.pid=" + AdminApplicationConfigurator.class.getName() + ")";

	@Override
	public void configure(final Application application) {
		final ServiceRegistration dummyServiceRegistration = AdminUiActivator.getInstance().getServiceHelper().registerService(AdminApplicationConfigurator.class.getName(), this, "Eclipse Gyrex", "Dummy service for configuring RAP. Do not use!", AdminApplicationConfigurator.class.getName(), null);
		try {
			// we continue to use the WorkbenchApplicationConfigurator because
			// we still need a lot of workbench stuff (themes, etc.)
			final Constructor<WorkbenchApplicationConfigurator> constructor = WorkbenchApplicationConfigurator.class.getDeclaredConstructor(ServiceReference.class);
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}
			// use workbench configurator (to support all the extension points)
			new WorkbenchApplicationConfigurator(null).configure(application);
		} catch (final Exception e) {
			throw new UnhandledException("Unable to intantiate internal workbench configurator.", e);
		} finally {
			dummyServiceRegistration.unregister();
		}
	}

	Bundle getBundle() {
		return AdminUiActivator.getInstance().getBundle();
	}
}
