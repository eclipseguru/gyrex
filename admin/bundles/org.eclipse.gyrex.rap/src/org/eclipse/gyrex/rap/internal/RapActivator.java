/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.internal;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.StatusHandler;

import org.osgi.framework.BundleContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The activator.
 */
public class RapActivator extends BaseBundleActivator {
	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(final String path) {
		return ImageDescriptor.createFromURL(FileLocator.find(getInstance().getBundle(), new Path(path), null));
	}

	/**
	 * Returns the instance.
	 *
	 * @return the instance
	 */
	public static RapActivator getInstance() {
		final RapActivator activator = instance;
		checkState(activator != null, "The bundle org.eclipse.gyrex.rap is inactive!");
		return activator;
	}

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.rap";

	private static RapActivator instance;

	public RapActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
		setupJFacePolicy();
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	private void setupJFacePolicy() {
		Policy.setLog(new ILogger() {
			final Logger LOG = LoggerFactory.getLogger("org.eclipse.gyrex.rap.JFaceLog");

			@Override
			public void log(final IStatus status) {
				if (status.matches(IStatus.CANCEL) || status.matches(IStatus.ERROR)) {
					LOG.error(status.getMessage(), status.getException());
				} else if (status.matches(IStatus.WARNING)) {
					LOG.warn(status.getMessage(), status.getException());
				} else {
					LOG.info(status.getMessage(), status.getException());
				}
			}
		});
		final StatusHandler defaultStatusHandler = Policy.getStatusHandler();
		Policy.setStatusHandler(new StatusHandler() {

			@Override
			public void show(final IStatus status, final String title) {
				Policy.getLog().log(status);
				defaultStatusHandler.show(status, title);
			}
		});
	}

}
