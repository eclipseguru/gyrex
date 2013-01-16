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
package org.eclipse.gyrex.admin.ui.internal.jetty;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

/**
 * {@link ServletHolder} that requires the admin role.
 */
public class AdminServletHolder extends ServletHolder {

	public static final String ADMIN_ROLE = "admin";

	private ServletMapping servletMapping;

	public AdminServletHolder(final Servlet servlet) {
		super(servlet);
		setRunAsRole(AdminServletHolder.ADMIN_ROLE);
	}

	public ServletMapping getServletMapping() {
		return servletMapping;
	}

	public void setServletMapping(final ServletMapping servletMapping) {
		this.servletMapping = servletMapping;
	}

}