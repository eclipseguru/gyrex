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
package org.eclipse.gyrex.admin.ui.adapter;

/**
 * An adapter for presenting elements in the Admin UI.
 */
public interface LabelAdapter {

	/**
	 * Returns the label text for this element. This is typically used to assign
	 * a label to this object when displayed in the UI. Returns an empty string
	 * if there is no appropriate label text for this object.
	 * 
	 * @param o
	 *            The object to get a label for.
	 * @return String
	 */
	public String getLabel(Object o);
}
