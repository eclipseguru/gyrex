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
package org.eclipse.gyrex.http.internal.httpservice;

import java.util.Dictionary;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.eclipse.gyrex.http.application.context.IApplicationContext;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

/**
 * Extension to {@link HttpService} implementing {@link ExtendedHttpService}.
 */
public class ExtendedHttpServiceImpl extends HttpServiceImpl implements ExtendedHttpService {

	public ExtendedHttpServiceImpl(final IApplicationContext context, final Bundle bundle, final org.osgi.framework.Filter registrationFilter) {
		super(context, bundle, registrationFilter);
	}

	@Override
	public void registerFilter(final String alias, final Filter filter, @SuppressWarnings("rawtypes") final Dictionary initparams, final HttpContext context) throws ServletException, NamespaceException {
		super.registerFilter(alias, filter, initparams, context);
	}

	@Override
	public void unregisterFilter(final Filter filter) {
		super.unregisterFilter(filter);
	}
}
