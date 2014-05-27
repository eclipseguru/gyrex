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
 * A builder for {@link ITopic} instances.
 */
public interface ITopicBuilder {

	/**
	 * Adds one or more {@link IEventDeserializer deserializer} to the topic.
	 * 
	 * @param deserializers
	 *            the deserializer to add
	 * @return the builder instance
	 */
	ITopicBuilder addDeserializer(IEventDeserializer<?>... deserializers);

	/**
	 * Adds one or more {@link IEventSerializer serializer} to the topic.
	 * 
	 * @param serializers
	 *            the serializer to add
	 * @return the builder instance
	 */
	ITopicBuilder addSerializer(IEventSerializer<?>... serializers);

	/**
	 * Builds the topic handle.
	 * 
	 * @return the topic handle
	 * @throws IllegalArgumentException
	 *             if a required property is missing
	 * @throws IllegalStateException
	 *             if the cloud event service is unavailable
	 * @throws SecurityException
	 *             if security restrictions (either in the system or on the
	 *             cloud event service) prevented the requests
	 */
	ITopic build() throws IllegalArgumentException, IllegalStateException, SecurityException;

	/**
	 * Sets a property.
	 * 
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 * @return the builder instance
	 */
	ITopicBuilder setProperty(String name, Object value);

}
