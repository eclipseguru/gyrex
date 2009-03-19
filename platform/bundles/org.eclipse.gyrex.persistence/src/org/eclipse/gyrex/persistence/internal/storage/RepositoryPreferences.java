/*******************************************************************************
 * Copyright (c) 2009 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.persistence.internal.storage;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.gyrex.persistence.storage.settings.IRepositoryPreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Repository preferences implementation.
 */
final class RepositoryPreferences implements IRepositoryPreferences {

	/** securePreferences */
	private final ISecurePreferences securePreferences;
	/** eclipsePreferences */
	private final IEclipsePreferences eclipsePreferences;

	/**
	 * Creates a new instance.
	 * 
	 * @param securePreferences
	 * @param eclipsePreferences
	 */
	RepositoryPreferences(final ISecurePreferences securePreferences, final IEclipsePreferences eclipsePreferences) {
		this.securePreferences = securePreferences;
		this.eclipsePreferences = eclipsePreferences;
	}

	@Override
	public void flush() throws BackingStoreException, IOException {
		eclipsePreferences.flush();
		securePreferences.flush();
	}

	@Override
	public IEclipsePreferences getPreferences() throws IllegalStateException {
		return eclipsePreferences;
	}

	@Override
	public ISecurePreferences getSecurePreferences() throws IllegalStateException {
		return securePreferences;
	}
}