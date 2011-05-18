/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
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

import org.eclipse.gyrex.admin.ui.configuration.ConfigurationPage;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.part.ViewPart;

/**
 * A View that displays all registered admin configuration elements in the
 * running platform. Depending on the current selection, the matching
 * {@link ConfigurationPage} is displayed in the {@link ConfigurationPanelView}
 */
public class ConfigurationNavigatorView extends ViewPart {

	static class ViewContentProvider implements ITreeContentProvider {

		public void dispose() {
		}

		@Override
		public Object[] getChildren(final Object parent) {
			return getElements(parent);
		}

		public Object[] getElements(final Object parent) {
			if (parent instanceof ConfigurationPageRegistration) {
				return ConfigurationPageRegistry.getInstance().getChildren((ConfigurationPageRegistration) parent);
			} else {
				return ConfigurationPageRegistry.getInstance().getChildren(null);
			}
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof ConfigurationPageRegistration) {
				return ConfigurationPageRegistry.getInstance().getParent((ConfigurationPageRegistration) element);
			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof ConfigurationPageRegistration) {
				return ConfigurationPageRegistry.getInstance().hasChildren((ConfigurationPageRegistration) element);
			}
			return false;
		}

		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	static class ViewLabelProvider extends LabelProvider {

		@Override
		public Image getImage(final Object obj) {
			return null;//PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public String getText(final Object element) {
			if (element instanceof ConfigurationPageRegistration) {
				final ConfigurationPageRegistration provider = (ConfigurationPageRegistration) element;
				return provider.getName();
			} else {
				return super.getText(element);
			}
		}
	}

	public static final String ID = "org.eclipse.gyrex.admin.ui.view.navigation";

	private FilteredTree filteredTree;

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		filteredTree = new FilteredTree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL, new ConfigurationPagePatternFilter());

		final TreeViewer viewer = filteredTree.getViewer();
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setComparator(new ViewerComparator());
		viewer.setInput(new Object());

		getSite().setSelectionProvider(viewer);

		// select the default page
		final ConfigurationPageRegistration page = ConfigurationPageRegistry.getInstance().getPage("org.eclipse.gyrex.admin.ui.general");
		if (page != null) {
			viewer.setSelection(new StructuredSelection(page), true);
		}
	}

	@Override
	public Object getAdapter(final Class adapter) {
//		if (adapter == IPropertySheetPage.class) {
//			return new TabbedPropertySheetPage(ConfigurationTabbedPropertySheetPageContributor.INSTANCE);
//		}
		return super.getAdapter(adapter);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		filteredTree.setFocus();
	}
}