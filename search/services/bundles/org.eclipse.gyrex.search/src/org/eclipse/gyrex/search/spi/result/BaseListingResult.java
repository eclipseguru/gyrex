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
package org.eclipse.cloudfree.services.listings.implementors;


import org.eclipse.cloudfree.common.context.IContext;
import org.eclipse.cloudfree.services.listings.query.ListingQuery;
import org.eclipse.cloudfree.services.listings.restult.IListingResult;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.PlatformObject;

/**
 * {@link IListingResult} base class for listing service implementations.
 */
public abstract class BaseListingResult extends PlatformObject implements IListingResult {

	/** the query */
	private final ListingQuery query;

	/** the context */
	private final IContext context;

	/**
	 * Called by subclasses to initialize the result with the original query.
	 * 
	 * @param query
	 *            the original query as submitted to the listing service
	 */
	protected BaseListingResult(final IContext context, final ListingQuery query) {
		if (null == context) {
			throw new IllegalArgumentException("context may not be null");
		}
		if (null == query) {
			throw new IllegalArgumentException("query may not be null");
		}
		this.context = context;
		this.query = query;
	}

	/**
	 * Returns an object which is an instance of the given class associated with
	 * this object. Returns <code>null</code> if no such object can be found.
	 * <p>
	 * This implementation of the method declared by <code>IAdaptable</code>
	 * passes the request along to the context and then to the platform's
	 * adapter service; roughly <code>getContext().getAdapter(adapter)</code>
	 * and if the first call returned <code>null</code>
	 * <code>IAdapterManager.getAdapter(this, adapter)</code>. Subclasses may
	 * override this method (however, if they do so, they must invoke the method
	 * on their superclass to ensure that the context and the Platform's adapter
	 * manager is consulted).
	 * </p>
	 * 
	 * @param adapter
	 *            the class to adapt to
	 * @return the adapted object or <code>null</code>
	 * @see IAdaptable#getAdapter(Class)
	 */
	@Override
	public Object getAdapter(final Class adapter) {
		// ask the context first
		final Object contextAdapter = getContext().getAdapter(adapter);
		if (null != contextAdapter) {
			return contextAdapter;
		}

		// fallback to adapter manager
		return super.getAdapter(adapter);
	}

	@Override
	public final IContext getContext() {
		return context;
	}

	@Override
	public final ListingQuery getQuery() {
		return query;
	}
}
