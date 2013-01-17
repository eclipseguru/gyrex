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
package org.eclipse.gyrex.admin.ui.cloud.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.gyrex.admin.ui.cloud.internal.NodeBrowserContentProvider.NodeItem;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;

import org.eclipse.jface.viewers.Viewer;

/**
 *
 */
public class NodePatternFilter extends PatternFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public NodePatternFilter() {
		setIncludeLeadingWildcard(true);
	}

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
		if (element instanceof NodeItem) {
			final NodeItem nodeItem = (NodeItem) element;
			final Set<String> keywords = new LinkedHashSet<String>();
			keywords.add(nodeItem.getDescriptor().getId());
			keywords.add(nodeItem.getDescriptor().getName());
			keywords.add(nodeItem.getDescriptor().getLocation());
			keywords.addAll(nodeItem.getDescriptor().getTags());
			if (nodeItem.isApproved()) {
				keywords.add("approved");
			} else {
				keywords.add("pending");
			}
			if (nodeItem.isOnline()) {
				keywords.add("online");
			}

			for (final String word : keywords) {
				if (wordMatches(word))
					return true;
			}
		}

		return super.isLeafMatch(viewer, element);
	}

}
