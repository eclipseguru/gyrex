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

import org.eclipse.gyrex.admin.ui.cloud.internal.CloudUiActivator;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.ViewPart;

/**
 *
 */
public class ZooKeeperExplorer extends ViewPart {

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
			if (parent instanceof ZooKeeperData) {
				return ((ZooKeeperData) parent).getChildren();
			} else {
				return NO_CHILDREN;
			}
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof ZooKeeperData) {
				return ((ZooKeeperData) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof ZooKeeperData) {
				return ((ZooKeeperData) element).hasChildren();
			} else {
				return false;
			}
		}

		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	static class ViewLabelProvider extends LabelProvider {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public Image getImage(final Object obj) {
			return null;//PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public String getText(final Object element) {
			if (element instanceof ZooKeeperData) {
				return ((ZooKeeperData) element).getLabel();
			} else {
				return super.getText(element);
			}
		}
	}

	public static final String ID = "org.eclipse.gyrex.admin.ui.cloud.view.zookeeper";

	private FilteredTree filteredTree;

	private IAction refreshAction;

	private void createActions() {
		refreshAction = new Action() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void run() {
				// reset input
				filteredTree.getViewer().setInput(new ZooKeeperData(Path.ROOT, null));
			}
		};
		refreshAction.setId(ActionFactory.REFRESH.getId());
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh the tree!");
		refreshAction.setImageDescriptor(CloudUiActivator.getImageDescriptor("/icons/refresh.gif"));
		refreshAction.setDisabledImageDescriptor(CloudUiActivator.getImageDescriptor("/icons/refresh_disabled.gif"));
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		final PathPatternFilter filter = new PathPatternFilter();
		filter.setIncludeLeadingWildcard(true);
		filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, filter, true);

		final TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setComparator(new ViewerComparator());
		viewer.setInput(new Object());

		createActions();
		initToolBar();
		getSite().setSelectionProvider(viewer);

		viewer.setInput(new ZooKeeperData(Path.ROOT, null));
	}

	private void initToolBar() {
		final IActionBars bars = getViewSite().getActionBars();
		final IToolBarManager tm = bars.getToolBarManager();

		if (null != refreshAction) {
			tm.add(refreshAction);
		}
	}

	@Override
	public void setFocus() {
		filteredTree.setFocus();
	}

}
