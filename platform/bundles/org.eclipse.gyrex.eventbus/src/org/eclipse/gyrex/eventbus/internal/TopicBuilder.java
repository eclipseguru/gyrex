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

import static com.google.common.base.Preconditions.checkArgument;
import static org.eclipse.gyrex.common.identifiers.IdHelper.isValidId;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;
import org.eclipse.gyrex.eventbus.ITopicBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

public class TopicBuilder implements ITopicBuilder {

	@VisibleForTesting
	final String id;

	@VisibleForTesting
	final Map<Class<?>, IEventSerializer<Object>> serializers = new HashMap<>();

	@VisibleForTesting
	final Map<Class<?>, IEventDeserializer<Object>> deserializers = new HashMap<>();

	@VisibleForTesting
	final Map<String, Object> properties = new HashMap<>();

	private final EventService eventService;

	TopicBuilder(final String id, final EventService eventService) {
		this.eventService = eventService;
		checkArgument(isValidId(id), "invalid id");
		this.id = id;
	}

	@Override
	public TopicBuilder addDeserializer(final IEventDeserializer<?>... deserializers) {
		checkArgument((deserializers != null) && (deserializers.length > 0), "no deserializer specified");
		for (final IEventDeserializer<?> deserializer : deserializers) {
			addDeserializerInternal(deserializer);
		}
		return this;
	}

	@VisibleForTesting
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void addDeserializerInternal(final IEventDeserializer<?> deserializer) {
		checkArgument(deserializer != null, "null deserializer not allowed");

		final Method[] methods = deserializer.getClass().getMethods();
		for (final Method method : methods) {
			if (method.getName().equals("deserializeEvent") && (method.getParameterTypes().length == 1)) {
				final Invokable<?, Object> invokable = Invokable.from(method);
				final Class<?> eventType = invokable.getReturnType().getRawType();
				if (isAllowedEventType(eventType) && isByteArray(invokable.getParameters().get(0).getType())) {
					checkArgument(!deserializers.containsKey(eventType), "duplicate deserializer for type '%s'", eventType);
					// note, the cast below is guarded by the if condition above
					deserializers.put(eventType, (IEventDeserializer) deserializer);
				}
			}
		}
	}

	@Override
	public TopicBuilder addSerializer(final IEventSerializer<?>... serializers) {
		checkArgument((serializers != null) && (serializers.length > 0), "no serializer specified");
		for (final IEventSerializer<?> serializer : serializers) {
			addSerializerInternal(serializer);
		}
		return this;
	}

	@VisibleForTesting
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void addSerializerInternal(final IEventSerializer<?> serializer) {
		checkArgument(serializer != null, "null serializer not allowed");

		final Method[] methods = serializer.getClass().getMethods();
		for (final Method method : methods) {
			if (method.getName().equals("serializeEvent") && (method.getParameterTypes().length == 1)) {
				final Invokable<?, Object> invokable = Invokable.from(method);
				final Class<?> eventType = invokable.getParameters().get(0).getType().getRawType();
				if (isAllowedEventType(eventType) && isByteArray(invokable.getReturnType())) {
					checkArgument(!serializers.containsKey(eventType), "duplicate serializer for type '%s'", eventType);
					// note, the cast below is guarded by the if condition above
					serializers.put(eventType, (IEventSerializer) serializer);
				}
			}
		}
	}

	@Override
	public Topic build() throws IllegalArgumentException, IllegalStateException, SecurityException {
		return new Topic(id, properties, serializers, deserializers, getEventService());
	}

	EventService getEventService() {
		return eventService;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	private boolean isAllowedEventType(final Class<?> eventType) {
		return getEventService().getReflectionService().isAllowedEventType(eventType);
	}

	private boolean isByteArray(final TypeToken<? extends Object> returnType) {
		return returnType.isArray() && returnType.getComponentType().isPrimitive() && returnType.getComponentType().getRawType().equals(byte.class);
	}

	@Override
	public TopicBuilder setProperty(final String name, final Object value) {
		properties.put(name, value);
		return this;
	}

}
