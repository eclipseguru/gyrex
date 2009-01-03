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
package org.eclipse.cloudfree.cds.service.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * A query object for listings.
 * <p>
 * The query object defines general querying capabilities for listings. It
 * allows to specify a query
 * </p>
 * <p>
 * This class may be subclassed by clients.
 * </p>
 */
public class ListingQuery {

	/**
	 * The result dimension indicates what amount of fields the result should
	 * contain.
	 */
	public static enum ResultDimension {
		COMPACT, FULL;
	}

	/**
	 * The sort direction for sorting based fields.
	 */
	public static enum SortDirection {
		ASCENDING, DESCENDING;
	}

	/** the user query */
	private String query;

	/** the advanced query */
	private String advancedQuery;

	/** the start index (zero-based) */
	private long startIndex = 0;

	/** the maximum results to return */
	private int maxResults = 10;

	/** the result dimension */
	private ResultDimension resultDimension = ResultDimension.COMPACT;

	/** the filter queries */
	private final List<String> filterQueries = new ArrayList<String>(4);

	/** the fields to sort on */
	private final LinkedHashMap<String, SortDirection> sortFields = new LinkedHashMap<String, SortDirection>(4);

	private static final Pattern escapePattern = Pattern.compile("(\\W)");

	/**
	 * Escapes the specified input string according to the query syntax escaping
	 * requirements as noted in {@link #setAdvancedQuery(String)}.
	 * 
	 * @param input
	 *            the input string
	 * @return the input string with special chars escaped.
	 * @see #setAdvancedQuery(String)
	 */
	public static String escapeQueryChars(final String input) {
		if (null == input) {
			return null;
		}
		final Matcher matcher = escapePattern.matcher(input);
		return matcher.replaceAll("\\\\$1");
	}

	/**
	 * Adds a filter query.
	 * <p>
	 * The query will be added to the list of filters. Filter queries which are
	 * <code>null</code> or blank will be ignored.
	 * </p>
	 * <p>
	 * Filter queries are used to filter the set of listings to be searched.
	 * They can be exclusive or inclusive. For example, using a filter query it
	 * is possible to exclude listings with a specific attribute (eg. not in
	 * stock) from the search. It's also possible to limit the search to only
	 * those listing with a specific attribute set (eg. with tag AAA and/or
	 * BBB).
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
	 * @see #setFilterQueries(String...)
	 * @see #setAdvancedQuery(String)
	 */
	public ListingQuery addFilterQuery(final String filterQuery) {
		if (StringUtils.isNotBlank(filterQuery)) {
			filterQueries.add(filterQuery);
		}
		return this;
	}

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
	public ListingQuery addSortField(final String fieldName, final SortDirection direction) {
		if (StringUtils.isNotBlank(fieldName)) {
			sortFields.remove(fieldName);
			sortFields.put(fieldName, direction);
		}
		return this;
	}

	/**
	 * Returns the advanced query.
	 * 
	 * @return the advanced query (maybe <code>null</code>)
	 * @see #setAdvancedQuery(String)
	 */
	public String getAdvancedQuery() {
		return advancedQuery;
	}

	/**
	 * Returns the list of filter queries.
	 * <p>
	 * Note, the returned list does not allow modifications.
	 * </p>
	 * 
	 * @return the list of filter queries
	 */
	public List<String> getFilterQueries() {
		return Collections.unmodifiableList(filterQueries);
	}

	/**
	 * Returns the maximum results to return.
	 * 
	 * @return the maximum results to return
	 * @see #setMaxResults(int)
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/**
	 * Returns the query.
	 * 
	 * @return the query (maybe <code>null</code>)
	 * @see #setQuery(String)
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Returns the result dimension.
	 * 
	 * @return the result dimension
	 * @see #setResultDimension(ResultDimension)
	 */
	public ResultDimension getResultDimension() {
		return resultDimension;
	}

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
	public Map<String, SortDirection> getSortFields() {
		return Collections.unmodifiableMap(sortFields);
	}

	/**
	 * Returns the start index (zero-based).
	 * 
	 * @return the start index
	 */
	public long getStartIndex() {
		return startIndex;
	}

	/**
	 * Sets the advanced query.
	 * <p>
	 * If a not <code>null</code> and non-empty advanced query is set, it will
	 * be prefered over the {@link #setQuery(String) user query}.
	 * </p>
	 * <h2>Query Syntax</h2>
	 * <p>
	 * In contrast to a "user" {@link #setQuery(String) query} an advanced query
	 * has a much richer but also more strict syntax. The advanced query syntax
	 * is modelled after the <a
	 * href="http://wiki.apache.org/solr/SolrQuerySyntax">Apache Solr</a> and <a
	 * href="http://lucene.apache.org/java/docs/queryparsersyntax.html">Apache
	 * Lucene</a> query syntax. All listening service implementors are required
	 * to provide a parser for reading and understanding this syntax. Therefore,
	 * it's safe for clients to rely on the syntax specified here.
	 * </p>
	 * <p>
	 * Note, in case a listening service implementation may not support all
	 * features offered by Solr/Lucene it will do its best to ensure that the
	 * query still works (for example, by ignoring those parts). However, the
	 * results might vary depending on the listing service implementation.
	 * Therefore, it's possible to prefix an advanced query with
	 * <code>"[SPECIAL:"</code> + a listening service implementation specific
	 * string + <code>"]"</code>. This will ensure that the query works only
	 * with the specified implementation. All other will ignore the query and
	 * return an empty result with an error.
	 * </p>
	 * <h3>Terms</h3>
	 * <p>
	 * A query is broken up into terms and operators. There are two types of
	 * terms: Single Terms and Phrases.
	 * </p>
	 * <p>
	 * A Single Term is a single word such as "test" or "hello".
	 * </p>
	 * <p>
	 * A Phrase is a group of words surrounded by double quotes such as
	 * "hello dolly".
	 * </p>
	 * <p>
	 * Multiple terms can be combined together with Boolean operators to form a
	 * more complex query (see below).
	 * </p>
	 * <h3>Fields</h3>
	 * <p>
	 * The listing service supports fielded data. When performing a search you
	 * can either specify a field, or use the default field. The field names and
	 * default field is implementation/context specific.
	 * </p>
	 * <p>
	 * You can search any field by typing the field name followed by a colon ":"
	 * and then the term you are looking for.
	 * </p>
	 * <p>
	 * As an example, let's assume a listing repository contains two fields,
	 * title and text and text is the default field. If you want to find the
	 * document entitled "The Right Way" which contains the text
	 * "don't go this way", you can enter:
	 * </p>
	 * <p>
	 * <code>title:"The Right Way" AND text:go</code>
	 * </p>
	 * <p>
	 * or
	 * </p>
	 * <p>
	 * <code>title:"Do it right" AND right</code>
	 * </p>
	 * <p>
	 * Since text is the default field, the field indicator is not required.
	 * </p>
	 * <p>
	 * Note: The field is only valid for the term that it directly precedes, so
	 * the query
	 * </p>
	 * <p>
	 * <code>title:Do it right</code>
	 * </p>
	 * <p>
	 * Will only find "Do" in the title field. It will find "it" and "right" in
	 * the default field (in this case the text field).
	 * </p>
	 * <h3>Term Modifiers</h3>
	 * <p>
	 * The listing service supports modifying query terms to provide a wide
	 * range of searching options.
	 * </p>
	 * <h4>Wildcard Searches</h4>
	 * <p>
	 * The listing service supports single and multiple character wildcard
	 * searches within single terms (not within phrase queries).
	 * </p>
	 * <p>
	 * To perform a single character wildcard search use the "?" symbol.
	 * </p>
	 * <p>
	 * To perform a multiple character wildcard search use the "*" symbol.
	 * </p>
	 * <p>
	 * The single character wildcard search looks for terms that match that with
	 * the single character replaced. For example, to search for "text" or
	 * "test" you can use the search:
	 * </p>
	 * <p>
	 * <code>te?t</code>
	 * </p>
	 * <p>
	 * Multiple character wildcard searches looks for 0 or more characters. For
	 * example, to search for test, tests or tester, you can use the search:
	 * </p>
	 * <p>
	 * <code>test*</code>
	 * </p>
	 * <p>
	 * You can also use the wildcard searches in the middle of a term.
	 * </p>
	 * <p>
	 * <code>te*t</code>
	 * </p>
	 * <p>
	 * Note: You cannot use a * or ? symbol as the first character of a search.
	 * </p>
	 * <h4>Fuzzy Searches</h4>
	 * <p>
	 * The listing service supports fuzzy searches based on the Levenshtein
	 * Distance, or Edit Distance algorithm. To do a fuzzy search use the tilde,
	 * "~", symbol at the end of a Single word Term. For example to search for a
	 * term similar in spelling to "roam" use the fuzzy search:
	 * </p>
	 * <p>
	 * <code>roam~</code>
	 * </p>
	 * <p>
	 * This search will find terms like foam and roams.
	 * </p>
	 * <p>
	 * An additional (optional) parameter can specify the required similarity.
	 * The value is between 0 and 1, with a value closer to 1 only terms with a
	 * higher similarity will be matched. For example:
	 * </p>
	 * <p>
	 * <code>roam~0.8</code>
	 * </p>
	 * <p>
	 * The default that is used if the parameter is not given is 0.5.
	 * </p>
	 * <h4>Proximity Searches</h4>
	 * <p>
	 * The listing service supports finding words which are a within a specific
	 * distance away. To do a proximity search use the tilde, "~", symbol at the
	 * end of a Phrase. For example to search for a "shirt" and "black" within
	 * 10 words of each other in a document use the search:
	 * </p>
	 * <p>
	 * <code>"black shirt"~10</code>
	 * </p>
	 * <h4>Range Searches</h4>
	 * <p>
	 * Range Queries allow one to match documents whose field(s) values are
	 * between the lower and upper bound specified by the Range Query. Range
	 * Queries can be inclusive or exclusive of the upper and lower bounds.
	 * Sorting is done lexicographically.
	 * </p>
	 * <p>
	 * <code>mod_date:[20020101 TO 20030101]</code>
	 * </p>
	 * <p>
	 * This will find documents whose mod_date fields have values between
	 * 20020101 and 20030101, inclusive. Note that Range Queries are not
	 * reserved for date fields. You could also use range queries with non-date
	 * fields:
	 * </p>
	 * <p>
	 * <code>title:{Aida TO Carmen}</code>
	 * </p>
	 * <p>
	 * This will find all documents whose titles are between Aida and Carmen,
	 * but not including Aida and Carmen.
	 * </p>
	 * <p>
	 * Inclusive range queries are denoted by square brackets. Exclusive range
	 * queries are denoted by curly brackets.
	 * </p>
	 * <p>
	 * A * may be used for either or both endpoints to specify an open-ended
	 * range query.
	 * </p>
	 * <p>
	 * The following query will find all listings with a price less than or
	 * equal to 100:
	 * </p>
	 * <p>
	 * <code>price:[* TO 100]</code>
	 * </p>
	 * <p>
	 * The following query will find all listings containing the field color:
	 * </p>
	 * <p>
	 * <code>color:[* TO *]</code>
	 * </p>
	 * <p>
	 * Note: Range queries may depend on the implemention/context specific field
	 * types.
	 * </p>
	 * <h4>Boosting a Term</h4>
	 * <p>
	 * The listing service provides the relevance level of matching documents
	 * based on the terms found. To boost a term use the caret, "^", symbol with
	 * a boost factor (a number) at the end of the term you are searching. The
	 * higher the boost factor, the more relevant the term will be.
	 * </p>
	 * <p>
	 * Boosting allows you to control the relevance of a document by boosting
	 * its term. For example, if you are searching for
	 * </p>
	 * <p>
	 * <code>black shirts</code>
	 * </p>
	 * <p>
	 * and you want the term "black" to be more relevant boost it using the ^
	 * symbol along with the boost factor next to the term. You would type:
	 * </p>
	 * <p>
	 * <code>black^4 shirts</code>
	 * </p>
	 * <p>
	 * This will make documents with the term shorts appear more relevant. You
	 * can also boost Phrase Terms as in the example:
	 * </p>
	 * <p>
	 * <code>"black shirts"^4 "short arms"</code>
	 * </p>
	 * <p>
	 * By default, the boost factor is 1. Although the boost factor must be
	 * positive, it can be less than 1 (e.g. 0.2)
	 * </p>
	 * <h3>Boolean Operators</h3>
	 * <p>
	 * Boolean operators allow terms to be combined through logic operators. The
	 * listing service supports AND, "+", OR, NOT and "-" as Boolean
	 * operators(Note: Boolean operators must be ALL CAPS).
	 * </p>
	 * <h4>OR</h4>
	 * <p>
	 * The OR operator is the default conjunction operator. This means that if
	 * there is no Boolean operator between two terms, the OR operator is used.
	 * The OR operator links two terms and finds a matching document if either
	 * of the terms exist in a document. This is equivalent to a union using
	 * sets. The symbol || can be used in place of the word OR.
	 * </p>
	 * <p>
	 * To search for documents that contain either "black shirt" or just "black"
	 * use the query:
	 * </p>
	 * <p>
	 * <code>"black shirt" black</code>
	 * </p>
	 * <p>
	 * or
	 * </p>
	 * <p>
	 * <code>"black shirt" OR black</code>
	 * <h4>AND</h4>
	 * </p>
	 * <p>
	 * The AND operator matches documents where both terms exist anywhere in the
	 * text of a single document. This is equivalent to an intersection using
	 * sets. The symbol &amp;&amp; can be used in place of the word AND.
	 * </p>
	 * <p>
	 * To search for documents that contain "black shirt" and "long arms" use
	 * the query:
	 * </p>
	 * <p>
	 * <code>"black shirt" AND "long arms"</code>
	 * </p>
	 * <h4>+</h4>
	 * <p>
	 * The "+" or required operator requires that the term after the "+" symbol
	 * exist somewhere in a the field of a single document.
	 * </p>
	 * <p>
	 * To search for documents that must contain "black" and may contain "long"
	 * use the query:
	 * </p>
	 * <p>
	 * <code>+black long</code>
	 * </p>
	 * <h4>NOT</h4>
	 * <p>
	 * The NOT operator excludes documents that contain the term after NOT. This
	 * is equivalent to a difference using sets. The symbol ! can be used in
	 * place of the word NOT.
	 * </p>
	 * <p>
	 * To search for documents that contain "black shirt" but not "long arms"
	 * use the query:
	 * </p>
	 * <p>
	 * <code>"black shirt" NOT "long arms"</code>
	 * </p>
	 * <p>
	 * Note: Pure negative queries (all clauses prohibited) are also allowed.
	 * For example, to search for all listings whose field inStock is not false
	 * use the following query:
	 * </p>
	 * <p>
	 * <code>-inStock:false</code>
	 * </p>
	 * <p>
	 * The following query will find all listings without a value for color:
	 * </p>
	 * <p>
	 * <code>-color:[* TO *]</code>
	 * </p>
	 * <h4>-</h4>
	 * <p>
	 * The "-" or prohibit operator excludes documents that contain the term
	 * after the "-" symbol.
	 * </p>
	 * <p>
	 * To search for documents that contain "black shirt" but not "long arms"
	 * use the query:
	 * </p>
	 * <p>
	 * <code>"black shirt" -"long arms"</code>
	 * </p>
	 * <h3>Grouping</h3>
	 * <p>
	 * The listing service supports using parentheses to group clauses to form
	 * sub queries. This can be very useful if you want to control the boolean
	 * logic for a query.
	 * </p>
	 * <p>
	 * To search for either "black" or "shirt" and "website" use the query:
	 * </p>
	 * <p>
	 * <code>(black OR shirt) AND website</code>
	 * </p>
	 * <p>
	 * This eliminates any confusion and makes sure you that website must exist
	 * and either term black or shirt may exist.
	 * </p>
	 * <h3>Field Grouping</h3>
	 * <p>
	 * The listing service supports using parentheses to group multiple clauses
	 * to a single field.
	 * </p>
	 * <p>
	 * To search for a title that contains both the word "return" and the phrase
	 * "pink panther" use the query:
	 * </p>
	 * <p>
	 * <code>title:(+return +"pink panther")</code>
	 * </p>
	 * <h3>Escaping Special Characters</h3>
	 * <p>
	 * The listing service supports escaping special characters that are part of
	 * the query syntax. The current list special characters are
	 * </p>
	 * <p>
	 * + - &amp;&amp; || ! ( ) { } [ ] ^ " ~ * ? : \
	 * </p>
	 * <p>
	 * To escape these character use the \ before the character. For example to
	 * search for (1+1):2 use the query:
	 * </p>
	 * <p>
	 * <code>\(1\+1\)\:2</code>
	 * </p>
	 * <p>
	 * A {@link ListingQuery#escapeQueryChars(String) utility method} is
	 * available which is capable of encoding strings.
	 * </p>
	 * 
	 * @param advancedQuery
	 *            the advancedQuery to set
	 * @return this query object for convenience
	 * @see #setQuery(String)
	 */
	public ListingQuery setAdvancedQuery(final String advancedQuery) {
		if (StringUtils.isNotEmpty(advancedQuery)) {
			this.advancedQuery = advancedQuery;
		} else {
			this.advancedQuery = null;
		}
		return this;
	}

	/**
	 * Sets a list of filter queries.
	 * <p>
	 * The list of existing queries will be cleared before setting the new
	 * queries. Filter queries which are <code>null</code> or blank will be
	 * ignored.
	 * </p>
	 * <p>
	 * Filter queries are used to filter the set of listings to be searched.
	 * They can be exclusive or inclusive. For example, using a filter query it
	 * is possible to exclude listings with a specific attribute (eg. not in
	 * stock) from the search. It's also possible to limit the search to only
	 * those listing with a specific attribute set (eg. with tag AAA and/or
	 * BBB).
	 * </p>
	 * <p>
	 * Filter queries are considered {@link #setAdvancedQuery(String) advanced
	 * queries}. Therefore, the same syntax rules apply to filter queries which
	 * also apply to advanced queries.
	 * </p>
	 * 
	 * @param filterQueries
	 *            the filter queries to set
	 * @return this query object for convenience
	 * @see #addFilterQuery(String)
	 * @see #setAdvancedQuery(String)
	 */
	public ListingQuery setFilterQueries(final String... filterQueries) {
		this.filterQueries.clear();
		if (null != filterQueries) {
			for (final String filterQuery : filterQueries) {
				if (StringUtils.isNotBlank(filterQuery)) {
					this.filterQueries.add(filterQuery);
				}
			}
		}
		return this;
	}

	/**
	 * Sets the maximum results to return.
	 * <p>
	 * The default value is <code>10</code>.
	 * </p>
	 * <p>
	 * Note, despite the number set here the listing service implementation
	 * might enforce a specific limit if the number is too high to ensure
	 * scalability.
	 * </p>
	 * <p>
	 * This is typically used with {@link #setStartIndex(int) a start index} for
	 * paging. As a recommended practice the number should be less than 100.
	 * Pages with more than 100 items don't really make sense anyway.
	 * </p>
	 * <p>
	 * If you want to retrieve <em>all</em> listings then multiple requests
	 * should be issued one after each other in a resource friendly way.
	 * Otherwise the CloudFree platform may deny requests based on configured
	 * capacity/throughput limits.
	 * </p>
	 * 
	 * @param maxResults
	 *            the maximum results to return
	 * @return this query object for convenience
	 * @see #setStartIndex(int)
	 */
	public ListingQuery setMaxResults(final int maxResults) {
		if (maxResults < 0) {
			throw new IllegalArgumentException("maxResults must not be negative");
		}
		this.maxResults = maxResults;
		return this;
	}

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
	public ListingQuery setQuery(final String query) {
		this.query = query;
		return this;
	}

	/**
	 * Sets the result dimension.
	 * <p>
	 * The result dimension is used to optimize the amount of fields contained
	 * in the result. The default is {@link ResultDimension#COMPACT} which only
	 * retrieves minimal amount of fields sufficient for presenting listings in
	 * overview lists or galleries. The actual fields are implementation/context
	 * specific.
	 * </p>
	 * <p>
	 * {@link ResultDimension#FULL} will instruct the listing service
	 * implementation to return all available fields for a listing. There will
	 * be a performance impact here especially if a listing contains very large
	 * fields.
	 * </p>
	 * 
	 * @param resultDimension
	 *            the result dimension to set
	 */
	public void setResultDimension(final ResultDimension resultDimension) {
		this.resultDimension = resultDimension;
	}

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
	public ListingQuery setSortField(final String fieldName, final SortDirection direction) {
		sortFields.clear();
		if (StringUtils.isNotBlank(fieldName)) {
			sortFields.put(fieldName, direction);
		}
		return this;
	}

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
	public ListingQuery setStartIndex(final long startIndex) {
		this.startIndex = startIndex;
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder toString = new StringBuilder();
		toString.append("ListingQuery[");
		if (null != advancedQuery) {
			toString.append(" advancedQuery(").append(advancedQuery).append(')');
		}
		if (null != query) {
			toString.append(" query(").append(query).append(')');
		}
		toString.append(" filterQueries(").append(filterQueries).append(')');
		toString.append(" sortFields(").append(sortFields).append(')');
		toString.append(" startIndex(").append(startIndex).append(')');
		toString.append(" maxResults(").append(maxResults).append(')');
		toString.append(" ]");
		return toString.toString();
	}
}
