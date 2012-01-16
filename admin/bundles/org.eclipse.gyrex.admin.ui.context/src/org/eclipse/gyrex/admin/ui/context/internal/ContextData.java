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
package org.eclipse.gyrex.admin.ui.context.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.context.internal.configuration.ContextConfiguration;
import org.eclipse.gyrex.context.internal.preferences.GyrexContextPreferencesImpl;
import org.eclipse.gyrex.context.internal.registry.ContextDefinition;
import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 */
public class ContextData implements IPropertySource {

	private static final Object PROP_PATH = new Object();
	private static final Object PROP_NAME = new Object();
	private static final Object PROP_DATA = new Object();

	private Object[] children;
	private Object data;
	private final ContextData parent;
	private final ContextDefinition context;

	/**
	 * Creates a new instance.
	 */
	public ContextData(final ContextDefinition context, final ContextData parent) {
		this.context = context;
		this.parent = parent;
	}

	/**
	 * Returns the children.
	 * 
	 * @return the children
	 */
	public Object[] getChildren() {
		if (null == children) {
			load();
		}
		return children;
	}

	private ContextRegistryImpl getContextRegistry() {
		return (ContextRegistryImpl) ContextUiActivator.getInstance().getService(IRuntimeContextRegistry.class);
	}

	public Object getData() {
		if (null == data) {
			load();
		}
		return data;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	public String getLabel() {
		if (StringUtils.isBlank(context.getName())) {
			return context.getPath().toString();
		}
		return context.getName();
	}

	/**
	 * Returns the parent.
	 * 
	 * @return the parent
	 */
	public ContextData getParent() {
		return parent;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return new IPropertyDescriptor[] { new PropertyDescriptor(PROP_PATH, "Path"), new PropertyDescriptor(PROP_NAME, "Name"), new PropertyDescriptor(PROP_DATA, "Settings") };
	}

	@Override
	public Object getPropertyValue(final Object id) {
		if (id == PROP_PATH) {
			return context.getPath();
		}
		if (id == PROP_NAME) {
			return context.getName();
		}
		if (id == PROP_DATA) {
			return getData();
		}
		return null;
	}

	public boolean hasChildren() {
		return getChildren().length > 0;
	}

	@Override
	public boolean isPropertySet(final Object id) {
		return false;
	}

	private void load() {
		try {
			final Collection<ContextDefinition> contexts = getContextRegistry().getDefinedContexts();
			final List<ContextData> children = new ArrayList<ContextData>();
			for (final ContextDefinition definition : contexts) {
				if ((definition.getPath().segmentCount() > context.getPath().segmentCount()) && context.getPath().isPrefixOf(definition.getPath())) {
					children.add(new ContextData(definition, this));
				}
			}
			this.children = children.toArray();
		} catch (final Exception e) {
			children = new String[] { ExceptionUtils.getRootCauseMessage(e) };
		}

		try {
			data = new PreferenceData(ContextConfiguration.getRootNodeForContextPreferences(), GyrexContextPreferencesImpl.getPreferencesPathToSettings(context.getPath(), null));
		} catch (final Exception e) {
			data = ExceptionUtils.getRootCauseMessage(e);
		}
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
