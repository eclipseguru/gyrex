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
package org.eclipse.gyrex.admin.ui.internal.configuration;

import org.eclipse.gyrex.admin.ui.internal.pages.PageContribution;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 *
 */
public class ConfigurationPagePatternFilter extends PatternFilter {

	@Override
	public boolean isElementSelectable(final Object element) {
		return element instanceof PageContribution;
	}

	@Override
	public boolean isElementVisible(final Viewer viewer, final Object element) {
		// always show the root input
		if (!(element instanceof PageContribution)) {
			return true;
		}

		// configuration pages are not differentiated based on category since
		// categories are selectable nodes.
		if (isLeafMatch(viewer, element)) {
			return true;
		}

		final ITreeContentProvider contentProvider = (ITreeContentProvider) ((TreeViewer) viewer).getContentProvider();
		final PageContribution node = (PageContribution) element;
		final Object[] children = contentProvider.getChildren(node);
		// Will return true if any subnode of the element matches the search
		if (filter(viewer, element, children).length > 0) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		// always match the root input
		if (!(element instanceof PageContribution)) {
			return true;
		}

		final PageContribution page = (PageContribution) element;

		// check page name
		final String text = page.getName();
		if (wordMatches(text)) {
			return true;
		}

		// check the keywords
		final String[] keywords = page.getKeywords();
		for (int i = 0; i < keywords.length; i++) {
			if (wordMatches(keywords[i])) {
				return true;
			}
		}

		// no match
		return false;
	}
}
