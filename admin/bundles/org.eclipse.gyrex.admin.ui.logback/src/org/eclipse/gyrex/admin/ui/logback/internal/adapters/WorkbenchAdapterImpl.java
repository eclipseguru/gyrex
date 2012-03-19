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
package org.eclipse.gyrex.admin.ui.logback.internal.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class WorkbenchAdapterImpl implements IWorkbenchAdapter {
	public static final Object[] NO_CHILDREN = new Object[0];

	@Override
	public Object[] getChildren(final Object o) {
		if (o instanceof LogbackConfig) {
			final LogbackConfig logbackConfig = (LogbackConfig) o;
			final List<Object> children = new ArrayList<Object>();
			children.addAll(logbackConfig.getAppenders());
			children.addAll(logbackConfig.getLoggers());
			return children.toArray(new Object[children.size()]);
		}
		return NO_CHILDREN;
	}

	@Override
	public ImageDescriptor getImageDescriptor(final Object object) {
		return null;
	}

	@Override
	public String getLabel(final Object o) {
		if (o instanceof LogbackConfig) {
			return "Loback Configuration";
		}
		if (o instanceof Appender) {
			return ((Appender) o).getName();
		}
		if (o instanceof Logger) {
			return ((Logger) o).getName();
		}
		return String.valueOf(o);
	}

	@Override
	public Object getParent(final Object o) {
		return null;
	}

}