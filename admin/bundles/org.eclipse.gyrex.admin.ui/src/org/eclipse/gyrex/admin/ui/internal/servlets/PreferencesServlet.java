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
package org.eclipse.gyrex.admin.ui.internal.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import org.osgi.service.prefs.Preferences;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

/**
 *
 */
public class PreferencesServlet extends HttpServlet {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		final IPreferencesService preferencesService = AdminUiActivator.getInstance().getService(IPreferencesService.class);
		final IEclipsePreferences node = preferencesService.getRootNode();
		final Preferences preferencesNode = node.node(StringUtils.trimToEmpty(req.getPathInfo()));

		resp.setCharacterEncoding(CharEncoding.UTF_8);
		resp.setContentType("text/plain");

		try {
			dumpTree(0, preferencesNode, resp.getWriter());
		} catch (final Exception e) {
			throw new ServletException(e);
		}

		resp.flushBuffer();
	}

	private void dumpTree(final int indent, final Preferences node, final PrintWriter writer) throws Exception {
		printNodeInfo(indent, node, writer);
		final String[] children = node.childrenNames();
		for (final String child : children) {
			dumpTree(indent + 1, node.node(child), writer);
		}
	}

	private void printNodeInfo(final int indent, final Preferences node, final PrintWriter writer) throws Exception {
		final StrBuilder spaces = new StrBuilder();
		for (int i = 0; i < indent; i++) {
			spaces.append(" ");
		}
		writer.println(spaces.append(node.name()).append(" (").append(node.toString()).append(")").toString());
	}

}
