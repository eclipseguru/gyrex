/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.eventbus.internal.examples;

import org.eclipse.gyrex.eventbus.EventHandler;

public class Handler1And2UsingAnnotation {

	@EventHandler
	public void handleCustomEventType1(final CustomEventType1 event) {
		// no-op
	}

	@EventHandler
	public void handleCustomEventType2(final CustomEventType2 event) {
		// no-op
	}
}
