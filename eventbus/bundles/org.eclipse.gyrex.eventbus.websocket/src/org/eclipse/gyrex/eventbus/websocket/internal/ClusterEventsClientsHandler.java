/**
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andreas Mihm - initial API and implementation
 */
package org.eclipse.gyrex.eventbus.websocket.internal;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.gyrex.cloud.admin.ICloudManager;
import org.eclipse.gyrex.cloud.admin.INodeDescriptor;

import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * takes care of establishing the outgoing WebSocket connections to all other
 * nodes in the Gyrex cluster and receiving events from the other nodes. It uses
 * the location attribute of the {@link INodeDescriptor} as the nodes IP or
 * networkname and tries to establish the WebSocket connection to every node.
 * the object, which initialzes the {@link ClusterEventsClientsHandler} needs to
 * take care of calling the connectToAllClusterNodes method within a recurring
 * job to make sure, that last connections will be established again once the
 * other node is online again.
 * 
 * @author mihm
 */
@SuppressWarnings("restriction")
public class ClusterEventsClientsHandler {

	private final ICloudManager cloudManager;

	private final HashMap<String, ClusterEventsSocketClient> nodeConnections;

	private final String localNodeId;
	private final Gson gson;

	private final WebsocketEventTransport transportService;

	public ClusterEventsClientsHandler(final WebsocketEventTransport transportService) {
		this.transportService = transportService;
		nodeConnections = new HashMap<String, ClusterEventsSocketClient>();
		cloudManager = ClusterEventsActivator.getInstance().getCloudManager();
		localNodeId = cloudManager.getLocalInfo().getNodeId();
		gson = new GsonBuilder().create();
	}

	/**
	 * establishes an outgoing WebSocket connection to a node by given ip
	 * 
	 * @param ip
	 */
	private void connectGyrexNode(final String ip) {

		if (!nodeConnections.containsKey(ip)) {

			final String destUri = "ws://" + ip + ":3111/eventbus/";

			final WebSocketClient client = new WebSocketClient();
			final ClusterEventsSocketClient socketClient = new ClusterEventsSocketClient(ip, this);
			try {
				client.start();
				final URI echoUri = new URI(destUri);
				final ClientUpgradeRequest request = new ClientUpgradeRequest();
				client.connect(socketClient, echoUri, request);
				System.out.printf("Connecting to : %s%n", echoUri);
				// socket.awaitClose(5, TimeUnit.SECONDS);
			} catch (final Throwable t) {
				t.printStackTrace();
			}

		}
	}

	/**
	 * loops over all approved nodes of the current cluster and tries to
	 * establish an outgoing WebSocket connection to every node. Thois method
	 * needs to be called in a recurring pattern to repair lost connections.
	 */
	public void connectToAllClusterNodes() {

		final Collection<INodeDescriptor> approvedNodes = cloudManager.getApprovedNodes();
		for (final INodeDescriptor approvedNode : approvedNodes) {
			if (!approvedNode.getId().equals(localNodeId)) {
				final String location = approvedNode.getLocation();
				connectGyrexNode(location);
			}
		}
	}

	/**
	 * this method is called from the underlying WebSocketClient, whenever an
	 * outgoing connection is established
	 * 
	 * @param ip
	 * @param client
	 */
	public void onNodeConnect(final String ip, final ClusterEventsSocketClient client) {
		nodeConnections.put(ip, client);
	}

	/**
	 * this method is called from the underlying WebSocketClient, whenever an
	 * outgoing connection gets disconnected
	 * 
	 * @param ip
	 * @param client
	 */
	public void onNodeDisconnect(final String ip, final ClusterEventsSocketClient client) {
		nodeConnections.remove(ip);
	}

	/**
	 * posts an Event, which has been received from another node, up to the
	 * {@link EventBus}
	 * 
	 * @param eventJson
	 */
	public void postEventFromNetwork(final String eventJson) {

		//transportService.sendEvent(topicId, eventMessage, properties);

		final TransportableEvent event = gson.fromJson(eventJson, TransportableEvent.class);
		if ((event.getNodeId() != null) && !event.getNodeId().equals(localNodeId)) {
			transportService.distributeEventToSubscribers(event.getTopicId(), event.getMessage());
		}
	}

}
