/*******************************************************************************
 * Copyright (c) 2013 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Konrad Schergaut - support for multi value settings
 *******************************************************************************/
package org.eclipse.gyrex.server.settings;

import java.util.List;
import java.util.Objects;

import com.google.common.annotations.VisibleForTesting;

/**
 * A builder to create {@link SystemSetting} instances in a fluent way.
 *
 * @param <T>
 *            the value type (see {@link SystemSetting} for supported types)
 */
public final class SystemSettingBuilder<T> {

	/**
	 * Provides a builder for list-value settings of the given type.
	 *
	 * @param <T>
	 *            The element type of the list.
	 */
	static <T> SystemSettingBuilder<List<T>> multiValued(final Class<T> valueType) {
		return new SystemSettingBuilder<>(new ListValueConverter<>(valueType), valueType);
	}

	/**
	 * Provides a builder for single value settings of the given type.
	 *
	 * @param <T>
	 *            The element type of the list.
	 */
	static <T> SystemSettingBuilder<T> singleValued(final Class<T> valueType) {
		return new SystemSettingBuilder<>(new SingleValueConverter<>(valueType), valueType);
	}

	private static String toEnvironmentVariableName(final CharSequence systemProperty) {
		final StringBuilder result = new StringBuilder(systemProperty.length());
		for (int i = 0; i < systemProperty.length(); i++) {
			final char c = systemProperty.charAt(i);
			if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
				result.append(c);
			} else {
				result.append('_');
			}
		}
		return result.toString();
	}

	@VisibleForTesting
	String environmentVariable, systemProperty, description;

	/**
	 * The type for single values. The type is not always identical to the type
	 * T. If T is a List then this holds the type-argument for that list.<br/>
	 * We might handle this by adding an additional type parameter, but that
	 * would reduce the readability of this class, so it is kept internal.
	 */
	@VisibleForTesting
	final Class<?> valueType;

	private T defaultValue;

	private final ValueConverter<T> valueConverter;

	@VisibleForTesting
	SystemSettingBuilder(final ValueConverter<T> valueConverter, final Class<?> valueType) {
		this.valueConverter = valueConverter;
		this.valueType = valueType;
	}

	/**
	 * Creates and returns the concrete {@link SystemSetting} instance based on
	 * the builder configuration.
	 * <p>
	 * If no environment variable name has been explicitly configured at this
	 * point, a default one will be used by converting the system property name.
	 * All chars other than US-ASCII 0-9, a-z and A-Z will be replaced by an
	 * underscore ('_').
	 * </p>
	 *
	 * @return a new {@link SystemSetting} instance
	 */
	public SystemSetting<T> create() {
		final String environmentVariable;
		if (this.environmentVariable == null) {
			environmentVariable = toEnvironmentVariableName(Objects.requireNonNull(systemProperty, "Please specify at least a system property!"));
		} else {
			environmentVariable = this.environmentVariable;
		}
		return new SystemSetting<>(environmentVariable, systemProperty, description, valueConverter, defaultValue);
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the description to set
	 * @return this {@link SystemSettingBuilder}
	 */
	public SystemSettingBuilder<T> description(final String description) {
		this.description = description;
		return this;
	}

	/**
	 * Sets the environment variable.
	 *
	 * @param environmentVariable
	 *            the environment variable to set
	 * @return this {@link SystemSettingBuilder}
	 */
	public SystemSettingBuilder<T> environmentVariable(final String environmentVariable) {
		this.environmentVariable = environmentVariable;
		return this;
	}

	/**
	 * Sets the system property.
	 *
	 * @param systemProperty
	 *            the system property to set
	 * @return this {@link SystemSettingBuilder}
	 */
	public SystemSettingBuilder<T> systemProperty(final String systemProperty) {
		this.systemProperty = systemProperty;
		return this;
	}

	/**
	 * Sets the default value.
	 *
	 * @param defaultValue
	 *            the default value to set
	 * @return this {@link SystemSettingBuilder}
	 */
	public SystemSettingBuilder<T> usingDefault(final T defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

}
