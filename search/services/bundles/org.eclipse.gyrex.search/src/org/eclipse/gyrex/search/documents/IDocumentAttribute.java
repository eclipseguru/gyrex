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

import java.util.Collection;
import java.util.Date;

import org.eclipse.gyrex.model.common.IModelObject;

/**
 * A document attribute.
 * <p>
 * Document attributes can have no, one or many values. All values must be of
 * the same type.
 * </p>
 * <p>
 * Although the list of supported types might vary depending on the
 * implementation it is expected that each implementation shall supports at a
 * minimum {@link String}, {@link Boolean}, {@link Double}, {@link Long} and
 * {@link Date}.
 * </p>
 * 
 * @param <T>
 *            the attribute value type
 */
public interface IDocumentAttribute<T> extends IModelObject {

	/**
	 * Adds multiple values.
	 * 
	 * @param values
	 *            the values to add
	 */
	void add(Iterable<T> values);

	/**
	 * Adds a value.
	 * 
	 * @param value
	 *            the value to add
	 */
	void add(T value);

	/**
	 * Adds multiple values.
	 * 
	 * @param values
	 *            the values to add
	 */
	void add(T... values);

	/**
	 * Adds a value if it is not already present.
	 * 
	 * @param value
	 *            the value to add
	 */
	void addIfNotPresent(T value);

	/**
	 * Indicates if a value is already present.
	 * 
	 * @param value
	 *            the value to check
	 * @return <code>true</code> if the value is already present in the
	 *         attribute, <code>false</code> otherwise
	 */
	boolean contains(T value);

	/**
	 * Returns the attribute id.
	 * 
	 * @return the attribute id
	 */
	String getId();

	/**
	 * Returns the single attribute value.
	 * <p>
	 * If this attribute is a multi-value attribute, just the first value will
	 * be returned.
	 * </p>
	 * 
	 * @return the single attribute value
	 */
	T getValue();

	/**
	 * Returns a collection of attribute values.
	 * 
	 * @return a modifiable collection of attribute values
	 */
	Collection<T> getValues();

	/**
	 * Indicates if an attribute is empty, i.e. contains no values.
	 * 
	 * @return <code>true</code> if the attribute contains no values,
	 *         <code>false</code> otherwise
	 */
	boolean isEmpty();

	/**
	 * Convenience method to check that the document attribute values are of the
	 * specified type.
	 * 
	 * @param type
	 *            the type to check
	 * @param <E>
	 *            the expected type
	 * @return the attribute casted to the type
	 * @throws IllegalArgumentException
	 *             if the attribute values are of a different type
	 */
	<E> IDocumentAttribute<E> ofType(Class<E> type) throws IllegalArgumentException;

	/**
	 * Removes a value.
	 * 
	 * @param value
	 *            the value to remove
	 */
	void remove(T value);

	/**
	 * Sets multiple values at once.
	 * <p>
	 * Any existing value (or values) will be discarded.
	 * </p>
	 * 
	 * @param values
	 *            the values to set
	 */
	void set(Iterable<T> values);

	/**
	 * Sets a single value.
	 * <p>
	 * Any existing value (or values) will be discarded.
	 * </p>
	 * 
	 * @param value
	 *            the value to set (maybe <code>null</code> to unset)
	 */
	void set(T value);

	/**
	 * Sets multiple values at once.
	 * <p>
	 * Any existing value (or values) will be discarded.
	 * </p>
	 * 
	 * @param values
	 *            the values to set
	 */
	void set(T... values);
}
