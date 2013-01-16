/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal.zookeeper;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * ZooKeeper Perspective
 */
public class ZooKeeperPerspective implements IPerspectiveFactory {

	public static final String ID = "org.eclipse.gyrex.admin.ui.cloud.perspective.zookeeper";

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);

		final IFolderLayout navigationFolder = layout.createFolder("navigation", IPageLayout.LEFT, 0.3F, editorArea);
		navigationFolder.addView(ZooKeeperExplorer.ID);

		final IFolderLayout bottomFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.3F, editorArea);
		bottomFolder.addView("org.eclipse.ui.views.PropertySheet");
	}
}
