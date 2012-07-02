/*******************************************************************************
 * Copyright (c) 2002, 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Austin Riddle (Texas Center for Applied Technology) - migration to support
 *                  compatibility with varied upload widget implementations
 *    EclipseSource - ongoing development
 *    Gunnar Wagenknecht - copied from RAP Incubator
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.IServiceHandler;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

public final class FileUploadServiceHandler implements IServiceHandler {

	private static final String PARAMETER_TOKEN = "token";

	static final String SERVICE_HANDLER_ID = "org.eclipse.gyrex.admin.ui.fileupload";

	public static String getUrl(final String token) {
		final StringBuffer url = new StringBuffer();
		url.append(RWT.getRequest().getContextPath());
		url.append(RWT.getRequest().getServletPath());
		url.append("?");
		url.append(IServiceHandler.REQUEST_PARAM).append("=").append(SERVICE_HANDLER_ID);
		url.append("&");
		url.append(PARAMETER_TOKEN).append("=").append(token);
		final int relativeIndex = url.lastIndexOf("/");
		if (relativeIndex > -1) {
			url.delete(0, relativeIndex + 1);
		}
		return RWT.getResponse().encodeURL(url.toString());
	}

	public void service() throws IOException, ServletException {
		final HttpServletRequest request = RWT.getRequest();
		final HttpServletResponse response = RWT.getResponse();
		// TODO [rst] Revise: does this double security make it any more secure?
		// Ignore requests to this service handler without a valid session for security reasons
		final boolean hasSession = request.getSession(false) != null;
		if (hasSession) {
			final String token = request.getParameter(PARAMETER_TOKEN);
			final FileUploadHandler registeredHandler = FileUploadHandlerStore.getInstance().getHandler(token);
			if (registeredHandler == null) {
				final String message = "Invalid or missing token";
				response.sendError(HttpServletResponse.SC_FORBIDDEN, message);
			} else if (!"POST".equals(request.getMethod().toUpperCase())) {
				final String message = "Only POST requests allowed";
				response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, message);
			} else if (!ServletFileUpload.isMultipartContent(request)) {
				final String message = "Content must be in multipart type";
				response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, message);
			} else {
				final FileUploadProcessor processor = new FileUploadProcessor(registeredHandler);
				processor.handleFileUpload(request, response);
			}
		}
	}
}
