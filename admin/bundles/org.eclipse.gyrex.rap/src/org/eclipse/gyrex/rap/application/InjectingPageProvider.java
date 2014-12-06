/*******************************************************************************
 * Copyright (c) 2014 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.application;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.gyrex.context.IRuntimeContext;

/**
 * A {@link PageProvider} supporting instantiation and injection of pages.
 */
public class InjectingPageProvider extends PageProvider {

	/**
	 * A builder for registering categories.
	 */
	public class CategoryRegistrationBuilder {
		private final String id;
		private String name;
		private String sortKey;

		public CategoryRegistrationBuilder(final String id) {
			this.id = id;
		}

		public CategoryRegistrationBuilder name(final String name) {
			this.name = name;
			return this;
		}

		public void register() {
			final Category category = new Category(id);
			category.setName(name);
			category.setSortKey(sortKey);
			addCategory(category);

		}

		public CategoryRegistrationBuilder sortKey(final String sortKey) {
			this.sortKey = sortKey;
			return this;
		}
	}

	class PageHandleWithClass extends PageHandle {
		private final Class<? extends Page> pageClass;

		public PageHandleWithClass(final String id, final Class<? extends Page> pageClass) {
			super(id);
			this.pageClass = pageClass;
		}

		Class<? extends Page> getPageClass() {
			return pageClass;
		}
	}

	/**
	 * A builder for registering pages.
	 */
	public class PageRegistrationBuilder {
		private final String id;
		private String name;
		private String sortKey;
		private String categoryId;
		private Class<? extends Page> pageClass;

		public PageRegistrationBuilder(final String id) {
			this.id = id;
		}

		public PageRegistrationBuilder categoryId(final String categoryId) {
			this.categoryId = categoryId;
			return this;
		}

		public PageRegistrationBuilder name(final String name) {
			this.name = name;
			return this;
		}

		public PageRegistrationBuilder pageClass(final Class<? extends Page> pageClass) {
			this.pageClass = pageClass;
			return this;
		}

		public void register() {
			final PageHandleWithClass handle = new PageHandleWithClass(id, pageClass);
			handle.setName(name);
			handle.setSortKey(sortKey);
			handle.setCategoryId(categoryId);
			addPage(handle);

		}

		public PageRegistrationBuilder sortKey(final String sortKey) {
			this.sortKey = sortKey;
			return this;
		}
	}

	private final IRuntimeContext context;

	/**
	 * Creates a new instance using the specified context for injection.
	 *
	 * @param context
	 *            the runtime context
	 */
	public InjectingPageProvider(final IRuntimeContext context) {
		checkArgument(context != null, "context must not be null");
		this.context = context;
	}

	@Override
	public Page createPage(final PageHandle pageHandle) throws Exception {
		return context.getInjector().make(((PageHandleWithClass) pageHandle).getPageClass());
	}

	/**
	 * Returns a new {@link CategoryRegistrationBuilder} for registering a
	 * category with the specified id
	 *
	 * @param id
	 *            the category id
	 * @return a {@link CategoryRegistrationBuilder}
	 */
	public CategoryRegistrationBuilder newCategory(final String id) {
		return new CategoryRegistrationBuilder(id);
	}

	/**
	 * Returns a new {@link PageRegistrationBuilder} for registering a page with
	 * the specified id
	 *
	 * @param id
	 *            the category id
	 * @return a {@link PageRegistrationBuilder}
	 */
	public PageRegistrationBuilder newPage(final String id) {
		return new PageRegistrationBuilder(id);
	}

}
