/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.Collection;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

/**
 * {@link IDocumentAttribute} implementation that marks a parent dirty when
 * modified.
 */
public class StoredDocumentAttribute<T> extends BaseDocumentAttribute<T> {

	private final StoredDocument parent;

	/**
	 * Creates a new instance.
	 * 
	 * @param id
	 * @param fieldValues
	 * @param parent
	 */
	public StoredDocumentAttribute(final String id, final Collection<T> fieldValues, final StoredDocument parent) {
		super(id);
		// set values before setting parent so that it will be null to avoid too early dirty trigger
		if (fieldValues != null) {
			set(fieldValues);
		}
		// set parent after setting values
		this.parent = parent;
	}

	@Override
	protected boolean doAdd(final T value) {
		if (parent != null) {
			parent.setDirty(true);
		}
		return super.doAdd(value);
	};

	@Override
	protected void doClear() {
		super.doClear();
	}
}
