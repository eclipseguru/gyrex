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

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rwt.RWT;
import org.eclipse.ui.PlatformUI;

import org.osgi.framework.BundleContext;

public class LogbackUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.logback";
	private static final String IMAGE_REGISTRY = LogbackUiActivator.class.getName() + "#imageRegistry";
	private static LogbackUiActivator instance;

	public static LogbackUiActivator getInstance() {
		final LogbackUiActivator activator = instance;
		if (null == activator) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

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
	}

	public ImageRegistry getImageRegistry() {
		// ImageRegistry must be session scoped in RAP
		ImageRegistry imageRegistry = (ImageRegistry) RWT.getSessionStore().getAttribute(IMAGE_REGISTRY);
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
			initializeImageRegistry(imageRegistry);
			RWT.getSessionStore().setAttribute(IMAGE_REGISTRY, imageRegistry);
		}
		return imageRegistry;
	}

	private void initializeImageRegistry(final ImageRegistry reg) {
		createImageDescriptor(LogbackUiImages.IMG_LOGGER, reg);
		createImageDescriptor(LogbackUiImages.IMG_APPENDER, reg);
		createImageDescriptor(LogbackUiImages.IMG_CONSOLE_APPENDER, reg);
		createImageDescriptor(LogbackUiImages.IMG_SIFTING_APPENDER, reg);
	}
}
