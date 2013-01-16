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
package org.eclipse.gyrex.admin.ui.pages;

/**
 * A little helper that provides hooks into the Admin UI.
 */
public interface IAdminUi {

	/**
	 * Opens a new page in the Admin UI
	 * 
	 * @param pageId
	 * @param args
	 */
	void openPage(String pageId, String[] args);

}
