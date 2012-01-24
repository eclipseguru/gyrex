/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.context.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 */
public class PreferenceData implements IPropertySource {

	private static final String PREFIX_CHILD = "child__";
	private static final String PREFIX_KEY = "key__";

	private final IPropertyDescriptor[] descriptors;
	private final Preferences root;
	private final String path;

	/**
	 * Creates a new instance.
	 * 
	 * @param rootNodeForContextPreferences
	 * @param path
	 * @throws BackingStoreException
	 */
	public PreferenceData(final Preferences root, final String path) throws BackingStoreException {
		this.root = root;
		this.path = path;

		if (root.nodeExists(path)) {
			// initialize descriptors
			final List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
			final String[] names = root.node(path).keys();
			for (final String name : names) {
				descriptors.add(new PropertyDescriptor(PREFIX_KEY + name, name));
			}
			final String[] childrenNames = root.node(path).childrenNames();
			for (final String name : childrenNames) {
				descriptors.add(new PropertyDescriptor(PREFIX_CHILD + name, name));
			}
			this.descriptors = descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
		} else {
			descriptors = new IPropertyDescriptor[0];
		}
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(final Object id) {
		final String name = id.toString();
		if (name.startsWith(PREFIX_CHILD)) {
			final String childPath = path + "/" + name.substring(PREFIX_CHILD.length());
			try {
				return new PreferenceData(root, childPath);
			} catch (final BackingStoreException e) {
				return ExceptionUtils.getRootCauseMessage(e);
			}
		} else if (name.startsWith(PREFIX_KEY)) {
			return root.node(path).get(name.substring(PREFIX_KEY.length()), "");
		}
		return null;
	}

	@Override
	public boolean isPropertySet(final Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(final Object id) {
		// no-op
	}

	@Override
	public void setPropertyValue(final Object id, final Object value) {
		// no-op
	}

}
