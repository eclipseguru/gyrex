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
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.context.IRuntimeContext;

public class ApplicationGroup {

	private final IRuntimeContext value;
	private final List<ApplicationItem> children = new ArrayList<ApplicationItem>();
	private final Object parent;

	public ApplicationGroup(final IRuntimeContext value, final Object parent) {
		this.value = value;
		this.parent = parent;
	}

	public void addChild(final ApplicationItem item) {
		children.add(item);
	}

	public List<ApplicationItem> getChildren() {
		return children;
	}

	public Object getParent() {
		return parent;
	}

	public IRuntimeContext getValue() {
		return value;
	}

}