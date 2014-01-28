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
 *     Peter Grube - add new separator image
 */
package org.eclipse.gyrex.admin.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class AdminUiImages {

	// bundle-relative icon path
	public final static String ICON_PATH = "$nl$/icons/"; //$NON-NLS-1$

	//objects
	public final static String IMG_VIEW_MENU = "obj/view_menu.gif";
	public final static String IMG_SEPARATOR = "obj/separator.gif";

	private static void createImageDescriptor(final String id, final ImageRegistry reg) {
		final URL url = FileLocator.find(AdminUiActivator.getInstance().getBundle(), new Path(ICON_PATH + id), null);
		reg.put(id, ImageDescriptor.createFromURL(url));
	}

	/**
	 * Returns the image for the given image ID. Returns <code>null</code> if
	 * there is no such image.
	 * 
	 * @param id
	 *            the identifier for the image to retrieve
	 * @return the image associated with the given ID. This image is managed in
	 *         an image registry and should not be freed by the client.
	 */
	public static Image getImage(final String id) {
		return AdminUiActivator.getInstance().getImageRegistry().get(id);
	}

	/**
	 * Returns the image descriptor for the given image ID. Returns
	 * <code>null</code> if there is no such image.
	 * 
	 * @param id
	 *            the identifier for the image to retrieve
	 * @return the image descriptor associated with the given ID
	 */
	public static ImageDescriptor getImageDescriptor(final String id) {
		return AdminUiActivator.getInstance().getImageRegistry().getDescriptor(id);
	}

	/**
	 * @param imageRegistry
	 */
	static void initializeImageRegistry(final ImageRegistry imageRegistry) {
		createImageDescriptor(IMG_VIEW_MENU, imageRegistry);
		createImageDescriptor(IMG_SEPARATOR, imageRegistry);
	}

}
