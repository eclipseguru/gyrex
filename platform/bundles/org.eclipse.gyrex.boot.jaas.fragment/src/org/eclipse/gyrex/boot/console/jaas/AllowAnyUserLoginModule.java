/*******************************************************************************
 * Copyright (c) 2013 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.boot.console.jaas;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.eclipse.equinox.console.jaas.UserPrincipal;

/**
 * A simple login module which allows any combination of username/password.
 */
public class AllowAnyUserLoginModule implements LoginModule {

	private volatile Subject subject;
	private volatile CallbackHandler callbackHandler;
	private volatile UserPrincipal userPrincipal;

	@Override
	public boolean abort() throws LoginException {
		userPrincipal.destroy();
		userPrincipal = null;
		return true;
	}

	@Override
	public synchronized boolean commit() throws LoginException {
		subject.getPrincipals().add(userPrincipal);
		return true;
	}

	@Override
	public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options) {
		this.subject = subject;
		this.callbackHandler = callbackHandler;
	}

	@Override
	public boolean login() throws LoginException {
		final NameCallback nameCallback = new NameCallback("username: ");
		try {
			callbackHandler.handle(new Callback[] { nameCallback });
		} catch (final Exception e) {
			throw new FailedLoginException("Cannot get username");
		}

		final String username = nameCallback.getName();
		userPrincipal = new UserPrincipal(username, username);
		return true;
	}

	@Override
	public boolean logout() throws LoginException {
		synchronized (this) {
			subject.getPrincipals().remove(userPrincipal);
		}
		subject = null;
		userPrincipal.destroy();
		userPrincipal = null;
		return true;
	}

}
