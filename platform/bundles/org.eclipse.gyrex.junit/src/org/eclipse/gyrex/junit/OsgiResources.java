/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.junit;

import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.gyrex.common.services.BundleServiceHelper;

import org.osgi.framework.BundleContext;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;

/**
 * A {@link TestRule} which obtains an OSGi service.
 */
public class OsgiResources extends ExternalResource {

	private final BundleServiceHelper bundleServiceHelper;

	public OsgiResources(final BundleContext bundleContext) {
		bundleServiceHelper = new BundleServiceHelper(checkNotNull(bundleContext, "BundleContext must not be null"));
	}

	@Override
	protected void after() {
		bundleServiceHelper.dispose();
	}

	public <T> T getService(final Class<T> serviceInterface) {
		return bundleServiceHelper.trackService(serviceInterface).getService();
	}

}
