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
package org.eclipse.gyrex.cds.documents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A document field.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public abstract class Field<T> {

	private final String name;
	private final List<T> values;

	/**
	 * Creates a new document field using the specified name.
	 * 
	 * @param name
	 *            the field name
	 */
	/*package*/Field(final String name) {
		if ((null == name) || (name.trim().length() == 0)) {
			throw new IllegalArgumentException("field name must be a non-empty string");
		}
		this.name = name;
		values = new ArrayList<T>(1);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param name2
	 * @param value
	 */
	/*package*/Field(final String name, final T value) {
		this(name);
		setValue(value);
	}

	public void addValues(final Iterable<T> values) {
		for (final T value : values) {
			this.values.add(value);
		}
	}

	public void addValues(final T... values) {
		for (final T value : values) {
			this.values.add(value);
		}
	}

	public void addValuesIfAbsent(final Iterable<T> values) {
		for (final T value : values) {
			if (!this.values.contains(value)) {
				this.values.add(value);
			}
		}
	}

	public void addValuesIfAbsent(final T... values) {
		for (final T value : values) {
			if (!this.values.contains(value)) {
				this.values.add(value);
			}
		}
	}

	public T getFirstValue() {
		if (values.size() > 0) {
			return values.get(0);
		}
		return null;
	}

	/**
	 * Returns the name of the field.
	 * 
	 * @return the field name
	 */
	public String getName() {
		return name;
	}

	abstract Class<T> getType();

	/**
	 * Returns an unmodifiable list of all field values.
	 * 
	 * @return an unmodifiable list of all field values
	 */
	public List<T> getValues() {
		return Collections.unmodifiableList(values);
	}

	public Field<T> setValue(final T value) {
		values.clear();
		if (null != value) {
			values.add(value);
		}
		return this;
	}

	public Field<T> setValues(final Iterable<T> values) {
		this.values.clear();
		for (final T value : values) {
			this.values.add(value);
		}
		return this;
	}

	public Field<T> setValues(final T... values) {
		this.values.clear();
		for (final T value : values) {
			this.values.add(value);
		}
		return this;
	}

	@Override
	public String toString() {
		return name + "=" + (values.size() > 1 ? "[" + values + "]" : getFirstValue());
	}
}
