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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.gyrex.admin.ui.adapter.AdapterUtil;
import org.eclipse.gyrex.admin.ui.adapter.LabelAdapter;
import org.eclipse.gyrex.admin.ui.persistence.internal.PersistenceUiActivator;
import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.internal.registry.ContextRegistryImpl;
import org.eclipse.gyrex.context.registry.IRuntimeContextRegistry;
import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;
import org.eclipse.gyrex.persistence.storage.lookup.RepositoryContentTypeAssignments;

import org.eclipse.core.runtime.IPath;

@SuppressWarnings("restriction")
public class RepositoryContentTypeAssignmentsByContextPath {

	private final RepositoryContentTypeAssignments parent;
	private final IPath contextPath;
	private Object[] children;

	public RepositoryContentTypeAssignmentsByContextPath(final RepositoryContentTypeAssignments parent, final IPath contextPath) {
		this.parent = parent;
		this.contextPath = contextPath;
	}

	public Object[] getChildren() {
		if (null != children)
			return children;

		final HashSet<String> unresolvedContentTypes = new HashSet<String>();
		final List<RepositoryContentType> mediaTypes = parent.getContentTypes(contextPath, unresolvedContentTypes);
		final List<Object> children = new ArrayList<Object>(mediaTypes.size() + unresolvedContentTypes.size());
		for (final RepositoryContentType mediaType : mediaTypes) {
			children.add(new RepositoryContentTypeAssignmentsByContextPathAndMediaType(this, mediaType));
		}
		for (final String unresolvedType : unresolvedContentTypes) {
			children.add(new RepositoryContentTypeAssignmentsByContextPathAndMediaType(this, unresolvedType));
		}
		return this.children = children.toArray(new Object[children.size()]);
	}

	private ContextRegistryImpl getContextRegistry() {
		return (ContextRegistryImpl) PersistenceUiActivator.getInstance().getService(IRuntimeContextRegistry.class);
	}

	public String getLabel() {
		// try to lookup the context definition
		final ContextDefinition definition = getContextRegistry().getDefinition(contextPath);
		if (null != definition) {
			final LabelAdapter adapter = AdapterUtil.getAdapter(definition, LabelAdapter.class);
			if (null != adapter)
				return adapter.getLabel(definition);
		}
		return contextPath.toString();
	}

	public RepositoryContentTypeAssignments getParent() {
		return parent;
	}

}