/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jetty.servlet.DefaultServlet;

import org.osgi.service.component.ComponentContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * simple servlet for providing static resources
 */
public class StaticResourceServlet extends DefaultServlet implements IAdminServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	private ComponentContext context;
	private String resourceBase;
	private String bundlePath;

	public void activate(final ComponentContext context) {
		this.context = context;
		bundlePath = (String) context.getProperties().get("bundlePath");
		if (StringUtils.isBlank(bundlePath))
			throw new IllegalArgumentException("property 'bundlePath' not set");
	}

	public void deactivate(final ComponentContext context) {
		this.context = null;
	}

	@Override
	public String getInitParameter(final String name) {
		if ("resourceBase".equals(name))
			return resourceBase;
		return super.getInitParameter(name);
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		try {
			resourceBase = FileLocator.resolve(FileLocator.find(context.getBundleContext().getBundle(), new Path(bundlePath), null)).toExternalForm();
		} catch (final Exception e) {
			throw new ServletException(String.format("Unable to initialize resource base (%s). %s", ExceptionUtils.getRootCauseMessage(e)), e);
		}
		super.init(config);
	}
}
