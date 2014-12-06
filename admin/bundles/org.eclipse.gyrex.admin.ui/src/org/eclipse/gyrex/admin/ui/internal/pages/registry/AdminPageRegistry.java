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

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.common.lifecycle.IShutdownParticipant;
import org.eclipse.gyrex.rap.application.Category;
import org.eclipse.gyrex.rap.application.Page;
import org.eclipse.gyrex.rap.application.PageHandle;
import org.eclipse.gyrex.rap.application.PageProvider;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A registry for contributed admin pages
 */
public class AdminPageRegistry extends PageProvider implements IExtensionChangeHandler {

	/**
	 * Returns the shared instance.
	 *
	 * @return the instance
	 */
	public static AdminPageRegistry getInstance() {
		return instance;
	}

	private static final String ELEMENT_PAGE = "page";
	private static final String ELEMENT_CATEGORY = "category";

	private static final String EP_PAGES = "pages";

	private static final Logger LOG = LoggerFactory.getLogger(AdminPageRegistry.class);

	private static final AdminPageRegistry instance = new AdminPageRegistry();

	private AdminPageRegistry() {
		final IExtensionRegistry registry = RegistryFactory.getRegistry();
		if (null == registry)
			throw new IllegalStateException("Extension registry is not available!");

		// get extension point
		final IExtensionPoint extensionPoint = registry.getExtensionPoint(AdminUiActivator.SYMBOLIC_NAME, EP_PAGES);
		if (null == extensionPoint)
			throw new IllegalStateException("Admin pages extension point not found!");

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
				if (!addPage(page)) {
					LOG.warn("Ignoring duplicate page {} contributed by {}", page.getId(), extension.getContributor().getName());
				}
			} else if (StringUtils.equals(ELEMENT_CATEGORY, element.getName())) {
				final Category category = new Category(element.getAttribute("id"));
				category.setName(element.getAttribute("name"));
				category.setSortKey(element.getAttribute("sortKey"));
				if (!addCategory(category)) {
					LOG.warn("Ignoring duplicate category {} contributed by {}", category.getId(), extension.getContributor().getName());
				}
			}
		}
	}

	@Override
	public Page createPage(final PageHandle pageHandle) throws Exception {
		return ((PageContribution) pageHandle).createPage();
	}

	@Override
	public void removeExtension(final IExtension extension, final Object[] objects) {
		final IConfigurationElement[] elements = extension.getConfigurationElements();
		for (final IConfigurationElement element : elements) {
			if (StringUtils.equals(ELEMENT_PAGE, element.getName())) {
				final String id = element.getAttribute("id");
				removePage(id);
			} else if (StringUtils.equals(ELEMENT_CATEGORY, element.getName())) {
				final String id = element.getAttribute("id");
				removeCategory(id);
			}
		}
	}

}
