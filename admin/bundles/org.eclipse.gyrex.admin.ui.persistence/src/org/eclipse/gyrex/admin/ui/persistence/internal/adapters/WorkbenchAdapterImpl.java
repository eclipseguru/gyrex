/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.persistence.internal.adapters;

import java.util.List;

import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.admin.ui.persistence.internal.PersistenceUiActivator;
import org.eclipse.gyrex.persistence.storage.lookup.RepositoryContentTypeAssignments;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryDefinition;
import org.eclipse.gyrex.persistence.storage.registry.IRepositoryRegistry;

import org.eclipse.core.runtime.IPath;

public final class WorkbenchAdapterImpl implements LabelAdapter {
	public static final Object[] NO_CHILDREN = new Object[0];

	public Object[] getChildren(final Object o) {
		if (o instanceof RepositoryContentTypeAssignments) {
			final List<IPath> contextPaths = ((RepositoryContentTypeAssignments) o).getContextPaths();
			final Object[] children = new Object[contextPaths.size()];
			for (int i = 0; i < children.length; i++) {
				children[i] = new RepositoryContentTypeAssignmentsByContextPath((RepositoryContentTypeAssignments) o, contextPaths.get(i));
			}
			return children;
		}
		if (o instanceof RepositoryContentTypeAssignmentsByContextPath)
			return ((RepositoryContentTypeAssignmentsByContextPath) o).getChildren();
		if (o instanceof RepositoryContentTypeAssignmentsByContextPathAndMediaType)
			return ((RepositoryContentTypeAssignmentsByContextPathAndMediaType) o).getChildren();
		return NO_CHILDREN;
	}

	@Override
	public String getLabel(final Object o) {
		if (o instanceof RepositoryContentTypeAssignments)
			return String.format("Assignments of %s", ((RepositoryContentTypeAssignments) o).getRepositoryId());
		if (o instanceof RepositoryContentTypeAssignmentsByContextPath)
			return ((RepositoryContentTypeAssignmentsByContextPath) o).getLabel();
		if (o instanceof RepositoryContentTypeAssignmentsByContextPathAndMediaType)
			return ((RepositoryContentTypeAssignmentsByContextPathAndMediaType) o).getLabel();
		if (o instanceof IRepositoryDefinition)
			return ((IRepositoryDefinition) o).getRepositoryId();
		return String.valueOf(o);
	}

	public Object getParent(final Object o) {
		if (o instanceof RepositoryContentTypeAssignments)
			return PersistenceUiActivator.getInstance().getService(IRepositoryRegistry.class).getRepositoryDefinition(((RepositoryContentTypeAssignments) o).getRepositoryId());
		if (o instanceof RepositoryContentTypeAssignmentsByContextPath)
			return ((RepositoryContentTypeAssignmentsByContextPath) o).getParent();
		if (o instanceof RepositoryContentTypeAssignmentsByContextPathAndMediaType)
			return ((RepositoryContentTypeAssignmentsByContextPathAndMediaType) o).getParent();
		return null;
	}

}