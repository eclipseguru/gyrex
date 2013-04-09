/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import org.eclipse.gyrex.logback.config.internal.model.Appender;
import org.eclipse.gyrex.logback.config.internal.model.ConsoleAppender;
import org.eclipse.gyrex.logback.config.internal.model.FileAppender;
import org.eclipse.gyrex.logback.config.internal.model.LogbackConfig;
import org.eclipse.gyrex.logback.config.internal.model.Logger;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.text.StrBuilder;

public class LogbackLabelProvider extends LabelProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private LocalResourceManager manager;

	@Override
	public void dispose() {
		if (null != manager) {
			manager.dispose();
			manager = null;
		}
		super.dispose();
	}

	@Override
	public Image getImage(final Object element) {
		final ImageDescriptor descriptor = getImageDescriptor(element);
		if (descriptor != null)
			return getResourceManager().createImage(descriptor);
		return super.getImage(element);
	}

	private ImageDescriptor getImageDescriptor(final Object element) {
		if (element instanceof ConsoleAppender)
			return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_CONSOLE_APPENDER);
		if (element instanceof Appender) {
			final Appender appender = (Appender) element;
			if (appender.canSift() && appender.isSeparateLogOutputsPerMdcProperty())
				return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_SIFTING_APPENDER);
			return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_APPENDER);
		}
		if (element instanceof Logger)
			return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_LOGGER);
		if (element instanceof DefaultLogger)
			//			return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_LOGGER);
			return null;
		return null;
	}

	private ResourceManager getResourceManager() {
		if (null == manager) {
			manager = new LocalResourceManager(JFaceResources.getResources());
		}
		return manager;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof LogbackConfig)
			return "Loback Configuration";
		if (element instanceof FileAppender) {
			final FileAppender fileAppender = (FileAppender) element;
			final StrBuilder text = new StrBuilder();
			text.append(fileAppender.getName());
			text.append(String.format(" (-> %s)", fileAppender.getFileName()));
			if (null != fileAppender.getThreshold()) {
				text.append(String.format(" [%s]", fileAppender.getThreshold()));
			}
			if (fileAppender.isSeparateLogOutputsPerMdcProperty()) {
				text.append(String.format(" [mdc:%s]", fileAppender.getSiftingMdcPropertyName()));
			}
			return text.toString();
		}
		if (element instanceof Appender)
			return ((Appender) element).getName();
		if (element instanceof Logger) {
			final Logger logger = (Logger) element;
			final StrBuilder text = new StrBuilder();
			text.append(logger.getName()).append(": ");
			text.append(logger.getLevel());
			if (!logger.isInheritOtherAppenders()) {
				text.append(" (not inheriting other appenders)");
			}
			return text.toString();
		}
		if (element instanceof DefaultLogger)
			return String.format("Default Level: %s", ((DefaultLogger) element).getLevel());
		if (element instanceof DefaultLogger)
			return String.format("Default Log Level: %s", ((DefaultLogger) element).getLevel());
		if (element instanceof AppenderReference)
			return ((AppenderReference) element).getAppenderRef();
		return String.valueOf(element);
	}
}
