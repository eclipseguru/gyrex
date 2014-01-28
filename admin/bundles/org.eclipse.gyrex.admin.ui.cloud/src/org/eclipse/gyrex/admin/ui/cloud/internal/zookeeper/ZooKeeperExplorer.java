/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal.zookeeper;

import org.eclipse.gyrex.admin.ui.cloud.internal.ZooKeeperBasedAdminPage;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ZooKeeperExplorer extends ZooKeeperBasedAdminPage {

	static class ViewContentProvider implements ITreeContentProvider {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private static final Object[] NO_CHILDREN = new Object[0];

		@Override
		public void dispose() {
		}

		@Override
		public Object[] getChildren(final Object parent) {
			return getElements(parent);
		}

		@Override
		public Object[] getElements(final Object parent) {
			if (parent instanceof ZooKeeperData)
				return ((ZooKeeperData) parent).getChildren();
			else
				return NO_CHILDREN;
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof ZooKeeperData)
				return ((ZooKeeperData) element).getParent();
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof ZooKeeperData)
				return ((ZooKeeperData) element).hasChildren();
			else
				return false;
		}

		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	public ZooKeeperExplorer() {
		super(0);
		setTitle("ZooKeeper Explorer");
		setTitleToolTip("Browse ZooKeeper data");
	}

	@Override
	protected void createButtons(final Composite parent) {
		// no buttons
	}

	@Override
	protected ITreeContentProvider createContentProvider() {
		return new ViewContentProvider();
	}

	@Override
	protected Control createHeader(final Composite parent) {
		// no header
		return null;
	}

	@Override
	protected PatternFilter createPatternFilter() {
		final PathPatternFilter filter = new PathPatternFilter();
		filter.setIncludeLeadingWildcard(true);
		return filter;
	}

	@Override
	protected String getColumnLabel(final int column) {
		// no columns
		return null;
	}

	@Override
	protected String getElementLabel(final Object element, final int column) {
		if (element instanceof ZooKeeperData)
			return ((ZooKeeperData) element).getLabel();
		else
			return String.valueOf(element);
	}

	@Override
	protected Object getViewerInput() {
		return new ZooKeeperData(Path.ROOT, null);
	}

	@Override
	protected boolean isColumnSortable(final int column) {
		return false;
	}

	@Override
	protected void refresh() {
		getTreeViewer().setInput(getViewerInput());
	}

	@Override
	protected void updateButtons() {
		// no buttons
	}
}
