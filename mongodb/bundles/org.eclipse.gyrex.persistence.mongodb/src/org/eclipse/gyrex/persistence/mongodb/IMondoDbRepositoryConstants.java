/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.persistence.mongodb;

/**
 * Interface with shared constants.
 */
public interface IMondoDbRepositoryConstants {

	/**
	 * the repository provider id for the default {@link MongoDbRepository}
	 * implementation
	 */
	public static final String PROVIDER_ID = "org.eclipse.gyrex.persistence.mongodb";

	/**
	 * the repository provider id for an
	 * <code>org.eclipse.gyrex.persistence.eclipselink.EclipseLinkRepository</code>
	 * implementation backed by MongoDb
	 */
	public static final String PROVIDER_ID_JPA = "org.eclipse.gyrex.persistence.mongodb.jpa";
}
