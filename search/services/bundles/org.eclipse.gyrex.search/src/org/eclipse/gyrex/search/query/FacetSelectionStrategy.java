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