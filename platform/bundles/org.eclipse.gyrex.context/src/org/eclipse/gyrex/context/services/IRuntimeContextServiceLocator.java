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
package org.eclipse.gyrex.context.services;

import org.eclipse.gyrex.common.services.IServiceProxy;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A service locator may be used to locate OSGi services in a context dependent
 * way.
 * <p>
 * Easily put, this is a centrally implemented context specific service ranking
 * or filtering mechanism. A caller typically just wants an OSGi service.
 * However, multiple implementations might be available at runtime. Not all
 * contexts might be privileged to access all services. The service locator
 * makes this <em>configurable</em> at runtime.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRuntimeContextServiceLocator {

	/**
	 * Tracks a context specific OSGi service.
	 * <p>
	 * This is a convenience method which works similar to
	 * {@link ServiceTracker} for consuming an OSGi service. The tracker is kept
	 * open as long as the bundle a service locator has been request for is not
	 * stopped.
	 * </p>
	 * <p>
	 * The service tracker is made available to the caller through a service
	 * proxy. The service proxy ensures access to the service object in a
	 * convenient way.
	 * </p>
	 * 
	 * @param <T>
	 *            the service interface
	 * @param serviceInterface
	 *            the service interface class
	 * @return the service proxy object
	 * @throws IllegalArgumentException
	 *             if the service interface is <code>null</code>
	 */
	public <T> IServiceProxy<T> trackService(final Class<T> serviceInterface) throws IllegalArgumentException;

	/**
	 * Tracks an OSGi service matching the specified filter in addition to any
	 * context specific filter.
	 * <p>
	 * This is a convenience method which installs a {@link ServiceTracker} for
	 * consuming an OSGi service. The tracker is kept open as long as the bundle
	 * a service locator has been request for is not stopped.
	 * </p>
	 * <p>
	 * A filter can be specified to further limit the available services which
	 * should be tracked. It will be combined with any configured context
	 * specific filter so that the specified filter as well as the context
	 * specific filter must match. In case no context specific filter is
	 * configured, a default filter will be used that will filter based on
	 * <code>objectClass=&lt;serviceInterface&gt;</code> property. Thus, the
	 * additional filter does not need to include such a condition.
	 * </p>
	 * <p>
	 * The service tracker is made available to the caller through a service
	 * proxy. The service proxy provides access to the service object in a
	 * convenient way.
	 * </p>
	 * 
	 * @param <T>
	 *            the service interface
	 * @param serviceInterface
	 *            the service interface class
	 * @param additionalFilter
	 *            the additional filter for tracking a service
	 * @return the service proxy object
	 * @throws IllegalArgumentException
	 *             if the service interface is <code>null</code> or the
	 *             specified filter is invalid
	 */
	public <T> IServiceProxy<T> trackService(final Class<T> serviceInterface, final String additionalFilter) throws InvalidSyntaxException, IllegalArgumentException;

}
