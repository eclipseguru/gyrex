/*******************************************************************************
 * Copyright (c) 2011 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.http.internal;

import org.eclipse.gyrex.http.internal.HttpActivator;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationManager;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.views.properties.IPropertySource;

import org.apache.commons.lang.text.StrBuilder;

@SuppressWarnings("restriction")
public class HttpUiAdapterFactory implements IAdapterFactory {

	public static final WorkbenchAdapter WORKBENCH_ADAPTER = new WorkbenchAdapter() {
		@Override
		public String getLabel(final Object object) {
			if (object instanceof ApplicationProviderRegistration) {
				final ApplicationProviderRegistration applicationProviderRegistration = (ApplicationProviderRegistration) object;
				return String.format("%s (%s)", applicationProviderRegistration.getProviderInfo(), applicationProviderRegistration.getProviderId());
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
			return super.getLabel(object);
		};
	};

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (IWorkbenchAdapter.class == adapterType) {
			return WORKBENCH_ADAPTER;
		} else if (IPropertySource.class == adapterType) {
			if (adaptableObject instanceof ApplicationRegistration) {
				return new ApplicationRegistrationPropertySource((ApplicationRegistration) adaptableObject);
			}
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class, IPropertySource.class };
	}

}
