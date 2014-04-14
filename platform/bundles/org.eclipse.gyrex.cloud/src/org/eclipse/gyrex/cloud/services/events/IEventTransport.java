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

import java.util.Map;

/**
 * A transport for events.
 * <p>
 * The event transport allows to post and receive events between nodes in the
 * cloud. Implementors are expected to deliver any event submitted via
 * {@link #sendEvent(String, EventMessage, Map)} to all active nodes in the
 * cloud. Clients interested in receiving events must subscribe via
 * {@link #subscribeTopic(String, IEventReceiver, Map)}. It does not define any
 * strong guarantees about typical event characteristics (ordering, reliability,
 * etc.) but an underlying implementation may do so.
 * </p>
 * <p>
 * In general, clients using an event transport should have a few things in
 * mind.
 * <ul>
 * <li>Due to the nature of cloud services and especially distributed events
 * it's not easy for event service implementors to guarantee on time delivery.
 * Therefore, delivery specific behavior (for example, such as order of events)
 * must not be relied upon when working with events.</li>
 * <li>Some cloud event transports might store copies of your events on multiple
 * servers. It may happen that a server dies right when sending an event. When
 * it it is restored it may send the event again. Therefore, an application must
 * be idempotent regarding to event retrieval, i.e. it must be prepared to
 * handle the same event twice.</li>
 * </ul>
 * </p>
 * <p>
 * An event transport must be obtained from the OSGi service registry.
 * References may be kept around for a longer period of time as long as the
 * service is still active. Once an event transport goes away, any references
 * must be released. From that time on, any method invocation may throw
 * {@link IllegalStateException}.
 * </p>
 * <p>
 * This interface is typically not implemented by clients but by service
 * providers. As such it is considered part of a service provider API which may
 * evolve faster than the general API. Please get in touch with the development
 * team through the preferred channels listed on <a
 * href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay up-to-date
 * of possible changes for implementors.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IEventTransport {

	/**
	 * The service name under which an event deserializer might be registered in
	 * a service registry (eg., such as OSGi) (value {@value #SERVICE_NAME}).
	 */
	String SERVICE_NAME = IEventTransport.class.getName();

	/**
	 * Sends a given event to the specified topic.
	 * <p>
	 * The event must be send to all active, participating nodes in the cloud.
	 * This may exclude nodes not actively participating in the event bus.
	 * </p>
	 * <p>
	 * When this method returns the event has been successfully submitted. This
	 * does not necessarily include delivery.
	 * </p>
	 * 
	 * @param topicId
	 *            the topic id (may not be <code>null</code>)
	 * @param eventMessage
	 *            the event message (may not be <code>null</code>)
	 * @param properties
	 *            additional properties for the request (may not be
	 *            <code>null</code>)
	 */
	void sendEvent(String topicId, EventMessage eventMessage, Map<String, ?> properties);

	/**
	 * Subscribes a given receiver for receiving all events broadcasted for the
	 * specified topic.
	 * <p>
	 * Implementors must remember the specified receiver and call
	 * {@link IEventReceiver#receiveEvent(EventMessage)} for every event
	 * broadcasted for a topic until
	 * {@link #unsubscribeTopic(String, IEventReceiver, Map)} was called with
	 * the same topic and receiver as input.
	 * </p>
	 * <p>
	 * Implementors may rely on {@link IEventReceiver#equals(Object)} for
	 * identifying the same event receiver and for ignoring multiple/duplicate
	 * subscription requests for the same receiver and topic combination.
	 * </p>
	 * 
	 * @param topicId
	 *            the topic id (may not be <code>null</code>)
	 * @param receiver
	 *            the receiver (may not be <code>null</code>)
	 * @param properties
	 *            additional properties for the request (may not be
	 *            <code>null</code>)
	 */
	void subscribeTopic(String topicId, IEventReceiver receiver, Map<String, ?> properties);

	/**
	 * Unsubscribes a given receiver from the specified topic.
	 * <p>
	 * As a result the receiver will no longer receive any events broadcasted
	 * for a topic. Implementors should remove all references for the given
	 * receiver and topic combination.
	 * </p>
	 * 
	 * @param topicId
	 *            the topic id (may not be <code>null</code>)
	 * @param receiver
	 *            the receiver (may not be <code>null</code>)
	 * @param properties
	 *            additional properties for the request (may not be
	 *            <code>null</code>)
	 */
	void unsubscribeTopic(String topicId, IEventReceiver receiver, Map<String, ?> properties);

}
