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
package org.eclipse.gyrex.admin.ui.persistence.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.eclipse.jface.resource.ImageRegistry;

import org.osgi.framework.BundleContext;

public class PersistenceUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.persistence";
	private static volatile PersistenceUiActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static PersistenceUiActivator getInstance() {
		final PersistenceUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalArgumentException("inactive");
		}
		return activator;
	}

	private final AtomicReference<ImageRegistry> imageRegistryRef = new AtomicReference<ImageRegistry>();

	/**
	 * Creates a new instance.
	 */
	public PersistenceUiActivator() {
		super(SYMBOLIC_NAME);
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

}
