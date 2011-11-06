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

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Looks and works similar to the {@link PreferenceDialog} in eclipse
 * applications.
 * <p>
 * The tree to choose configuration pages is represented by the
 * {@link ConfigurationNavigatorView}, the matching configuration pages to the
 * tree elements are displayed as {@link ConfigurationPage} in the
 * {@link ConfigurationPanelView}
 */
public class ConfigurationPerspective implements IPerspectiveFactory {

	public static final String ID = "org.eclipse.gyrex.admin.ui.perspective.configuration";

	public void createInitialLayout(final IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		layout.addStandaloneView(ConfigurationNavigatorView.ID, false, IPageLayout.LEFT, 0.3f, editorArea);
		layout.addStandaloneView(ConfigurationPanelView.ID, false, IPageLayout.TOP, 0.7f, editorArea);
		layout.addStandaloneView("org.eclipse.ui.views.PropertySheet", false, IPageLayout.BOTTOM, 0.3f, editorArea);
	}
}
