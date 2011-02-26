/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.tests;

import org.eclipse.gyrex.cds.BaseFacetManager;
import org.eclipse.gyrex.cds.BaseSolrDocumentManager;
import org.eclipse.gyrex.cds.documents.IDocumentManager;
import org.eclipse.gyrex.cds.facets.IFacetManager;
import org.eclipse.gyrex.common.runtime.BaseBundleActivator;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.provider.RuntimeContextObjectProvider;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.persistence.context.preferences.ContextPreferencesRepository;
import org.eclipse.gyrex.persistence.solr.SolrServerRepository;
import org.eclipse.gyrex.persistence.storage.Repository;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryRegistry;
import org.osgi.framework.BundleContext;

public class SolrCdsTestsActivator extends BaseBundleActivator {

	/** BSN */
	private static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cds.solr.tests";

	private static SolrCdsTestsActivator instance;

	static SolrCdsTestsActivator getInstance() {
		final SolrCdsTestsActivator instance = SolrCdsTestsActivator.instance;
		if (instance == null) {
			throw new IllegalStateException("inactive");
		}
		return instance;
	}

	private IServiceProxy<IRepositoryRegistry> repositoryRegistryProxy;

	public SolrCdsTestsActivator() {
		super(SYMBOLIC_NAME);
	}

	@Override
	protected void doStart(final BundleContext context) throws Exception {
		instance = this;
		repositoryRegistryProxy = getServiceHelper().trackService(IRepositoryRegistry.class);
		
		getServiceHelper().registerService(RuntimeContextObjectProvider.class.getName(), new FacetModelProvider(), "Eclipse Gyrex", "Gyrex Test Facet Model Implementation", null, null);
		getServiceHelper().registerService(RuntimeContextObjectProvider.class.getName(), new DocumentModelProvider(), "Eclipse Gyrex", "Gyrex Test Document Model Implementation", null, null);
		
	}

	@Override
	protected void doStop(final BundleContext context) throws Exception {
		instance = null;
	}

	public IRepositoryRegistry getRepositoryRegistry() {
		final IServiceProxy<IRepositoryRegistry> proxy = repositoryRegistryProxy;
		if (proxy == null) {
			throw createBundleInactiveException();
		}
		return proxy.getService();
	}
	
	private class FacetModelProvider extends ModelProvider {
		
		public FacetModelProvider() {
			super(FacetManagerTest.FACET_CONTENT_TYPE, IFacetManager.class);
		}
		
		@Override
		public BaseModelManager createModelManagerInstance(Class modelManagerType, Repository repository,
				IRuntimeContext context) {
			return new BaseFacetManager(context, (ContextPreferencesRepository) repository, SYMBOLIC_NAME + ".facets"){};
		}
	}
	
	private class DocumentModelProvider extends ModelProvider {
		
		public DocumentModelProvider() {
			super(DocumentManagerTest.DOCUMENT_CONTENT_TYPE, IDocumentManager.class);
		}
		
		@Override
		public BaseModelManager createModelManagerInstance(Class modelManagerType, Repository repository,
				IRuntimeContext context) {
			return new BaseSolrDocumentManager(context, (SolrServerRepository) repository, SYMBOLIC_NAME + ".documents"){};
		}
	}

}
