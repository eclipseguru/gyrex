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
package org.eclipse.cloudfree.cds.model.solr.internal;

import java.util.Collection;


import org.eclipse.cloudfree.cds.model.IListingAttribute;
import org.eclipse.core.runtime.PlatformObject;

/**
 * 
 */
public class SolrListingAttribute extends PlatformObject implements IListingAttribute {

	private final String name;
	private final Object[] values;

	/**
	 * Creates a new instance.
	 * 
	 * @param name
	 * @param values
	 */
	public SolrListingAttribute(final String name, final Collection<Object> values) {
		this.name = name;
		this.values = values.toArray(new Object[values.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.cds.model.IListingAttribute#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.cds.model.IListingAttribute#getValues()
	 */
	@Override
	public Object[] getValues() {
		return values;
	}

}
