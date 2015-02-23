/**
 * Copyright (c) 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.logback.config.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.gyrex.boot.internal.logback.LogbackConfigurator;

import org.apache.commons.lang.StringUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.jul.LevelChangePropagator;

/**
 * Model object representing an entire Logback configuration which can be used
 * to generate the Logback XML.
 * <p>
 * Note, for simplicity the generation of the XML is tightly coupled to the
 * model. This might be an anti-pattern. Contributions to separate this are
 * welcome!
 * </p>
 */
public final class LogbackConfig extends LobackConfigElement {

	static void writeProperty(final XMLStreamWriter writer, final String name, final String value) throws XMLStreamException {
		writer.writeEmptyElement("property");
		writer.writeAttribute("name", name);
		writer.writeAttribute("value", value);
	}

	private final List<Appender> appenders = new ArrayList<Appender>();
	private final List<Logger> loggers = new ArrayList<Logger>();
	private boolean shortenStackTraces;
	private Level defaultLevel;
	private List<String> defaultAppenders;

	public void addAppender(final Appender appender) {
		checkArgument(StringUtils.isNotBlank(appender.getName()), "appender name must not be blank");
		checkArgument(getAppender(appender.getName()) == null, "Duplicate appender name '%s'!", appender.getName());

		appenders.add(appender);
	}

	private String addExceptionPattern(final String pattern) {
		if (isShortenStackTraces()) {
			return pattern + "%rootException{6}";
		} else {
			return pattern + "%rootException";
		}
	}

	public void addLogger(final org.eclipse.gyrex.logback.config.model.Logger logger) {
		checkArgument(StringUtils.isNotBlank(logger.getName()), "logger name must not be blank");
		checkArgument(getAppender(logger.getName()) == null, "Duplicate logger name '%s'!", logger.getName());

		loggers.add(logger);
	}

	public Appender getAppender(final String name) {
		for (final Appender a : getAppenders()) {
			if (name.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public List<Appender> getAppenders() {
		return Collections.unmodifiableList(appenders);
	}

	public List<String> getDefaultAppenders() {
		if (null == defaultAppenders) {
			defaultAppenders = new ArrayList<String>();
		}
		return defaultAppenders;
	}

	/**
	 * Returns the defaultLevel.
	 *
	 * @return the defaultLevel
	 */
	public Level getDefaultLevel() {
		if (null == defaultLevel) {
			return Level.INFO;
		}
		return defaultLevel;
	}

	public Logger getLogger(final String name) {
		for (final Logger l : getLoggers()) {
			if (name.equals(l.getName())) {
				return l;
			}
		}
		return null;
	}

	public List<Logger> getLoggers() {
		return Collections.unmodifiableList(loggers);
	}

	private String getLongPattern() {
		return addExceptionPattern("%date{ISO8601} [%thread.%property{HOSTNAME}] %-5level %logger{36} %mdc{gyrex.contextPath, '[CTX:', '] '}%mdc{gyrex.applicationId, '[APP:', '] '}%mdc{gyrex.jobId, '[JOB:', '] '}- %msg%n");
	}

	private String getShortPattern() {
		return addExceptionPattern(LogbackConfigurator.DEFAULT_PATTERN);
	}

	public boolean isShortenStackTraces() {
		return shortenStackTraces;
	}

	public void removeAppender(final String name) {
		checkArgument(StringUtils.isNotBlank(name), "appender name must not be blank");

		final Appender appender = getAppender(name);
		if (appender != null) {
			appenders.remove(appender);
		}
	}

	public void removeLogger(final String name) {
		checkArgument(StringUtils.isNotBlank(name), "logger name must not be blank");

		final Logger logger = getLogger(name);
		if (logger != null) {
			loggers.remove(logger);
		}
	}

	public void setDefaultAppenders(final List<String> defaultAppenders) {
		this.defaultAppenders = defaultAppenders;
	}

	/**
	 * Sets the defaultLevel.
	 *
	 * @param defaultLevel
	 *            the defaultLevel to set
	 */
	public void setDefaultLevel(final Level defaultLevel) {
		this.defaultLevel = defaultLevel;
	}

	public void setShortenStackTraces(final boolean shortenStackTraces) {
		this.shortenStackTraces = shortenStackTraces;
	}

	/**
	 * Serializes the Logback configuration to the specified XML writer.
	 * <p>
	 * The XML is expected to be readable by Logback. As such, it depends
	 * heavily on Logback and may be bound to different evolution/compatibility
	 * rules.
	 * </p>
	 *
	 * @param writer
	 *            the stream writer
	 * @throws XMLStreamException
	 */
	@Override
	public void toXml(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartDocument();

		writer.writeStartElement("configuration");
		writer.writeAttribute("scan", "true");
		writer.writeAttribute("scanPeriod", "2 minutes");

		writeCommonProperties(writer);
		writeJulLevelChangePropagator(writer);

		for (final Appender appender : getAppenders()) {
			appender.toXml(writer);
		}
		for (final Logger logger : getLoggers()) {
			logger.toXml(writer);
		}

		writeRootLogger(writer);

		writer.writeEndElement();

		writer.writeEndDocument();
	}

	private void writeCommonProperties(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeComment("common properties");
		writeProperty(writer, "BASE_PATH", "${gyrex.instance.area.logs:-logs}");
		writeProperty(writer, "PATTERN_SHORT", getShortPattern());
		writeProperty(writer, "PATTERN_LONG", getLongPattern());
	}

	private void writeJulLevelChangePropagator(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeComment("propagate log level changes to JUL");
		writer.writeStartElement("contextListener");
		writer.writeAttribute("class", LevelChangePropagator.class.getName());
		{
			writer.writeStartElement("resetJUL");
			writer.writeCData("true");
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	private void writeRootLogger(final XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("root");
		writer.writeAttribute("level", getDefaultLevel().toString());
		for (final String appenderRef : getDefaultAppenders()) {
			writer.writeEmptyElement("appender-ref");
			writer.writeAttribute("ref", appenderRef);
		}
		writer.writeEndElement();
	}

}
