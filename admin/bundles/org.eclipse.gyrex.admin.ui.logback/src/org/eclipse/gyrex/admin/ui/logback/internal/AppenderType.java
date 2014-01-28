/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
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

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.ImageAdapter;
import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardAdapter;
import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.lang.StringUtils;

/**
 * Use by content providers to represent an appender type.
 */
public class AppenderType implements ImageAdapter {

	final String id, name;
	final AppenderProvider provider;

	public AppenderType(final String id, final String name, final AppenderProvider provider) {
		this.id = id;
		this.name = name;
		this.provider = provider;
	}

	public String getId() {
		return id;
	}

	@Override
	public ImageDescriptor getImageDescriptor(final Object object) {
		if (object != this)
			return null;

		switch (getId()) {
			case "console":
				return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_CONSOLE_APPENDER);

			case "file":
				return LogbackUiImages.getImageDescriptor(LogbackUiImages.IMG_SIFTING_APPENDER);

			default:
				return null;
		}
	}

	public String getName() {
		if (StringUtils.isNotBlank(name))
			return name;
		// fallback to id
		return id;
	}

	public AppenderConfigurationWizardAdapter getWizardAdapter() {
		return AdapterUtil.getAdapter(provider, AppenderConfigurationWizardAdapter.class);
	}
}
