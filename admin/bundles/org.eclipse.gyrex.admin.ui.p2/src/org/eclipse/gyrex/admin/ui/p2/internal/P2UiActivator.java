/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Peter Grube - add new image and image registry
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.net.URL;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;

import org.osgi.framework.BundleContext;

public class P2UiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.p2";
	private static final String IMAGE_REGISTRY = P2UiActivator.class.getName() + "#imageRegistry";
	private static volatile P2UiActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static P2UiActivator getInstance() {
		final P2UiActivator activator = instance;
		if (activator == null) {
			throw new IllegalArgumentException("inactive");
		}
		return activator;
	}

	/**
	 * Creates a new instance.
	 */
	public P2UiActivator() {
		super(SYMBOLIC_NAME);
	}

	/**
	 * Creates the specified image descriptor and registers it
	 */
	private void createImageDescriptor(final String id, final ImageRegistry reg) {
		final URL url = FileLocator.find(getBundle(), new Path(P2UiImages.ICON_PATH + id), null);
		final ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		reg.put(id, desc);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	@Override
	protected Class getDebugOptions() {
		return P2UiDebug.class;
	}

	public ImageRegistry getImageRegistry() {
		// ImageRegistry must be session scoped in RAP
		ImageRegistry imageRegistry = (ImageRegistry) RWT.getSessionStore().getAttribute(IMAGE_REGISTRY);
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(Display.getCurrent());
			initializeImageRegistry(imageRegistry);
			RWT.getSessionStore().setAttribute(IMAGE_REGISTRY, imageRegistry);
		}
		return imageRegistry;
	}

	private void initializeImageRegistry(final ImageRegistry reg) {
		createImageDescriptor(P2UiImages.IMG_METADATA_REPOSITORY, reg);
		createImageDescriptor(P2UiImages.IMG_ARTIFACT_REPOSITORY, reg);
		createImageDescriptor(P2UiImages.IMG_IU, reg);
		createImageDescriptor(P2UiImages.IMG_DISABLED_IU, reg);
		createImageDescriptor(P2UiImages.IMG_UPDATED_IU, reg);
		createImageDescriptor(P2UiImages.IMG_PATCH_IU, reg);
		createImageDescriptor(P2UiImages.IMG_DISABLED_PATCH_IU, reg);
		createImageDescriptor(P2UiImages.IMG_CATEGORY, reg);
		createImageDescriptor(P2UiImages.IMG_PROFILE, reg);
		createImageDescriptor(P2UiImages.IMG_VIEW_MENU, reg);
		createImageDescriptor(P2UiImages.IMG_SEPARATOR, reg);
	}
}
