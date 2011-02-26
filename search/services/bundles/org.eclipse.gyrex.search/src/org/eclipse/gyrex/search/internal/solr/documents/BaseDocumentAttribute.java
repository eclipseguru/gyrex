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
 *     Mike Tschierschke - rework of the SolrRepository concept (https://bugs.eclipse.org/bugs/show_bug.cgi?id=337404)
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.osgi.util.NLS;

/**
 * {@link IDocumentAttribute} implementation.
 */
public class BaseDocumentAttribute<T> extends PlatformObject implements IDocumentAttribute<T> {

	private static Set<Class<?>> allowedTypes;
	static {
		final HashSet<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(String.class);
		classes.add(Boolean.class);
		classes.add(Double.class);
		classes.add(Float.class);
		classes.add(Long.class);
		classes.add(Integer.class);
		classes.add(Date.class);
		allowedTypes = Collections.unmodifiableSet(classes);
	}

	private final String id;
	private final List<T> values = new ArrayList<T>(3);

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 */
	public BaseDocumentAttribute(final String id) {
		this.id = id;
	}

	@Override
	public void add(final Iterable<T> values) {
		for (final T value : values) {
			doAdd(value);
		}
	}

	@Override
	public void add(final T value) {
		doAdd(value);
	}

	@Override
	public void add(final T... values) {
		for (final T value : values) {
			doAdd(value);
		}
	}

	@Override
	public void addIfNotPresent(final T value) {
		if (!values.contains(value)) {
			doAdd(value);
		}
	}

	private void checkType(Class<?> type) {
		do {
			if (allowedTypes.contains(type)) {
				return;
			}
			if (type.getSuperclass() == null) {
				throw new IllegalArgumentException(NLS.bind("value type {0} not supported", type.getName()));
			}
			type = type.getSuperclass();
		} while (Boolean.TRUE);
	}

	private void checkValue(final T value) {
		if (value != null) {
			checkType(value.getClass());
		}
	}

	private void clearIfNecessary() {
		if (!this.values.isEmpty()) {
			doClear();
		}
	}

	@Override
	public boolean contains(final T value) {
		return values.contains(value);
	}

	protected boolean doAdd(final T value) {
		checkValue(value);
		return this.values.add(value);
	}

	protected void doClear() {
		this.values.clear();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public T getValue() {
		if (values.isEmpty()) {
			return null;
		}
		return values.get(0);
	}

	@Override
	public Collection<T> getValues() {
		return values;
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> IDocumentAttribute<E> ofType(final Class<E> type) throws IllegalArgumentException {
		// check type
		checkType(type);

		// check value
		final T value = getValue();
		if ((value != null) && !type.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException(NLS.bind("value type {0} not assignale to type {1}", value.getClass().getName(), type.getName()));
		}
		// this might be unsafe, callers are responsible
		return (IDocumentAttribute<E>) (this);
	}

	@Override
	public void remove(final T value) {
		values.remove(value);
	}

	@Override
	public void set(final Iterable<T> values) {
		clearIfNecessary();
		add(values);
	}

	@Override
	public void set(final T value) {
		checkValue(value);
		clearIfNecessary();
		if (value != null) {
			add(value);
		}
	}

	@Override
	public void set(final T... values) {
		clearIfNecessary();
		add(values);
	}

	@Override
	public String toString() {
		return id + "={" + values + "}";
	}
}
