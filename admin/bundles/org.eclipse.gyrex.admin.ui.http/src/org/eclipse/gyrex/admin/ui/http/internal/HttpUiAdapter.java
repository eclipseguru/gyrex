/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Andreas Mihm	- rework new admin ui
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

public class HttpUiAdapter {

	public static String getLabel(final Object object) {
		if (object instanceof ApplicationProviderRegistration) {
			final ApplicationProviderRegistration applicationProviderRegistration = (ApplicationProviderRegistration) object;
			if (StringUtils.isNotBlank(applicationProviderRegistration.getProviderInfo()))
				return applicationProviderRegistration.getProviderInfo();
			return applicationProviderRegistration.getProviderId();
		}
		if (object instanceof ApplicationRegistration) {
			final ApplicationManager mgr = HttpUiActivator.getAppManager();
			final ApplicationRegistration app = (ApplicationRegistration) object;
			final StrBuilder builder = new StrBuilder();
			builder.append(app.getApplicationId());
			final ApplicationProviderRegistration providerRegistration = HttpActivator.getInstance().getProviderRegistry().getProviderRegistration(app.getProviderId());
			if (null != providerRegistration) {
				builder.append(" (").append(providerRegistration.getProviderInfo()).append(")");
			}
			if (mgr.isActive(app.getApplicationId())) {
				builder.append(" [ACTIVE]");
			} else {
				builder.append(" [INACTIVE]");
			}
			return builder.toString();
		}
		return "";
	};

}
