/**
 * Copyright (c) 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class JobsUiImages {

	// bundle-relative icon path
	public final static String ICON_PATH = "$nl$/icons/"; //$NON-NLS-1$

	//objects
	public static final String IMG_OBJ_SCHEDULE = "obj/schedule_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_SCHEDULE_DISABLED = "obj/schedule_disabled_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_ERROR_RESULT = "obj/error_result.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_WARN_RESULT = "obj/warn_result.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_ACTIVE = "obj/active.gif"; //$NON-NLS-1$
	public static final String IMG_OBJ_INACTIVE = "obj/inactive.gif"; //$NON-NLS-1$

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
		return JobsUiActivator.getInstance().getImageRegistry().get(id);
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
		return JobsUiActivator.getInstance().getImageRegistry().getDescriptor(id);
	}

}
