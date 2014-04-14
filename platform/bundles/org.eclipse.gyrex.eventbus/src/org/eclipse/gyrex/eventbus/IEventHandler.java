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
 * A handler for events.
 * <p>
 * This interface is typically implemented by clients that want to receive
 * events. In case where implementation of an interface is not desired, clients
 * may annotate methods with {@link EventHandler} annotation.
 * </p>
 */
public interface IEventHandler<T> {

	/**
	 * Handles the specified event.
	 * <p>
	 * Note, events are typically received in random order and may arrive
	 * multiple at a time. This method must be thread-safe, i.e. it must be able
	 * to handle receiving multiple events concurrently.
	 * </p>
	 * <p>
	 * In case detailed information about an event is desired, please implement
	 * {@link IEventHandlerWithDetails} instead of this interface.
	 * </p>
	 * 
	 * @param event
	 *            the event
	 */
	void handleEvent(T event);
}
