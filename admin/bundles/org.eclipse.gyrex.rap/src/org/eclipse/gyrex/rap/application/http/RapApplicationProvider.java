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
package org.eclipse.gyrex.rap.application.http;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.http.application.Application;
import org.eclipse.gyrex.http.application.provider.ApplicationProvider;
import org.eclipse.gyrex.rap.application.RapApplicationConfiguration;

/**
 * Base class for an {@link ApplicationProvider} providing RAP applications.
 * <p>
 * In order to provide a RAP application as a Gyrex HTTP applicarion, this class
 * needs to be subclasses and registered as an {@link ApplicationProvider}.
 * </p>
 */
public abstract class RapApplicationProvider extends ApplicationProvider {

	/**
	 * Creates a new provider instance allowing extenders to initialize the
	 * provider identifier by there own.
	 * <p>
	 * When this constructor is used callers must ensure that
	 * {@link #setId(String)} is called within object creation at some point
	 * before the provider is made available as an OSGi service to the Gyrex
	 * runtime.
	 * </p>
	 */
	protected RapApplicationProvider() {
		super();
	}

	/**
	 * Creates a new provider instance using the specified id.
	 * <p>
	 * Invoking this constructor initialized the id using {@link #setId(String)}
	 * with the specified id.
	 * </p>
	 *
	 * @param id
	 *            the provider id
	 * @see #setId(String)
	 */
	protected RapApplicationProvider(final String id) {
		super(id);
	}

	@Override
	public Application createApplication(final String applicationId, final IRuntimeContext context) throws Exception {
		return new RapApplication(applicationId, context, getApplicationConfiguration(applicationId, context));
	}

	/**
	 * Returns the RAP application configuration.
	 * <p>
	 * Subclasses must implement and return a
	 * {@link RapApplicationConfiguration} object which will be used for
	 * initializing the {@link RapApplication}.
	 * </p>
	 *
	 * @param applicationId
	 *            the application id
	 * @param context
	 *            the runtime context
	 * @return the application configuration
	 */
	protected abstract RapApplicationConfiguration getApplicationConfiguration(String applicationId, IRuntimeContext context);

}
