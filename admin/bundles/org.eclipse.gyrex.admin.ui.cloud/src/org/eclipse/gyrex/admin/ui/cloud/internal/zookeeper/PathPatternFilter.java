/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.cloud.internal.zookeeper;

import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;

/**
 * {@link IPath} based {@link PatternFilter} for {@link ZooKeeperData}.
 */
public class PathPatternFilter extends PatternFilter {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isElementSelectable(final Object element) {
		return element instanceof IPath;
	}

	@Override
	public boolean isElementVisible(final Viewer viewer, final Object element) {
		// always show the root input
		if (!(element instanceof ZooKeeperData))
			return true;

		// ZooKeeperData is not differentiated based on category since
		// categories are selectable nodes.
		if (isLeafMatch(viewer, element))
			return true;

		final Object[] children = ((ZooKeeperData) element).getChildren();
		// Will return true if any subnode of the element matches the search
		if (filter(viewer, element, children).length > 0)
			return true;
		return false;
	}

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		return super.isLeafMatch(viewer, element);
	}
}
