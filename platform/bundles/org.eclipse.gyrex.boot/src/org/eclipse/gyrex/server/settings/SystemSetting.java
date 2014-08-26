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

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Typed access to configurable system settings.
 * <p>
 * System settings control the behavior of server. Following the principles of
 * the <a href="http://12factor.net/">twelve factor apps</a>, a process should
 * allowed to be configured using environment variables. However, Java
 * applications are typically configured via system properties. This class
 * allows clients to support both worlds including typed access to values.
 * </p>
 * 
 * @param <T>
 *            the value type (currently only {@link String}, {@link Integer},
 *            {@link Long}, {@link Double}, {@link Float}, {@link Boolean} and
 *            {@link List lists of Strings} are supported)
 */
public final class SystemSetting<T> {

	final static Logger LOG = LoggerFactory.getLogger(SystemSetting.class);

	/**
	 * Returns a new builder for a system setting with a {@link Boolean} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Boolean> newBooleanSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(Boolean.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Double} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Double> newDoubleSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(Double.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Float} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Float> newFloatSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(Float.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Integer} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Integer> newIntegerSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(Integer.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Long} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Long> newLongSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(Long.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link String} value
	 * type.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<String> newStringSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.singleValued(String.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with multiple {@link String}
	 * values. The values are expected to be separated by a comma.
	 * 
	 * @param systemProperty
	 *            The property name to read
	 * @param description
	 *            A description of this property
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<List<String>> newMultiValueStringSetting(final String systemProperty, final String description) {
		return SystemSettingBuilder.multiValued(String.class).systemProperty(systemProperty).description(description);
	}

	final String environmentVariable;
	final String systemProperty;
	final String description;
	final T defaultValue;
	final ValueConverter<T> converter;

	/**
	 * Creates a new system setting.
	 * 
	 * @param environmentVariable
	 *            name of the environment variable (must not be
	 *            <code>null</code>)
	 * @param systemProperty
	 *            name of the system property (must not be <code>null</code>)
	 * @param description
	 *            description of the system setting (eg., for documentation
	 *            purposes) (must not be <code>null</code>)
	 * @param valueType
	 *            the value type (must not be <code>null</code>)
	 * @param defaultValue
	 *            a default value
	 */
	SystemSetting(final String environmentVariable, final String systemProperty, final String description, final ValueConverter<T> converter, final T defaultValue) {
		if (StringUtils.isBlank(environmentVariable))
			throw new IllegalArgumentException("Please specify a non blank environment variable!");
		if (StringUtils.isBlank(systemProperty))
			throw new IllegalArgumentException("Please specify a non blank system property!");
		if (StringUtils.isBlank(description))
			throw new IllegalArgumentException("Please specify a non blank description!");
		this.environmentVariable = environmentVariable;
		this.systemProperty = systemProperty;
		this.description = description;
		this.converter = Objects.requireNonNull(converter, "Please specify a value converter!");
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SystemSetting))
			return false;
		final SystemSetting<?> other = (SystemSetting<?>) obj;
		return Objects.equals(environmentVariable, other.environmentVariable) && Objects.equals(systemProperty, other.systemProperty) && Objects.equals(description, other.description) && Objects.equals(converter, other.converter) && Objects.equals(defaultValue, other.defaultValue);
	}

	/**
	 * Reads and returns the system setting value.
	 * <p>
	 * The lookup process of a system setting is as follows:
	 * <ol>
	 * <li>Check for {@link System#getenv(String) environment variable} with
	 * specified name. If a non <code>null</code> value is found and can be
	 * converted/parsed to the required value type, use this value.</li>
	 * <li>Check for {@link System#getProperty(String) system property} with
	 * specified name. If a non <code>null</code> value is found and can be
	 * converted/parsed to the required value type, use this value.</li>
	 * <li>Return a given default value.</li>
	 * <ol>
	 * </p>
	 * <p>
	 * Note, in case a value was found but cannot be parsed/converted into the
	 * required value type, a warning message will be logged to a logger named
	 * with the full name of this class and the lookup will continue as
	 * specified above.
	 * </p>
	 * 
	 * @return the system setting value
	 */
	public T get() {
		String value = System.getenv(environmentVariable);
		if (null != value) {
			try {
				LOG.debug("Using value {} from environment variable {}", value, environmentVariable);
				return converter.convertValue(value);
			} catch (final IllegalArgumentException e) {
				LOG.warn("Unable to parse environment variable '{}': {}", environmentVariable, e);
			}
		}
		value = System.getProperty(systemProperty);
		if (null != value) {
			try {
				LOG.debug("Using value {} from system property {}", value, systemProperty);
				return converter.convertValue(value);
			} catch (final IllegalArgumentException e) {
				LOG.warn("Unable to parse syste property '{}': {}", systemProperty, e);
			}
		}
		LOG.debug("No value set for {}/{}, using default value {}", environmentVariable, systemProperty, defaultValue);
		return defaultValue;
	}

	/**
	 * Returns the default value.
	 * 
	 * @return the default value
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Reads and returns the system setting value.
	 * <p>
	 * The lookup process of a system setting is as follows:
	 * <ol>
	 * <li>Check for {@link System#getenv(String) environment variable} with
	 * specified name. If a non <code>null</code> value is found use this value.
	 * </li>
	 * <li>Check for {@link System#getProperty(String) system property} with
	 * specified name. If a non <code>null</code> value is found use this value.
	 * </li>
	 * <li>Return a given default value.</li>
	 * <ol>
	 * </p>
	 * 
	 * @return the system setting value
	 * @throws IllegalArgumentException
	 *             in case a value cannot be converted to the required value
	 *             type (eg. parsing error for numbers)
	 */
	public T getOrFail() throws IllegalArgumentException {
		String value = System.getenv(environmentVariable);
		if (null != value) {
			LOG.debug("Using value {} from environment variable {}", value, environmentVariable);
			return converter.convertValue(value);
		}
		value = System.getProperty(systemProperty);
		if (null != value) {
			LOG.debug("Using value {} from system property {}", value, systemProperty);
			return converter.convertValue(value);
		}
		LOG.debug("No value set for {}/{}, using default value {}", environmentVariable, systemProperty, defaultValue);
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(environmentVariable, systemProperty, description, defaultValue, converter);
	}

	/**
	 * Convenient method that indicates if the system setting is set, i.e. the
	 * value returned by {@link #get()} is neither <code>null</code> nor the
	 * default value.
	 * <p>
	 * In case no default value is set, this method will return true if
	 * {@link #get()} returns a non <code>null</code> value.
	 * </p>
	 * 
	 * @return <code>true</code> if the value returned by {@link #get()} is
	 *         different than the {@link #getDefaultValue() default value},
	 *         <code>false</code> otherwise
	 */
	public boolean isSet() {
		return (System.getenv(environmentVariable) != null) || (System.getProperty(systemProperty) != null);
	}

	/**
	 * Convenient method that indicates if the value returned by {@link #get()}
	 * is a {@link Boolean} and equal to <code>true</code>
	 * 
	 * @return <code>true</code> if the value returned by {@link #get()} is
	 *         {@link Boolean} and equal to {@link Boolean#TRUE},
	 *         <code>false</code> otherwise
	 */
	public boolean isTrue() {
		return Boolean.TRUE.equals(get());
	}

	@Override
	public String toString() {
		return String.format("%s (%s) - %s (default %s)", environmentVariable, systemProperty, description, defaultValue);
	}

}
