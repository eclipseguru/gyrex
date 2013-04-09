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

import java.util.List;

import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class LogbackConfigContentProvider implements ITreeContentProvider {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	public static final Object[] NO_CHILDREN = new Object[0];

	private AppendersGroup appendersGroup;

	private LoggersGroup loggersGroup;

	private DefaultLogger defaultLogger;

	@Override
	public void dispose() {
		// empty
	}

	@Override
	public Object[] getChildren(final Object o) {
		if (o instanceof LogbackConfig)
			return new Object[] { appendersGroup, loggersGroup, defaultLogger };
		else if (o == appendersGroup)
			return appendersGroup.getAppenders().values().toArray();
		else if (o == loggersGroup)
			return loggersGroup.getLoggers().values().toArray();
		else if (o == defaultLogger)
			return toAppenderReferences(defaultLogger, defaultLogger.getAppenderReferences());
		else if (o instanceof Logger) {
			final Logger logger = (Logger) o;
			return toAppenderReferences(logger, logger.getAppenderReferences());
		}
		return NO_CHILDREN;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof AppenderReference)
			return ((AppenderReference) element).getParent();
		if (element instanceof Logger)
			return loggersGroup;
		if (element instanceof Appender)
			return appendersGroup;
		if ((element == appendersGroup) || (element == loggersGroup) || (element == defaultLogger))
			return defaultLogger.getConfig();
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if (newInput instanceof LogbackConfig) {
			final LogbackConfig logbackConfig = (LogbackConfig) newInput;
			appendersGroup = new AppendersGroup(logbackConfig);
			loggersGroup = new LoggersGroup(logbackConfig);
			defaultLogger = new DefaultLogger(logbackConfig);
		}
	}

	private Object[] toAppenderReferences(final Object parent, final List<String> appenderReferences) {
		final Object[] children = new Object[appenderReferences.size()];
		for (int i = 0; i < children.length; i++) {
			children[i] = new AppenderReference(parent, appenderReferences.get(i));
		}
		return children;
	}

}
