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

import java.util.Objects;

/**
 * Default implementation for generating one single version of basic type T out
 * of a String.
 */
class SingleValueConverter<T> extends ValueConverter<T> {

	final Class<T> valueType;

	SingleValueConverter(final Class<T> valueType) {
		this.valueType = valueType;
	}

	@Override
	T convertValue(final String value) {
		return ValueConverter.convertToValueType(value, valueType);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SingleValueConverter))
			return false;
		final SingleValueConverter<?> other = (SingleValueConverter<?>) obj;
		return Objects.equals(valueType, other.valueType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(valueType);
	}
}
