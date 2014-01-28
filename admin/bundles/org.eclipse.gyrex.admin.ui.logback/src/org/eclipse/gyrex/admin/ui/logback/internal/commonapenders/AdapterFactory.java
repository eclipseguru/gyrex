package org.eclipse.gyrex.admin.ui.logback.internal.commonapenders;

import org.eclipse.gyrex.admin.ui.logback.configuration.wizard.AppenderConfigurationWizardAdapter;

import org.eclipse.core.runtime.IAdapterFactory;

public class AdapterFactory implements IAdapterFactory {

	private static final CommonApendersWizardAdapter SHARED_ADAPTER = new CommonApendersWizardAdapter();
	private static final Class[] CLASSES = new Class[] { AppenderConfigurationWizardAdapter.class };

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adapterType == AppenderConfigurationWizardAdapter.class)
			return SHARED_ADAPTER;
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return CLASSES;
	}

}
