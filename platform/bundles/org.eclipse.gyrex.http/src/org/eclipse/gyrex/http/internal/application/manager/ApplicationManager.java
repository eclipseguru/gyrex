/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.cloudfree.http.internal.application.manager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.http.application.manager.ApplicationRegistrationException;
import org.eclipse.cloudfree.http.application.manager.IApplicationManager;
import org.eclipse.cloudfree.http.application.manager.MountConflictException;
import org.eclipse.cloudfree.http.application.provider.ApplicationProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The application manager.
 * 
 * @TODO we need to support providers in multiple versions
 */
public class ApplicationManager implements IApplicationManager, ServiceTrackerCustomizer {

	private final BundleContext context;
	private final ServiceTracker providerTracker;
	private final ConcurrentMap<String, ApplicationProviderRegistration> providers = new ConcurrentHashMap<String, ApplicationProviderRegistration>(1);
	private final ConcurrentMap<String, ApplicationRegistration> applications = new ConcurrentHashMap<String, ApplicationRegistration>(1);
	private final ConcurrentMap<String, ApplicationMount> mounts = new ConcurrentHashMap<String, ApplicationMount>(1);
	private String defaultApplicationId;

	public ApplicationManager(final BundleContext context) {
		this.context = context;
		providerTracker = new ServiceTracker(context, ApplicationProvider.class.getName(), this);
	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#addingService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public Object addingService(final ServiceReference reference) {
		final ApplicationProvider provider = (ApplicationProvider) context.getService(reference);
		if (null == provider) {
			return null;
		}

		final ApplicationProviderRegistration registration = new ApplicationProviderRegistration(reference, provider);
		providers.putIfAbsent(provider.getId(), registration);
		return provider;
	}

	/**
	 * Closes the manager and releases all resources held by it.
	 */
	public void close() {
		providerTracker.close();
	}

	/**
	 * Finds a mounted application for the specified url.
	 * 
	 * @param url
	 *            the request url
	 * @return the found mount, or <code>null</code> if none could be found
	 */
	public ApplicationMount findApplicationMount(final URL url) {
		String urlLookup = url.toString().intern();
		ApplicationMount applicationMount;
		do {
			applicationMount = mounts.get(urlLookup);
			if (null == applicationMount) {
				final int lastSlash = urlLookup.lastIndexOf('/');
				if (lastSlash != -1) {
					// one dir up
					urlLookup = urlLookup.substring(0, lastSlash);
				} else {
					// abort
					urlLookup = null;
				}
			}
		} while ((null == applicationMount) && (null != urlLookup));

		// fallback to default if we have one
		if (null == applicationMount) {
			final String applicationId = defaultApplicationId;
			if (null != applicationId) {
				return new ApplicationMount(url, applicationId);
			}
		}

		return applicationMount;
	}

	/**
	 * Returns the registration for the specified application id.
	 * 
	 * @param applicationId
	 *            the application id
	 * @return the application registration, or <code>null</code> if no
	 *         application is registered (or the registration has been removed)
	 */
	public ApplicationRegistration getApplicationRegistration(final String applicationId) {
		return applications.get(applicationId);
	}

	/**
	 * Returns a provider registration.
	 * 
	 * @param providerId
	 *            the provider id
	 * @return the provider registration, or <code>null</code> if no such
	 *         provider is registered (or was unregistered meanwhile)
	 */
	public ApplicationProviderRegistration getProviderRegistration(final String providerId) {
		return providers.get(providerId);
	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void modifiedService(final ServiceReference reference, final Object service) {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.http.application.registry.IApplicationManager#mount(java.lang.String, java.lang.String)
	 */
	@Override
	public void mount(String url, final String applicationId) throws MountConflictException, MalformedURLException {
		if (null == url) {
			throw new IllegalArgumentException("url must not be null");
		}

		// strip trailing slashes
		while ((url.length() > 0) && (url.charAt(url.length() - 1) == '/')) {
			url = url.substring(0, url.length() - 1);
		}

		// verify protocol
		if (!(url.startsWith("http://") || url.startsWith("https://"))) {
			throw new IllegalArgumentException("url must start with 'http://' or 'https://'");
		}

		final ApplicationMount mount = new ApplicationMount(new URL(url), applicationId);
		final ApplicationMount existing = mounts.putIfAbsent(url.intern(), mount);
		if (null != existing) {
			throw new MountConflictException(url);
		}
	}

	/**
	 * Opens the manager.
	 */
	public void open() {
		providerTracker.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.http.application.registry.IApplicationManager#register(java.lang.String, java.lang.String, org.eclipse.cloudfree.common.context.IContext, java.util.Map)
	 */
	@Override
	public void register(final String applicationId, final String providerId, final IContext context, final Map<String, String> properties) throws ApplicationRegistrationException {
		if (null == applicationId) {
			throw new IllegalArgumentException("application id must not be null");
		}
		if (null == providerId) {
			throw new IllegalArgumentException("url application provider id not be null");
		}
		if (null == context) {
			throw new IllegalArgumentException("context must not be null");
		}
		final ApplicationRegistration applicationRegistration = new ApplicationRegistration(applicationId, providerId, context, properties, this);
		final ApplicationRegistration existing = applications.putIfAbsent(applicationId.intern(), applicationRegistration);
		if (null != existing) {
			throw new ApplicationRegistrationException(applicationId);
		}

	}

	/* (non-Javadoc)
	 * @see org.osgi.util.tracker.ServiceTrackerCustomizer#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
	 */
	@Override
	public void removedService(final ServiceReference reference, final Object service) {
		final ApplicationProvider provider = (ApplicationProvider) service;
		if (null != provider) {
			removeProvider(provider);
		}

		// unget the service
		context.ungetService(reference);
	}

	private void removeProvider(final ApplicationProvider provider) {
		// remove provider registration
		final ApplicationProviderRegistration providerRegistration = providers.remove(provider.getId());
		if (null == providerRegistration) {
			return;
		}

		// unmount and destroy all applications bound to the provider
		providerRegistration.destroy();
	}

	public void setDefaultApplication(final String applicationId) {
		defaultApplicationId = applicationId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.http.application.registry.IApplicationManager#unmount(java.lang.String)
	 */
	@Override
	public void unmount(String url) {
		// strip trailing slashes
		while ((url.length() > 0) && (url.charAt(url.length() - 1) == '/')) {
			url = url.substring(0, url.length() - 1);
		}

		// verify protocol
		if (!(url.startsWith("http://") || url.startsWith("https://"))) {
			throw new IllegalArgumentException("url must start with 'http://' or 'https://'");
		}

		mounts.remove(url.intern());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.http.application.registry.IApplicationManager#unregister(java.lang.String)
	 */
	@Override
	public void unregister(final String applicationId) {
		applications.remove(applicationId);
	}

}