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
package org.eclipse.gyrex.eventbus.internal;

import static java.lang.String.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.gyrex.eventbus.IEventHandler;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

/**
 * Abstraction for event handlers (either {@link IEventHandler} implementations
 * or annotated methods).
 * <p>
 * The following guarantees apply:
 * <ul>
 * <li>hashCode and equals are properly implemented to allow identifying of
 * duplicate handlers</li>
 * </ul>
 * </p>
 */
public abstract class EventHandler {

	static final class EventHandlerMethod extends EventHandler {
		private final Method method;
		private final Class<?> eventType;
		private final Object object;

		EventHandlerMethod(final Method method, final Class<?> eventType, final Object object) {
			this.method = method;
			this.eventType = eventType;
			this.object = object;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this)
				return true;
			// the method and the object are relevant for equality and hashcode
			// note, we have to include event type as well
			if (obj.getClass() == getClass()) {
				final EventHandlerMethod other = (EventHandlerMethod) obj;
				return Objects.equal(eventType, other.eventType) && Objects.equal(method, other.method) && Objects.equal(object, other.object);
			}
			return false;
		}

		@Override
		public Class<?> getEventType() {
			return eventType;
		}

		@Override
		public void handleEvent(final Object event) {
			try {
				method.invoke(object, event);
			} catch (final InvocationTargetException e) {
				Throwables.propagate(e.getCause());
			} catch (final IllegalAccessException e) {
				throw new IllegalStateException(format("Event handler method (%s) on object (%s) not accessible!", method, object), e);
			}
		}

		@Override
		public int hashCode() {
			// see equals
			return Objects.hashCode(eventType, method, object);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper("EventHandlerMethod").add("type", eventType).add("method", method).add("object", object).toString();
		}

	}

	static final class WrappedEventHandler extends EventHandler {
		private final IEventHandler<Object> eventHandler;
		private final Class<?> eventType;

		WrappedEventHandler(final Class<?> eventType, final IEventHandler<Object> eventHandler) {
			this.eventType = eventType;
			this.eventHandler = eventHandler;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this)
				return true;
			// only the event handler object is relevant for equality and hashcode
			// note, we have to include event type as well
			if (obj.getClass() == getClass()) {
				final WrappedEventHandler other = (WrappedEventHandler) obj;
				return Objects.equal(eventType, other.eventType) && Objects.equal(eventHandler, other.eventHandler);
			}
			return false;
		}

		@Override
		public Class<?> getEventType() {
			return eventType;
		}

		@Override
		public void handleEvent(final Object event) {
			eventHandler.handleEvent(event);
		}

		@Override
		public int hashCode() {
			// see equals
			return Objects.hashCode(eventType, eventHandler);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper("WrappedEventHandler").add("type", eventType).add("handler", eventHandler).toString();
		}
	}

	public static EventHandler of(final Class<?> eventType, final IEventHandler<Object> eventHandler) {
		return new WrappedEventHandler(eventType, eventHandler);
	}

	public static EventHandler of(final Class<?> eventType, final Method method, final Object object) {
		return new EventHandlerMethod(method, eventType, object);
	}

	/**
	 * Returns the event type.
	 * 
	 * @return the event type
	 */
	public abstract Class<?> getEventType();

	/**
	 * Invokes the underlying handler to handle the specified event.
	 * 
	 * @param event
	 *            the event to handle
	 */
	public abstract void handleEvent(Object event);

}
