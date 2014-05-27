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
import org.eclipse.gyrex.eventbus.IEventHandler;

public class Handler1ImplementingInterfaceAndUsingAnnotations implements IEventHandler<CustomEventType1> {

	@Override
	public void handleEvent(final CustomEventType1 event) {
		// no-op
	}

	@EventHandler
	public void handlerEventMethod2(final CustomEventType1 event) {
		//no-op
	}
}
