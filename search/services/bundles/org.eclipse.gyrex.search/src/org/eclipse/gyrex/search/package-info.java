/*******************************************************************************
 * Copyright (c) 2008, 2010 Gunnar Wagenknecht and others.
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
 * This package defines API for a content delivery story.
 * <p>
 * A common concept of on-line commerce systems is to not serve raw content
 * (eg., products) directly but in some enhanced format (eg., storefront items
 * which represent the raw products). There are various reasons for decoupling
 * the store items from the products most notably being design driven
 * (separation of concerns) and scalability/performance.
 * </p>
 * <p>
 * The content delivery story provided by Gyrex is a based on the same basic
 * principles. It creates an abstraction which is suitable for delivering
 * various kinds of content to enable different kinds of applications (eg.,
 * auction sites, online shopping websites, product catalogs, etc.).
 * </p>
 * <p>
 * A key concept of the content delivery story is a very deep integrated with
 * search. Actually, search is the primary way of retrieving content from an
 * underlying repositories. This includes support for advanced concepts such as
 * facetted navigation, flexible filtering and content relevancy sorting.
 * </p>
 */
package org.eclipse.gyrex.cds;

