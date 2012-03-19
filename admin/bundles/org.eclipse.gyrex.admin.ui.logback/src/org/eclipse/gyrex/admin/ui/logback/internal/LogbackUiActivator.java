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
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;

import org.osgi.framework.BundleContext;

public class LogbackUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.logback";
	private static LogbackUiActivator instance;

	public static LogbackUiActivator getInstance() {
		final LogbackUiActivator activator = instance;
		if (null == activator) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	private final AtomicReference<ImageRegistry> imageRegistryRef = new AtomicReference<ImageRegistry>();

	public LogbackUiActivator() {
		super(SYMBOLIC_NAME);
	}

	/**
	 * Creates the specified image descriptor and registers it
	 */
	private void createImageDescriptor(final String id, final ImageRegistry reg) {
		final URL url = FileLocator.find(getBundle(), new Path(LogbackUiImages.ICON_PATH + id), null);
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
		createImageDescriptor(LogbackUiImages.IMG_LOGGER, reg);
		createImageDescriptor(LogbackUiImages.IMG_APPENDER, reg);
		createImageDescriptor(LogbackUiImages.IMG_CONSOLE_APPENDER, reg);
		createImageDescriptor(LogbackUiImages.IMG_SIFTING_APPENDER, reg);
	}
}
