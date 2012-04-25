/*******************************************************************************
 * Copyright (c) 2012 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 *
 */
public class NodePatternFilter extends PatternFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean isElementSelectable(final Object element) {
		// TODO Auto-generated method stub
		return super.isElementSelectable(element);
	}

	@Override
	public boolean isElementVisible(final Viewer viewer, final Object element) {
		// TODO Auto-generated method stub
		return super.isElementVisible(viewer, element);
	}

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		// TODO Auto-generated method stub
		return super.isLeafMatch(viewer, element);
	}

}
