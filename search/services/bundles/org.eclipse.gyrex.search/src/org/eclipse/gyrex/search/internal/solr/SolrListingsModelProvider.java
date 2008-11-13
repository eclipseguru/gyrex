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
package org.eclipse.cloudfree.listings.model.solr.internal;


import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.listings.model.IListingManager;
import org.eclipse.cloudfree.model.common.provider.BaseModelManager;
import org.eclipse.cloudfree.model.common.provider.ModelProvider;
import org.eclipse.cloudfree.persistence.solr.internal.SolrRepository;
import org.eclipse.cloudfree.persistence.storage.Repository;
import org.eclipse.cloudfree.persistence.storage.content.ContentType;

/**
 * 
 */
public class SolrListingsModelProvider extends ModelProvider {

	/**
	 * Creates a new instance.
	 */
	/*package*/SolrListingsModelProvider() {
		super(new ContentType("application/x-cf-listings-solr", ListingsSolrModelActivator.getInstance().getBundleVersion().toString()), IListingManager.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.model.common.provider.ModelProvider#createModelManagerInstance(java.lang.Class, org.eclipse.cloudfree.persistence.storage.Repository, org.eclipse.cloudfree.common.context.IContext)
	 */
	@Override
	public BaseModelManager createModelManagerInstance(final Class modelManagerType, final Repository repository, final IContext context) {
		if (IListingManager.class.equals(modelManagerType) && (repository instanceof SolrRepository)) {
			return new SolrListingsManager(context, (SolrRepository) repository);
		}
		return null;
	}

}
