/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
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

import org.eclipse.gyrex.context.internal.registry.ContextDefinition;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class WorkbenchAdapterImpl implements IWorkbenchAdapter {

	private static final Object[] NO_CHILDREN = new Object[0];

	static String getElementText(final ContextDefinition definition) {
		if (StringUtils.isBlank(definition.getName())) {
			return definition.getPath().toString();
		}
		return definition.getName();
	}

	@Override
	public Object[] getChildren(final Object o) {
		if (o instanceof ContextData) {
			return ((ContextData) o).getChildren();
		}
		return NO_CHILDREN;
	}

	@Override
	public ImageDescriptor getImageDescriptor(final Object object) {
		return null;
	}

	@Override
	public String getLabel(final Object o) {
		if (o instanceof ContextData) {
			return ((ContextData) o).getLabel();
		}
		if (o instanceof ContextDefinition) {
			return getElementText((ContextDefinition) o);
		}
		return "";
	}

	@Override
	public Object getParent(final Object o) {
		if (o instanceof ContextData) {
			return ((ContextData) o).getParent();
		}
		return null;
	}

}
