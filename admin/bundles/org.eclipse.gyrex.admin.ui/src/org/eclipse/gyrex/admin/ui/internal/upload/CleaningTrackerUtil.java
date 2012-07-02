/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Gunnar Wagenknecht - copied from RAP
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.upload;

import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import org.apache.commons.io.FileCleaningTracker;

class CleaningTrackerUtil {

	private static class FileUploadCleanupHandler implements SessionStoreListener {
		public void beforeDestroy(final SessionStoreEvent event) {
			stopCleaningTracker(event.getSessionStore());
		}
	}

	static final String TRACKER_ATTR = CleaningTrackerUtil.class.getName().concat("#cleaningTrackerInstance");

	private static final FileUploadCleanupHandler LISTENER = new FileUploadCleanupHandler();

	public static FileCleaningTracker getCleaningTracker(final boolean create) {
		FileCleaningTracker tracker;
		final ISessionStore store = RWT.getSessionStore();
		synchronized (store) {
			tracker = (FileCleaningTracker) store.getAttribute(TRACKER_ATTR);
			if ((tracker == null) && create) {
				tracker = new FileCleaningTracker();
				store.setAttribute(TRACKER_ATTR, tracker);
				store.addSessionStoreListener(LISTENER);
			}
		}
		return tracker;
	}

	static void stopCleaningTracker(final ISessionStore store) {
		synchronized (store) {
			final FileCleaningTracker tracker = (FileCleaningTracker) store.getAttribute(TRACKER_ATTR);
			if (tracker != null) {
				tracker.exitWhenFinished();
				store.removeAttribute(TRACKER_ATTR);
			}
		}
	}

	private CleaningTrackerUtil() {
		// prevent instantiation
	}
}
