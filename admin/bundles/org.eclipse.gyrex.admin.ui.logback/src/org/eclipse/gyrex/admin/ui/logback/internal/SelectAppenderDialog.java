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
 *     Peter Grube        - rework to Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.logback.internal;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.logback.config.internal.model.Appender;

import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class SelectAppenderDialog extends ElementListSelectionDialog {

	private final AtomicReference<DialogCallback> callbackRef = new AtomicReference<DialogCallback>();

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	public SelectAppenderDialog(final Shell shell, final Collection<Appender> appenders) {
		super(shell, new LogbackLabelProvider());
		setTitle("Select Appender");
		setMessage("&Select an appender to add to your logger:");
		setElements(appenders.toArray());
	}

	@Override
	public boolean close() {
		final boolean closed = super.close();
		if (closed) {
			final DialogCallback callback = callbackRef.getAndSet(null);
			if (null != callback) {
				callback.dialogClosed(getReturnCode());
			}
		}
		return closed;
	}

	/**
	 * Opens this window, creating it first if it has not yet been created.
	 * <p>
	 * The window will be configured to not block on open. The specified
	 * callback will be set and (if not <code>null</code>) will be called when
	 * the windows is closed. Clients may use {@link #getReturnCode()} to obtain
	 * the return code that {@link #open()} returns in blocking mode.
	 * </p>
	 * 
	 * @see #create()
	 */
	public void openNonBlocking(final DialogCallback callback) {
		if (!callbackRef.compareAndSet(null, callback)) {
			throw new IllegalStateException("Concurrent operation not supported!");
		}
		setBlockOnOpen(false);
		super.open();
	}

}
