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
package org.eclipse.gyrex.admin.ui.http.internal;

import java.util.Set;

import org.eclipse.gyrex.http.internal.application.manager.ApplicationProviderRegistration;
import org.eclipse.gyrex.http.internal.application.manager.ApplicationRegistration;

import org.apache.commons.lang.StringUtils;

public class ApplicationItem {

	private final Set<String> mounts;
	private final ApplicationRegistration applicationRegistration;
	private final ApplicationProviderRegistration applicationProviderRegistration;
	private ApplicationGroup parent;
	private final boolean active;

	public ApplicationItem(final ApplicationRegistration applicationRegistration, final ApplicationProviderRegistration applicationProviderRegistration, final boolean active, final Set<String> mounts) {
		this.applicationRegistration = applicationRegistration;
		this.applicationProviderRegistration = applicationProviderRegistration;
		this.active = active;
		this.mounts = mounts;
	}

	public String getApplicationId() {
		return applicationRegistration.getApplicationId();
	}

	public ApplicationProviderRegistration getApplicationProviderRegistration() {
		return applicationProviderRegistration;
	}

	public ApplicationRegistration getApplicationRegistration() {
		return applicationRegistration;
	}

	public String getContextPath() {
		return applicationRegistration.getContext().getContextPath().toString();
	}

	public Set<String> getMounts() {
		return mounts;
	}

	public ApplicationGroup getParent() {
		return parent;
	}

	public String getProviderId() {
		return applicationRegistration.getProviderId();
	}

	public String getProviderLabel() {
		final String providerInfo = applicationProviderRegistration.getProviderInfo();
		if (StringUtils.isNotBlank(providerInfo))
			return providerInfo;
		return applicationRegistration.getProviderId();
	}

	public boolean isActive() {
		return active;
	}

	public void setParent(final ApplicationGroup parent) {
		this.parent = parent;
	}
}