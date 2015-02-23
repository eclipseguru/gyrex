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
package org.eclipse.gyrex.logback.config.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.model.Logger;
import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ch.qos.logback.classic.Level;

public class PreferenceBasedLogbackConfigStore {

	private static final String TYPE = "type";
	private static final String LEVEL = "level";
	private static final String INHERIT_OTHER_APPENDERS = "inheritOtherAppenders";
	private static final String APPENDER_REFS = "appenderRefs";
	private static final String LOGGERS = "loggers";
	private static final String APPENDERS = "appenders";
	private static final String DEFAULT_APPENDER_REFS = "defaultAppenderRefs";
	private static final String DEFAULT_LEVEL = "defaultLevel";
	private static final String THRESHOLD = "threshold";

	private void configureAppender(final Appender appender, final Preferences node, final AppenderProvider provider) throws Exception {
		appender.setName(node.name());
		if (null != node.get(THRESHOLD, null)) {
			appender.setThreshold(Level.toLevel(node.get(THRESHOLD, null), Level.OFF));
		}

		provider.configureAppender(appender, node);
		checkState(node.name().equals(appender.getName()), "provider (%s) must not change appender name from '%s' to '%s'", provider, node.name(), appender.getName());
	}

	private Appender loadAppender(final Preferences node) throws Exception {
		final String typeId = node.get(TYPE, null);
		final AppenderProvider provider = LogbackConfigActivator.getInstance().getAppenderProviderRegistry().getProvider(typeId);
		if (provider != null) {
			final Appender appender = provider.createAppender(typeId);
			checkState(appender != null, "provider (%s) did not return an appender for type '%s'", provider, typeId);
			configureAppender(appender, node, provider);
			return appender;
		}

		// TODO can we support a generic appender?
		throw new IllegalArgumentException(String.format("unknown appender type '%s' (appender '%s')", typeId, node.name()));
	}

	public LogbackConfig loadConfig(final Preferences node) throws Exception {
		final LogbackConfig config = new LogbackConfig();

		final String defaultLevel = node.get(DEFAULT_LEVEL, null);
		if (null != defaultLevel) {
			config.setDefaultLevel(Level.toLevel(defaultLevel, Level.INFO));
		}
		for (final String appender : node.node(DEFAULT_APPENDER_REFS).keys()) {
			config.getDefaultAppenders().add(appender);
		}

		final String[] appenders = node.node(APPENDERS).childrenNames();
		for (final String appender : appenders) {
			config.addAppender(loadAppender(node.node(APPENDERS).node(appender)));
		}

		final String[] loggers = node.node(LOGGERS).childrenNames();
		for (final String logger : loggers) {
			config.addLogger(loadLogger(logger, node.node(LOGGERS).node(logger)));
		}

		return config;
	}

	private Logger loadLogger(final String name, final Preferences node) throws BackingStoreException {
		final Logger logger = new Logger();
		logger.setName(name);
		if (null != node.get(LEVEL, null)) {
			logger.setLevel(Level.toLevel(node.get(LEVEL, null), Level.INFO));
		}
		if (null != node.get(INHERIT_OTHER_APPENDERS, null)) {
			logger.setInheritOtherAppenders(node.getBoolean(INHERIT_OTHER_APPENDERS, true));
		}
		for (final String appender : node.node(APPENDER_REFS).keys()) {
			logger.getAppenderReferences().add(appender);
		}
		return logger;
	}

	private void saveAppender(final Appender appender, final Preferences node) throws Exception {
		final AppenderProvider provider = LogbackConfigActivator.getInstance().getAppenderProviderRegistry().getProvider(appender.getTypeId());
		if (provider != null) {
			writeAppenderConfiguration(appender, node, provider);
		} else {
			throw new IllegalArgumentException(String.format("unknown appender type '%s' (appender '%s')", appender.getClass().getSimpleName(), appender.getName()));
		}
	}

	private void saveAppenderRefs(final List<String> appenderRefs, final Preferences appenderRefsNode) throws BackingStoreException {
		if (appenderRefs.isEmpty()) {
			appenderRefsNode.removeNode();
		} else {
			for (final String appender : appenderRefsNode.keys()) {
				if (!appenderRefs.contains(appender)) {
					appenderRefsNode.remove(appender);
				}
			}
			for (final String appender : appenderRefs) {
				appenderRefsNode.put(appender, "inUse");
			}
		}
	}

	public void saveConfig(final LogbackConfig config, final Preferences node) throws Exception {
		if (config.getDefaultLevel() != Level.INFO) {
			node.put(DEFAULT_LEVEL, config.getDefaultLevel().toString());
		} else {
			node.remove(DEFAULT_LEVEL);
		}

		saveAppenderRefs(config.getDefaultAppenders(), node.node(DEFAULT_APPENDER_REFS));

		final Preferences appendersNode = node.node(APPENDERS);
		final Map<String, Appender> appenders = toAppendersByNameMap(config.getAppenders());
		for (final String appender : appendersNode.childrenNames()) {
			if (!appenders.containsKey(appender)) {
				appendersNode.node(appender).removeNode();
			}
		}
		for (final Appender appender : appenders.values()) {
			saveAppender(appender, appendersNode.node(appender.getName()));
		}

		final Preferences loggersNode = node.node(LOGGERS);
		final Map<String, Logger> loggers = toLoggersByNameMap(config.getLoggers());
		for (final String logger : loggersNode.childrenNames()) {
			if (!loggers.containsKey(logger)) {
				loggersNode.node(logger).removeNode();
			}
		}
		for (final Logger logger : loggers.values()) {
			saveLogger(logger, loggersNode.node(logger.getName()));
		}

		node.flush();
	}

	private void saveLogger(final Logger logger, final Preferences node) throws BackingStoreException {
		if (null != logger.getLevel()) {
			node.put(LEVEL, logger.getLevel().toString());
		} else {
			node.remove(LEVEL);
		}
		if (!logger.isInheritOtherAppenders()) {
			node.putBoolean(INHERIT_OTHER_APPENDERS, false);
		} else {
			node.remove(INHERIT_OTHER_APPENDERS);
		}

		saveAppenderRefs(logger.getAppenderReferences(), node.node(APPENDER_REFS));
	}

	private Map<String, Appender> toAppendersByNameMap(final List<Appender> appenders) {
		final LinkedHashMap<String, Appender> map = new LinkedHashMap<String, Appender>(appenders.size());
		for (final Appender a : appenders) {
			map.put(a.getName(), a);
		}
		return map;
	}

	private Map<String, Logger> toLoggersByNameMap(final List<Logger> loggers) {
		final LinkedHashMap<String, Logger> map = new LinkedHashMap<String, Logger>(loggers.size());
		for (final Logger l : loggers) {
			map.put(l.getName(), l);
		}
		return map;
	}

	private void writeAppenderConfiguration(final Appender appender, final Preferences node, final AppenderProvider provider) throws Exception {
		if (null != appender.getThreshold()) {
			node.put(THRESHOLD, appender.getThreshold().toString());
		} else {
			node.remove(THRESHOLD);
		}

		provider.writeAppenderConfiguration(appender, node);
		node.put(TYPE, appender.getTypeId());
	}
}
