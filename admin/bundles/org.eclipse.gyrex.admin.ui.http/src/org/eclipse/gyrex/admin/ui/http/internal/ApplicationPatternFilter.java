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
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.gyrex.admin.ui.http.internal.ApplicationBrowserContentProvider.ApplicationItem;
import org.eclipse.gyrex.admin.ui.internal.widgets.PatternFilter;

import org.eclipse.jface.viewers.Viewer;

/**
 *
 */
public class ApplicationPatternFilter extends PatternFilter {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance.
	 */
	public ApplicationPatternFilter() {
		setIncludeLeadingWildcard(true);
	}

	@Override
	public boolean isElementSelectable(final Object element) {
		return super.isElementSelectable(element);
	}

	@Override
	public boolean isElementVisible(final Viewer viewer, final Object element) {
		return super.isElementVisible(viewer, element);
	}

	@Override
	protected boolean isLeafMatch(final Viewer viewer, final Object element) {
		if (element instanceof ApplicationItem) {
			final ApplicationItem appReg = (ApplicationItem) element;
			final Set<String> keywords = new LinkedHashSet<String>();
			keywords.add(appReg.getApplicationId());
			keywords.add(appReg.getProviderId());
			keywords.add(appReg.getProviderLabel());
			keywords.add(appReg.getContextPath());
			keywords.addAll(appReg.getMounts());
			keywords.add(appReg.isActive() ? "active" : "inactive");

			for (final String word : keywords) {
				if (wordMatches(word))
					return true;
			}
		}

		return super.isLeafMatch(viewer, element);
	}

}
