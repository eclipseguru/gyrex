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
package org.eclipse.gyrex.http.internal.application.manager;

import static com.google.common.base.Preconditions.checkState;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.http.application.manager.ApplicationRegistrationException;
import org.eclipse.gyrex.http.application.manager.IApplicationManager;
import org.eclipse.gyrex.http.application.manager.MountConflictException;
import org.eclipse.gyrex.http.application.provider.ApplicationProvider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.osgi.service.component.ComponentConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * A DS component which allows automatic mounting of HTTP application providers.
 */
public class AutomountComponent implements IAutomountService {

	public static final String PROPERTY_AUTOMOUNT_URL = "automount.url";
	public static final String PROPERTY_AUTOMOUNT_RUNTIME_CONTEXT_PATH = "automount.runtimeContextPath";
	public static final String PROPERTY_AUTOMOUNT_APPLICATION_ID = "automount.applicationId";

	private IApplicationManager applicationManager;
	private IRuntimeContextRegistry runtimeContextRegistry;

	private static final Logger LOG = LoggerFactory.getLogger(AutomountComponent.class);

	private Map<String, String> extractStringValueProperties(final Map<String, Object> properties) {
		final Map<String, String> result = new HashMap<>();
		for (final Entry<String, Object> e : properties.entrySet()) {
			if (e.getValue() instanceof String) {
				result.put(e.getKey(), (String) e.getValue());
			}
		}
		return result;
	}

	private String getApplicationId(final Map<String, Object> properties) {
		final Object applicationIdValue = properties.get(PROPERTY_AUTOMOUNT_APPLICATION_ID);

		if (null == applicationIdValue)
			return (String) properties.get(ComponentConstants.COMPONENT_NAME);

		checkState(applicationIdValue instanceof String, "The provider property 'applicationId' must be of type String!");
		return (String) applicationIdValue;
	}

	/**
	 * Returns the applicationManager.
	 *
	 * @return the applicationManager
	 */
	public IApplicationManager getApplicationManager() {
		final IApplicationManager manager = applicationManager;
		checkState(manager != null, "inactive");
		return manager;
	}

	/**
	 * Performs lookup of the auto-mount context
	 *
	 * @param properties
	 *            properties to read context path from
	 * @return the context path, defauts to root (/) if none is set in the
	 *         properties
	 */
	private IPath getAutomountContextPath(final Map<String, Object> properties) {
		final Object contextValue = properties.get(PROPERTY_AUTOMOUNT_RUNTIME_CONTEXT_PATH);

		if (null == contextValue)
			return Path.ROOT;

		if (contextValue instanceof IPath)
			return (IPath) contextValue;

		checkState(contextValue instanceof String, "The provider property 'automountRuntimeContext' must be of type String!");
		if (Strings.isNullOrEmpty((String) contextValue))
			return Path.ROOT;

		return new Path((String) contextValue).makeAbsolute();
	}

	/**
	 * Reads the auto-mount urls from the properties
	 *
	 * @param properties
	 * @return a list of urls to mount
	 */
	private List<String> getAutomountUrls(final Map<String, Object> properties) {
		final Object urlValue = properties.get(PROPERTY_AUTOMOUNT_URL);

		if (null == urlValue)
			return null;

		checkState(urlValue instanceof String, "The provider property 'automountUrl' must be of type String!");
		return Arrays.asList((String) urlValue);
	}

	/**
	 * Returns the runtimeContextRegistry.
	 *
	 * @return the runtimeContextRegistry
	 */
	public IRuntimeContextRegistry getRuntimeContextRegistry() {
		final IRuntimeContextRegistry registry = runtimeContextRegistry;
		checkState(registry != null, "inactive");
		return registry;
	}

	private void logAlreadyRegistered(final ApplicationProvider provider, final Collection<String> urls, final String applicationId) {
		LOG.debug("Skipping auto-mount of application '{}' (provider '{}'). Already found a registration.", applicationId, provider.getId());

		// log a warning for auto-mount urls which are not mounted
		final SortedSet<String> mounts = getApplicationManager().getMounts(applicationId);
		final List<String> urlsNotMounted = new ArrayList<String>(urls);
		for (final String url : urls) {
			if (mounts.contains(url)) {
				urlsNotMounted.remove(url);
			}
		}
		if (!urlsNotMounted.isEmpty()) {
			LOG.warn("Auto-mount not possible for url(s) '{}'. The exising application '{}' is configured differntly.", urlsNotMounted, applicationId);
		}
	}

	/**
	 * Mounts the provider if its configured for automatic mounting.
	 *
	 * @param provider
	 * @param properties
	 */
	public void mountProvider(final ApplicationProvider provider, final Map<String, Object> properties) {
		LOG.debug("Discovering auto-mounts for provider '{}'.", provider.getId());
		final Collection<String> urls = getAutomountUrls(properties);
		if ((urls == null) || urls.isEmpty()) {
			LOG.debug("No auto-mount url configured for provider '{}'.", provider.getId());
			return;
		}

		final String applicationId = getApplicationId(properties);

		// check if already registered
		if (getApplicationManager().isRegistered(applicationId)) {
			logAlreadyRegistered(provider, urls, applicationId);
			return;
		}

		// get runtime context
		final IPath contextPath = getAutomountContextPath(properties);
		final IRuntimeContext context = getRuntimeContextRegistry().get(contextPath);
		if (context == null) {
			LOG.error("Unable to auto-mount application '{}'. Runtime context '{}' not defined!", applicationId, contextPath);
			return;
		}

		LOG.info("Creating application '{}' at context '{}' mounted to urls '{}' as adviced by auto-mount configuration of provider '{}'.", applicationId, contextPath, urls, provider.getId());

		// register
		try {
			getApplicationManager().register(applicationId, provider.getId(), context, extractStringValueProperties(properties));
		} catch (final ApplicationRegistrationException e) {
			logAlreadyRegistered(provider, urls, applicationId);
			return;
		}

		// mount
		for (final String url : urls) {
			try {
				getApplicationManager().mount(url, applicationId);
			} catch (IllegalArgumentException | MountConflictException | MalformedURLException e) {
				LOG.error("Unable to auto-mount url '{}' for application '{}'. {}", url, applicationId, e.getMessage(), e);
			}
		}
	}

	/**
	 * Sets the applicationManager.
	 *
	 * @param applicationManager
	 *            the applicationManager to set
	 */
	public void setApplicationManager(final IApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	/**
	 * Sets the runtimeContextRegistry.
	 *
	 * @param runtimeContextRegistry
	 *            the runtimeContextRegistry to set
	 */
	public void setRuntimeContextRegistry(final IRuntimeContextRegistry runtimeContextRegistry) {
		this.runtimeContextRegistry = runtimeContextRegistry;
	}
}
