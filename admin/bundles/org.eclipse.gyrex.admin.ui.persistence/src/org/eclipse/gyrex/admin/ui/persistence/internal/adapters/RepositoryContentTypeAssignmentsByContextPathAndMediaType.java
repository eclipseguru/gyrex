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

import org.eclipse.gyrex.persistence.storage.content.RepositoryContentType;

public class RepositoryContentTypeAssignmentsByContextPathAndMediaType {

	private final RepositoryContentTypeAssignmentsByContextPath parent;
	private final Object mediaType;

	/**
	 * Creates a new instance.
	 * 
	 * @param repositoryContentTypeAssignmentsByContextPath
	 * @param contentType
	 */
	public RepositoryContentTypeAssignmentsByContextPathAndMediaType(final RepositoryContentTypeAssignmentsByContextPath parent, final RepositoryContentType contentType) {
		this.parent = parent;
		mediaType = contentType;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param repositoryContentTypeAssignmentsByContextPath
	 * @param mediaType
	 */
	public RepositoryContentTypeAssignmentsByContextPathAndMediaType(final RepositoryContentTypeAssignmentsByContextPath parent, final String mediaType) {
		this.parent = parent;
		this.mediaType = mediaType;
	}

	public Object[] getChildren() {
		return WorkbenchAdapterImpl.NO_CHILDREN;
	}

	public String getLabel() {
		if (mediaType instanceof RepositoryContentType) {
			return ((RepositoryContentType) mediaType).getMediaType();
		}

		if (null != mediaType) {
			return String.format("%s (unresolved)", mediaType);
		}
		return "<unknown>";
	}

	public RepositoryContentTypeAssignmentsByContextPath getParent() {
		return parent;
	}

}