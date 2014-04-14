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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.cloud.services.events.IEventReceiver;
import org.eclipse.gyrex.cloud.services.events.IEventTransport;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.eventbus.IEventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class EventService implements IEventBus {

	class SendEvent implements Runnable {

		private final String topciId;
		private final EventMessage message;

		SendEvent(final String topciId, final EventMessage message) {
			this.topciId = topciId;
			this.message = message;
		}

		@Override
		public void run() {
			try {
				LOG.trace("Sending event ({}, topic {}).", message.getId(), topciId);
				getTransport().sendEvent(topciId, message, Collections.<String, Object> emptyMap());
			} catch (final RuntimeException | LinkageError | AssertionError e) {
				LOG.warn("Unable to send event ({}) for topic ({}). Event discarded. {}", message.getId(), topciId, e.getMessage(), e);
			}
		}

	}

	static class TopicEventReceiver implements IEventReceiver {
		private final Topic topic;

		TopicEventReceiver(final Topic topic) {
			this.topic = topic;
		}

		@Override
		public void receiveEvent(final EventMessage eventMessage) {
			topic.dispatchEvent(eventMessage);
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

	private final ReflectionService reflectionService = new ReflectionService();
	private final String nodeId;
	private final AtomicLong eventCounter = new AtomicLong(0);
	private final IServiceProxy<IEventTransport> transportServiceProxy;
	private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(r, "EventService-SendThread");
			t.setDaemon(true);
			return t;
		}
	});
	private final ConcurrentMap<Topic, TopicEventReceiver> activeTopics = new ConcurrentHashMap<>();
	private volatile boolean disposed;

	public EventService(final String nodeId, final IServiceProxy<IEventTransport> transportServiceProxy) {
		this.nodeId = checkNotNull(nodeId);
		this.transportServiceProxy = checkNotNull(transportServiceProxy);
	}

	/**
	 * Activates the specified topic so that remote events are propagated to it.
	 * 
	 * @param topic
	 */
	void activateTopic(final Topic topic) {
		checkDisposed();
		final TopicEventReceiver receiver = new TopicEventReceiver(topic);
		if (null == activeTopics.putIfAbsent(topic, receiver)) {
			LOG.debug("Subscribing topic ({}).", topic);
			getTransport().subscribeTopic(topic.getId(), receiver, null);
		}
	}

	private void checkDisposed() {
		checkState(!disposed, "disposed");
	}

	/**
	 * Deactivates the specified topic.
	 * <p>
	 * As a result, the topic will no longer receive remote events.
	 * </p>
	 * 
	 * @param topic
	 */
	void deactivateTopic(final Topic topic) {
		final TopicEventReceiver receiver = activeTopics.remove(topic);
		if (receiver != null) {
			LOG.debug("Unsubscribing topic ({}).", topic);
			getTransport().unsubscribeTopic(topic.getId(), receiver, null);
		}
	}

	public void dispose() {
		disposed = true;
		while (activeTopics.size() > 0)
			deactivateTopic(activeTopics.keySet().iterator().next());
	}

	ReflectionService getReflectionService() {
		return reflectionService;
	}

	@Override
	public TopicBuilder getTopic(final String id) throws IllegalArgumentException, IllegalStateException {
		checkDisposed();
		return new TopicBuilder(id, this).addSerializer(ByteArrayDeSerializer.sharedInstance).addSerializer(ByteBufferDeSerializer.sharedInstance).addSerializer(StringDeSerializer.sharedInstance).addDeserializer(ByteArrayDeSerializer.sharedInstance).addDeserializer(ByteBufferDeSerializer.sharedInstance).addDeserializer(StringDeSerializer.sharedInstance);
	}

	/**
	 * @return the active event transport
	 */
	@VisibleForTesting
	IEventTransport getTransport() {
		return transportServiceProxy.getService();
	}

	/**
	 * @return a new, generated event id
	 */
	String newEventId() {
		return nodeId + "-" + nextId();
	}

	long nextId() {
		while (true) {
			final long last = eventCounter.get();
			// overflow back to 0
			final long next = last == Long.MAX_VALUE ? 0 : last + 1;
			if (eventCounter.compareAndSet(last, next))
				return next;
		}
	}

	/**
	 * Enqueues the specific event for sending.
	 * 
	 * @param id
	 * @param eventMessage
	 */
	public void queueEvent(final String topciId, final EventMessage eventMessage) {
		if (transportServiceProxy.isAvailable()) {
			LOG.trace("Queuing event ({}, topic {}) for delivery.", eventMessage.getId(), topciId);
			sendExecutor.execute(new SendEvent(topciId, eventMessage));
		} else
			LOG.trace("Discarding event ({}, topic {}). No transport available.", eventMessage.getId(), topciId);
	}
}
