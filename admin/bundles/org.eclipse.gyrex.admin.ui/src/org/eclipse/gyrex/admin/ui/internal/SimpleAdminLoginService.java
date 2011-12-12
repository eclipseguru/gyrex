/*******************************************************************************
 * Copyright (c) 2011 Ageto Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal;

import java.io.IOException;

import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;

import org.apache.commons.lang.StringUtils;

/**
 * This class represents a simple {@link LoginService}, that only knows one user
 * with the given name, that only can have the role
 * {@link AdminUiActivator#ADMIN_DEFAULT_ROLE}
 */
public class SimpleAdminLoginService extends MappedLoginService {

	private final String username;
	private final String password;

	/**
	 * Creates a new instance with a giben password. It's highly recommended to
	 * save only hashed passwords.
	 * 
	 * @see Credential#getCredential(String)
	 */
	protected SimpleAdminLoginService(final String username, final String password) {

		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			throw new IllegalArgumentException("Username and password must not be blank");
		}

		this.password = password;
		this.username = username;
	}

	@Override
	protected UserIdentity loadUser(final String username) {
		loadUsersInternal();
		return _users.get(username);
	}

	@Override
	protected void loadUsers() throws IOException {
		loadUsersInternal();
	}

	private void loadUsersInternal() {
		_users.clear();
		putUser(username, Credential.getCredential(password), new String[] { AdminUiActivator.ADMIN_DEFAULT_ROLE });
	}

}
