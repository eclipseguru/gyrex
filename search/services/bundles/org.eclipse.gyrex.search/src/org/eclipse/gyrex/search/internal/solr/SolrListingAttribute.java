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

import java.util.Collection;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.eclipse.core.runtime.PlatformObject;

/**
 *
 */
public class SolrListingAttribute extends PlatformObject implements IDocumentAttribute {

	private final String name;
	private final Collection<Object> values;

	/**
	 * Creates a new instance.
	 * 
	 * @param name
	 * @param values
	 */
	public SolrListingAttribute(final String name, final Collection<Object> values) {
		this.name = name;
		this.values = values;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListingAttribute#getName()
	 */
	@Override
	public String getId() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListingAttribute#getValues()
	 */
	@Override
	public Collection<Object> getValues() {
		return values;
	}

}
