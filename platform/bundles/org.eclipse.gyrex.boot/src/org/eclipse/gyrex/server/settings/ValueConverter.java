/*******************************************************************************
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Konrad Schergaut - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.server.settings;

/**
 * Converter class from String to T as used for reading system settings.
 */
abstract class ValueConverter<T> {

	@SuppressWarnings("unchecked")
	protected static <S> S convertToValueType(final String value, final Class<S> valueType) {
		if (valueType.isInstance(value))
			return (S) value;
		if (valueType.equals(Boolean.class))
			return (S) new Boolean(Boolean.parseBoolean(value));
		if (valueType.equals(Integer.class))
			return (S) new Integer(Integer.parseInt(value));
		if (valueType.equals(Long.class))
			return (S) new Long(Long.parseLong(value));
		if (valueType.equals(Double.class))
			return (S) new Double(Double.parseDouble(value));
		if (valueType.equals(Float.class))
			return (S) new Float(Float.parseFloat(value));
		throw new IllegalArgumentException("Unsupported value type: " + valueType);
	}

	/**
	 * Converts a String into a value of the target type.
	 * 
	 * @param value
	 *            The value to convert.
	 * @return Never <code>null</code>
	 */
	abstract T convertValue(final String value);

}
