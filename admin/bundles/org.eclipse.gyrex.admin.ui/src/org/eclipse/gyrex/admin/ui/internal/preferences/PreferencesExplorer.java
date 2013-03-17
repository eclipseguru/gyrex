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
package org.eclipse.gyrex.admin.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.widgets.AdminPageWithTree;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class PreferencesExplorer extends AdminPageWithTree {

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
			if (parent instanceof Preferences) {
				final Preferences node = (Preferences) parent;
				try {
					final List<Object> children = new ArrayList<Object>();
					for (final String name : node.childrenNames()) {
						children.add(node.node(name));
					}
					for (final String name : node.keys()) {
						children.add(name + "=" + StringUtils.left(node.get(name, StringUtils.EMPTY), 70));

					}
					return children.toArray();
				} catch (final BackingStoreException e) {
					return new String[] { ExceptionUtils.getRootCauseMessage(e) };
				}
			} else
				return NO_CHILDREN;
		}

		@Override
		public Object getParent(final Object element) {
			if (element instanceof Preferences)
				return ((Preferences) element).parent();
			return null;
		}

		@Override
		public boolean hasChildren(final Object element) {
			if (element instanceof Preferences) {
				try {
					return (((Preferences) element).childrenNames().length > 0) || (((Preferences) element).keys().length > 0);
				} catch (final BackingStoreException e) {
					// try again (or report exception)
					return true;
				}
			} else
				return false;
		}

		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
		}
	}

	public PreferencesExplorer() {
		super(0);
		setTitle("Preferences Explorer");
		setTitleToolTip("Browse Preferences");
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
		final PatternFilter filter = new PatternFilter();
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
		if (element instanceof Preferences)
			return ((Preferences) element).name();
		else
			return String.valueOf(element);
	}

	@Override
	protected Object getViewerInput() {
		return AdminUiActivator.getInstance().getService(IPreferencesService.class).getRootNode();
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
