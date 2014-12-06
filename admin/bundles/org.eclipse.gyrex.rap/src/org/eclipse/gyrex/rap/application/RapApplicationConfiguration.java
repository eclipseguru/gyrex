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
package org.eclipse.gyrex.rap.application;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.rap.internal.RapActivator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ResourceLoader;

import org.osgi.framework.Bundle;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for a Gyrex RAP application.
 * <p>
 * Clients my extend this class and customize it to suite their needs.
 * </p>
 */
public abstract class RapApplicationConfiguration implements ApplicationConfiguration {

	/**
	 * A {@link ResourceLoader} implementation which loads resources from within
	 * OSGi bundles.
	 */
	protected static final class BundleResourceLoader implements ResourceLoader {

		private final Bundle bundle;
		private final IPath basePath;

		/**
		 * Creates a new instance.
		 *
		 * @param bundle
		 *            the bundle
		 */
		public BundleResourceLoader(final Bundle bundle) {
			this(bundle, Path.ROOT);
		}

		/**
		 * Creates a new instance.
		 *
		 * @param bundle
		 *            the bundle
		 * @param basePath
		 *            the base path within the bundle (from the bundle root)
		 */
		public BundleResourceLoader(final Bundle bundle, final IPath basePath) {
			checkArgument(bundle != null, "bundle must not be null");
			checkArgument(basePath != null, "basePath must not be null");
			this.bundle = bundle;
			this.basePath = basePath;
		}

		@Override
		public InputStream getResourceAsStream(final String resourceName) throws IOException {
			if (checkNotNull(resourceName).indexOf("../") > -1) {
				LOG.debug("Detected illegal '..' in resource name '{}'.", resourceName);
				return null;
			}
			return FileLocator.openStream(bundle, basePath.append(resourceName), true);
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(RapApplicationConfiguration.BundleResourceLoader.class);

	@Override
	public void configure(final Application application) {
		final BundleResourceLoader webresourcesLoader = new BundleResourceLoader(getBundle(), new Path("webresources"));

		application.addEntryPoint(getEntryPointPath(), new EntryPointFactory() {
			@Override
			public EntryPoint create() {
				return createEntryPoint();
			}

		}, getEntryPointProperties());

		application.addStyleSheet(RWT.DEFAULT_THEME_ID, "theme/style.css", webresourcesLoader);
		application.addResource("images/loading.gif", webresourcesLoader);
	}

	/**
	 * Creates a ready to use entry point for this application.
	 *
	 * @return the entry point
	 */
	protected abstract RapApplicationEntryPoint createEntryPoint();

	private Bundle getBundle() {
		return RapActivator.getInstance().getBundle();
	}

	/**
	 * Returns the entry point path.
	 * <p>
	 * A valid path must start with a slash ('/') and must not contain any other
	 * slashes. The servlet path &quot;/&quot; denotes the root path. Nested
	 * paths (e.g. &quot;/path/subpath&quot;) are currently not supported.
	 * </p>
	 * <p>
	 * The default implementation returns a slash "/". Subclasses may override
	 * in case they want to customize the path.
	 * </p>
	 *
	 * @return the entry point path
	 * @see Application#addEntryPoint(String, Class, Map)
	 */
	protected String getEntryPointPath() {
		return "/";
	}

	/**
	 * Returns the branding properties to be used for the entry point.
	 *
	 * @return the branding properties.
	 */
	protected Map<String, String> getEntryPointProperties() {
		final Map<String, String> brandingProps = new HashMap<String, String>(4);
		brandingProps.put(WebClient.PAGE_TITLE, getPageTitle());
		brandingProps.put(WebClient.BODY_HTML, readBundleResource("webresources/static/body.html", CharEncoding.UTF_8));
		brandingProps.put(WebClient.HEAD_HTML, "<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,800,600' rel='stylesheet' type='text/css'>");
		return brandingProps;
	}

	/**
	 * Returns the page title.
	 * <p>
	 * Subclasses should override and return a meaningful footer text.
	 * </p>
	 * <p>
	 * Note, subsequent invocations are expected to return the same text.
	 * Dynamic changes to the page are not supported.
	 * </p>
	 *
	 * @return the page title
	 * @see WebClient#PAGE_TITLE
	 */
	protected String getPageTitle() {
		return "Gyrex RAP Application";
	}

	private String readBundleResource(final String resourceName, final String charset) {
		final URL entry = getBundle().getEntry(resourceName);
		if (entry == null)
			throw new IllegalStateException(String.format("Bundle resource '%s' not available!", resourceName));
		try (InputStream in = entry.openStream()) {
			return IOUtils.toString(in, charset);
		} catch (final IOException e) {
			throw new IllegalStateException(String.format("Unable to read bundle resource '%s': %s", resourceName, e.getMessage()), e);
		}
	}
}