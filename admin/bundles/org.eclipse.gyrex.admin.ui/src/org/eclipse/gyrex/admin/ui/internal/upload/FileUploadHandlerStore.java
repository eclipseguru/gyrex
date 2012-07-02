/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - copied from RAP Incubator
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rwt.RWT;

public final class FileUploadHandlerStore {

	private static final String ATTR_NAME = FileUploadHandlerStore.class.getName() + ".instance";

	private static final Object LOCK = new Object();

	public static String createToken() {
		final int random1 = (int) (Math.random() * 0xfffffff);
		final int random2 = (int) (Math.random() * 0xfffffff);
		return Integer.toHexString(random1) + Integer.toHexString(random2);
	}

	public static FileUploadHandlerStore getInstance() {
		FileUploadHandlerStore result;
		synchronized (LOCK) {
			result = (FileUploadHandlerStore) RWT.getApplicationStore().getAttribute(ATTR_NAME);
			if (result == null) {
				result = new FileUploadHandlerStore();
				RWT.getApplicationStore().setAttribute(ATTR_NAME, result);
			}
		}
		return result;
	}

	private final Map<String, FileUploadHandler> handlers;

	private final Object lock;

	private boolean registered;

	private FileUploadHandlerStore() {
		handlers = new HashMap<String, FileUploadHandler>();
		lock = new Object();
	}

	public void deregisterHandler(final String token) {
		synchronized (lock) {
			handlers.remove(token);
		}
	}

	private void ensureServiceHandler() {
		synchronized (lock) {
			if (!registered) {
				RWT.getServiceManager().registerServiceHandler(FileUploadServiceHandler.SERVICE_HANDLER_ID, new FileUploadServiceHandler());
				registered = true;
			}
		}
	}

	public FileUploadHandler getHandler(final String token) {
		FileUploadHandler result;
		synchronized (lock) {
			result = handlers.get(token);
		}
		return result;
	}

	public void registerHandler(final String token, final FileUploadHandler fileUploadHandler) {
		ensureServiceHandler();
		synchronized (lock) {
			handlers.put(token, fileUploadHandler);
		}
	}
}
