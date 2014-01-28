/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.servlets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.admin.ui.internal.jetty.AdminServletHolder;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracker for {@link IAdminServlet}.
 */
public class AdminServletTracker extends ServiceTracker<IAdminServlet, AdminServletHolder> {

	private static final Logger LOG = LoggerFactory.getLogger(AdminServletTracker.class);

	private final ServletContextHandler contextHandler;

	public AdminServletTracker(final BundleContext context, final ServletContextHandler contextHandler) {
		super(context, IAdminServlet.class, null);
		this.contextHandler = contextHandler;
	}

	@Override
	public AdminServletHolder addingService(final ServiceReference<IAdminServlet> reference) {
		final IAdminServlet servlet = context.getService(reference);
		if (servlet == null)
			return null;
		final AdminServletHolder holder = new AdminServletHolder(servlet);
		final Object alias = reference.getProperty("http.alias");
		if (alias instanceof String) {
			String pathSpec = (String) alias;
			// convert to path spec
			if (!StringUtils.endsWith(pathSpec, "/*")) {
				pathSpec = StringUtils.removeEnd(pathSpec, "/") + "/*";
			}

			try {
				// register servlet
				contextHandler.getServletHandler().addServlet(holder);

				// create, remember & register mapping
				final ServletMapping mapping = new ServletMapping();
				mapping.setPathSpec(pathSpec);
				mapping.setServletName(holder.getName());
				holder.setServletMapping(mapping);
				contextHandler.getServletHandler().addServletMapping(mapping);
			} catch (final Exception e) {
				LOG.error("Error registering contributed servlet {} ({}). {}", reference, pathSpec, ExceptionUtils.getRootCauseMessage(e), e);
			}
		}
		return holder;
	}

	@Override
	public void removedService(final ServiceReference<IAdminServlet> reference, final AdminServletHolder holder) {
		try {
			// remove mapping
			final ServletMapping[] mappings = contextHandler.getServletHandler().getServletMappings();
			final List<ServletMapping> newMappings = new ArrayList<>();
			for (final ServletMapping servletMapping : mappings) {
				if (servletMapping != holder.getServletMapping()) {
					newMappings.add(servletMapping);
				}
			}
			contextHandler.getServletHandler().setServletMappings(newMappings.toArray(new ServletMapping[newMappings.size()]));

			// remove servlet
			final ServletHolder[] holders = contextHandler.getServletHandler().getServlets();
			final List<ServletHolder> newHolders = new ArrayList<>();
			for (final ServletHolder servletHolder : holders) {
				if (holder != servletHolder) {
					newHolders.add(servletHolder);
				}
			}
			contextHandler.getServletHandler().setServlets(newHolders.toArray(new ServletHolder[newHolders.size()]));
		} finally {
			// unget servlet
			context.ungetService(reference);
		}
	}
}
