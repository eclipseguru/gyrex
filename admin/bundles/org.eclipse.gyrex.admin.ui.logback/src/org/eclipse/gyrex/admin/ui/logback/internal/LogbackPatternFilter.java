/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Peter Grube - initial implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * template for implementation of LogbackPatternFilter
 */
public class LogbackPatternFilter extends PatternFilter {

	private static final long serialVersionUID = 1L;

	public LogbackPatternFilter() {
		setIncludeLeadingWildcard(true);
	}

	@Override
	public boolean isElementSelectable(final Object element) {
		return super.isElementSelectable(element);
	}

	@Override
	public boolean isElementVisible(final Viewer viewer, final Object element) {
		return super.isElementVisible(viewer, element);
	}

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		//TODO: implement functionality
		return true;
	}

}
