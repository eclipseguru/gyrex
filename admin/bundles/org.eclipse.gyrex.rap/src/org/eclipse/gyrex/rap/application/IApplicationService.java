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
package org.eclipse.gyrex.rap.application;

/**
 * A service provided by the application for interacting with it.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IApplicationService {

	/**
	 * Opens the specified page with the optional arguments in the application.
	 * <p>
	 * Note, if a page with the specified id is already opened it will be
	 * reloaded.
	 * </p>
	 *
	 * @param pageId
	 * @param args
	 */
	void openPage(String pageId, String... args);

}
