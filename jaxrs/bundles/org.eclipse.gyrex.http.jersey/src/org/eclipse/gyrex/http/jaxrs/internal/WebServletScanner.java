/**
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.http.jaxrs.internal;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.spi.scanning.ScannerException;
import com.sun.jersey.spi.container.ReloadListener;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;

/**
 * A scanner which scans a bundle for classes annotated with WebServlet.
 */
public class WebServletScanner implements ReloadListener {

	private static final Logger LOG = LoggerFactory.getLogger(WebServletScanner.class);

	private final Bundle bundle;

	private Set<Class<? extends HttpServlet>> classes;

	/**
	 * Creates a new instance.
	 */
	public WebServletScanner(final Bundle bundle) {
		this.bundle = bundle;
		scan();
	}

	public Set<Class<? extends HttpServlet>> getClasses() {
		if (classes == null) {
			classes = new HashSet<>();
		}
		return classes;
	}

	@Override
	public void onReload() {
		getClasses().clear();
		scan();
	}

	@SuppressWarnings("unchecked")
	private void scan() {
		if (JaxRsDebug.resourceDiscovery) {
			LOG.debug("Scanning bundle '{}' for annotated classes.", bundle);
		}

		final BundleWiring bundleWiring = bundle.adapt(BundleWiring.class);
		if (null == bundleWiring) {
			throw new ScannerException(String.format("No wiring available for bundle '%s'", bundle));
		}

		final ClassLoader loader = bundleWiring.getClassLoader();
		if (null == loader) {
			throw new ScannerException(String.format("No class loader available for bundle '%s'", bundle));
		}

		final AnnotationScannerListener scannerListener = new AnnotationScannerListener(loader, WebServlet.class);
		new BundleScanner(bundle, bundleWiring, loader).scan(scannerListener);

		final Set<Class<?>> annotatedClasses = scannerListener.getAnnotatedClasses();
		if (annotatedClasses.isEmpty()) {
			LOG.warn("No JAX-RS annotated classed found in bundle '{}'.", bundle);
		} else {
			for (final Class<?> annotatedClass : annotatedClasses) {
				if (annotatedClass.isAnnotationPresent(WebServlet.class) && HttpServlet.class.isAssignableFrom(annotatedClass)) {
					if (JaxRsDebug.resourceDiscovery) {
						LOG.debug("Found servlet: {}", annotatedClass.getName());
					}
					getClasses().add((Class<? extends HttpServlet>) annotatedClass);
				} else if (JaxRsDebug.resourceDiscovery) {
					LOG.debug("Not a HttpServlet: {}", annotatedClass.getName());
				}
			}
		}
	}
}
