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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.resources.ResourceLoader;

import org.osgi.framework.Bundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

public class AdminApplicationConfiguration implements ApplicationConfiguration {

	private static String readBundleResource(final String resourceName, final String charset) {
		final URL entry = AdminUiActivator.getInstance().getBundle().getEntry(resourceName);
		if (entry == null)
			throw new IllegalStateException(String.format("Bundle resource '%s' not available!", resourceName));
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
		application.addResource("img/gyrex/eclipse.ico", new ResourceLoader() {
			@Override
			public InputStream getResourceAsStream(final String resourceName) throws IOException {
				return FileLocator.openStream(AdminUiActivator.getInstance().getBundle(), new Path("img/gyrex/eclipse.ico"), false);
			}
		});
	}

	Bundle getBundle() {
		return AdminUiActivator.getInstance().getBundle();
	}

}
