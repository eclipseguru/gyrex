/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.application;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.gyrex.common.identifiers.IdHelper;

import com.google.common.base.Strings;

/**
 * A handle for a page.
 * <p>
 * The application uses handles in order to defer the page creation till it is
 * actually required.
 * <p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class PageHandle implements Comparable<PageHandle> {

	private static final String[] NO_KEYWORDS = new String[0];

	private final String id;
	private String[] keywords;
	private String name;
	private String sortKey;
	private String categoryId;

	/**
	 * Creates a new instance.
	 *
	 * @param id
	 *            the page id (must not be <code>null</code>)
	 */
	public PageHandle(final String id) {
		checkArgument(IdHelper.isValidId(id), "id is invalid");
		this.id = id;
	}

	@Override
	public int compareTo(final PageHandle o) {
		return getSortKey().compareTo(o.getSortKey());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PageHandle))
			return false;
		final PageHandle other = (PageHandle) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * Returns the category identifier a page belongs to.
	 *
	 * @return the category id (may be <code>null</code>)
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * Returns the page id.
	 *
	 * @return the page identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the keywords used when searching for a page.
	 *
	 * @return the keywords (never <code>null</code>)
	 */
	public String[] getKeywords() {
		if (keywords == null)
			return NO_KEYWORDS;

		return keywords;
	}

	/**
	 * Returns a human readable name of the page.
	 *
	 * @return the page name
	 */
	public String getName() {
		final String name = this.name;
		return name != null ? name : getId();
	}

	/**
	 * Returns the key used for sorting pages.
	 * <p>
	 * If a custom sort key was set it will be used. Otherwise a fallback to the
	 * name will be attempted. If no custom sort key and no name is set, the
	 * {@link #getId() id} will be returned.
	 * </p>
	 *
	 * @return the key used for sorting pages
	 */
	public String getSortKey() {
		String value = sortKey;
		if (!Strings.isNullOrEmpty(value))
			return value;
		value = getName();
		if (!Strings.isNullOrEmpty(value))
			return value;
		return getId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Sets the category identifier.
	 *
	 * @param categoryId
	 *            the category id to set
	 */
	public void setCategoryId(final String categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * Sets the keywords.
	 *
	 * @param keywords
	 *            the keywords to set
	 */
	public void setKeywords(final String[] keywords) {
		this.keywords = keywords;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Sets the sortKey.
	 *
	 * @param sortKey
	 *            the sortKey to set
	 */
	public void setSortKey(final String sortKey) {
		this.sortKey = sortKey;
	}

	@Override
	public String toString() {
		return "PageHandle [" + id + ", " + name + "]";
	}

}