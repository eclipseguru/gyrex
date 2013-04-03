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
package org.eclipse.gyrex.admin.ui.jobs.internal;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardAdapter;
import org.eclipse.gyrex.jobs.provider.JobProvider;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import org.apache.commons.lang.StringUtils;

/**
 * Use by content providers to represent a job type.
 */
public class JobType {

	final String id;
	final JobProvider provider;
	private final ServiceReference<JobProvider> serviceReference;

	public JobType(final String id, final JobProvider provider, final ServiceReference<JobProvider> serviceReference) {
		this.id = id;
		this.provider = provider;
		this.serviceReference = serviceReference;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		final String name = provider.getName(id);
		if (StringUtils.isNotBlank(name))
			return name;

		// fallback to service description
		final Object serviceDescription = serviceReference.getProperty(Constants.SERVICE_DESCRIPTION);
		if (serviceDescription instanceof String) {
			final String desc = (String) serviceDescription;
			if (StringUtils.isNotBlank(desc))
				return desc;
		}

		// fallback to id
		return id;
	}

	public JobConfigurationWizardAdapter getWizardAdapter() {
		return AdapterUtil.getAdapter(provider, JobConfigurationWizardAdapter.class);
	}
}
