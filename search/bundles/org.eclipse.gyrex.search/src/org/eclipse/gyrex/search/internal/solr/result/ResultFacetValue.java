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
 *******************************************************************************/
package org.eclipse.gyrex.search.internal.solr.result;

import org.eclipse.gyrex.search.result.IResultFacetValue;

/**
 * {@link IResultFacetValue} implementation
 */
public class ResultFacetValue implements IResultFacetValue {

	private final long count;
	private final String value;

	/**
	 * Creates a new instance.
	 * 
	 * @param count
	 */
	public ResultFacetValue(final long count, final String value) {
		this.count = count;
		this.value = value;
	}

	@Override
	public long getCount() {
		return count;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value + "(" + count + ")";
	}

}
