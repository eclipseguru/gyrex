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
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;

import org.osgi.framework.BundleContext;

public class P2UiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.p2";
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

	private final AtomicReference<ImageRegistry> imageRegistryRef = new AtomicReference<ImageRegistry>();

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

		final ImageRegistry imageRegistry = imageRegistryRef.getAndSet(null);
		if (null != imageRegistry) {
			imageRegistry.dispose();
		}
	}

	@Override
	protected Class getDebugOptions() {
		return P2UiDebug.class;
	}

	public ImageRegistry getImageRegistry() {
		final ImageRegistry imageRegistry = imageRegistryRef.get();
		if (null != imageRegistry) {
			return imageRegistry;
		}
		final ImageRegistry newRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
		if (imageRegistryRef.compareAndSet(null, newRegistry)) {
			initializeImageRegistry(newRegistry);
		}
		return imageRegistryRef.get();
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
	}
}
