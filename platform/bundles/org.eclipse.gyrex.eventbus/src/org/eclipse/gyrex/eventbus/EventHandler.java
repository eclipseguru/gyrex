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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which marks a method for receiving events.
 * <p>
 * Any annotated method must have exactly one parameter which is the event and
 * it must be defined as public member method.
 * </p>
 * <p>
 * Note, just defining a method with this annotation does not enable event
 * retrieval. The purpose of this annotation is to identify event handling
 * methods. The objects containing those methods must still be registered with a
 * topic.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * &#064;EventHandler
 * public void handleMyEvent(MyEvent event) {
 * 	// do something with event
 * }
 * </pre>
 * 
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {

}
