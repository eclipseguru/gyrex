package org.eclipse.gyrex.cds.query;

/**
 * The result dimension indicates what amount of fields the result should
 * contain.
 */
public enum ResultProjection {
	/** a compact set of document data */
	COMPACT,

	/** full set of document data */
	FULL;
}