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
package org.eclipse.gyrex.admin.ui.p2.internal;

import org.eclipse.jface.resource.ImageDescriptor;

public class P2UiImages {

	// bundle-relative icon path
	public final static String ICON_PATH = "$nl$/icons/"; //$NON-NLS-1$

	//objects
	public final static String IMG_ARTIFACT_REPOSITORY = "obj/artifact_repo_obj.gif"; //$NON-NLS-1$
	public final static String IMG_METADATA_REPOSITORY = "obj/metadata_repo_obj.gif"; //$NON-NLS-1$
	public final static String IMG_IU = "obj/iu_obj.gif"; //$NON-NLS-1$
	public final static String IMG_DISABLED_IU = "obj/iu_disabled_obj.gif"; //$NON-NLS-1$
	public final static String IMG_UPDATED_IU = "obj/iu_update_obj.gif"; //$NON-NLS-1$
	public final static String IMG_PATCH_IU = "obj/iu_patch_obj.gif"; //$NON-NLS-1$
	public final static String IMG_DISABLED_PATCH_IU = "obj/iu_disabled_patch_obj.gif"; //$NON-NLS-1$
	public final static String IMG_PROFILE = "obj/profile_obj.gif"; //$NON-NLS-1$
	public final static String IMG_CATEGORY = "obj/category_obj.gif"; //$NON-NLS-1$
	public final static String IMG_VIEW_MENU = "obj/view_menu.gif";
	public final static String IMG_SEPARATOR = "obj/separator.gif";

	/**
	 * Returns the image descriptor for the given image ID. Returns
	 * <code>null</code> if there is no such image.
	 * 
	 * @param id
	 *            the identifier for the image to retrieve
	 * @return the image descriptor associated with the given ID
	 */
	public static ImageDescriptor getImageDescriptor(final String id) {
		return P2UiActivator.getInstance().getImageRegistry().getDescriptor(id);
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
