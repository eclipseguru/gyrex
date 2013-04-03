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
package org.eclipse.gyrex.admin.ui.jobs.internal.externalprocess;

import org.eclipse.gyrex.admin.ui.jobs.configuration.wizard.JobConfigurationWizardAdapter;

import org.eclipse.core.runtime.IAdapterFactory;

public class AdapterFactory implements IAdapterFactory {

	private static final ExternalProcessJobConfigurationWizardAdapter ADAPTER = new ExternalProcessJobConfigurationWizardAdapter();
	private static final Class[] TYPES = new Class[] { JobConfigurationWizardAdapter.class };

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == JobConfigurationWizardAdapter.class)
			return ADAPTER;
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return TYPES;
	}

}
