/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Konrad Schergaut - Bug 407535
 *     Konrad Schergaut - introducing handling for cloud solr server (https://bugs.eclipse.org/bugs/show_bug.cgi?id=411016)
 *******************************************************************************/
package org.eclipse.gyrex.persistence.solr.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.osgi.framework.Bundle;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating SolrJ servers instances.
 */
public class SolrServerFactory {

	private static final Logger LOG = LoggerFactory.getLogger(SolrServerFactory.class);

	private static Constructor<?> commonsHttpSolrServerConstructor;
	private static Constructor<?> httpSolrServerConstructor;
	private static Constructor<?> cloudSolrServerConstructor;
	static {
		final Bundle bundle = SolrActivator.getInstance().getBundle();

		try {
			commonsHttpSolrServerConstructor = bundle.loadClass("org.apache.solr.client.solrj.impl.CommonsHttpSolrServer").getConstructor(String.class);
		} catch (Exception | AssertionError | LinkageError e) {
			LOG.debug("Error loading SolrJ 3 CommonsHttpSolrServer!", e);
		}

		try {
			httpSolrServerConstructor = bundle.loadClass("org.apache.solr.client.solrj.impl.HttpSolrServer").getConstructor(String.class);
		} catch (Exception | AssertionError | LinkageError e) {
			LOG.debug("Error loading SolrJ 4 HttpSolrServer!", e);
		}

		try {
			cloudSolrServerConstructor = bundle.loadClass("org.apache.solr.client.solrj.impl.CloudSolrServer").getConstructor(String.class);
			LOG.info("SolrJ 4 Cloud client successfully loaded.");
		} catch (Exception | AssertionError | LinkageError e) {
			LOG.debug("Error loading SolrJ 4 CloudSolrServer!", e);
		}
	}

	public static SolrServer createCloudSolrServer(final String zkHost, final String collection) throws MalformedURLException {
		// try CommonsHttpSolrServer
		if (cloudSolrServerConstructor != null) {
			final SolrServer server = createSolrServer(zkHost, cloudSolrServerConstructor);
			try {
				final Method defaultCollectionSetter = server.getClass().getMethod("setDefaultCollection", String.class);
				defaultCollectionSetter.invoke(server, collection);
			} catch (final Exception e) {
				throw new IllegalStateException("No CloudSolrServer available!", e);
			}
			return server;
		}
		throw new IllegalStateException("No CloudSolrServer available!");
	}

	public static SolrServer createDefaultSolrServer(final String urlString) throws MalformedURLException {
		// try HttpSolrServer
		if (httpSolrServerConstructor != null) {
			return createSolrServer(urlString, httpSolrServerConstructor);
		}

		// try CommonsHttpSolrServer
		if (commonsHttpSolrServerConstructor != null) {
			return createSolrServer(urlString, commonsHttpSolrServerConstructor);
		}

		// give up
		throw new IllegalStateException("No compatible SolrJ API available!");
	}

	public static SolrServer createEmbeddedServer(final String repositoryId) {
		// compute the core name
		final String coreName = SolrActivator.getEmbeddedSolrCoreName(repositoryId);
		// check core
		final CoreContainer coreContainer = SolrActivator.getInstance().getEmbeddedCoreContainer();
		final SolrCore core = coreContainer.getCore(coreName);
		if (null == core) {
			throw new IllegalStateException("Solr core '" + coreName + "' not found");
		}
		core.close();
		return new EmbeddedSolrServer(coreContainer, coreName);
	}

	public static SolrServer createLoadBalancingReadOptimizedServer(final String[] readUrls) throws MalformedURLException {
		// create load balancing server
		final LBHttpSolrServer solrServerForRead = new LBHttpSolrServer(readUrls);
		optimizeForRead(solrServerForRead);
		return solrServerForRead;
	}

	public static SolrServer createReadOptimizedServer(final String urlString) throws MalformedURLException {
		final SolrServer solrServerForRead = createDefaultSolrServer(urlString);
		optimizeForRead(solrServerForRead);
		return solrServerForRead;
	}

	private static SolrServer createSolrServer(final String urlString, final Constructor<?> solrServerConstructor) throws MalformedURLException {
		try {
			return (SolrServer) solrServerConstructor.newInstance(urlString);
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getTargetException();
			if (t instanceof MalformedURLException) {
				throw (MalformedURLException) t;
			} else {
				throw new IllegalStateException("Unhandled exception creating SolrJ server instance. " + t.getMessage(), t);
			}
		} catch (final Exception | LinkageError | AssertionError e) {
			throw new IllegalStateException("Error creating SolrJ server instance. " + e.getMessage(), e);
		}
	}

	private static void optimizeForRead(final SolrServer solrServerForRead) {
		try {
			BeanUtils.setProperty(solrServerForRead, "connectionTimeout", new Integer(1000));
			BeanUtils.setProperty(solrServerForRead, "connectionManagerTimeout", new Integer(1000));
			BeanUtils.setProperty(solrServerForRead, "soTimeout", new Integer(5000));
		} catch (Exception | LinkageError | AssertionError e) {
			LOG.error("Unable to optimize Solr server object ({}) for read. {}", solrServerForRead, ExceptionUtils.getRootCauseMessage(e), e);
		}
	}

	private SolrServerFactory() {
		// empty
	}
}
