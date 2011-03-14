package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.osgi.framework.BundleContext;

public class HttpUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.http";

	private static volatile HttpUiActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static HttpUiActivator getInstance() {
		final HttpUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	/**
	 * Creates a new instance.
	 */
	public HttpUiActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}
}
