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
 * A deserializer for event objects.
 * <p>
 * This interface is typically implemented by clients working with their own
 * event types.
 * </p>
 * 
 * @param <T>
 *            the event type
 * @see IEventBus#getTopic(String) for event types supported out-of-the box
 */
public interface IEventDeserializer<T extends Object> {

	/**
	 * The service name under which an event deserializer might be registered in
	 * a service registry (eg., such as OSGi) (value {@value #SERVICE_NAME}).
	 */
	String SERVICE_NAME = IEventDeserializer.class.getName();

	/**
	 * Deserializes the specified event.
	 * 
	 * @param bytes
	 *            the serialized bytes of the event (must not be
	 *            <code>null</code>)
	 * @return the event object (never <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid (eg., the passed in bytes
	 *             can't be deserialized into the expected event object)
	 * @throws IllegalStateException
	 *             if the system is not able to produce event objects (due to
	 *             missing dependencies, etc.)
	 */
	T deserializeEvent(byte[] bytes) throws IllegalArgumentException, IllegalStateException;

}
