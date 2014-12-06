/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Mike Tschierschke - initial API and implementation
 *     Gunnar Wagenknecht - rework to new console look (based on RAP Examples)
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.rap.application.RapApplicationEntryPoint;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.osgi.framework.Version;

import org.apache.commons.lang.StringUtils;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class AdminApplication extends RapApplicationEntryPoint implements EntryPoint {

	public static Image getImage(final Display display, final String path) {
		final ImageDescriptor imageDescriptor = AdminUiActivator.getImageDescriptor("img/" + path);
		return imageDescriptor.createImage(display);
	}

	private static String getVersion() {
		final Version version = AdminUiActivator.getInstance().getBundleVersion();
		final StringBuilder resultBuffer = new StringBuilder(60);

		final String buildId = AdminUiActivator.getInstance().getBundle().getBundleContext().getProperty("gyrex.buildId");
		if (StringUtils.isNotBlank(buildId)) {
			resultBuffer.append("(Gyrex ").append(buildId);
			final String buildTimestamp = AdminUiActivator.getInstance().getBundle().getBundleContext().getProperty("gyrex.buildTimestamp");
			if (StringUtils.isNotBlank(buildTimestamp)) {
				resultBuffer.append(" Build ").append(buildTimestamp);
				resultBuffer.append(buildTimestamp);
			}
			resultBuffer.append(')');
		} else if (StringUtils.equals(version.getQualifier(), "qualifier")) {
			// running out of the IDE
			resultBuffer.append("(Developer Build)");
		} else {
			// don't append any version info at all
		}
		return resultBuffer.toString();
	}

	private Image logo;

	@Override
	protected String getFooterText() {
		return "Admin Console " + getVersion();
	}

	@Override
	protected Image getLogo() {
		if (logo != null)
			return logo;
		return logo = getImage(Display.getCurrent(), "gyrex/gyrex-juno.png");
	}

}
