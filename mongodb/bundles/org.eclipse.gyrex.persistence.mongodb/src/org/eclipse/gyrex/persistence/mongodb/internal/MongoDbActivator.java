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
package org.eclipse.gyrex.persistence.mongodb.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.osgi.framework.BundleContext;

public class MongoDbActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.persistence.mongodb";

	private static MongoDbActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static MongoDbActivator getInstance() {
		final MongoDbActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	private MongoDbRegistry registry;

	/**
	 * Creates a new instance.
	 */
	public MongoDbActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;

		registry = new MongoDbRegistry();
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;

		registry.stop();
		registry = null;
	}

	@Override
	protected Class getDebugOptions() {
		return MongoDbDebug.class;
	}

	/**
	 * Returns the registry.
	 * 
	 * @return the registry
	 */
	public MongoDbRegistry getRegistry() {
		final MongoDbRegistry mongoDbRegistry = registry;
		if (null == mongoDbRegistry) {
			throw createBundleInactiveException();
		}
		return mongoDbRegistry;
	}
}
