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
package org.eclipse.gyrex.cds.documents;

/**
 * A Double field.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class DoubleField extends Field<Double> {

	/**
	 * Creates a new Long field.
	 * 
	 * @param name
	 *            the field name
	 */
	public DoubleField(final String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.documents.Field#getType()
	 */
	@Override
	Class<Double> getType() {
		return Double.class;
	}
}
