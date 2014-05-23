/*******************************************************************************
 * Copyright (c) 2014 AGETO Service GmbH
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Andreas Mihm - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.eventbus.websocket.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.cloud.services.events.IEventReceiver;
import org.eclipse.gyrex.cloud.services.events.IEventTransport;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.monitoring.diagnostics.StatusTracker;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class WebsocketEventTransport implements IEventTransport {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEventTransport.class);

	private static final int DEFAULT_CLOUD_EVENT_PORT = 3111;

	private static volatile Server server;
	private int eventsPort;

	private String adminHost;

	private StatusTracker statusTracker;
	private IServiceProxy<ICloudManager> cloudManagerProxy;

	ConcurrentMap<String, CopyOnWriteArrayList<IEventReceiver>> eventReceiverListsByTopicId;

	private ClusterEventsClientsHandler clusterEventHandler;

	private EventBusWebSocket outgoingWebSocket;

	public WebsocketEventTransport() {
		eventReceiverListsByTopicId = new ConcurrentHashMap<>();
	}

	/*
	 * starts a separate jetty instance which provides the EventBus websocket
	 * URL and starts the ClusterEventHandler to establish the outgoing
	 * websocket connections to all other nodes in the cluster
	 * 
	 */
	public void activate(final ComponentContext context) {
		LOG.info("Activating WebsocketEventTransport.");
		startWebSocketServer(ClusterEventsActivator.getInstance().getBundle().getBundleContext());
		connectAllGyrexNodes();
		clusterEventHandler = new ClusterEventsClientsHandler(this);
	}

	private void addNonSslConnector(final Server server) {
		final HttpConfiguration httpConfiguration = new HttpConfiguration();
		httpConfiguration.setSendServerVersion(false);
		httpConfiguration.setSendDateHeader(false);

		final ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));

		connector.setPort(eventsPort);
		if (null != adminHost) {
			connector.setHost(adminHost);
		}
		connector.setIdleTimeout(60000);
		// TODO: (Jetty9?) connector.setLowResourcesConnections(20000);
		// TODO: (Jetty9?) connector.setLowResourcesMaxIdleTime(5000);
		// TODO: (Jetty9?) connector.setForwarded(true);

		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=356988 for an issue
		// with configuring the connector

		server.addConnector(connector);
	}

	private void connectAllGyrexNodes() {

		// connect to the other cluster nodes for event distribution
		// asynchronously
		final Job websocketConnectJob = new Job("Connect to other nodes for event distribution") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					clusterEventHandler.connectToAllClusterNodes();
					//ClusterEventHandler.getInstance().setEventTransportProxy(eventTransportProxy);
					return Status.OK_STATUS;
				} catch (final Exception e) {
					LOG.error("Failed to Connect to other nodes for event distribution.", e);
					return Status.CANCEL_STATUS;
				} finally {
					schedule(30000); // start again in 30 sec
				}
			}
		};
		websocketConnectJob.setSystem(true);
		websocketConnectJob.setPriority(Job.SHORT);
		websocketConnectJob.schedule();
	}

	public void deactivate(final ComponentContext context) {
		LOG.info("Deactivating WebsocketEventTransport.");

		stopJettyServer();

	}

	public void distributeEventToSubscribers(final String topicId, final EventMessage message) {
		final CopyOnWriteArrayList<IEventReceiver> receivers = eventReceiverListsByTopicId.get(topicId);
		if (receivers != null) {
			for (final IEventReceiver eventReceiver : receivers) {
				eventReceiver.receiveEvent(message);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cloud.services.events.IEventTransport#sendEvent(java.lang.String, org.eclipse.gyrex.cloud.services.events.EventMessage, java.util.Map)
	 */
	@Override
	public void sendEvent(final String topicId, final EventMessage eventMessage, final Map<String, ?> properties) {
		//clusterEventHandler.postEventFromNetwork needs to call this method

		// 1. distribute to subscribers
		distributeEventToSubscribers(topicId, eventMessage);
		// 2. distribute into network
		final TransportableEvent event = new TransportableEvent(eventMessage, topicId);
		event.setNodeId(cloudManagerProxy.getService().getLocalInfo().getNodeId());

		if (outgoingWebSocket != null) {
			outgoingWebSocket.sendEvent(event);
		}

	}

	/**
	 * Sets the outgoingWebSocket.
	 * 
	 * @param outgoingWebSocket
	 *            the outgoingWebSocket to set
	 */
	public void setOutgoingWebSocket(final EventBusWebSocket outgoingWebSocket) {
		this.outgoingWebSocket = outgoingWebSocket;
	}

	/**
	 * configures and starts the jetty instance with tow WebSocket URLs.
	 * <p>
	 * The url ws://<ip>:3111/eventbus/ is the url for the evntbus between the
	 * nodes
	 * <p>
	 * The url ws://<ip>:3111/eventbusobserver/ can be used from external
	 * monitoring tools to observer all events sent through te eventbus
	 */
	private void startJettyServer() {
		try {
			server = new Server();

			addNonSslConnector(server);

			// tweak server
			server.setStopAtShutdown(true);
			server.setStopTimeout(5000);

			// set thread pool
			// TODO: (Jetty9?) final QueuedThreadPool threadPool = new
			// QueuedThreadPool(5);
			// TODO: (Jetty9?) threadPool.setName("jetty-server-admin");
			// TODO: (Jetty9?) server.setThreadPool(threadPool);

			final HandlerCollection handlers = new HandlerCollection();

			// create context for eventbus, which is used between the nodes
			final WebSocketHandler wsHandler = new WebSocketHandler() {
				@Override
				public void configure(final WebSocketServletFactory factory) {
					factory.setCreator(new WebSocketCreator() {

						@Override
						public Object createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
							outgoingWebSocket = new EventBusWebSocket(WebsocketEventTransport.this);
							return outgoingWebSocket;
						}
					});
					factory.register(EventBusWebSocket.class);
				}

			};
			final ContextHandler context = new ContextHandler();
			context.setContextPath("/eventbus");
			context.setHandler(wsHandler);
			handlers.addHandler(context);

			// create context for eventbus, which is used between the nodes
//			final WebSocketHandler wsHandler1 = new WebSocketHandler() {
//				@Override
//				public void configure(final WebSocketServletFactory factory) {
//					factory.register(EventBusObserverWebSocket.class);
//				}
//
//			};
//			final ContextHandler context1 = new ContextHandler();
//			context1.setContextPath("/eventobserver");
//			context1.setHandler(wsHandler1);
//			handlers.setHandlers(new Handler[] { context1, context });
//
			server.setHandler(handlers);

			server.start();
			server.join();

		} catch (final Exception e) {
			throw new IllegalStateException("Error starting jetty for cluster event websocket", e);
		}
	}

	private void startWebSocketServer(final BundleContext context) {
		cloudManagerProxy = ClusterEventsActivator.getInstance().getServiceHelper().trackService(ICloudManager.class);
		if (cloudManagerProxy.getService() == null)
			return;

		try {
			// set to default admin port if null or not a number
			eventsPort = Integer.valueOf(context.getProperty("gyrex.event.websocket.port"));
		} catch (final NumberFormatException nfe) {
			eventsPort = DEFAULT_CLOUD_EVENT_PORT;
		}

		adminHost = context.getProperty("gyrex.admin.http.host");

		statusTracker = new StatusTracker(context);
		statusTracker.open();

		// start the admin server asynchronously
		final Job jettyStartJob = new Job("Start Cluster events websocket server") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					startJettyServer();
				} catch (final Exception e) {
					LOG.error("Failed to Start Cluster events websocket server.", e);
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		jettyStartJob.setSystem(true);
		jettyStartJob.setPriority(Job.LONG);
		jettyStartJob.schedule();

	}

	private void stopJettyServer() {

		statusTracker.close();
		statusTracker = null;

		try {

			server.stop();
			server = null;
		} catch (final Exception e) {
			throw new IllegalStateException("Error stopping jetty for admin ui", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cloud.services.events.IEventTransport#subscribeTopic(java.lang.String, org.eclipse.gyrex.cloud.services.events.IEventReceiver, java.util.Map)
	 */
	@Override
	public void subscribeTopic(final String topicId, final IEventReceiver receiver, final Map<String, ?> properties) {
		CopyOnWriteArrayList<IEventReceiver> eventReceiverList = eventReceiverListsByTopicId.get(topicId);
		if (eventReceiverList == null) {
			eventReceiverList = new CopyOnWriteArrayList<>();
			eventReceiverListsByTopicId.put(topicId, eventReceiverList);
		}
		eventReceiverList.add(receiver);
		// TODO what to do with the properties, for what do we need them

	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cloud.services.events.IEventTransport#unsubscribeTopic(java.lang.String, org.eclipse.gyrex.cloud.services.events.IEventReceiver, java.util.Map)
	 */
	@Override
	public void unsubscribeTopic(final String topicId, final IEventReceiver receiver, final Map<String, ?> properties) {
		final CopyOnWriteArrayList<IEventReceiver> eventReceiverList = eventReceiverListsByTopicId.get(topicId);
		if (eventReceiverList != null) {
			if (!eventReceiverList.remove(receiver))
				throw new IllegalStateException("cannot unsubscribe an eventreceiver, which hasn't being subsribed");
		} else
			throw new IllegalStateException("cannot unsubscribe an eventreceiver, which hasn't being subsribed");

	}

}
