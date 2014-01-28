/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
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

import org.eclipse.gyrex.logback.config.model.LogbackConfig;

import ch.qos.logback.classic.Level;

public class DefaultLogger {

	private final LogbackConfig config;

	public DefaultLogger(final LogbackConfig logbackConfig) {
		config = logbackConfig;
	}

	public List<String> getAppenderReferences() {
		return config.getDefaultAppenders();
	}

	public LogbackConfig getConfig() {
		return config;
	}

	public Level getLevel() {
		return config.getDefaultLevel();
	}

}