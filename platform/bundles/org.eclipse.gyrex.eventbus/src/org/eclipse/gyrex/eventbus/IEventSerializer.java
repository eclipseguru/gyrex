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

/**
 * A serializer for event objects.
 * <p>
 * This interface is typically implemented by clients working with their own
 * event types.
 * </p>
 * 
 * @param <T>
 *            the event type
 * @see IEventBus#getTopic(String) for event types supported out-of-the box
 */
public interface IEventSerializer<T extends Object> {

	/**
	 * The service name under which an event serializer might be registered in a
	 * service registry (eg., such as OSGi) (value {@value #SERVICE_NAME}).
	 */
	String SERVICE_NAME = IEventSerializer.class.getName();

	/**
	 * Serializes the specified event.
	 * <p>
	 * It is highly recommended to keep the message size small. If you need to
	 * transfer large data consider storing the data in a database or another
	 * system and just send a pointer to the data in the message. Any event
	 * service implementation may enforce limits on the message size. Please
	 * consult the underlying event service documentation for further details.
	 * </p>
	 * <p>
	 * Serializers must be prepared to be called for subclasses of the event
	 * type. If they don't implement proper serialization for such subclasses
	 * they must fail with an {@link IllegalArgumentException}.
	 * </p>
	 * 
	 * @param value
	 *            the event to serialize (must not be <code>null</code>)
	 * @return the serialized bytes of the event (never <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid (eg., the passed in event
	 *             object can't be serialized)
	 * @throws IllegalStateException
	 *             if the system is not able to produce event objects (due to
	 *             missing dependencies, etc.)
	 */
	byte[] serializeEvent(T value) throws IllegalArgumentException, IllegalStateException;

}
