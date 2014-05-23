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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Client for handeling the gyrex cluster events sent through node to node
 * websocket connections. This client listens for incoming events form other
 * nodes, which have been connected. These received Events will be posted
 * forward to the {@link EventBus}
 */
@WebSocket
public class ClusterEventsSocketClient {

	private final CountDownLatch closeLatch;

	@SuppressWarnings("unused")
	private Session session;

	private final ClusterEventsClientsHandler clusterEventHandler;
	private final String ip;

	public ClusterEventsSocketClient(final String ip, final ClusterEventsClientsHandler clusterEventHandler) {
		this.ip = ip;
		this.clusterEventHandler = clusterEventHandler;
		closeLatch = new CountDownLatch(1);
	}

	public boolean awaitClose(final int duration, final TimeUnit unit) throws InterruptedException {
		return closeLatch.await(duration, unit);
	}

	@OnWebSocketClose
	public void onClose(final int statusCode, final String reason) {
		System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
		session = null;
		closeLatch.countDown();
		clusterEventHandler.onNodeDisconnect(ip, this);
	}

	@OnWebSocketConnect
	public void onConnect(final Session session) {
		System.out.printf("Got connect: %s%n", session);
		clusterEventHandler.onNodeConnect(ip, this);
		this.session = session;
	}

	@OnWebSocketMessage
	public void onMessage(final String msg) {
		System.out.printf("WEBSOCKETCLIENT MESSAGE RECEIVED: %s%n", msg);
		clusterEventHandler.postEventFromNetwork(msg);
	}
}