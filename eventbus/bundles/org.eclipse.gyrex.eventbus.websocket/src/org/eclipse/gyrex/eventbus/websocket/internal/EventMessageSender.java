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

import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A web socket that converts events into incoming binary messages and forwards
 * them to the connected remote endpoint
 */
final class EventMessageSender extends WebSocketAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(EventMessageSender.class);

	private final String localINodeId;
	final ByteBufferPool bufferPool = new ArrayByteBufferPool(1, 50, 100);

	/**
	 * Creates a new instance.
	 */
	EventMessageSender(final String localINodeId) {
		this.localINodeId = localINodeId;
	}

	@Override
	public void onWebSocketClose(final int statusCode, final String reason) {
		super.onWebSocketClose(statusCode, reason);
	}

	public void sendEvent(final String topic, final EventMessage message) {
		final ByteBuffer buffer = bufferPool.acquire(getSession().getPolicy().getMaxBinaryMessageSize(), false);
		buffer.clear();

		// header
		writeUtf8String(buffer, message.getId());
		writeUtf8String(buffer, topic);
		writeUtf8String(buffer, message.getType());
		writeUtf8String(buffer, localINodeId);

		// payload
		buffer.put(message.getPayload());

		// send asynchronously
		buffer.flip();
		getRemote().sendBytes(buffer, new WriteCallback() {

			@Override
			public void writeFailed(final Throwable x) {
				LOG.error("Error writing event ({}) to ({}). {}", message.getId(), getSession(), ExceptionUtils.getRootCauseMessage(x), x);
				bufferPool.release(buffer);
			}

			@Override
			public void writeSuccess() {
				bufferPool.release(buffer);
			}
		});
	}

	private void writeUtf8String(final ByteBuffer buffer, final String string) {
		buffer.putInt(string.length());
		buffer.put(StringUtil.getUtf8Bytes(string));
	}
}