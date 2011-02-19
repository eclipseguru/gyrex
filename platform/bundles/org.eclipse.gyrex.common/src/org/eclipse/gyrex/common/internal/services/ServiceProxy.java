/*******************************************************************************
 * Copyright (c) 2008, 2009 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.common.internal.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.common.services.ServiceNotAvailableException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;

/**
 * {@link IServiceProxy} implementation
 */
public class ServiceProxy<T> implements IServiceProxy<T>, InvocationHandler {

	private final Class<T> serviceInterface;
	private final AtomicReference<ServiceTracker<T, T>> serviceTrackerRef = new AtomicReference<ServiceTracker<T, T>>();
	private final AtomicReference<BundleContext> bundleContextRef = new AtomicReference<BundleContext>();
	private volatile T dynamicProxy;

	/**
	 * Creates a new service proxy instance which will track the specified
	 * service.
	 * 
	 * @param bundleContext
	 *            the bundle context against which the tracking is done
	 * @param serviceInterface
	 *            the service interface to track
	 */
	public ServiceProxy(final BundleContext bundleContext, final Class<T> serviceInterface) {
		this.serviceInterface = serviceInterface;
		this.bundleContextRef.set(bundleContext);
		serviceTrackerRef.set(new ServiceTracker<T, T>(bundleContext, serviceInterface, null));
	}

	/**
	 * Creates a new service proxy instance which will track the specified
	 * service.
	 * 
	 * @param bundleContext
	 *            the bundle context against which the tracking is done
	 * @param serviceInterface
	 *            the service interface to track
	 * @param filter
	 *            the filter to use for the service tracker
	 */
	public ServiceProxy(final BundleContext bundleContext, final Class<T> serviceInterface, final Filter filter) {
		this.serviceInterface = serviceInterface;
		this.bundleContextRef.set(bundleContext);
		serviceTrackerRef.set(new ServiceTracker<T, T>(bundleContext, filter, null));
	}

	@SuppressWarnings("unchecked")
	private T createProxy() {
		final BundleContext bundleContext = bundleContextRef.get();
		if (null == bundleContext) {
			throw new IllegalStateException("inactive");
		}
		return (T) Proxy.newProxyInstance(new BundleDelegatingClassLoader(bundleContext.getBundle()), new Class[] { serviceInterface }, this);
	}

	@Override
	public void dispose() {
		bundleContextRef.set(null);
		final ServiceTracker tracker = serviceTrackerRef.getAndSet(null);
		if (null != tracker) {
			tracker.close();
		}
		dynamicProxy = null;
	}

	@Override
	public T getProxy() {
		if (null != dynamicProxy) {
			return dynamicProxy;
		}
		return dynamicProxy = createProxy();
	}

	@Override
	public T getService() throws ServiceNotAvailableException {
		final ServiceTracker<T, T> serviceTracker = serviceTrackerRef.get();
		if (null == serviceTracker) {
			throw new ServiceNotAvailableException(bundleContextRef.get(), serviceInterface.getName());
		}
		// ensure the tracker is open
		serviceTracker.open();

		final T service = serviceTracker.getService();
		if (null == service) {
			throw new ServiceNotAvailableException(bundleContextRef.get(), serviceInterface.getName());
		}
		return service;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		return method.invoke(getService(), args);
	}

	//	@Override
	//	public IServiceProxy<T> setTimeout(final long timeout, final TimeUnit unit) {
	//		if (null == dynamicProxy) {
	//			this.timeout = timeout;
	//			this.unit = unit;
	//		}
	//		return this;
	//	}
}
