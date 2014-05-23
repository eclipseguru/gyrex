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

import java.nio.ByteBuffer;

import org.eclipse.gyrex.cloud.services.events.EventMessage;

/**
 * the actual class used for json based transport through the websocket based
 * event distribution
 * 
 * @author mihm
 */
public class TransportableEvent {

	protected String nodeId = null;
	private final String topicId;

	private String id = null;
	private long created = 0;
	private String type = null;
	private byte[] payload = null;

	public TransportableEvent(final EventMessage message, final String topicId) {
		super();
		this.topicId = topicId;
		type = message.getType();
		id = message.getId();
		created = message.getCreated();
		payload = message.getPayload().array();

	}

	/**
	 * Returns the message.
	 * 
	 * @return the message
	 */
	public EventMessage getMessage() {
		final EventMessage message = new EventMessage(id, type, ByteBuffer.wrap(payload));
		return message;
	}

	/**
	 * @return the nodeId, the id of the gyrex cluster node, which has created
	 *         the event
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Returns the topicId.
	 * 
	 * @return the topicId
	 */
	public String getTopicId() {
		return topicId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setNodeId(final String nodeId) {
		this.nodeId = nodeId;
	}

}
