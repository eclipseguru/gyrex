/*******************************************************************************
 * Copyright (c) 2011 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.StringUtils;

@SuppressWarnings("restriction")
public class ApplicationMountsPropertySource implements IPropertySource {

	private final SortedSet<String> mounts;

	/**
	 * Creates a new instance.
	 */
	public ApplicationMountsPropertySource(final ApplicationRegistration applicationRegistration) {
		final IEclipsePreferences urlsNode = ApplicationManager.getUrlsNode();
		mounts = new TreeSet<String>();
		try {
			final String[] urls = urlsNode.keys();
			for (final String url : urls) {
				final String appId = urlsNode.get(url, StringUtils.EMPTY);
				if (appId.equals(applicationRegistration.getApplicationId())) {
					mounts.add(url);
				}
			}
		} catch (final BackingStoreException e) {
			mounts.add(e.getMessage());
		}
	}

	@Override
	public Object getEditableValue() {
		return mounts;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		final List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
		for (final String url : mounts) {
			descriptors.add(new PropertyDescriptor(url, "URL"));
		}
		return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
	}

	@Override
	public Object getPropertyValue(final Object id) {
		return id;
	}

	@Override
	public boolean isPropertySet(final Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(final Object id) {
		// empty
	}

	@Override
	public void setPropertyValue(final Object id, final Object value) {
		// empty
	}

}