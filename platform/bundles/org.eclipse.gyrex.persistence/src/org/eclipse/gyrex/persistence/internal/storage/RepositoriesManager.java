/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.cloudfree.persistence.internal.storage;

import java.text.MessageFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


import org.eclipse.cloudfree.configuration.preferences.PlatformScope;
import org.eclipse.cloudfree.persistence.internal.PersistenceActivator;
import org.eclipse.cloudfree.persistence.storage.Repository;
import org.eclipse.cloudfree.persistence.storage.registry.IRepositoryRegistry;
import org.eclipse.cloudfree.persistence.storage.settings.IRepositoryPreferences;
import org.eclipse.cloudfree.persistence.storage.type.RepositoryType;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * The platform repository manager stores repository information.
 */
public class RepositoriesManager implements IRepositoryRegistry {

	/**
	 * A repository definition.
	 */
	private static class RepositoryDefinition {
		private static final String KEY_TYPE = "type";
		private static final String NODE_PROPERTIES = "properties";
		private final RepositoryDefinitionsStore store;
		private final String repositoryId;

		/**
		 * Creates a new instance.
		 * 
		 * @param repositoryId
		 * @param repositoryType
		 */
		protected RepositoryDefinition(final String repositoryId, final RepositoryDefinitionsStore repositoryStore) {
			this.repositoryId = repositoryId;
			store = repositoryStore;
		}

		private void checkExist() {
			if (!exists()) {
				throw new IllegalStateException(MessageFormat.format("Repository definition for repository ''{0}'' does not exist!", repositoryId));
			}
		}

		public boolean exists() {
			try {
				return store.storage.nodeExists(repositoryId);
			} catch (final BackingStoreException e) {
				// ignore
				return false;
			}
		}

		private Preferences getNode() {
			return store.storage.node(repositoryId);
		}

		/**
		 * @return
		 */
		public IRepositoryPreferences getPreferences() {
			// TODO implement me using secure storage
			return null;
		}

		/**
		 * @return
		 */
		public Dictionary<String, String> getProperties() {
			try {
				final String propertiesNodePath = repositoryId + "/" + NODE_PROPERTIES;
				if (!store.storage.nodeExists(propertiesNodePath)) {
					return null;
				}

				final Preferences node = store.storage.node(propertiesNodePath);
				final String[] keys = node.keys();
				final Dictionary<String, String> properties = new Hashtable<String, String>(keys.length);
				for (final String key : keys) {
					properties.put(key, node.get(key, ""));
				}
				return properties;
			} catch (final Exception e) {
				// TODO should log
				// ignore
				return null;
			}
		}

		public String getType() {
			checkExist();
			return getNode().get(KEY_TYPE, null);

		}

	}

	/**
	 * The repository definitions store.
	 */
	private static class RepositoryDefinitionsStore {

		private IEclipsePreferences storage;
		private String[] repositories;

		public RepositoryDefinition get(final String repositoryId) {
			if (null == storage) {
				throw new IllegalStateException("not loaded");
			}

			final RepositoryDefinition repositoryDefinition = new RepositoryDefinition(repositoryId, this);
			if (!repositoryDefinition.exists()) {
				return null;
			}

			return repositoryDefinition;
		}

		/**
		 * Returns the repositories.
		 * 
		 * @return the repositories
		 */
		public String[] getRepositories() {
			return repositories;
		}

		/**
		 * Loads the store
		 */
		public synchronized void load() {
			storage = (IEclipsePreferences) new PlatformScope().getNode(PersistenceActivator.PLUGIN_ID).node("repositories");
			try {
				repositories = storage.childrenNames();
			} catch (final Exception e) {
				// TODO log error
				// no children; ignore for now
				repositories = new String[0];
			}
		}

	}

	private static void errorCreatingRepository(final RepositoryDefinition repositoryDef, final String detail) {
		throw new IllegalStateException(MessageFormat.format("Invalid repository definition ''{0}'': {1}", repositoryDef.repositoryId, detail));
	}

	private RepositoryDefinitionsStore repositoryDefinitionsStore;

	private final ConcurrentMap<String, Lock> locksByRepositoryId = new ConcurrentHashMap<String, Lock>(4);
	private final ConcurrentMap<String, Repository> repositoryCache = new ConcurrentHashMap<String, Repository>(4);

	/**
	 * Creates a repository from a definition
	 * 
	 * @param repositoryDef
	 * @return
	 * @throws IllegalStateException
	 *             if the repository could not be created
	 */
	private Repository createRepository(final RepositoryDefinition repositoryDef) throws IllegalStateException {
		final String type = repositoryDef.getType();
		if (null == type) {
			errorCreatingRepository(repositoryDef, "invalid type");
		}

		// get repository type
		final RepositoryType repositoryType = PersistenceActivator.getInstance().getRepositoryTypeRegistry().getRepositoryType(type);

		// get repository settings
		final IRepositoryPreferences repositoryPreferences = repositoryDef.getPreferences();

		// create repository instance
		final Repository repository = repositoryType.newRepositoryInstance(repositoryDef.repositoryId, repositoryPreferences);
		if (null == repository) {
			errorCreatingRepository(repositoryDef, MessageFormat.format("repository type ''{0}'' returned no repository instance", type));
		}

		return repository;
	}

	private Lock getOrCreateRepositoryLock(final String repositoryId) {
		Lock lock = locksByRepositoryId.get(repositoryId);
		if (lock == null) {
			final Lock newLock = new ReentrantLock();
			lock = locksByRepositoryId.putIfAbsent(repositoryId, newLock);
			if (lock == null) {
				// put succeeded, use new value
				lock = newLock;
			}
		}
		return lock;
	}

	/**
	 * Returns the repository with the specified id.
	 * 
	 * @param repositoryId
	 *            the repository id.
	 * @return the repository
	 * @throws IllegalStateException
	 *             if a repository with the specified id is not available
	 */
	public Repository getRepository(final String repositoryId) throws IllegalStateException {
		if (null == repositoryId) {
			throw new IllegalArgumentException("repository id must not be null");
		}

		// lookup a cached instance
		Repository repository = repositoryCache.get(repositoryId.intern());
		if (null != repository) {
			return repository;
		}

		// open store if necessary
		if (null == repositoryDefinitionsStore) {
			open();
		}

		// get repository definition
		final RepositoryDefinition repositoryDef = repositoryDefinitionsStore.get(repositoryId);
		if (null == repositoryDef) {
			throw new IllegalStateException(MessageFormat.format("The repository with id \"{0}\" could not be found!", repositoryId));
		}

		// create a new instance
		final Lock repositoryCreationLock = getOrCreateRepositoryLock(repositoryId);
		repositoryCreationLock.lock();
		try {
			// make sure the cache is empty
			repository = repositoryCache.get(repositoryId.intern());
			if (null != repository) {
				// use cached repository
				return repository;
			}

			// create the repository
			repository = createRepository(repositoryDef);

			// put the repository instance in the cache
			repositoryCache.put(repositoryId.intern(), repository);

			// return the repository
			return repository;
		} finally {
			repositoryCreationLock.unlock();
		}
	}

	private synchronized void open() {
		if (null != repositoryDefinitionsStore) {
			return;
		}

		repositoryDefinitionsStore = new RepositoryDefinitionsStore();
		repositoryDefinitionsStore.load();
	}

}