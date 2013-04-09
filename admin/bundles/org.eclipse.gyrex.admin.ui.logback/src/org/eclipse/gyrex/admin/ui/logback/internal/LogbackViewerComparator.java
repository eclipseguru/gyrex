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

import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.jface.viewers.ViewerComparator;

public class LogbackViewerComparator extends ViewerComparator {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	public int category(final Object element) {
		if (element instanceof LogbackConfig) {
			return 0;
		}
		if (element instanceof Appender) {
			return 10;
		}
		if (element instanceof Logger) {
			return 20;
		}
		if (element instanceof DefaultLogger) {
			return 30;
		}
		return element.getClass().hashCode();
	}

}
