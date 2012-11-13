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
package org.eclipse.gyrex.admin.ui.internal.pages.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.common.lifecycle.IShutdownParticipant;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.osgi.framework.eventmgr.EventManager;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for contributed admin pages
 */
public class AdminPageRegistry extends EventManager implements IExtensionChangeHandler {

	private static final String ELEMENT_PAGE = "page";
	private static final String ELEMENT_CATEGORY = "category";
	private static final String EP_PAGES = "pages";

	private static final Logger LOG = LoggerFactory.getLogger(AdminPageRegistry.class);

	private static final AdminPageRegistry instance = new AdminPageRegistry();

	/**
	 * Returns the shared instance.
	 * 
	 * @return the instance
	 */
	public static AdminPageRegistry getInstance() {
		return instance;
	}

	private final ConcurrentMap<String, PageContribution> pagesById = new ConcurrentHashMap<String, PageContribution>();
	private final ConcurrentMap<String, CategoryContribution> categoriesById = new ConcurrentHashMap<String, CategoryContribution>(4);
	private volatile Map<String, Set<PageContribution>> pagesByCategoryId;

	private AdminPageRegistry() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		if (null == registry) {
			throw new IllegalStateException("Extension registry is not available!");
		}

		// get extension point
		final IExtensionPoint extensionPoint = registry.getExtensionPoint(AdminUiActivator.SYMBOLIC_NAME, EP_PAGES);
		if (null == extensionPoint) {
			throw new IllegalStateException("Admin pages extension point not found!");
		}

		// register tracker
		final IExtensionTracker tracker = new ExtensionTracker(registry);
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(extensionPoint));
		AdminUiActivator.getInstance().addShutdownParticipant(new IShutdownParticipant() {
			@Override
			public void shutdown() throws Exception {
				tracker.close();
			}
		});

		// initial population
		final IExtension[] extensions = extensionPoint.getExtensions();
		for (final IExtension extension : extensions) {
			addExtension(tracker, extension);
		}
	}

	@Override
	public void addExtension(final IExtensionTracker tracker, final IExtension extension) {
		final IConfigurationElement[] elements = extension.getConfigurationElements();
		for (final IConfigurationElement element : elements) {
			if (StringUtils.equals(ELEMENT_PAGE, element.getName())) {
				final PageContribution page = new PageContribution(element);
				final String id = page.getId();
				if (null == pagesById.putIfAbsent(id, page)) {
					rebuildCategories();
				} else {
					LOG.warn("Ignoring duplicate page {} contributed by {}", id, extension.getContributor().getName());
				}
			} else if (StringUtils.equals(ELEMENT_CATEGORY, element.getName())) {
				final CategoryContribution category = new CategoryContribution(element);
				final String id = category.getId();
				if (null != categoriesById.putIfAbsent(id, category)) {
					LOG.warn("Ignoring duplicate category {} contributed by {}", id, extension.getContributor().getName());
				}
			}
		}
	}

	public List<CategoryContribution> getCategories() {
		if (categoriesById.isEmpty()) {
			return Collections.emptyList();
		}
		return new ArrayList<CategoryContribution>(categoriesById.values());
	}

	public PageContribution getPage(final String id) {
		return pagesById.get(id);
	}

	public List<PageContribution> getPages(final CategoryContribution category) {
		final Map<String, Set<PageContribution>> mappings = pagesByCategoryId;
		if (mappings == null || mappings.isEmpty()) {
			return Collections.emptyList();
		}
		final Set<PageContribution> children = mappings.get(category.getId());
		if (children == null || children.isEmpty()) {
			return Collections.emptyList();
		}
		// return a copy
		return new ArrayList<PageContribution>(children);
	}

	public CategoryContribution getParent(final PageContribution page) {
		if (null == page.getCategoryId()) {
			return null;
		}
		// return a copy
		return categoriesById.get(page.getCategoryId());
	}

	public boolean hasPages(final CategoryContribution category) {
		if (category == null) {
			return false;
		}

		final Map<String, Set<PageContribution>> mappings = pagesByCategoryId;
		if (mappings == null) {
			return false;
		}
		final Set<PageContribution> children = mappings.get(category.getId());
		return children != null && !children.isEmpty();
	}

	private void rebuildCategories() {
		final Map<String, Set<PageContribution>> mappings = new HashMap<String, Set<PageContribution>>();
		final Collection<PageContribution> values = pagesById.values();
		for (final PageContribution page : values) {
			if (!mappings.containsKey(page.getCategoryId())) {
				mappings.put(page.getCategoryId(), new HashSet<PageContribution>(1));
			}
			mappings.get(page.getCategoryId()).add(page);
		}
		pagesByCategoryId = mappings;
	}

	@Override
	public void removeExtension(final IExtension extension, final Object[] objects) {
		final IConfigurationElement[] elements = extension.getConfigurationElements();
		for (final IConfigurationElement element : elements) {
			if (StringUtils.equals(ELEMENT_PAGE, element.getName())) {
				final String id = element.getAttribute("id");
				final PageContribution page = pagesById.remove(id);
				if (page != null) {
					rebuildCategories();
				}
			}
		}
	}
}
