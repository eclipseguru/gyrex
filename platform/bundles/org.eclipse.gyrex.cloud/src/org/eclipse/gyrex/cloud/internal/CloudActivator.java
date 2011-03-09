/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.cloud.internal.locking.ZooKeeperLockService;
import org.eclipse.gyrex.cloud.internal.queue.ZooKeeperQueueService;
import org.eclipse.gyrex.cloud.services.locking.ILockService;
import org.eclipse.gyrex.cloud.services.queue.IQueueService;
import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.util.NLS;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundle activator.
 */
public class CloudActivator extends BaseBundleActivator {

	static final Logger LOG = LoggerFactory.getLogger(CloudActivator.class);

	/** SYMBOLIC_NAME */
	public static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cloud";

	/** shared instance (only set when active) */
	private static final AtomicReference<CloudActivator> instanceRef = new AtomicReference<CloudActivator>();

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static CloudActivator getInstance() {
		final CloudActivator instance = instanceRef.get();
		if (instance == null) {
			throw new IllegalArgumentException(NLS.bind("Bundle {0} is not active.", SYMBOLIC_NAME));
		}
		return instance;
	}

	private final AtomicReference<IServiceProxy<Location>> instanceLocationServiceRef = new AtomicReference<IServiceProxy<Location>>();
	private final AtomicReference<IServiceProxy<IPreferencesService>> preferenceServiceRef = new AtomicReference<IServiceProxy<IPreferencesService>>();
	private final AtomicReference<IServiceProxy<EventAdmin>> eventAdminRef = new AtomicReference<IServiceProxy<EventAdmin>>();

	private volatile NodeEnvironmentImpl nodeEnvironment;

	private ServiceRegistration<IQueueService> queueServiceRegistration;

	private ServiceRegistration<ILockService> lockServiceRegistration;

	/**
	 * Creates a new instance.
	 */
	public CloudActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instanceRef.set(this);

		// eager read server id in order to prevent activation in case of errors
		final String nodeId = new NodeInfo().getNodeId();
		LOG.info("Node id: {}", nodeId);

		// track services
		instanceLocationServiceRef.set(getServiceHelper().trackService(Location.class, context.createFilter(Location.INSTANCE_FILTER)));
		preferenceServiceRef.set(getServiceHelper().trackService(IPreferencesService.class));
		eventAdminRef.set(getServiceHelper().trackService(EventAdmin.class));

		// register node environment
		nodeEnvironment = new NodeEnvironmentImpl();
		getServiceHelper().registerService(INodeEnvironment.SERVICE_NAME, nodeEnvironment, "Eclipse Gyrex", "Node environment service.", null, null);

		// register node with cloud
		CloudState.registerNode();
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		// unregister node with cloud
		CloudState.unregisterNode();

		// unset all other
		nodeEnvironment = null;
		lockServiceRegistration = null;
		queueServiceRegistration = null;

		instanceRef.set(null);
		instanceLocationServiceRef.set(null);
		preferenceServiceRef.set(null);
		eventAdminRef.set(null);
	}

	@Override
	protected Class getDebugOptions() {
		return CloudDebug.class;
	}

	public EventAdmin getEventAdmin() {
		final IServiceProxy<EventAdmin> serviceProxy = eventAdminRef.get();
		if (null == serviceProxy) {
			throw createBundleInactiveException();
		}
		return serviceProxy.getService();
	}

	/**
	 * Returns the nodeEnvironment.
	 * 
	 * @return the nodeEnvironment
	 */
	public INodeEnvironment getNodeEnvironment() {
		return nodeEnvironment;
	}

	public IPreferencesService getPreferenceService() {
		final IServiceProxy<IPreferencesService> serviceProxy = preferenceServiceRef.get();
		if (null == serviceProxy) {
			throw createBundleInactiveException();
		}
		return serviceProxy.getService();
	}

	void startCloudServices() {
		if (CloudDebug.debug) {
			LOG.debug("Starting cloud services");
		}
		lockServiceRegistration = getServiceHelper().registerService(ILockService.class, new ZooKeeperLockService(), "Eclipse Gyrex", "ZooKeeper base lock service.", null, null);
		queueServiceRegistration = getServiceHelper().registerService(IQueueService.class, new ZooKeeperQueueService(), "Eclipse Gyrex", "ZooKeeper base queue service.", null, null);
	}

	void stopCloudServices() {
		if (CloudDebug.debug) {
			LOG.debug("Stopping cloud services");
		}
		final ServiceRegistration<ILockService> lockServiceRegistration = this.lockServiceRegistration;
		if (lockServiceRegistration != null) {
			lockServiceRegistration.unregister();
			this.lockServiceRegistration = null;
		}
		final ServiceRegistration<IQueueService> queueServiceRegistration = this.queueServiceRegistration;
		if (queueServiceRegistration != null) {
			queueServiceRegistration.unregister();
			this.queueServiceRegistration = null;
		}
	}
}
