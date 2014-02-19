/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.context.internal.di;

import java.lang.annotation.Annotation;

import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.internal.GyrexContextImpl;
import org.eclipse.gyrex.context.internal.IContextDisposalListener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import org.apache.commons.lang.StringUtils;

public class GyrexContextObjectSupplier extends BaseContextObjectSupplier {

	private final GyrexContextImpl contextImpl;

	public GyrexContextObjectSupplier(final GyrexContextImpl contextImpl) {
		this.contextImpl = contextImpl;
	}

	@Override
	protected void addDisposable(final IContextDisposalListener listener) {
		contextImpl.addDisposable(listener);
	}

	@Override
	protected Object getContextObject(final Class<?> key) {
		if (key == null)
			return null;

		if (IRuntimeContext.class.equals(key))
			// inject handle to the context
			return contextImpl.getHandle();

		// find a context object
		return contextImpl.get(key);
	}

	@Override
	protected Object getQualifiedObject(final Class<?> type, final Annotation annotation) {
		return getQualifiedObjectFromExtendedObjectSupplier(type, annotation, contextImpl.getHandle());
	}

	@Override
	protected IServiceProxy<?> trackService(final BundleContext bundleContext, final Class<?> serviceInterface, final String filter) throws InvalidSyntaxException {
		if (StringUtils.isNotBlank(filter))
			return contextImpl.getServiceLocator(bundleContext).trackService(serviceInterface, filter);

		return contextImpl.getServiceLocator(bundleContext).trackService(serviceInterface);
	}
}
