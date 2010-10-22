/*******************************************************************************
 * Copyright (c) 2008, 2010 AGETO Service GmbH and others.
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gyrex.cds.facets.IFacet;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Allows to query for documents.
 * <p>
 * The query object defines general querying capabilities for documents. It
 * allows to specify a query and a common set of additional features (eg.
 * filters, facets, grouping, etc.).
 * </p>
 * <p>
 * This interface must be implemented by contributors of a document model
 * implementation. As such it is considered part of a service provider API which
 * may evolve faster than the general API. Please get in touch with the
 * development team through the prefered channels listed on <a
 * href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay up-to-date
 * of possible changes.
 * </p>
 * <p>
 * Clients may not implement or extend this interface directly. If
 * specialization is desired they should look at the options provided by the
 * model implementation.
 * </p>
 */
public interface IQuery extends IAdaptable {

	/**
	 * Adds an attribute filter
	 * 
	 * @param attributeId
	 *            the attribute id to filter on
	 * @return the added attribute filter
	 */
	public IAttributeFilter addAttributeFilter(final String attributeId);

	/**
	 * Adds a facet filter.
	 * 
	 * @param facet
	 *            the facet to filter on
	 * @return the added facet filter
	 */
	public IFacetFilter addFacetFilter(final IFacet facet);

	/**
	 * Adds a filter query.
	 * <p>
	 * The query will be added to the list of filters. Filter queries which are
	 * <code>null</code> or blank will be ignored.
	 * </p>
	 * <p>
	 * Filter queries are considered {@link #setAdvancedQuery(String) advanced
	 * queries}. Therefore, the same syntax rules apply to filter queries which
	 * also apply to advanced queries.
	 * </p>
	 * 
	 * @param filterQuery
	 *            the filter query to add
	 * @return this query object for convenience
	 * @see #setAdvancedQuery(String)
	 */
	public IQuery addFilterQuery(final String filterQuery);

	/**
	 * Adds a field for sorting the result.
	 * <p>
	 * The field and direction will be added to the map of fields to sort on.
	 * Blank or <code>null</code> field names will be ignored.
	 * </p>
	 * <p>
	 * Note, an existing entry for the field will be removed prior to adding the
	 * new value.
	 * </p>
	 * 
	 * @param fieldName
	 *            the field name
	 * @param direction
	 *            the sort direction
	 * @return this query object for convenience
	 * @see #setSortField(String, SortDirection)
	 */
	public IQuery addSortField(final String fieldName, final SortDirection direction);

	/**
	 * Returns the advanced query.
	 * 
	 * @return the advanced query (maybe <code>null</code>)
	 * @see #setAdvancedQuery(String)
	 */
	public String getAdvancedQuery();

	/**
	 * Returns the list of attribute filters.
	 * 
	 * @return an unmodifiable list of attribute filters
	 */
	public List<IAttributeFilter> getAttributeFilters();

	/**
	 * Returns the list of facet filters.
	 * 
	 * @return an unmodifiable list of facet filters
	 */
	public List<IFacetFilter> getFacetFilters();

	/**
	 * Returns the list of filter queries.
	 * 
	 * @return an unmodifiable list of filter queries
	 */
	public List<String> getFilterQueries();

	/**
	 * Returns the maximum results to return.
	 * 
	 * @return the maximum results to return
	 * @see #setMaxResults(int)
	 */
	public int getMaxResults();

	/**
	 * Returns the query.
	 * 
	 * @return the query (maybe <code>null</code>)
	 * @see #setQuery(String)
	 */
	public String getQuery();

	/**
	 * Returns the result projection.
	 * 
	 * @return the result projection
	 * @see #setResultProjection(ResultProjection)
	 */
	public ResultProjection getResultProjection();

	/**
	 * Returns the map of sort fields.
	 * <p>
	 * The map is internally backed by a {@link LinkedHashMap} to represent the
	 * exact sort order for multiple fields.
	 * </p>
	 * <p>
	 * Note, the returned map does not allow modifications.
	 * </p>
	 * 
	 * @return the map of fields to sort on
	 * @see #setSortField(String, SortDirection)
	 * @see #addSortField(String, SortDirection)
	 */
	public Map<String, SortDirection> getSortFields();

	/**
	 * Returns the start index (zero-based).
	 * 
	 * @return the start index
	 */
	public long getStartIndex();

	/**
	 * Sets an advanced query.
	 * <p>
	 * If a non-<code>null</code> and non-empty advanced query is set, it will
	 * be prefered over the {@link #setQuery(String) user query}.
	 * </p>
	 * <h2>Query Syntax</h2>
	 * <p>
	 * There are no strict rules about an advanced query syntax. It's completely
	 * up to the underlying search implementation. Thus, an advanced query must
	 * be considered implementation specific which tightly couples clients to a
	 * specific implementation. However, using this API method should help with
	 * an easier discovery and management of such tight couplings.
	 * </p>
	 * <h3>Escaping Special Characters</h3>
	 * <p>
	 * Each advanced syntax comes with a set of special chars that serve special
	 * purposes. In order to aid API clients and implementors the content
	 * delivery service supports escaping special characters that are consider
	 * part of an advanced query syntax. The current list special characters are
	 * </p>
	 * <p>
	 * + - &amp; | ! ( ) { } [ ] &lt; &gt; ^ " ~ * ? : \
	 * <code>{@link Character#isWhitespace(char) &lt;any whitespace&gt;}</code>
	 * </p>
	 * <p>
	 * To escape these character use the \ before the character. For example to
	 * search for (1+1):2 use the query:
	 * </p>
	 * <p>
	 * <code>\(1\+1\)\:2</code>
	 * </p>
	 * <p>
	 * A {@link QueryUtil#escapeQueryChars(String) utility method} is available
	 * which is capable of escaping strings. Clients are advised to use this
	 * method in order to be protected from future modifications to the list of
	 * chars.
	 * </p>
	 * 
	 * @param advancedQuery
	 *            the advancedQuery to set
	 * @return this query object for convenience
	 * @see #setQuery(String)
	 */
	public IQuery setAdvancedQuery(final String advancedQuery);

	/**
	 * Sets the maximum results to return.
	 * <p>
	 * The default value is <code>10</code>.
	 * </p>
	 * <p>
	 * Note, despite the number set here the content delivery service
	 * implementation might enforce a specific limit if the number is too high
	 * to ensure scalability.
	 * </p>
	 * <p>
	 * This is typically used with {@link #setStartIndex(int) a start index} for
	 * paging. As a recommended practice the number should be less than 100.
	 * Pages with more than 100 items don't really make sense anyway.
	 * </p>
	 * <p>
	 * If you want to retrieve <em>all</em> documents then multiple requests
	 * should be issued one after each other in a resource friendly way.
	 * Otherwise Gyrex may deny requests based on configured capacity/throughput
	 * limits.
	 * </p>
	 * 
	 * @param maxResults
	 *            the maximum results to return
	 * @return this query object for convenience
	 * @see #setStartIndex(int)
	 */
	public IQuery setMaxResults(final int maxResults);

	/**
	 * Sets the query.
	 * <p>
	 * The query is designed to support raw input strings provided by users with
	 * no special escaping. <code>'+'</code> and <code>'-'</code> characters are
	 * treated as <em>"mandatory"</em> and <em>"prohibited"</em> modifiers for
	 * the subsequent terms. Text wrapped in <em>balanced</em> quote characters
	 * <code>'"'</code> are treated as phrases, any query containing an odd
	 * number of quote characters is evaluated as if there were no quote
	 * characters at all.
	 * </p>
	 * <p>
	 * Support for wildcards in a query is optional. Therefore, clients must
	 * check with the underlying search technology if wildcard search is
	 * possible.
	 * </p>
	 * <p>
	 * Note, if an {@link #setAdvancedQuery(String) advanced query} is set, the
	 * query set here will be ignored.
	 * </p>
	 * 
	 * @param query
	 *            the query to set
	 * @return this query object for convenience
	 * @see #setAdvancedQuery(String)
	 */
	public IQuery setQuery(final String query);

	/**
	 * Sets the result data projection.
	 * <p>
	 * The result dimension is used to optimize the amount of fields contained
	 * in the result. The default is {@link ResultProjection#COMPACT} which only
	 * retrieves minimal amount of fields sufficient for presenting documents in
	 * overview lists or galleries. The actual fields are implementation/context
	 * specific.
	 * </p>
	 * <p>
	 * {@link ResultProjection#FULL} will instruct the content delivery service
	 * implementation to return all available fields for a document. There will
	 * be a performance impact here especially if a document contains very large
	 * fields.
	 * </p>
	 * 
	 * @param resultProjection
	 *            the result projection to set
	 */
	public void setResultProjection(final ResultProjection resultProjection);

	/**
	 * Sets a field for sorting the result.
	 * <p>
	 * All existing sort entries will be cleared before setting the new sort
	 * field. Blank or <code>null</code> field names will be ignored.
	 * </p>
	 * 
	 * @param fieldName
	 *            the field name
	 * @param direction
	 *            the sort direction
	 * @return this query object for convenience
	 * @see #addSortField(String, SortDirection)
	 */
	public IQuery setSortField(final String fieldName, final SortDirection direction);

	/**
	 * Sets the start index (zero-based).
	 * <p>
	 * The start index is typically used for paging scenarios. The default value
	 * is <code>0</code>.
	 * </p>
	 * 
	 * @param startIndex
	 *            the start index to set
	 * @return this query object for convenience
	 * @see #setMaxResults(int)
	 */
	public IQuery setStartIndex(final long startIndex);

}
