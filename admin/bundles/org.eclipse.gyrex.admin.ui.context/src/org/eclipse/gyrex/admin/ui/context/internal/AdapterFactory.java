package org.eclipse.gyrex.admin.ui.context.internal;

import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.context.IRuntimeContext;
import org.eclipse.gyrex.context.internal.registry.ContextDefinition;

import org.eclipse.core.runtime.IAdapterFactory;

public class AdapterFactory implements IAdapterFactory {

	/** CLASSES */
	private static final Class[] ADAPTER_LIST = new Class[] { LabelAdapter.class };
	private static final LabelAdapter WORKBENCH_ADAPTER = new WorkbenchAdapterImpl();

	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof ContextDefinition) {
			if (adapterType == LabelAdapter.class) {
				return WORKBENCH_ADAPTER;
			}
		}
		if (adaptableObject instanceof IRuntimeContext) {
			if (adapterType == LabelAdapter.class) {
				return WORKBENCH_ADAPTER;
			}
		}
		// not supported
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
