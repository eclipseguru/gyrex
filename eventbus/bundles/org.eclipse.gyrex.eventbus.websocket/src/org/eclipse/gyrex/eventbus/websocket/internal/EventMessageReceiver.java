/*******************************************************************************
 * Copyright (c) 2014 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.eventbus.websocket.internal;

import java.nio.ByteBuffer;

import org.eclipse.gyrex.cloud.services.events.EventMessage;

import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

/**
 * A web socket that converts all incoming binary messages into events and
 * forwards them to all local subscribers
 */
final class EventMessageReceiver extends WebSocketAdapter {

	public static interface IEventMessageCallback {
		void onEventMessage(String topicId, EventMessage message);
	}

	private final IEventMessageCallback dispatcher;
	private final String localNodeId;

	EventMessageReceiver(final String localNodeId, final IEventMessageCallback dispatcher) {
		this.dispatcher = dispatcher;
		this.localNodeId = localNodeId;
	}

	@Override
	public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
		final ByteBuffer buffer = ByteBuffer.wrap(payload, offset, len);

		final String id = readNextUtf8String(buffer);
		final String topic = readNextUtf8String(buffer);
		final String type = readNextUtf8String(buffer);
		final String source = readNextUtf8String(buffer);

		// build event with payload from remaining buffer
		final EventMessage message = new EventMessage(id, type, buffer.slice());

		// publish events from other nodes
		if (!localNodeId.equals(source)) {
			dispatcher.onEventMessage(topic, message);
		}
	}

	private String readNextUtf8String(final ByteBuffer buffer) {
		final int length = buffer.getInt();
		final Utf8StringBuilder stringBuilder = new Utf8StringBuilder(length);
		stringBuilder.append((ByteBuffer) buffer.slice().limit(length));
		buffer.position(buffer.position() + length);
		return stringBuilder.toString();
	}
}