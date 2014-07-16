/*******************************************************************************
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Konrad Schergaut - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.context.tests.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.internal.GyrexContextHandle;
import org.eclipse.gyrex.context.internal.GyrexContextImpl;
import org.eclipse.gyrex.context.internal.IContextDisposalListener;
import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class GyrexContextImplTest {

	private ContextRegistryImpl contextRegistry;

	@Before
	public void setUp() throws Exception {
		//use a modified registry for not depending on actual prefs node
		contextRegistry = new ContextRegistryImpl() {

			@Override
			public GyrexContextHandle getHandle(final IPath contextPath) {
				return new GyrexContextHandle(contextPath, this);
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		contextRegistry = null;
	}

	@Test
	public void testDispose() {
		final GyrexContextImpl contextToTest = new GyrexContextImpl(new Path("/test"), contextRegistry);

		final boolean[] disposableDisposed = new boolean[] { false };//java restriction forces using an array here...
		/*
		 * have a depending object for ensuring, that the
		 * dispose-delegate-mechanism is working.
		 */
		final IContextDisposalListener testableDisposal = new IContextDisposalListener() {

			@Override
			public void contextDisposed(final IRuntimeContext runtimeContext) {
				assertTrue(contextToTest.isDisposed());
				disposableDisposed[0] = true;
			}

		};
		contextToTest.addDisposable(testableDisposal);

		assertFalse(contextToTest.isDisposed());
		assertFalse(disposableDisposed[0]);

		//The actual method to test
		contextToTest.dispose();

		assertTrue(contextToTest.isDisposed());
		assertTrue(disposableDisposed[0]);
	}

}
