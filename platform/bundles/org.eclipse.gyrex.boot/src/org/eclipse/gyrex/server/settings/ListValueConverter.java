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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of value converters that parse the system setting into a list
 * of values.
 */
class ListValueConverter<T> extends ValueConverter<List<T>> {

	private static final String SEPARATOR = ",";

	final Class<T> valueType;

	ListValueConverter(final Class<T> valueType) {
		this.valueType = valueType;
	}

	@Override
	List<T> convertValue(final String value) {
		final String[] split = value.split(SEPARATOR);
		final List<T> convertedValues = new ArrayList<>(split.length);
		for (final String unconvertedValue : split) {
			convertedValues.add(ValueConverter.convertToValueType(unconvertedValue, valueType));
		}
		return convertedValues;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ListValueConverter))
			return false;
		final ListValueConverter<?> other = (ListValueConverter<?>) obj;
		return Objects.equals(valueType, other.valueType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(valueType);
	}

}
