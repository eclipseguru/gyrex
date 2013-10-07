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
 *******************************************************************************/
package org.eclipse.gyrex.server.settings;

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
 *            {@link Long}, {@link Double}, {@link Float} and {@link Boolean}
 *            are supported)
 */
public final class SystemSetting<T> {

	final static Logger LOG = LoggerFactory.getLogger(SystemSetting.class);

	/**
	 * Returns a new builder for a system setting with a {@link Boolean} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Boolean> newBooleanSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(Boolean.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Double} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Double> newDoubleSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(Double.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Float} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Float> newFloatSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(Float.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Integer} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Integer> newIntegerSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(Integer.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link Long} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<Long> newLongSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(Long.class).systemProperty(systemProperty).description(description);
	}

	/**
	 * Returns a new builder for a system setting with a {@link String} value
	 * type.
	 * 
	 * @return a new {@link SystemSettingBuilder}
	 */
	public static SystemSettingBuilder<String> newStringSetting(final String systemProperty, final String description) {
		return new SystemSettingBuilder<>(String.class).systemProperty(systemProperty).description(description);
	}

	final String environmentVariable;
	final String systemProperty;
	final String description;
	final T defaultValue;
	final Class<T> valueType;

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
	SystemSetting(final String environmentVariable, final String systemProperty, final String description, final Class<T> valueType, final T defaultValue) {
		if (StringUtils.isBlank(environmentVariable))
			throw new IllegalArgumentException("Please specify a non blank environment variable!");
		if (StringUtils.isBlank(systemProperty))
			throw new IllegalArgumentException("Please specify a non blank system property!");
		if (StringUtils.isBlank(description))
			throw new IllegalArgumentException("Please specify a non blank description!");
		this.environmentVariable = environmentVariable;
		this.systemProperty = systemProperty;
		this.description = description;
		this.valueType = Objects.requireNonNull(valueType, "Please specify a value type!");
		this.defaultValue = defaultValue;
	}

	@SuppressWarnings("unchecked")
	private T convertToValueType(final String value) {
		if (valueType.isInstance(value))
			return (T) value;
		if (valueType.equals(Boolean.class))
			return (T) new Boolean(Boolean.parseBoolean(value));
		if (valueType.equals(Integer.class))
			return (T) new Integer(Integer.parseInt(value));
		if (valueType.equals(Long.class))
			return (T) new Long(Long.parseLong(value));
		if (valueType.equals(Double.class))
			return (T) new Double(Double.parseDouble(value));
		if (valueType.equals(Float.class))
			return (T) new Float(Float.parseFloat(value));
		throw new IllegalArgumentException("Unsupported value type: " + valueType);
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
		return Objects.equals(environmentVariable, other.environmentVariable) && Objects.equals(systemProperty, other.systemProperty) && Objects.equals(description, other.description) && Objects.equals(valueType, other.valueType) && Objects.equals(defaultValue, other.defaultValue);
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
	 * with the full name of this class.
	 * </p>
	 * 
	 * @return the system setting value
	 */
	public T get() {
		String value = System.getenv(environmentVariable);
		if (null != value) {
			try {
				LOG.debug("Using value {} from environment variable {}", value, environmentVariable);
				return convertToValueType(value);
			} catch (final IllegalArgumentException e) {
				LOG.warn("Unable to parse environment variable '{}': {}", environmentVariable, e);
			}
		}
		value = System.getProperty(systemProperty);
		if (null != value) {
			try {
				LOG.debug("Using value {} from system property {}", value, systemProperty);
				return convertToValueType(value);
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
			return convertToValueType(value);
		}
		value = System.getProperty(systemProperty);
		if (null != value) {
			LOG.debug("Using value {} from system property {}", value, systemProperty);
			return convertToValueType(value);
		}
		LOG.debug("No value set for {}/{}, using default value {}", environmentVariable, systemProperty, defaultValue);
		return defaultValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(environmentVariable, systemProperty, description, defaultValue, valueType);
	}

	/**
	 * Convenient method that indicates if the system setting is set, i.e. the
	 * value returned by {@link #get()} is different than its default value.
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
		if (defaultValue != null)
			return !defaultValue.equals(get());
		else
			return get() != null;
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
