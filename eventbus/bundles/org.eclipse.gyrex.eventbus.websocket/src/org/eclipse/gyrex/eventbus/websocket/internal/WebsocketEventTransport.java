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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;
import org.eclipse.gyrex.cloud.admin.INodeListener;
import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.cloud.services.events.IEventReceiver;
import org.eclipse.gyrex.cloud.services.events.IEventTransport;
import org.eclipse.gyrex.eventbus.websocket.internal.EventMessageReceiver.IEventMessageCallback;
import org.eclipse.gyrex.server.Platform;
import org.eclipse.gyrex.server.settings.SystemSetting;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@SuppressWarnings("restriction")
public class WebsocketEventTransport implements IEventTransport {

	private static final Logger LOG = LoggerFactory.getLogger(WebsocketEventTransport.class);

	private static final SystemSetting<Integer> eventsPort = SystemSetting.newIntegerSetting("gyrex.event.websocket.port", "Default port for web socket based event transport.").usingDefault(3111).create();
	private static final AtomicReference<Server> serverRef = new AtomicReference<Server>();
	private static final AtomicReference<WebSocketClient> clientRef = new AtomicReference<WebSocketClient>();

	private final ConcurrentMap<String, CopyOnWriteArrayList<IEventReceiver>> eventReceiverListsByTopicId;

	private volatile ICloudManager cloudManager;

	private volatile Map<String, EventMessageSender> connectedNodesByNodeId = Collections.emptyMap();

	public WebsocketEventTransport() {
		eventReceiverListsByTopicId = new ConcurrentHashMap<>();
	}

	public void activate(final ComponentContext context) {
		LOG.info("Activating WebsocketEventTransport.");
		startWebSocketServer();
		startWebSocketClient();

		getCloudManager().addNodeListener(new INodeListener() {

			@Override
			public void nodesChanged() {
				// re-connect
				connectAllOnlineNodes();
			}
		});
	}

	private void connectAllOnlineNodes() {
		/*
		 * Concurrency rules:
		 * 1. connectedNodesByNodeId is never modified (!!)
		 * 2. instead a new map is built each time which replaces the
		 *    connectedNodesByNodeId once it is complete (atomic operation)
		 */
		final Map<String, EventMessageSender> existingConnections = new HashMap<>(connectedNodesByNodeId);
		final Map<String, EventMessageSender> newConnections = new HashMap<>();

		final ImmutableMap<String, INodeDescriptor> approvedNodesById = Maps.uniqueIndex(getCloudManager().getApprovedNodes(), new Function<INodeDescriptor, String>() {
			@Override
			public String apply(final INodeDescriptor input) {
				return input.getId();
			}
		});

		final WebSocketClient client = clientRef.get();
		if (client == null)
			return;

		for (final String nodeId : getCloudManager().getOnlineNodes()) {
			if (nodeId.equals(getCloudManager().getLocalInfo().getNodeId())) {
				continue;
			}

			final INodeDescriptor descriptor = approvedNodesById.get(nodeId);
			if (descriptor == null) {
				continue;
			}

			EventMessageSender sender = existingConnections.remove(nodeId);
			if ((null != sender) && sender.isConnected()) {
				newConnections.put(nodeId, sender);
				continue;
			}

			sender = new EventMessageSender(getCloudManager().getLocalInfo().getNodeId());

			if (connectToNextAvailableAddress(new ArrayList<String>(descriptor.getAddresses()).iterator(), sender, client, nodeId)) {
				newConnections.put(nodeId, sender);
			}
		}

		// replace
		connectedNodesByNodeId = newConnections;

		// kill remaining connections
		for (final EventMessageSender sender : existingConnections.values()) {
			if (sender.isConnected()) {
				final Session session = sender.getSession();
				session.close();
			}
		}
	}

	private boolean connectToNextAvailableAddress(final Iterator<String> addresses, final EventMessageSender sender, final WebSocketClient client, final String nodeId) {
		if (!addresses.hasNext()) {
			LOG.error("No routes available to node ({}). Unable to connect event transport.", nodeId);
			return false;
		}

		// get (and remove) next address
		final String address = addresses.next();
		addresses.remove();

		try {
			// try connection
			final URI echoUri = new URI("ws://" + address + ":" + Platform.getInstancePort(eventsPort.get()) + "/eventbus/");
			client.connect(sender, echoUri).get();
			return true;
		} catch (final CancellationException e) {
			LOG.debug("Aborted while connecting to node ({}) at ({}).", nodeId, address);
			return false;
		} catch (final URISyntaxException | IOException | ExecutionException e) {
			LOG.warn("Unable to connect to ({}). {}", address, e.getMessage(), e);
			// fall through to try next
			return connectToNextAvailableAddress(addresses, sender, client, nodeId);
		} catch (final InterruptedException e) {
			LOG.debug("Interrupted while connecting to node ({}) at ({}).", nodeId, address);
			Thread.currentThread().interrupt();
			return false;
		}
	}

	public void deactivate(final ComponentContext context) {
		LOG.info("Deactivating WebsocketEventTransport.");
		stopWebSocketServer();
	}

	@VisibleForTesting
	void distributeEventToSubscribers(final String topicId, final EventMessage message) {
		final CopyOnWriteArrayList<IEventReceiver> receivers = eventReceiverListsByTopicId.get(topicId);
		if (receivers != null) {
			for (final IEventReceiver eventReceiver : receivers) {
				eventReceiver.receiveEvent(message);
			}
		}
	}

	public ICloudManager getCloudManager() {
		final ICloudManager manager = cloudManager;
		checkState(manager != null, "inactive");
		return manager;
	}

	@Override
	public void sendEvent(final String topicId, final EventMessage eventMessage, final Map<String, ?> properties) {
		// 1. distribute to local subscribers
		distributeEventToSubscribers(topicId, eventMessage);

		// 2. distribute into network
		final Map<String, EventMessageSender> connectedNodesByNodeId = this.connectedNodesByNodeId;
		for (final Entry<String, EventMessageSender> e : connectedNodesByNodeId.entrySet()) {
			LOG.trace("Sending event ({}) to ({})", eventMessage.getId(), e.getKey());
			if (e.getValue().isConnected()) {
				e.getValue().sendEvent(topicId, eventMessage);
			} else {
				LOG.warn("Dead connection to node ({}).", e.getKey());
			}

		}
	}

	public void setCloudManager(final ICloudManager cloudManager) {
		this.cloudManager = cloudManager;
	}

	private void startWebSocketClient() {
		try {
			final WebSocketClient client = new WebSocketClient();
			checkState(clientRef.compareAndSet(null, client), "Only one active transport allowed!");

			client.setAsyncWriteTimeout(5000);
			client.setConnectTimeout(5000);
			client.start();
		} catch (final Exception e) {
			throw new IllegalStateException("Error starting web socket client for cluster event websocket", e);
		}
	}

	private void startWebSocketServer() {
		try {
			final Server server = new Server();
			checkState(serverRef.compareAndSet(null, server), "Only one active transport allowed!");

			final HttpConfiguration httpConfiguration = new HttpConfiguration();
			httpConfiguration.setSendServerVersion(false);
			httpConfiguration.setSendDateHeader(false);

			final ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
			connector.setPort(Platform.getInstancePort(eventsPort.get()));
			connector.setIdleTimeout(60000);
			server.addConnector(connector);

			// tweak server
			server.setStopAtShutdown(true);
			server.setStopTimeout(5000);

			final HandlerCollection handlers = new HandlerCollection();

			// create context for event bus, which is used between the nodes
			final EventMessageReceiver singletonReceiver = new EventMessageReceiver(getCloudManager().getLocalInfo().getNodeId(), new IEventMessageCallback() {

				@Override
				public void onEventMessage(final String topicId, final EventMessage message) {
					distributeEventToSubscribers(topicId, message);
				}
			});
			final WebSocketHandler wsHandler = new WebSocketHandler() {
				@Override
				public void configure(final WebSocketServletFactory factory) {
					factory.setCreator(new WebSocketCreator() {
						@Override
						public Object createWebSocket(final ServletUpgradeRequest req, final ServletUpgradeResponse resp) {
							return singletonReceiver;
						}
					});
				}

			};
			final ContextHandler context = new ContextHandler();
			context.setContextPath("/eventbus");
			context.setHandler(wsHandler);
			handlers.addHandler(context);

			server.setHandler(handlers);

			server.start();
		} catch (final Exception e) {
			throw new IllegalStateException("Error starting jetty for cluster event websocket", e);
		}
	}

	private void stopWebSocketServer() {
		try {
			final Server server = serverRef.getAndSet(null);
			if (server != null) {
				server.stop();
			}
		} catch (final Exception e) {
			LOG.error("Error stopping websocket server.", e);
		}
	}

	@Override
	public void subscribeTopic(final String topicId, final IEventReceiver receiver, final Map<String, ?> properties) {
		CopyOnWriteArrayList<IEventReceiver> eventReceiverList = eventReceiverListsByTopicId.get(topicId);
		while (eventReceiverList == null) {
			eventReceiverList = eventReceiverListsByTopicId.putIfAbsent(topicId, new CopyOnWriteArrayList<IEventReceiver>());
		}
		eventReceiverList.add(receiver);
	}

	@Override
	public void unsubscribeTopic(final String topicId, final IEventReceiver receiver, final Map<String, ?> properties) {
		final CopyOnWriteArrayList<IEventReceiver> eventReceiverList = eventReceiverListsByTopicId.get(topicId);
		if (eventReceiverList != null) {
			eventReceiverList.remove(receiver);
		}
	}

}
