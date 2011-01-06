/*******************************************************************************
 * Copyright (c) 2008, 2009 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *	   Mike Tschierschke - customization for rap based admin ui,
 *     						added method getRepositoryDefinition

 *******************************************************************************/
package org.eclipse.gyrex.persistence.storage.registry;

import java.util.List;

import org.eclipse.gyrex.persistence.storage.provider.RepositoryProvider;
import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;

/**
 * The repository registry provides administrative access to repositories
 * defined in the system.
 * <p>
 * Although this interface is made available as an OSGi service, security
 * restrictions may be used to prevent global access to the
 * {@link IRepositoryRegistry}.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRepositoryRegistry {

	/**
	 * Creates a repository of the specified type.
	 * 
	 * @param repositoryId
	 *            the repository id (may not be <code>null</code>)
	 * @param repositoryProviderId
	 *            the {@link RepositoryProvider#getProviderId() repository
	 *            provider id} (may not be <code>null</code>)
	 * @return the preferences to store repository specific configuration
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 */
	IRepositoryPreferences createRepository(String repositoryId, String repositoryProviderId) throws IllegalArgumentException;

	/**
	 * Used for administrative purposes.
	 * 
	 * @return an <b>unmodifiable</b> List wit the ids of all registered
	 *         Repositories. May be empty but never <code>null</code>.
	 */
	public List<String> getAllRegisteredRepositories();

	/**
	 * Retrieves the repository definition for the specified repository.
	 * 
	 * @param repositoryId
	 *            the repository id
	 * @return the repository definition (maybe <code>null</code> if the
	 *         repository does not exist)
	 */
	public IRepositoryDefinition getRepositoryDefinition(String repositoryId);

	/**
	 * Removes a repository.
	 * 
	 * @param repositoryId
	 *            the id of the repository to undefine (may not be
	 *            <code>null</code>)
	 * @throws IllegalArgumentException
	 *             if any of the arguments is invalid
	 */
	void removeRepository(String repositoryId) throws IllegalArgumentException;
}
