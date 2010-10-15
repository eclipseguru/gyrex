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
 * This class provides static helper methods when working with queries.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class QueryUtil {

	/**
	 * Escapes the specified input string according to the query syntax escaping
	 * requirements as noted in {@link IQuery#setAdvancedQuery(String)}.
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
		final StringBuilder sb = new StringBuilder(input.length() + 100);
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			// escape all chars which are part of the advanced syntax
			if ((c == '\\') || (c == '+') || (c == '-') || (c == '!') || (c == '(') || (c == ')') || (c == ':') || (c == '^') || (c == '[') || (c == ']') || (c == '\"') || (c == '{') || (c == '}') || (c == '<') || (c == '>') || (c == '~') || (c == '*') || (c == '?') || (c == '|') || (c == '&') || (c == ';') || Character.isWhitespace(c)) {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Hidden
	 */
	private QueryUtil() {
		// empty
	}

}
