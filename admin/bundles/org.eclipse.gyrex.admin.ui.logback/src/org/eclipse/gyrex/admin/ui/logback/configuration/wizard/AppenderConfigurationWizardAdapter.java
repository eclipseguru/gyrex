/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.configuration.wizard;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.eclipse.gyrex.logback.config.model.Appender;
import org.eclipse.gyrex.logback.config.spi.AppenderProvider;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * Adapter to allow {@link AppenderProvider appender providers} to participate
 * in the wizard driven appender configuration user interface.
 */
public abstract class AppenderConfigurationWizardAdapter {

	/**
	 * Creates and returns a new appender object.
	 *
	 * @param typeId
	 *            the appender type id (never <code>null</code>)
	 * @return the appender object
	 * @throws IllegalArgumentException
	 *             if the specified type id is not supported by this adapter
	 */
	protected abstract Appender createAppender(String typeId) throws IllegalArgumentException;

	/**
	 * Creates and returns the appender configuration specific wizard pages for
	 * the specified session.
	 *
	 * @param session
	 *            the current configuration session
	 */
	public abstract IWizardPage[] createPages(AppenderConfigurationWizardSession session);

	/**
	 * Initializes the session appender.
	 * <p>
	 * The optional parameter <code>appender</code> may be provided with an
	 * existing appender. If provided and compatible it is expected that the
	 * specified adapter is simply passed on to
	 * {@link AppenderConfigurationWizardSession#setAppender(Appender)}. If
	 * incompatible, an {@link IllegalArgumentException} may be thrown.
	 * </p>
	 * <p>
	 * If not provided, a new, compatible {@link Appender} object will be
	 * created.
	 * </p>
	 * <p>
	 * After the method returns, it is expected that
	 * {@link AppenderConfigurationWizardSession#getAppender()} will return a
	 * non-<code>null</code> value.
	 * </p>
	 *
	 * @param session
	 *            the current configuration session
	 * @param appender
	 *            an existing appender that may be used for initialization if
	 *            compatible (may be <code>null</code>)
	 */
	public final void initializeAppender(final AppenderConfigurationWizardSession session, Appender appender) {
		if (appender != null) {
			checkArgument(isCompatibleAppender(session.getAppenderTypeId(), appender), "Incompatible appender found while editing type '%s'. Please delete appender and re-create.");
		} else {
			appender = checkNotNull(createAppender(session.getAppenderTypeId()));
		}
		session.setAppender(appender);
	}

	/**
	 * Indicates if the specified appender is compatible for the specified type.
	 *
	 * @param typeId
	 *            the appender type id (never <code>null</code>)
	 * @param appender
	 *            the appender
	 * @return <code>true</code> if compatible, <code>false</code> otherwise
	 * @throws IllegalArgumentException
	 *             if the specified type id is not supported by this adapter
	 */
	protected abstract boolean isCompatibleAppender(String typeId, Appender appender) throws IllegalArgumentException;
}
