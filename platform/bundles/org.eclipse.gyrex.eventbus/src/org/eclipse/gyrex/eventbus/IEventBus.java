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
package org.eclipse.gyrex.eventbus;

import org.eclipse.gyrex.cloud.services.events.IEventTransport;
import org.eclipse.gyrex.common.identifiers.IdHelper;

/**
 * A distributed event bus.
 * <p>
 * The event bust allows to post and receive events between nodes in a cloud.
 * Events can be strongly typed. An extensible serialization and deserialization
 * mechanism is used to convert typed events into raw event messages sent
 * between nodes. They are deserialized on the retriever side.
 * </p>
 * <p>
 * All configuration/customization happens at {@link #getTopic(String) the topic
 * level}. Topics can be configured with custom de-/serializers.
 * </p>
 * <p>
 * The event bus is available in the system from the system service registry
 * (eg., as an OSGi service from the OSGi service registry). Where applicable,
 * injection into objects is supported as well.
 * </p>
 * <p>
 * It relys on {@link IEventTransport} for sending and receiving events.
 * Therefore all its limitations apply.
 * <ul>
 * <li>Due to the nature of cloud services and especially distributed events
 * it's not easy for event service implementors to guarantee a on time delivery.
 * Therefore, delivery specific behavior (for example, such as order of events)
 * must not be relied upon when working with events.</li>
 * <li>Some cloud event transports might store copies of your events on multiple
 * servers. It may happen that a server dies right when sending an event. When
 * it it is restored it may send the event again. Therefore, an application must
 * be idempotent regarding to event retrieval, i.e. it must be prepared to
 * handle the same event twice.</li>
 * </ul>
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IEventBus {

	/**
	 * The service name under which an event deserializer might be registered in
	 * a service registry (eg., such as OSGi) (value {@value #SERVICE_NAME}).
	 */
	String SERVICE_NAME = IEventBus.class.getName();

	/**
	 * Returns a builder for a topic with the specified id.
	 * <p>
	 * The topic builder is preconfigured for processing events of the following
	 * type:
	 * <ul>
	 * <li><code>byte[]</code></li>
	 * <li><code>java.lang.String</code></li>
	 * <li><code>java.nio.ByteBuffer</code></li>
	 * </ul>
	 * </p>
	 * 
	 * @param id
	 *            the topic id (must be a valid
	 *            {@link IdHelper#isValidId(String) API id})
	 * @return a topic builder (never <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 * @throws IllegalStateException
	 *             if the cloud event service is unavailable
	 */
	ITopicBuilder getTopic(String id) throws IllegalArgumentException, IllegalStateException;

}
