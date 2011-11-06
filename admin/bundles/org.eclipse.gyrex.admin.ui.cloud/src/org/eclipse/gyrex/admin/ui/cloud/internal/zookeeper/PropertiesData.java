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
package org.eclipse.gyrex.admin.ui.cloud.internal.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 *
 */
public class PropertiesData implements IPropertySource {

	private final Properties prop;
	private final IPropertyDescriptor[] descriptors;

	/**
	 * Creates a new instance.
	 * 
	 * @param prop
	 */
	public PropertiesData(final Properties prop) {
		this.prop = prop;

		// initialize descriptors
		final List<Object> names = new ArrayList<Object>(prop.keySet());
		descriptors = new IPropertyDescriptor[names.size()];
		for (int i = 0; i < descriptors.length; i++) {
			final String name = (String) names.get(i);
			descriptors[i] = new PropertyDescriptor(name, name);
		}
	}

	@Override
	public Object getEditableValue() {
		return prop;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(final Object id) {
		return prop.get(id);
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
