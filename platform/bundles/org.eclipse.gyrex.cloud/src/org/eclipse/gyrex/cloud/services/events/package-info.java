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

/**
 * A low-level service for distributing events
 * <p>
 * This services allows to submit and retrieve events by topics. No strong
 * guarantees are defined regarding the actual implementation which allows a
 * broad set of possible implementations (eg. Web Sockets, MQTT, JGroups, etc.).
 * </p>
 * <p>
 * Clients which do not rely on any specific behavior implied by a specific
 * implementation (eg. ordered delivery) and just use this API as is will be
 * able to implement once and run in any cloud which offers a suitable
 * implementation.
 * </p>
 * <p>
 * No guarantees are given regarding event delivery/ordering other than the
 * guarantees given by the underlying implementations.
 * </p>
 * <p>
 * This package contains a lot of API which must be implemented by contributors
 * of a service implementation. Therefore, this API is considered part of a
 * service provider API which may evolve faster than the general API. Please get
 * in touch with the development team through the preferred channels listed on
 * <a href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay
 * up-to-date of possible changes.
 * </p>
 */
package org.eclipse.gyrex.cloud.services.events;

