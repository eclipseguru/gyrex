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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A provider for pages and categories.
 * <p>
 * This class must be extended and provided to an application.
 * </p>
 */
public abstract class PageProvider {

	private final ConcurrentMap<String, PageHandle> pagesById = new ConcurrentHashMap<String, PageHandle>();
	private final ConcurrentMap<String, Category> categoriesById = new ConcurrentHashMap<String, Category>(4);
	private volatile Map<String, Set<PageHandle>> pagesByCategoryId;

	/**
	 * Adds a category to the provider.
	 *
	 * @param category
	 *            the category to add (must not <code>null</code>)
	 * @return <code>true</code> if the category has been added,
	 *         <code>false</code> otherwise (in case a category with the same id
	 *         was already added)
	 */
	protected boolean addCategory(final Category category) {
		checkArgument(category != null, "category must not be null");
		final String id = category.getId();
		if (null != categoriesById.putIfAbsent(id, category))
			return false;

		rebuildPagesByCategories();
		return true;
	}

	/**
	 * Adds a page to the provider.
	 *
	 * @param page
	 *            the page to add (must not <code>null</code>)
	 * @return <code>true</code> if the page has been added, <code>false</code>
	 *         otherwise (in case a page with the same id was already added)
	 */
	protected boolean addPage(final PageHandle page) {
		checkArgument(page != null, "page must not be null");
		final String id = page.getId();
		if (null != pagesById.putIfAbsent(id, page))
			return false;

		rebuildPagesByCategories();
		return true;
	}

	/**
	 * Called by the application to create a new instance of the actual
	 * {@link Page} object.
	 *
	 * @return a new {@link Page} instance
	 * @throws Exception
	 *             in case of errors creating the instance
	 */
	public abstract Page createPage(PageHandle pageHandle) throws Exception;

	/**
	 * Returns a list of categories.
	 * <p>
	 * Note, modifications to the returned list do not reflect into the page
	 * provider.
	 * </p>
	 *
	 * @return a list of categories
	 */
	public List<Category> getCategories() {
		if (categoriesById.isEmpty())
			return Collections.emptyList();
		return new ArrayList<Category>(categoriesById.values());
	}

	/**
	 * Returns the page with the specified id
	 *
	 * @param id
	 *            the page id
	 * @return the found page (may be <code>null</code>)
	 */
	public PageHandle getPage(final String id) {
		return pagesById.get(id);
	}

	/**
	 * Returns all pages for a category.
	 * <p>
	 * Note, modifications to the returned list do not reflect into the page
	 * provider.
	 * </p>
	 *
	 * @param category
	 *            the category
	 * @return a list of pages
	 */
	public List<PageHandle> getPages(final Category category) {
		final Map<String, Set<PageHandle>> mappings = pagesByCategoryId;
		if ((mappings == null) || mappings.isEmpty())
			return Collections.emptyList();
		final Set<PageHandle> children = mappings.get(category.getId());
		if ((children == null) || children.isEmpty())
			return Collections.emptyList();
		// return a copy
		return new ArrayList<PageHandle>(children);
	}

	boolean hasPages(final Category category) {
		if (category == null)
			return false;

		final Map<String, Set<PageHandle>> mappings = pagesByCategoryId;
		if (mappings == null)
			return false;
		final Set<PageHandle> children = mappings.get(category.getId());
		return (children != null) && !children.isEmpty();
	}

	private void rebuildPagesByCategories() {
		final Map<String, Set<PageHandle>> mappings = new HashMap<String, Set<PageHandle>>();
		final Collection<PageHandle> values = pagesById.values();
		for (final PageHandle page : values) {
			if (!mappings.containsKey(page.getCategoryId())) {
				mappings.put(page.getCategoryId(), new HashSet<PageHandle>());
			}
			mappings.get(page.getCategoryId()).add(page);
		}
		pagesByCategoryId = mappings;
	}

	/**
	 * Removes a category.
	 *
	 * @param id
	 *            the category id to remove (must not be <code>null</code>)
	 */
	protected void removeCategory(final String id) {
		checkArgument(id != null, "category id must not be null");
		if (null != categoriesById.remove(id)) {
			rebuildPagesByCategories();
		}
	}

	/**
	 * Removes a page.
	 *
	 * @param id
	 *            the page id to remove (must not be <code>null</code>)
	 */
	protected void removePage(final String id) {
		checkArgument(id != null, "page id must not be null");
		if (null != pagesById.remove(id)) {
			rebuildPagesByCategories();
		}
	}

}