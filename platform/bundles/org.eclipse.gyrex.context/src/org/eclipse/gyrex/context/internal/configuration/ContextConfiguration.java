/**
 * Copyright (c) 2009, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.context.internal.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.context.internal.ContextActivator;
import org.eclipse.gyrex.context.internal.ContextDebug;
import org.eclipse.gyrex.preferences.CloudScope;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for context configuration.
 */
public final class ContextConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(ContextConfiguration.class);

	/** CONTEXTS */
	public static final String CONTEXTS = "contexts";

	/** the path to the context preferences root node */
	public static final IPath CONTEXT_PREF_ROOT = new Path(ContextActivator.SYMBOLIC_NAME).append(CONTEXTS).makeRelative();

	/**
	 * Finds a filter if available from the context configuration.
	 * 
	 * @param context
	 *            the context
	 * @param typeName
	 *            the requested type name
	 * @return the filter (maybe <code>null</code> if none is explicitly defined
	 *         for the context)
	 */
	public static String findFilter(IPath contextPath, final String typeName) {
		// get preferences root node
		final IEclipsePreferences rootNode = getRootNodeForContextPreferences();

		// lookup filter in this context
		String filter = readFilterFromPreferences(rootNode, contextPath, typeName);
		if (null != filter)
			return filter;

		// search parent contexts
		while ((null == filter) && !contextPath.isRoot()) {
			filter = readFilterFromPreferences(rootNode, contextPath = contextPath.removeLastSegments(1), typeName);
		}

		// return what we have (may be nothing)
		return filter;
	}

	/**
	 * Returns a map all configured filter.
	 * <p>
	 * The map key is type name and the value is the configured filter. This
	 * method only looks at the specified context and does not search the
	 * hierarchy.
	 * </p>
	 * 
	 * @param context
	 *            the context
	 * @param typeName
	 *            the requested type name
	 * @return the filter (maybe <code>null</code> if none is explicitly defined
	 *         for the context)
	 */
	public static Map<String, String> getFilters(final IPath contextPath) {
		final Map<String, String> result = new HashMap<String, String>();

		// get preferences root node
		final IEclipsePreferences rootNode = getRootNodeForContextPreferences();

		// get the preferences
		final String preferencesPath = getPreferencesPathForContextObjectFilterSetting(contextPath);
		try {
			if (!rootNode.nodeExists(preferencesPath))
				return Collections.emptyMap();

			// loop over keys and assume those are type names
			for (final String typeName : rootNode.node(preferencesPath).keys()) {
				// get the filter string
				final String filterString = rootNode.node(preferencesPath).get(typeName, null);
				if (StringUtils.isNotBlank(filterString)) {
					result.put(typeName, filterString);
				}
			}
			return result;
		} catch (final BackingStoreException e) {
			LOG.warn("Error while accessing the preferences backend for context path \"{}\": {}", contextPath, e.getMessage());
			return Collections.emptyMap();
		}
	}

	private static String getPreferencesPathForContextObjectFilterSetting(final IPath contextPath) {
		return contextPath.makeRelative().toString();
	}

	public static IEclipsePreferences getRootNodeForContextPreferences() {
		return (IEclipsePreferences) CloudScope.INSTANCE.getNode(ContextActivator.SYMBOLIC_NAME).node(CONTEXTS);
	}

	/**
	 * Reads a filter from the preferences of the specified context path.
	 * 
	 * @param context
	 *            the context
	 * @param root
	 *            the preferences root node
	 * @param contextPath
	 *            the context path
	 * @param typeName
	 *            the type name
	 * @return
	 */
	private static String readFilterFromPreferences(final IEclipsePreferences root, final IPath contextPath, final String typeName) {
		// get the preferences
		final String preferencesPath = getPreferencesPathForContextObjectFilterSetting(contextPath);
		try {
			if (!root.nodeExists(preferencesPath))
				return null;
		} catch (final BackingStoreException e) {
			LOG.warn("Error while accessing the preferences backend for context path \"{}\": {}", contextPath, e.getMessage());
			return null;
		}

		// get the filter string
		final String filterString = root.node(preferencesPath).get(typeName, null);
		if (null == filterString)
			return null;

		// return the filter
		return filterString;
	}

	/**
	 * Configures a context to use the specified filter for the given type name.
	 * 
	 * @param context
	 *            the context
	 * @param typeName
	 *            the requested type name
	 * @return the filter (maybe <code>null</code> if none is explicitly defined
	 *         for the context
	 */
	public static void setFilter(final IPath contextPath, final String typeName, final String filter) {
		// get preferences root node
		final IEclipsePreferences rootNode = getRootNodeForContextPreferences();

		// log a debug message
		if (ContextDebug.objectLifecycle) {
			LOG.debug("Setting filter in context {} for type {} to {}", new Object[] { contextPath, typeName, filter });
		}

		// set the preferences
		final String preferencesPath = getPreferencesPathForContextObjectFilterSetting(contextPath);
		try {
			final Preferences contextPreferences = rootNode.node(preferencesPath);
			if (null != filter) {
				contextPreferences.put(typeName, filter);
			} else {
				contextPreferences.remove(typeName);
			}
			contextPreferences.flush();
		} catch (final BackingStoreException e) {
			LOG.warn("Error while accessing the preferences backend for context path \"{}\": {}", contextPath, e.getMessage());
		}

		// TODO we need to flush the context hierarch for the type name here
		// the flush can potentially be smart to only flush the contexts which are affected by the change
	};

	/**
	 * Hidden constructor.
	 */
	private ContextConfiguration() {
		// empty
	}
}
