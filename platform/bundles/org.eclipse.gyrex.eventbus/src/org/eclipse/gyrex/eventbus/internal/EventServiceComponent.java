/**
 * Copyright (c) 2014 <company> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     <author> - initial API and implementation
 */
package org.eclipse.gyrex.eventbus.internal;

import static com.google.common.base.Preconditions.checkState;

import org.eclipse.gyrex.cloud.environment.INodeEnvironment;
import org.eclipse.gyrex.cloud.services.events.IEventTransport;
import org.eclipse.gyrex.common.services.BundleServiceHelper;
import org.eclipse.gyrex.eventbus.IEventBus;
import org.eclipse.gyrex.eventbus.ITopicBuilder;

import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi service component for {@link IEventBus}.
 */
public class EventServiceComponent implements IEventBus {

	private static final Logger LOG = LoggerFactory.getLogger(EventServiceComponent.class);

	private volatile EventService eventService;
	private BundleServiceHelper serviceHelper;

	public void activate(final ComponentContext context) {
		LOG.debug("Activating event bus ({})", this);
		checkState(serviceHelper == null, "duplicate activation");
		serviceHelper = new BundleServiceHelper(context.getBundleContext());
		final String nodeId = serviceHelper.trackService(INodeEnvironment.class).getService().getNodeId();
		eventService = new EventService(nodeId, serviceHelper.trackService(IEventTransport.class));
	}

	public void deactivate(final ComponentContext context) {
		LOG.debug("Deactivating event bus ({})", this);
		if (eventService != null) {
			eventService.dispose();
			eventService = null;
		}
		if (serviceHelper != null) {
			serviceHelper.dispose();
			serviceHelper = null;
		}
	}

	private EventService getEventService() {
		final EventService service = eventService;
		checkState(service != null, "inactive");
		return service;
	}

	@Override
	public ITopicBuilder getTopic(final String id) throws IllegalArgumentException, IllegalStateException {
		return getEventService().getTopic(id);
	}
}
