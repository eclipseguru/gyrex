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
import static java.lang.String.format;

import org.eclipse.gyrex.common.services.BundleServiceHelper;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TestRule} which obtains an OSGi service.
 */
public class OsgiResources extends ExternalResource {

	private static final Logger LOG = LoggerFactory.getLogger(OsgiResources.class);

	private final BundleServiceHelper bundleServiceHelper;
	private final BundleContext bundleContext;

	public OsgiResources(final BundleContext context) {
		bundleContext = checkNotNull(context, "BundleContext must not be null");
		bundleServiceHelper = new BundleServiceHelper(bundleContext);
	}

	@Override
	protected void after() {
		bundleServiceHelper.dispose();
	}

	public <T> T getService(final Class<T> serviceInterface) {
		return bundleServiceHelper.trackService(serviceInterface).getService();
	}

	public void startBundle(final String symbolicName) {
		final Bundle[] bundles = bundleContext.getBundles();
		for (final Bundle bundle : bundles) {
			if (bundle.getSymbolicName().equals(symbolicName)) {
				// ignore unresolvable bundles
				if ((bundle.getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) != 0) {
					LOG.warn("Found un-resolved candidate for BSN '{}': {}", symbolicName, bundle);
					continue;
				}
				try {
					bundle.start(Bundle.START_TRANSIENT);
				} catch (final BundleException e) {
					LOG.warn("Erro starting bundle '{}' ({}): {}", symbolicName, bundle, e.getMessage(), e);
					continue;
				}
				return;
			}
		}
		throw new AssertionError(format("No bundle matching symbolic name '%s' started.", symbolicName));
	}
}
