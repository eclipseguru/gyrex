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
package org.eclipse.gyrex.cds;

import org.eclipse.gyrex.model.common.IModelObject;

/**
 * A listing attribute.
 */
public interface IListingAttribute extends IModelObject {

	/**
	 * Returns the attribute name.
	 * 
	 * @return the attribute name
	 */
	String getName();

	/**
	 * Returns the attribute values.
	 * 
	 * @return the attribute values
	 */
	Object[] getValues();
}
