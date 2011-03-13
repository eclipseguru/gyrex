/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

/**
 * Node dummy for {@link InstallStateSection}
 */
public class Node {

	private final String id;
	private final String location;
	private final String lockName;

	/**
	 * Creates a new instance.
	 * 
	 * @param string
	 * @param string2
	 */
	public Node(final String id, final String location, final String lockName) {
		this.id = id;
		this.location = location;
		this.lockName = lockName;
	}

	/**
	 * Returns the id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the location.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Returns the lockName.
	 * 
	 * @return the lockName
	 */
	public String getLockName() {
		return lockName;
	}
}
