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
package org.eclipse.gyrex.admin.ui.internal.application;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.application.Application;
import org.eclipse.rwt.application.ApplicationConfiguration;
import org.eclipse.rwt.client.WebClient;
import org.eclipse.rwt.resources.IResource;
import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;

import org.osgi.framework.Bundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

public class AdminApplicationConfiguration implements ApplicationConfiguration {
	/**
	 *
	 */
	private static final class BundleImage implements IResource {
		/** resourceName */
		private final String resourceName;

		/**
		 * Creates a new instance.
		 * 
		 * @param resourceName
		 */
		private BundleImage(final String resourceName) {
			this.resourceName = resourceName;
		}

		@Override
		public String getCharset() {
			return null;
		}

		@Override
		public ClassLoader getLoader() {
			return AdminUiActivator.class.getClassLoader();
		}

		@Override
		public String getLocation() {
			return resourceName;
		}

		@Override
		public RegisterOptions getOptions() {
			return RegisterOptions.NONE;
		}

		@Override
		public boolean isExternal() {
			return false;
		}

		@Override
		public boolean isJSLibrary() {
			return false;
		}
	}

	private static String readBundleResource(final String resourceName, final String charset) {
		final URL entry = AdminUiActivator.getInstance().getBundle().getEntry(resourceName);
		if (entry == null) {
			throw new IllegalStateException(String.format("Bundle resource '%s' not available!", resourceName));
		}
		InputStream in = null;
		try {
			in = entry.openStream();
			return IOUtils.toString(in, charset);
		} catch (final IOException e) {
			throw new IllegalStateException(String.format("Unable to read bundle resource '%s': %s", resourceName, e.getMessage()));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@Override
	public void configure(final Application application) {
		final Map<String, String> brandingProps = new HashMap<String, String>(4);
		brandingProps.put(WebClient.PAGE_TITLE, "Gyrex Admin");
		brandingProps.put(WebClient.BODY_HTML, readBundleResource("html/body.html", CharEncoding.UTF_8));
		brandingProps.put(WebClient.FAVICON, "img/gyrex/eclipse.ico");
		application.addEntryPoint("/admin", AdminApplication.class, brandingProps);
		application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/admin.css");
		application.addResource(new BundleImage("img/gyrex/eclipse.ico"));
	}

	Bundle getBundle() {
		return AdminUiActivator.getInstance().getBundle();
	}

}
