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
 * This packages provides a foundation for building simple web applications
 * based on RAP with a common UI.
 * <p>
 * RAP traditionally is very aligned with the Eclipse workbench. However, the
 * workbench is considered heavy-weight. They also do not feel like modern web
 * applications. In contract to the workbench, modern web applications expose a
 * different usability.
 * </p>
 * <p>
 * The concept implemented here is based on pages. Pages are hosted as content
 * inside the application. The navigation is driven by link. One navigates from
 * page to page by following links which will also be represented in the browser
 * history.
 * </p>
 */
package org.eclipse.gyrex.rap.application;

