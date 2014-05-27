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

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;
import org.eclipse.gyrex.eventbus.ITopic;

import org.apache.commons.lang.exception.ExceptionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class Topic implements ITopic {

	private static final Logger LOG = LoggerFactory.getLogger(Topic.class);

	private final String id;
	private final EventService eventService;
	private final ConcurrentMap<Class<?>, IEventSerializer<Object>> serializersByEventType;
	private final Multimap<String, IEventDeserializer<Object>> deserializersByEventTypeClassName = HashMultimap.create();
	private final Multimap<Class<?>, EventHandler> eventHandlersByType = HashMultimap.create();
	private final ReadWriteLock eventHandlersByTypeLock = new ReentrantReadWriteLock();
	private final AtomicBoolean active = new AtomicBoolean(false);
	private volatile boolean closed = false;

	Topic(final String id, final Map<String, Object> properties, final Map<Class<?>, IEventSerializer<Object>> serializers, final Map<Class<?>, IEventDeserializer<Object>> deserializers, final EventService eventService) {
		this.id = id;
		serializersByEventType = new ConcurrentHashMap<>(serializers);
		for (final Entry<Class<?>, IEventDeserializer<Object>> e : deserializers.entrySet())
			deserializersByEventTypeClassName.put(e.getKey().getName(), e.getValue());
		this.eventService = eventService;
	}

	private void activateIfNecessary() {
		eventHandlersByTypeLock.readLock().lock();
		try {
			if ((eventHandlersByType.size() > 0) && active.compareAndSet(false, true))
				getEventService().activateTopic(this);
		} finally {
			eventHandlersByTypeLock.readLock().unlock();
		}
	}

	private void checkClosed() {
		checkState(!closed, "closed");
	}

	@Override
	public void close() {
		// guard by event handler modifications
		eventHandlersByTypeLock.writeLock().lock();
		try {
			closed = true;
		} finally {
			eventHandlersByTypeLock.writeLock().unlock();
		}

		if (active.compareAndSet(true, false))
			getEventService().deactivateTopic(this);

		// clear directly; no more locks necessary after close
		eventHandlersByType.clear();
	}

	@VisibleForTesting
	EventMessage createEventMessage(final Class<?> eventType, final byte[] serializedEvent) {
		return new EventMessage(getEventService().newEventId(), eventType.getName(), ByteBuffer.wrap(serializedEvent));
	}

	private void deactivateIfPossible() {
		eventHandlersByTypeLock.readLock().lock();
		try {
			if ((eventHandlersByType.isEmpty()) && active.compareAndSet(true, false))
				getEventService().deactivateTopic(this);
		} finally {
			eventHandlersByTypeLock.readLock().unlock();
		}
	}

	/**
	 * Dispatches the specified event message to all compatible, interested
	 * event handlers.
	 * 
	 * @param eventMessage
	 */
	public void dispatchEvent(final EventMessage eventMessage) {
		if (closed) {
			LOG.trace("Ignoring event message ({}) for topic ({}). Topic is closed.", eventMessage, this);
			return;
		}
		LOG.trace("Dispatching event message ({}) for topic ({}).", eventMessage, this);
		final Collection<IEventDeserializer<Object>> deserializers = getDeserializers(eventMessage);
		if (deserializers.isEmpty()) {
			LOG.debug("No deserializers found in topic ({}) for event type ({}).", this, eventMessage.getType());
			return;
		}
		checkState(eventMessage.getPayload().hasArray());
		for (final IEventDeserializer<Object> deserializer : deserializers) {
			Object event;
			try {
				event = deserializer.deserializeEvent(eventMessage.getPayload().array());
				LOG.trace("Deserialized event message ({}) using ({}) to ({}).", eventMessage, deserializer, event);
			} catch (Exception | LinkageError e) {
				LOG.error("Unable to deserialized event message ({}, topic {}) using ({}). {}", eventMessage, getId(), deserializer, ExceptionUtils.getRootCause(e), e);
				continue;
			}
			for (final Class<? extends Object> eventType : getReflectionService().getHierarchy(event.getClass()))
				for (final EventHandler handler : getHandlers(eventType))
					dispatchEvent(event, handler);
		}
	}

	/**
	 * Safe dispatch of an event to a given handlers.
	 * <p>
	 * Any error is logged but not propagated.
	 * </p>
	 * 
	 * @param event
	 * @param handler
	 */
	@VisibleForTesting
	void dispatchEvent(final Object event, final EventHandler handler) {
		LOG.trace("Dispatching event ({}) to handler ({})", event, handler);
		try {
			handler.handleEvent(event);
		} catch (Exception | LinkageError e) {
			LOG.error("Unable to dispatch event ({}, topic {}) to handler ({}). {}", event, getId(), handler, ExceptionUtils.getRootCause(e), e);
		}
	}

	@VisibleForTesting
	Collection<IEventDeserializer<Object>> getDeserializers(final EventMessage eventMessage) {
		return deserializersByEventTypeClassName.get(eventMessage.getType());
	}

	EventService getEventService() {
		return eventService;
	}

	/**
	 * Returns a snapshot of all registered handlers for a specific event type
	 * <p>
	 * Note, the returned list is a snapshot copy, i.e. it does not reflect
	 * concurrent modifications via {@link #registerHandler(EventHandler)} and
	 * {@link #unregisterHandler(EventHandler)}.
	 * </p>
	 * 
	 * @param eventType
	 *            the event type
	 * @return a snapshot copy of all registered handlers
	 */
	@VisibleForTesting
	List<EventHandler> getHandlers(final Class<?> eventType) {
		eventHandlersByTypeLock.readLock().lock();
		try {
			return Lists.newArrayList(eventHandlersByType.get(eventType));
		} finally {
			eventHandlersByTypeLock.readLock().unlock();
		}
	}

	@Override
	public String getId() {
		return id;
	}

	ReflectionService getReflectionService() {
		return getEventService().getReflectionService();
	}

	@VisibleForTesting
	IEventSerializer<Object> getSerializer(final Class<?> eventType) {
		IEventSerializer<Object> serializer = serializersByEventType.get(eventType);
		if (serializer != null) {
			LOG.trace("Found serializer ({}) for event type ({})", serializer, eventType);
			return serializer;
		}

		for (final Class<?> type : getReflectionService().getHierarchy(eventType)) {
			serializer = serializersByEventType.get(type);
			if (serializer != null) {
				// cache mapping for future lookups
				serializersByEventType.putIfAbsent(type, serializer);
				LOG.trace("Found serializer ({}) registered for type ({}) for event type ({})", serializer, type, eventType);
				return serializer;
			}
		}
		throw new IllegalArgumentException(format("no serializer found for event of type '%s'", eventType));
	}

	@Override
	public void register(final Object object) throws IllegalArgumentException {
		LOG.debug("Registering object ({}) with topic ({})", object, this);
		for (final EventHandler handler : getReflectionService().getEventHandlers(object))
			registerHandler(handler);
	}

	@VisibleForTesting
	void registerHandler(final EventHandler handler) {
		LOG.debug("Registering handler ({}) with topic ({})", handler, this);
		eventHandlersByTypeLock.writeLock().lock();
		try {
			checkClosed();
			eventHandlersByType.put(handler.getEventType(), handler);
		} finally {
			eventHandlersByTypeLock.writeLock().unlock();
		}

		activateIfNecessary();
	}

	@Override
	public <T extends Object> void sendEvent(final T event) throws IllegalArgumentException {
		LOG.trace("Sending event ({}) for topic ({})", event, this);
		checkClosed();
		final byte[] serializedEvent = getSerializer(event.getClass()).serializeEvent(event);
		getEventService().queueEvent(getId(), createEventMessage(event.getClass(), serializedEvent));
	}

	@Override
	public void unregister(final Object object) throws IllegalArgumentException {
		LOG.debug("Unregistering object ({}) from topic ({})", object, this);
		for (final EventHandler handler : getReflectionService().getEventHandlers(object))
			unregisterHandler(handler);
	}

	@VisibleForTesting
	void unregisterHandler(final EventHandler handler) {
		LOG.debug("Unregistering handler ({}) from topic ({})", handler, this);
		eventHandlersByTypeLock.writeLock().lock();
		try {
			checkClosed();
			eventHandlersByType.remove(handler.getEventType(), handler);
		} finally {
			eventHandlersByTypeLock.writeLock().unlock();
		}

		deactivateIfPossible();
	}

}
