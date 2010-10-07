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
package org.eclipse.gyrex.cds.solr.internal;

import org.eclipse.gyrex.cds.model.IListingManager;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.model.common.provider.BaseModelManager;
import org.eclipse.gyrex.model.common.provider.ModelProvider;
import org.eclipse.gyrex.persistence.solr.internal.SolrRepository;
import org.eclipse.gyrex.persistence.storage.Repository;
import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;

/**
 * 
 */
public class SolrListingsModelProvider extends ModelProvider {

	/**
	 * Creates a new instance.
	 */
	/*package*/SolrListingsModelProvider() {
		super(new RepositoryContentType("application", "x-cf-listings-solr", SolrRepository.class.getName(), ListingsSolrModelActivator.getInstance().getBundleVersion().toString()), IListingManager.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.model.common.provider.ModelProvider#createModelManagerInstance(java.lang.Class, org.eclipse.gyrex.persistence.storage.Repository, org.eclipse.gyrex.context.IRuntimeContext)
	 */
	@Override
	public BaseModelManager createModelManagerInstance(final Class modelManagerType, final Repository repository, final IRuntimeContext context) {
		if (IListingManager.class.equals(modelManagerType) && (repository instanceof SolrRepository)) {
			return new SolrListingsManager(context, (SolrRepository) repository);
		}
		return null;
	}

}
