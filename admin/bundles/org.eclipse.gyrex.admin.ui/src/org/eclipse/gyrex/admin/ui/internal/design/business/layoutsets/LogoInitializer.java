/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Mike Tschierschke - Patch for Gyrex
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.design.business.layoutsets;

import org.eclipse.gyrex.admin.ui.internal.design.ILayoutSetConstants;

import org.eclipse.rap.ui.interactiondesign.layout.model.ILayoutSetInitializer;
import org.eclipse.rap.ui.interactiondesign.layout.model.LayoutSet;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;

public class LogoInitializer implements ILayoutSetInitializer {

	public void initializeLayoutSet(final LayoutSet layoutSet) {
		layoutSet.addImagePath(ILayoutSetConstants.LOGO, "img/gyrex/eclipse_gyrex.png"); //$NON-NLS-1$

		// positions
		final FormData fdLogo = new FormData();
		fdLogo.right = new FormAttachment(100, -75);
		fdLogo.top = new FormAttachment(0, 10);
		layoutSet.addPosition(ILayoutSetConstants.LOGO_POSITION, fdLogo);
	}
}
