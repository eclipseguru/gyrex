/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
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
 * A category for grouping pages.
 */
public class Category implements Comparable<Category> {

	private final String id;

	private String sortKey, name;

	public Category(final String id) {
		checkArgument(IdHelper.isValidId(id), "id is invalid");
		this.id = id;
	}

	@Override
	public int compareTo(final Category o) {
		return getSortKey().compareTo(o.getSortKey());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Category))
			return false;
		final Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		final String name = this.name;
		return name != null ? name : getId();
	}

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
		return "CategoryHandle [" + id + ", " + name + "]";
	}

}
