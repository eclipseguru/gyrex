/*******************************************************************************
 * Copyright (c) 2010 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.osgi.util.NLS;

/**
 * {@link IDocumentAttribute} implementation.
 */
public class BaseDocumentAttribute<T> extends PlatformObject implements IDocumentAttribute<T> {

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
		clearIfNecessary();
		add(value);
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
