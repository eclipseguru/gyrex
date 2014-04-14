/**
 * Copyright (c) 2014 <company> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     <author> - initial API and implementation
 */
package org.eclipse.gyrex.eventbus.internal;

import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.Annotation;

import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.provider.di.ExtendedObjectResolver;
import org.eclipse.gyrex.eventbus.IEventBus;
import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;
import org.eclipse.gyrex.eventbus.ITopic;
import org.eclipse.gyrex.eventbus.ITopicBuilder;
import org.eclipse.gyrex.eventbus.Topic;

@SuppressWarnings("restriction")
public class TopicInjectionResolver extends ExtendedObjectResolver {

	private IEventBus eventBus;

	@Override
	public Object get(final Class<?> type, final IRuntimeContext context, final Annotation annotation) {
		checkState(ITopic.class.isAssignableFrom(type), "The @Topic qualifier must only be used for injecting ITopic instances.");
		final org.eclipse.gyrex.eventbus.Topic topic = (Topic) annotation;
		final ITopicBuilder builder = eventBus.getTopic(topic.value());
		for (final Class<? extends IEventDeserializer<?>> deserializer : topic.deserializer())
			if (!InvalidDeSerializer.class.isAssignableFrom(deserializer))
				builder.addDeserializer(context.getInjector().make(deserializer));
		for (final Class<? extends IEventSerializer<?>> serializer : topic.serializer())
			if (!InvalidDeSerializer.class.isAssignableFrom(serializer))
				builder.addSerializer(context.getInjector().make(serializer));
		return builder.build();
	}

	public void setEventBus(final IEventBus eventBus) {
		this.eventBus = eventBus;
	}

}
