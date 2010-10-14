package org.eclipse.gyrex.cds.facets;

/**
 * A selection strategy is used to decide about the behavior when the facet
 * is used.
 */
public enum SelectionStrategy {
	/**
	 * Single selection is used when the facet can only be selected once
	 * time. If a facet value is selected all other values are discarded.
	 */
	SINGLE,

	/**
	 * Multi-selection indicates that multiple values of a facet can be
	 * selected.
	 */
	MULTI
}