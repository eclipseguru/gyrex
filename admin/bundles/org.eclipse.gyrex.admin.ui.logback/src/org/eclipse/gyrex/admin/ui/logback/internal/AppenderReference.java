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

import org.eclipse.gyrex.logback.config.internal.model.Logger;

public class LoggerAppenderRef {

	private final Logger logger;
	private final String appenderRef;

	/**
	 * Creates a new instance.
	 * 
	 * @param logger
	 * @param string
	 */
	public LoggerAppenderRef(final Logger logger, final String appenderRef) {
		this.logger = logger;
		this.appenderRef = appenderRef;
	}

	/**
	 * Returns the appenderRef.
	 * 
	 * @return the appenderRef
	 */
	public String getAppenderRef() {
		return appenderRef;
	}

	/**
	 * Returns the logger.
	 * 
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

}