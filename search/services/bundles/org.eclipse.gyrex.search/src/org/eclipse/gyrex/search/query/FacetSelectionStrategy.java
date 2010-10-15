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
 *******************************************************************************/
package org.eclipse.gyrex.cds.query;

/**
 * A selection strategy is used to decide about the behavior when the facet is
 * used.
 */
public enum FacetSelectionStrategy {
	/**
	 * This instructs a query to not return counts for any other facet value
	 * once a facet has been selected.
	 */
	SINGLE,

	/**
	 * This instructs a query to return counts for not selected values as if a
	 * facet filter had not yet been applied.
	 */
	MULTI
}