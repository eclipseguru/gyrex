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
package org.eclipse.gyrex.context.di;

/**
 * A injector is used to inject data and services from a context into a domain
 * object.
 * <p>
 * It is currently based on the {@link org.eclipse.e4.core.di.IInjector Eclipse
 * dependency injection system}. However, none of this is exposed as API.
 * Therefore, clients may not rely on any behavior of the dependency injector
 * other than what should be supported by JSR-330 standard.
 * </p>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @see org.eclipse.e4.core.di.IInjector Eclipse dependency injection system for
 *      details of the injection algorithm that is used.
 */
@SuppressWarnings("restriction")
public interface IRuntimeContextInjector {

	/**
	 * Obtain an instance of the specified class and inject it with the context.
	 * <p>
	 * Class'es scope dictates if a new instance of the class will be created,
	 * or existing instance will be reused.
	 * </p>
	 * <p>
	 * <em>Please be aware!</em> The specified object will be tracked within the
	 * injector. Changes to the context will result in updates to the object. In
	 * order to <strong>release</strong> the object from the injector it must be
	 * explicitly {@link #uninject(Object) un-injected}.
	 * </p>
	 * 
	 * @param clazz
	 *            The class to be instantiated
	 * @return an instance of the specified class
	 * @throws org.eclipse.e4.core.di.InjectionException
	 *             if an exception occurred while performing this operation
	 * @see javax.inject.Scope
	 * @see javax.inject.Singleton
	 */
	<T> T make(Class<T> clazz) throws RuntimeException;

	/**
	 * Un-injects the context from the object.
	 * 
	 * @param object
	 *            The domain object previously injected with the context
	 * @throws org.eclipse.e4.core.di.InjectionException
	 *             if an exception occurred while performing this operation
	 */
	void uninject(Object object) throws RuntimeException;
}
