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

public class AppenderReference {

	private final Object parent;
	private final String appenderRef;

	public AppenderReference(final Object parent, final String appenderRef) {
		this.parent = parent;
		this.appenderRef = appenderRef;
	}

	public String getAppenderRef() {
		return appenderRef;
	}

	public Object getParent() {
		return parent;
	}
}