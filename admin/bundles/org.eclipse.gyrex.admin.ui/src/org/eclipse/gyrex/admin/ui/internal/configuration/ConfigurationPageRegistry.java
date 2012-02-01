/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for configuration pages
 */
public class ConfigurationPageRegistry extends EventManager implements IExtensionChangeHandler {

	/** CONFIGURATION_PAGE_REGISTRATIONS */
	private static final ConfigurationPageRegistration[] EMPTY_CHILDREN = new ConfigurationPageRegistration[0];
	/** PAGE2 */
	private static final String ELEMENT_PAGE = "page";
	private static final String ROOT_ID = "/";
	private static final ConfigurationPageRegistry instance = new ConfigurationPageRegistry();
	private static final String EP_CONFIGURATION_PAGES = "configurationPages";
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationPageRegistry.class);

	private static IExtensionPoint getConfigurationPagesExtensionPoint() {
		final IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(AdminUiActivator.SYMBOLIC_NAME, EP_CONFIGURATION_PAGES);
		if (null == extensionPoint) {
			throw new IllegalStateException("extension point not found");
		}
		return extensionPoint;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the instance
	 */
	public static ConfigurationPageRegistry getInstance() {
		return instance;
	}

	private final ConcurrentMap<String, ConfigurationPageRegistration> pages = new ConcurrentHashMap<String, ConfigurationPageRegistration>();
	private volatile Map<String, Set<ConfigurationPageRegistration>> childrenIdsByParentId;

	private ConfigurationPageRegistry() {
		// get extension point
		final IExtensionPoint extensionPoint = getConfigurationPagesExtensionPoint();

		// register tracker
		final IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(extensionPoint));

		// initial population
		final IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addExtension(tracker, extensions[i]);
		}
	}

	@Override
	public void addExtension(final IExtensionTracker tracker, final IExtension extension) {
		final IConfigurationElement[] elements = extension.getConfigurationElements();
		for (final IConfigurationElement element : elements) {
			if (StringUtils.equals(ELEMENT_PAGE, element.getName())) {
				final ConfigurationPageRegistration page = new ConfigurationPageRegistration(element);
				final String id = page.getId();
				if (null == pages.putIfAbsent(id, page)) {
					rebuildChildrenMappings();
				} else {
					LOG.warn("Ignoring duplicate configuration page {} contributed by {}", id, extension.getContributor().getName());
				}
			}
		}
	}

	public ConfigurationPageRegistration[] getChildren(final ConfigurationPageRegistration page) {
		final Map<String, Set<ConfigurationPageRegistration>> mappings = childrenIdsByParentId;
		if (mappings == null) {
			return EMPTY_CHILDREN;
		}
		final String pageId = page != null ? page.getId() : ROOT_ID;
		final Set<ConfigurationPageRegistration> children = mappings.get(pageId);
		return children != null ? children.toArray(new ConfigurationPageRegistration[children.size()]) : EMPTY_CHILDREN;
	}

	public ConfigurationPageRegistration getPage(final String id) {
		return pages.get(id);
	}

	public ConfigurationPageRegistration getParent(final ConfigurationPageRegistration page) {
		if (null == page.getParentId()) {
			return null;
		}
		return pages.get(page.getParentId());
	}

	public boolean hasChildren(final ConfigurationPageRegistration page) {
		final Map<String, Set<ConfigurationPageRegistration>> mappings = childrenIdsByParentId;
		if (mappings == null) {
			return false;
		}
		final String pageId = page != null ? page.getId() : ROOT_ID;
		final Set<ConfigurationPageRegistration> children = mappings.get(pageId);
		return (children != null) && !children.isEmpty();
	}

	private void rebuildChildrenMappings() {
		final Map<String, Set<ConfigurationPageRegistration>> mappings = new HashMap<String, Set<ConfigurationPageRegistration>>();
		final Collection<ConfigurationPageRegistration> values = pages.values();
		for (final ConfigurationPageRegistration page : values) {
			final String parentId = page.getParentId() != null ? page.getParentId() : ROOT_ID;
			if (!mappings.containsKey(parentId)) {
				mappings.put(parentId, new HashSet<ConfigurationPageRegistration>(1));
			}
			mappings.get(parentId).add(page);

		}
		childrenIdsByParentId = mappings;

	}

	@Override
	public void removeExtension(final IExtension extension, final Object[] objects) {
		final IConfigurationElement[] elements = extension.getConfigurationElements();
		for (final IConfigurationElement element : elements) {
			if (StringUtils.equals(ELEMENT_PAGE, element.getName())) {
				final String id = element.getAttribute("id");
				final ConfigurationPageRegistration page = pages.remove(id);
				if (page != null) {
					rebuildChildrenMappings();
				}
			}
		}
	}
}
