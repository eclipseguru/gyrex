/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
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

/**
 * A handle to a topic for sending and receiving events.
 * <p>
 * Topics generally group a set of events. They support sending of arbitrary
 * event types.
 * </p>
 * <p>
 * As topics are the level of configuration/customization, they must be re-used
 * within the same logical context. For example, a listener registered with a
 * specific topic instance must be removed from the same topic instance. When a
 * topic is no longer necessary, it must be {@link #close() closed}.
 * </p>
 * <p>
 * A topic handle must be obtained from an event bus. References may be kept
 * around for a longer period of time as long as the event bus is still active.
 * Once an event bus goes away, any references must be released. From that time
 * on, any method invocation may throw {@link IllegalStateException}.
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
public interface ITopic extends AutoCloseable {

	/**
	 * Closes the topic.
	 * <p>
	 * This method must be called when a topic handle is no longer necessary. As
	 * a result, all registered listeners will be removed and no new listeners
	 * can be registered.
	 * </p>
	 */
	@Override
	void close();

	/**
	 * Returns the unique identifier of the topic.
	 * 
	 * @return the topic id (never <code>null</code>)
	 */
	String getId();

	/**
	 * Scans the specified object and registers the detected handlers.
	 * <p>
	 * The following logic will be used to detect handlers:
	 * <ol>
	 * <li>In case the object implements {@link IEventHandler}, the object will
	 * be registered as one handler.</li>
	 * <li>In case the object does not implement {@link IEventHandler}, it will
	 * be scanned for methods annotated with {@link EventHandler}. All found
	 * methods will be registered as handlers.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param object
	 *            the object (must not be <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid (eg., incompatible
	 *             signature for annotated method found)
	 * @see {@link EventHandler}
	 */
	void register(Object object) throws IllegalArgumentException;

	/**
	 * Sends an event.
	 * <p>
	 * When this method returns the event has been successfully serialized and
	 * queued for delivery. However, the actual delivery happens asynchronously.
	 * </p>
	 * <p>
	 * Serialization of an event will happen using a registered serializer or
	 * any of the matching built in serializers. If no serializer is registered
	 * for the specific event type its class hierarchy will be traversed in
	 * order to find a serializer.
	 * </p>
	 * 
	 * @param event
	 *            the event object (may not be <code>null</code>)
	 * @param <T>
	 *            the event type (must be a subclass of {@link Object};
	 *            {@link Object} itself is not supported)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid (eg., no serializer is
	 *             available)
	 */
	<T extends Object> void sendEvent(T event) throws IllegalArgumentException;

	/**
	 * Unregisters all handlers found in the specified object.
	 * <p>
	 * See {@link #register(Object)} for the logic of detecting handlers.
	 * </p>
	 * 
	 * @param object
	 *            the object (must not be <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid (eg., incompatible
	 *             signature for annotated method found)
	 */
	void unregister(Object object) throws IllegalArgumentException;

}
