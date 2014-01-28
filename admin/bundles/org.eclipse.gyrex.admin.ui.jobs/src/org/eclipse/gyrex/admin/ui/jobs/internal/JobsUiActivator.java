/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.lang.reflect.Field;
import java.net.URL;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Display;

import org.osgi.framework.BundleContext;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobsUiActivator extends BaseBundleActivator {

	/** SYMBOLIC_NAME */
	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.jobs";

	private static volatile JobsUiActivator instance;

	private static final String IMAGE_REGISTRY = JobsUiActivator.class.getName() + "#imageRegistry";

	private static final Logger LOG = LoggerFactory.getLogger(JobsUiActivator.class);

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static JobsUiActivator getInstance() {
		final JobsUiActivator activator = instance;
		if (null == activator)
			throw new IllegalArgumentException("inactive");
		return activator;
	}

	/**
	 * Creates a new instance.
	 */
	public JobsUiActivator() {
		super(SYMBOLIC_NAME);
	}

	private void createImageDescriptor(final String id, final ImageRegistry reg) {
		final URL url = FileLocator.find(getBundle(), new Path(JobsUiImages.ICON_PATH + id), null);
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
		ImageRegistry imageRegistry = (ImageRegistry) RWT.getUISession().getAttribute(IMAGE_REGISTRY);
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry(Display.getCurrent());
			initializeImageRegistry(imageRegistry);
			RWT.getUISession().setAttribute(IMAGE_REGISTRY, imageRegistry);
		}
		return imageRegistry;
	}

	private void initializeImageRegistry(final ImageRegistry reg) {
		final Field[] fields = JobsUiImages.class.getFields();
		for (final Field field : fields) {
			if (field.getName().startsWith("IMG_")) {
				try {
					createImageDescriptor((String) field.get(null), reg);
				} catch (final Exception e) {
					LOG.warn("Unable to initialize image ({}) in bundle {}. {}", field.getName(), SYMBOLIC_NAME, ExceptionUtils.getRootCauseMessage(e), e);
				}
			}
		}
	}

}
