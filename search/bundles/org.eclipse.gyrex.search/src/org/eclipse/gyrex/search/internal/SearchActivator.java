/*******************************************************************************
 * Copyright (c) 2008, 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.search.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.osgi.framework.BundleContext;

public class SearchActivator extends BaseBundleActivator {

	/** BSN */
	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.search";

	private static final AtomicReference<SearchActivator> instance = new AtomicReference<SearchActivator>();

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static SearchActivator getInstance() {
		final SearchActivator modelActivator = instance.get();
		if (null == modelActivator) {
			throw new IllegalStateException("inactive");
		}
		return modelActivator;
	}

	/**
	 * Creates a new instance.
	 */
	public SearchActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance.set(this);
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance.set(null);
	}

	@Override
	protected Class getDebugOptions() {
		return SearchDebug.class;
	}
}
