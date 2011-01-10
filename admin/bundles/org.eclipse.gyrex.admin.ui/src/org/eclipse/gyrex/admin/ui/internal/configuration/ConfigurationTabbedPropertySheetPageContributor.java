/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.configuration;

import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

public class ConfigurationTabbedPropertySheetPageContributor implements ITabbedPropertySheetPageContributor {

	public static final ConfigurationTabbedPropertySheetPageContributor INSTANCE = new ConfigurationTabbedPropertySheetPageContributor();

	private static final String ID = "org.eclipse.gyrex.admin.ui.content.properties";

	@Override
	public String getContributorId() {
		return ID;
	}

}
