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

import org.eclipse.gyrex.admin.ui.cloud.internal.NodeBrowserContentProvider.NodeItem;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public final class NodeBrowserComparator extends ViewerComparator {

	public static enum SortIndex {
		ID, LOCATION, STATUS
	}

	private SortIndex index = SortIndex.ID;
	private boolean reverse;

	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if ((e1 instanceof NodeItem) && (e2 instanceof NodeItem)) {
			return compareNodes((NodeItem) e1, (NodeItem) e2);
		}

		// fallback to super
		return super.compare(viewer, e1, e2);
	}

	@SuppressWarnings("unchecked")
	private int compareNodes(final NodeItem n1, final NodeItem n2) {
		final String t1 = StringUtils.trimToEmpty(getText(n1));
		final String t2 = StringUtils.trimToEmpty(getText(n2));
		if (isReverse()) {
			return getComparator().compare(t2, t1);
		} else {
			return getComparator().compare(t1, t2);
		}
	}

	/**
	 * Returns the index.
	 * 
	 * @return the index
	 */
	public SortIndex getIndex() {
		return null != index ? index : SortIndex.ID;
	}

	private String getText(final NodeItem node) {
		switch (getIndex()) {
			case LOCATION:
				return node.getDescriptor().getLocation();

			case STATUS:
				return node.isApproved() ? (node.isOnline() ? "A1" : "A2") : (node.isOnline() ? "P1" : "P2");

			case ID:
			default:
				return node.getDescriptor().getId();
		}
	}

	/**
	 * Returns the reverse.
	 * 
	 * @return the reverse
	 */
	public boolean isReverse() {
		return reverse;
	}

	/**
	 * Sets the index.
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(final SortIndex index) {
		this.index = index;

		// setting an index resets the reverse flag
		reverse = false;
	}

	/**
	 * Sets the reverse.
	 * 
	 * @param reverse
	 *            the reverse to set
	 */
	public void setReverse(final boolean reverse) {
		this.reverse = reverse;
	}

}