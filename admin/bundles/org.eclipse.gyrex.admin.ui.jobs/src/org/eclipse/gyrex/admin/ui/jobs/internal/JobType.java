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

import org.apache.commons.lang.StringUtils;

/**
 * Use by content providers to represent a job type.
 */
public class JobType {

	final String id, name;
	final JobProvider provider;

	public JobType(final String id, final String name, final JobProvider provider) {
		this.id = id;
		this.name = name;
		this.provider = provider;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		if (StringUtils.isNotBlank(name))
			return name;
		// fallback to id
		return id;
	}

	public JobConfigurationWizardAdapter getWizardAdapter() {
		return AdapterUtil.getAdapter(provider, JobConfigurationWizardAdapter.class);
	}
}
