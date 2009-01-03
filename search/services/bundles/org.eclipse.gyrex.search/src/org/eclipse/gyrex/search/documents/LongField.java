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
package org.eclipse.cloudfree.cds.model.documents;

/**
 * A Long field.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class LongField extends Field<Long> {

	/**
	 * Creates a new Long field.
	 * 
	 * @param name
	 *            the field name
	 */
	public LongField(final String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cloudfree.cds.model.documents.Field#getType()
	 */
	@Override
	Class<Long> getType() {
		return Long.class;
	}
}
