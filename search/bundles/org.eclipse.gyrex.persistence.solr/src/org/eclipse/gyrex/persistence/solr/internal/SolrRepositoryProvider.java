/*******************************************************************************
 * Copyright (c) 2008, 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *     Konrad Schergaut - Repository creation for Solr cloud repositories (https://bugs.eclipse.org/bugs/show_bug.cgi?id=411016)
 *******************************************************************************/
package org.eclipse.gyrex.persistence.solr.internal;

import java.net.MalformedURLException;

import org.eclipse.gyrex.persistence.solr.ISolrRepositoryConstants;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.solr.config.SolrServerType;
import org.eclipse.gyrex.persistence.storage.Repository;
import org.eclipse.gyrex.persistence.storage.provider.RepositoryProvider;
import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;

import org.osgi.service.prefs.BackingStoreException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.solr.client.solrj.SolrServer;

/**
 * A repository provider for Solr repositories.
 */
public class SolrRepositoryProvider extends RepositoryProvider {

	/**
	 * preference key for {@link SolrServerType server type} setting (value
	 * <code>serverType</code>)
	 */
	public static final String PREF_KEY_SERVER_TYPE = "serverType";

	/**
	 * preference key for a Solr base URL to be used with
	 * {@link SolrServerType#REMOTE remote} Solr servers (value
	 * <code>serverUrl</code>)
	 */
	public static final String PREF_KEY_SERVER_URL = "serverUrl";

	/**
	 * preference key for zk Host ensemble to be used with
	 * {@link SolrServerType#CLOUD cloud} Solr servers (value
	 * <code>zkHosts</code>).<br/>
	 * The value might be a csv-list equivalent to SolrCloud setup.
	 */
	public static final String PREF_KEY_ZK_HOSTS = "zkHosts";

	/**
	 * preference key for a Solr base URL to be used with
	 * {@link SolrServerType#REMOTE remote} Solr servers (value
	 * <code>serverUrl</code>)
	 */
	public static final String PREF_KEY_SERVER_READ_URLS = "serverReadUrls";

	/**
	 * Creates a new instance.
	 */
	public SolrRepositoryProvider() {
		super(ISolrRepositoryConstants.PROVIDER_ID, SolrServerRepository.class);
	}

	@Override
	public Repository createRepositoryInstance(final String repositoryId, final IRepositoryPreferences repositoryPreferences) {
		return new SolrRepository(repositoryId, this, createServers(repositoryId, repositoryPreferences));
	}

	private SolrServer[] createServers(final String repositoryId, final IRepositoryPreferences repositoryPreferences) {
		final String typeStr = repositoryPreferences.get(PREF_KEY_SERVER_TYPE, null);
		final SolrServerType serverType = typeStr == null ? SolrServerType.EMBEDDED : SolrServerType.valueOf(typeStr);
		switch (serverType) {
			case EMBEDDED:
				final SolrServer embeddedServer = SolrServerFactory.createEmbeddedServer(repositoryId);
				return new SolrServer[] { embeddedServer, embeddedServer };

			case REMOTE:
				final String masterUrlString = repositoryPreferences.get(PREF_KEY_SERVER_URL, null);
				try {
					// master server first
					final SolrServer masterServer = SolrServerFactory.createDefaultSolrServer(masterUrlString);

					// read servers
					final SolrServer readServer;
					final String[] readUrlKeys = repositoryPreferences.getKeys(PREF_KEY_SERVER_READ_URLS);
					if ((null == readUrlKeys) || (readUrlKeys.length == 0)) {
						readServer = SolrServerFactory.createReadOptimizedServer(masterUrlString);
					} else if (readUrlKeys.length == 1) {
						readServer = SolrServerFactory.createReadOptimizedServer(repositoryPreferences.get(PREF_KEY_SERVER_READ_URLS + "//" + readUrlKeys[0], null));
					} else {
						// need to convert positions to URLs
						final String[] urls = new String[readUrlKeys.length];
						try {
							for (int i = 0; i < readUrlKeys.length; i++) {
								final int pos = NumberUtils.toInt(readUrlKeys[i], -1);
								urls[pos] = repositoryPreferences.get(PREF_KEY_SERVER_READ_URLS + "//" + readUrlKeys[i], null);
							}
						} catch (final IndexOutOfBoundsException e) {
							throw new IllegalStateException(String.format("Unable to read replica urls for repository %s. %s", repositoryId, e.getMessage()), e);
						}
						readServer = SolrServerFactory.createLoadBalancingReadOptimizedServer(urls);
					}

					// done
					return new SolrServer[] { masterServer, readServer };
				} catch (final MalformedURLException e) {
					throw new IllegalStateException(String.format("Repository %s not configured correctly. Server URL '%s' is invalid.  %s", repositoryId, masterUrlString, e.getMessage()));
				} catch (final BackingStoreException e) {
					throw new IllegalStateException(String.format("Unable to read repository settings for repository %s. %s", repositoryId, e.getMessage()), e);
				}
			case CLOUD:
				final String zkHost = repositoryPreferences.get(PREF_KEY_ZK_HOSTS, null);
				try {
					final SolrServer cloudSolrServer = SolrServerFactory.createCloudSolrServer(zkHost, repositoryId);
					return new SolrServer[] { cloudSolrServer, cloudSolrServer };
				} catch (final MalformedURLException e) {
					throw new IllegalStateException(String.format("Repository %s not configured correctly. Zookeeper Host(s) '%s' invalid.  %s", repositoryId, zkHost, ExceptionUtils.getRootCauseMessage(e)));
				}

			default:
				throw new IllegalStateException(String.format("Repository %s not configured correctly. Unsupported server type %s", repositoryId, typeStr));
		}
	}

}
