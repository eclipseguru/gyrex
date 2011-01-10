/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.configuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;
import org.eclipse.gyrex.admin.ui.internal.ExtensionPointContentProviderNode;
import org.eclipse.gyrex.admin.ui.internal.IContentProviderNode;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

/**
 * A View that displays all registered admin configuration elements in the
 * running platform. Depending on the current selection, the matching
 * {@link ConfigurationPage} is displayed in the {@link ConfigurationPanelView}
 */
public class ConfigurationNavigatorView extends ViewPart {

	class NodeComparator implements Comparator<IContentProviderNode> {

		@Override
		public int compare(final IContentProviderNode o1, final IContentProviderNode o2) {
			if ((o1 != null) && (o2 != null)) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
			return 0;
		}
	}

	class ViewContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		@Override
		public Object[] getChildren(final Object parentElement) {
			return getElements(parentElement);
		}

		public Object[] getElements(final Object parent) {
			if (parent instanceof IContentProviderNode) {
				final IContentProviderNode node = (IContentProviderNode) parent;
				if (node.getChildren().isEmpty()) {
					initChildren(node);
				}
				return node.getChildren().toArray();
			} else {
				return rootNodes.toArray();
			}
		}

		@Override
		public Object getParent(final Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			return getElements(element).length > 0;
		}

		private void initChildren(final IContentProviderNode node) {
			for (final IContentProviderNode contentProviderNode : allNodes) {
				if (node.getId().equals(contentProviderNode.getParentId())) {
					node.addChild(contentProviderNode);
				}
			}
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	class ViewLabelProvider extends LabelProvider {

		@Override
		public Image getImage(final Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public String getText(final Object element) {
			if (element instanceof IContentProviderNode) {
				final IContentProviderNode provider = (IContentProviderNode) element;
				return provider.getName();
			} else {
				return super.getText(element);
			}
		}
	}

	public static final String ID = "org.eclipse.gyrex.admin.ui.view.navigation";

	private static final String EXTENSION_POINT_ID = "org.eclipse.gyrex.admin.ui.configurationPages";

	private FilteredTree filteredTree;

	private static final Object TOP = new Object();

	private List<IContentProviderNode> rootNodes;

	private List<IContentProviderNode> allNodes;

	private final Comparator<? super IContentProviderNode> nodeComparator = new NodeComparator();

	@Override
	public void createPartControl(final Composite parentComposite) {

		final Composite parent = new Composite(parentComposite, SWT.NONE);
		parent.setLayout(new GridLayout(1, true));

		final int style = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL;

		final PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(final Viewer viewer, final Object element) {
				if (element instanceof IContentProviderNode) {
					final IContentProviderNode cpNode = (IContentProviderNode) element;
					for (final String keyword : cpNode.getKeywords()) {
						if (wordMatches(keyword)) {
							return true;
						}
					}
				}
				return super.isLeafMatch(viewer, element);
			}
		};

		initializeExtensions();

		filteredTree = new FilteredTree(parent, style, filter);

		final TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(TOP);

		getSite().setSelectionProvider(viewer);

		if (!rootNodes.isEmpty()) {
			viewer.setSelection(new StructuredSelection(rootNodes.get(0)), true);
		}
	}

	@Override
	public Object getAdapter(final Class adapter) {
//		if (adapter == IPropertySheetPage.class) {
//			return new TabbedPropertySheetPage(ConfigurationTabbedPropertySheetPageContributor.INSTANCE);
//		}
		return super.getAdapter(adapter);
	}

	private void initializeExtensions() {

		rootNodes = new Vector<IContentProviderNode>();
		allNodes = new Vector<IContentProviderNode>();

		final IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);

		for (final IConfigurationElement configurationElement : config) {

			final ExtensionPointContentProviderNode node = new ExtensionPointContentProviderNode(configurationElement);
			if (node.isToplevel()) {
				rootNodes.add(node);
			}
			allNodes.add(node);
		}

		Collections.sort(rootNodes, nodeComparator);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		filteredTree.setFocus();
	}
}