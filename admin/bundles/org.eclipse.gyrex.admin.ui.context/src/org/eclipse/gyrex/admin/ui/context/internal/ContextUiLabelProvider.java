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
package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.context.internal.registry.ContextDefinition;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 *
 */
public class ContextUiLabelProvider extends LabelProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private ResourceManager manager;

	@Override
	public void dispose() {
		if (null != manager) {
			manager.dispose();
			manager = null;
		}
		super.dispose();
	}

	@Override
	public Image getImage(final Object element) {
		final ImageDescriptor descriptor = getImageDescriptor(element);
		if (descriptor == null) {
			return null;
		}
		return getResourceManager().createImage(descriptor);
	}

	private ImageDescriptor getImageDescriptor(final Object element) {
		return null;
	}

	private ResourceManager getResourceManager() {
		if (null == manager) {
			manager = new LocalResourceManager(JFaceResources.getResources());
		}
		return manager;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof ContextDefinition) {
			final ContextDefinition contextDefinition = (ContextDefinition) element;
			return contextDefinition.getName() + " (" + contextDefinition.getPath().toString() + ")";
		}

		return super.getText(element);
	}
}
