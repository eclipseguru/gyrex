/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.services.events;

import java.nio.ByteBuffer;

import com.google.common.base.Objects;

/**
 * An event message, encoding an event type, id and its payload.
 */
public class EventMessage {

	private final String id;
	private final long created;
	private final String type;
	private final ByteBuffer payload;

	/**
	 * Creates a new event.
	 * 
	 * @param id
	 *            the event id (see {@link #getId()})
	 * @param type
	 *            the event type
	 * @param payload
	 *            the event payload
	 */
	public EventMessage(final String id, final String type, final ByteBuffer payload) {
		this.id = id;
		this.type = type;
		this.payload = payload;
		created = System.currentTimeMillis();
	}

	/**
	 * Returns the milliseconds from the Java epoch of
	 * <code>1970-01-01T00:00:00Z</code> when the event was created at the
	 * originating node.
	 * 
	 * @return the milliseconds from the Java epoch of
	 *         <code>1970-01-01T00:00:00Z</code> when the event was created at
	 *         the originating node.
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * Returns the event id.
	 * <p>
	 * Note, the id is unique within the cloud only within a specific time
	 * period. It may be used for tracing purposes. Events are generally
	 * considered short lived objects. Therefore, an id may be re-appear for a
	 * different event after a certain amount of time.
	 * </p>
	 * <p>
	 * The id may contain a node specific component. In general, its
	 * format/layout is considered an implementation detail which may change at
	 * any point in time. Clients relying on specific semantics or syntax of
	 * this id will break eventually.
	 * </p>
	 * 
	 * @return the event id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the event payload.
	 * 
	 * @return the event payload
	 */
	public ByteBuffer getPayload() {
		return payload;
	}

	/**
	 * Returns the type.
	 * <p>
	 * An event type uniquely identifies the type event (eg., a fully qualified
	 * class name of event type used for serializing/deserializing the event).
	 * It may be used by receivers to identify the payload. However, no strong
	 * guarantee is given regarding the semantics. It may as well be a strongly
	 * kept secret between any producer and retriever.
	 * </p>
	 * 
	 * @return the fully qualified class name of event type used for serializing
	 *         the event
	 */
	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("type", type).add("created", created).toString();
	}

}
