/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - copied from RAP Incubator
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class FileUploadListenerList {

	private final Set<FileUploadListener> listeners;

	public FileUploadListenerList() {
		listeners = new HashSet<FileUploadListener>();
	}

	public void addUploadListener(final FileUploadListener listener) {
		listeners.add(listener);
	}

	public void notifyUploadFailed(final FileUploadEvent event) {
		final Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			final FileUploadListener listener = (FileUploadListener) iterator.next();
			listener.uploadFailed(event);
		}
	}

	public void notifyUploadFinished(final FileUploadEvent event) {
		final Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			final FileUploadListener listener = (FileUploadListener) iterator.next();
			listener.uploadFinished(event);
		}
	}

	public void notifyUploadProgress(final FileUploadEvent event) {
		final Iterator iterator = listeners.iterator();
		while (iterator.hasNext()) {
			final FileUploadListener listener = (FileUploadListener) iterator.next();
			listener.uploadProgress(event);
		}
	}

	public void removeUploadListener(final FileUploadListener listener) {
		listeners.remove(listener);
	}
}
