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
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

@SuppressWarnings("restriction")
public class ApplicationPropertiesPropertySource implements IPropertySource {

	private final SortedMap<String, String> properties = new TreeMap<String, String>();

	/**
	 * Creates a new instance.
	 */
	public ApplicationPropertiesPropertySource(final ApplicationRegistration applicationRegistration) {
		properties.putAll(applicationRegistration.getInitProperties());
	}

	@Override
	public Object getEditableValue() {
		return properties;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		final List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
		for (final Entry<String, String> p : properties.entrySet()) {
			descriptors.add(new PropertyDescriptor(p.getValue(), p.getKey()));
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