/*******************************************************************************
 * Copyright (c) 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.services.locking;

/**
 * A distributed lock.
 * <p>
 * Due to the nature of distributed systems distributed locks are designed with
 * failures in mind. For example, when a connection to a lock service is lost a
 * session may end and the lock is released automatically by the system.
 * Therefore, a client which acquires a lock must check regularly if it still (
 * {@link #isValid()} owns a lock).
 * </p>
 * <p>
 * This interface is typically not implemented by clients but by service
 * providers. As such it is considered part of a service provider API which may
 * evolve faster than the general API. Please get in touch with the development
 * team through the prefered channels listed on <a
 * href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay up-to-date
 * of possible changes for implementors.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDistributedLock {

	/**
	 * Returns the lock identifier.
	 * 
	 * @return the lock id
	 */
	String getId();

	/**
	 * Indicates if the lock is still valid.
	 * <p>
	 * Returns <code>true</code> if and only if the lock is still acquired by
	 * the node.
	 * </p>
	 * <p>
	 * Clients which acquired a lock should call that method regularly in order
	 * to ensure the lock is still valid. For example, a lock may become invalid
	 * as a result of a network interruption.
	 * </p>
	 * <p>
	 * If this method returns false clients must cancel all activities which
	 * require the exclusive lock.
	 * </p>
	 * 
	 * @return <code>true</code> if the lock is still valid, <code>false</code>
	 *         otherwise
	 */
	boolean isValid();

	/**
	 * Releases this lock. Locks must only be released by the thread that
	 * currently owns the lock.
	 */
	void release();

}
