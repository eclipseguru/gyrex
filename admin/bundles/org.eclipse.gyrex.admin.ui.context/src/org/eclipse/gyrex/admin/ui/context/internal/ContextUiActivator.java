package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

import org.osgi.framework.BundleContext;

public class ContextUiActivator extends BaseBundleActivator {

	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.admin.ui.context";

	private static volatile ContextUiActivator instance;

	/**
	 * Returns the instance.
	 * 
	 * @return the instance
	 */
	public static ContextUiActivator getInstance() {
		final ContextUiActivator activator = instance;
		if (activator == null) {
			throw new IllegalStateException("inactive");
		}
		return activator;
	}

	/**
	 * Creates a new instance.
	 */
	public ContextUiActivator() {
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
