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
package org.eclipse.cloudfree.cds.service.result;


/**
 * Asynchronous callback interface when searching is completed.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IListingResultCallback {

	/**
	 * Calles by the listing service when an error occurred during search.
	 * 
	 * @param exception
	 *            the exception
	 */
	void onError(Throwable exception);

	/**
	 * Called by the listing service when a search is completed.
	 * 
	 * @param result
	 *            the listing result
	 */
	void onResult(IListingResult result);

}
