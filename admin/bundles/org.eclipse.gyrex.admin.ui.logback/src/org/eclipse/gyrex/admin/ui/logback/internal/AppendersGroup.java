/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.Map;

import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.LogbackConfig;

public class AppendersGroup {
	private final LogbackConfig config;

	public AppendersGroup(final LogbackConfig logbackConfig) {
		config = logbackConfig;
	}

	public Map<String, Appender> getAppenders() {
		return config.getAppenders();
	}
}
