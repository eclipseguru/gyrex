/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.persistence.solr.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
	static {
		final Bundle bundle = SolrActivator.getInstance().getBundle();

		try {
			commonsHttpSolrServerConstructor = bundle.loadClass("org.apache.solr.client.solrj.impl.CommonsHttpSolrServer").getConstructor(String.class);
		} catch (Exception | AssertionError | LinkageError e) {
			LOG.debug("Error loading SolrJ CommonsHttpSolrServer!", e);
		}
	}

	public static SolrServer createDefaultSolrServer(final String urlString) throws MalformedURLException {
		// try CommonsHttpSolrServer
		if (commonsHttpSolrServerConstructor != null) {
			try {
				return (SolrServer) commonsHttpSolrServerConstructor.newInstance(urlString);
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

	private static void optimizeForRead(final SolrServer solrServerForRead) {
		try {
			BeanUtils.setProperty(solrServerForRead, "connectionTimeout", new Integer(1000));
			BeanUtils.setProperty(solrServerForRead, "connectionManagerTimeout", new Integer(1000));
			BeanUtils.setProperty(solrServerForRead, "soTimeout", new Integer(5000));
		} catch (Exception | LinkageError | AssertionError e) {
			LOG.error("Unable to optimize Solr server object ({}) for read. {}", solrServerForRead, ExceptionUtils.getRootCauseMessage(e), e);
		}
	}

}
