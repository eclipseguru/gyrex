/**
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.jface.resource.ImageDescriptor;

public class LogbackUiImages {

	// bundle-relative icon path
	public final static String ICON_PATH = "$nl$/icons/"; //$NON-NLS-1$

	//objects
	public final static String IMG_CONSOLE_APPENDER = "console.gif"; //$NON-NLS-1$
	public final static String IMG_SIFTING_APPENDER = "hierarchical.gif"; //$NON-NLS-1$
	public final static String IMG_APPENDER = "flat.gif"; //$NON-NLS-1$
	public final static String IMG_LOGGER = "category.gif"; //$NON-NLS-1$

	/**
	 * Returns the image descriptor for the given image ID. Returns
	 * <code>null</code> if there is no such image.
	 * 
	 * @param id
	 *            the identifier for the image to retrieve
	 * @return the image descriptor associated with the given ID
	 */
	public static ImageDescriptor getImageDescriptor(final String id) {
		return LogbackUiActivator.getInstance().getImageRegistry().getDescriptor(id);
	}

//	/**
//	 * Returns the image for the given image ID. Returns <code>null</code> if
//	 * there is no such image.
//	 *
//	 * @param id
//	 *            the identifier for the image to retrieve
//	 * @return the image associated with the given ID. This image is managed in
//	 *         an image registry and should not be freed by the client.
//	 */
//	public static Image getImage(String id) {
//		return P2UiActivator.getInstance().getImageRegistry().get(id);
//	}

}
