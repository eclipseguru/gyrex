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
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.qos.logback.classic.Level;

public class LogbackConfigContentProvider implements ITreeContentProvider {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	public static class DefaultLogger {

		private final LogbackConfig config;

		public DefaultLogger(final LogbackConfig logbackConfig) {
			config = logbackConfig;
		}

		public List<String> getAppenderReferences() {
			return config.getDefaultAppenders();
		}

		public Level getLevel() {
			return config.getDefaultLevel();
		}

	}

	public static final Object[] NO_CHILDREN = new Object[0];

	@Override
	public void dispose() {
		// empty
	}

	@Override
	public Object[] getChildren(final Object o) {
		if (o instanceof LogbackConfig) {
			final LogbackConfig logbackConfig = (LogbackConfig) o;
			final List<Object> children = new ArrayList<Object>();
			children.addAll(logbackConfig.getAppenders().values());
			children.addAll(logbackConfig.getLoggers().values());
			children.add(new DefaultLogger(logbackConfig));
			return children.toArray(new Object[children.size()]);
		} else if (o instanceof Logger) {
			final Logger logger = (Logger) o;
			final List<String> appenderReferences = logger.getAppenderReferences();
			final Object[] children = new Object[appenderReferences.size()];
			for (int i = 0; i < children.length; i++) {
				children[i] = new LoggerAppenderRef(logger, appenderReferences.get(i));
			}
			return children;
		} else if (o instanceof DefaultLogger) {
			final DefaultLogger logger = (DefaultLogger) o;
			return logger.getAppenderReferences().toArray();
		}
		return NO_CHILDREN;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// empty
	}

}
