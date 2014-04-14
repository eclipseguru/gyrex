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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import org.eclipse.gyrex.eventbus.internal.InvalidDeSerializer;

/**
 * Annotation which identifies a topic for injection.
 * <p>
 * Example for sending and receiving of events:
 * 
 * <pre>
 * &#064;Inject
 * <strong>&#064;Topic(&quot;my.topic&quot;)</strong>
 * private ITopic topic;
 * 
 * public void sendEventMethodExample(MyEvent event) {
 * 	topic.sendEvent(event);
 * }
 * 
 * &#064;EventHandler
 * public void handleMyEvent(MyEvent event) {
 * 	// do something with event
 * }
 * 
 * &#064;PostConstruct
 * public void initialize() {
 * 	topic.register(this);
 * }
 * 
 * &#064;PreDestroy
 * public void close() {
 * 	topic.unregister(this);
 * }
 * </pre>
 * 
 * </p>
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

	/** additional deserializer to consider */
	Class<? extends IEventDeserializer<?>>[] deserializer() default InvalidDeSerializer.class;

	/** additional serializer to consider */
	Class<? extends IEventSerializer<?>>[] serializer() default InvalidDeSerializer.class;

	/** the topic id */
	String value();
}
