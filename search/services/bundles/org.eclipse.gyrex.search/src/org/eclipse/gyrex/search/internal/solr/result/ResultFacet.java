/*******************************************************************************
 * Copyright (c) 2008, 2010 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.search.internal.solr.result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.search.facets.IFacet;
import org.eclipse.gyrex.search.result.IResultFacet;
import org.eclipse.gyrex.search.result.IResultFacetValue;

public class ResultFacet implements IResultFacet {

	private final IFacet facet;
	private final Map<String, IResultFacetValue> values;

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 * @param label
	 */
	public ResultFacet(final IFacet facet) {
		this.facet = facet;
		values = new HashMap<String, IResultFacetValue>(4);
	}

	void addValue(final ResultFacetValue value) {
		values.put(value.getValue(), value);
	}

	@Override
	public IFacet getFacet() {
		return facet;
	}

	@Override
	public Map<String, IResultFacetValue> getValues() {
		return Collections.unmodifiableMap(values);
	}

	@Override
	public String toString() {
		return facet.getAttributeId() + ":" + values.values();
	}
}
