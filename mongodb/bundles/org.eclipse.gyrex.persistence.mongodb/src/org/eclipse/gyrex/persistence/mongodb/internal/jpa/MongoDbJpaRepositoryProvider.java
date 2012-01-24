/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.persistence.mongodb.internal.jpa;

import org.eclipse.gyrex.persistence.eclipselink.EclipseLinkRepository;
import org.eclipse.gyrex.persistence.mongodb.IMondoDbRepositoryConstants;
import org.eclipse.gyrex.persistence.mongodb.internal.MongoDbActivator;
import org.eclipse.gyrex.persistence.storage.Repository;
import org.eclipse.gyrex.persistence.storage.provider.RepositoryProvider;
import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;

/**
 *
 */
public class MongoDbJpaRepositoryProvider extends RepositoryProvider {

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 * @param repositoryType
	 */
	public MongoDbJpaRepositoryProvider() {
		super(IMondoDbRepositoryConstants.PROVIDER_ID_JPA, EclipseLinkRepository.class);
	}

	@Override
	public Repository createRepositoryInstance(final String repositoryId, final IRepositoryPreferences repositoryPreferences) {
		// check that the optional dependencies are available
		try {
			MongoDbActivator.getInstance().getBundle().loadClass("org.eclipse.gyrex.persistence.eclipselink.EclipseLinkRepository");
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException("Required EclipseLink bundles not available. Please check the installation.", e);
		}
		return new MongoDbJpaRepositoryImpl(repositoryId, this, repositoryPreferences);
	}

}
