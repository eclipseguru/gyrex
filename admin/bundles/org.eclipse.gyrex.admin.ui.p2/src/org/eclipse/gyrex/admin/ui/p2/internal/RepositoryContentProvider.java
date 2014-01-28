/*******************************************************************************
 * Copyright (c) 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Peter Grube - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.util.Collection;

import org.eclipse.gyrex.p2.internal.repositories.IRepositoryDefinitionManager;
import org.eclipse.gyrex.p2.internal.repositories.RepositoryDefinition;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 */
public class RepositoryContentProvider implements IStructuredContentProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IRepositoryDefinitionManager) {
			final IRepositoryDefinitionManager repoManager = (IRepositoryDefinitionManager) inputElement;
			final Collection<RepositoryDefinition> packages = repoManager.getRepositories();
			return packages.toArray();
		}
		return null;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		// TODO Auto-generated method stub

	}

}
