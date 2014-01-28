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
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.adapter;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An adapter for presenting elements in the Admin UI.
 */
public interface ImageAdapter {

	/**
	 * Returns an image descriptor to be used for displaying an object in the
	 * workbench. Returns <code>null</code> if there is no appropriate image.
	 * 
	 * @param object
	 *            The object to get an image descriptor for.
	 * @return ImageDescriptor
	 */
	public ImageDescriptor getImageDescriptor(Object object);
}
