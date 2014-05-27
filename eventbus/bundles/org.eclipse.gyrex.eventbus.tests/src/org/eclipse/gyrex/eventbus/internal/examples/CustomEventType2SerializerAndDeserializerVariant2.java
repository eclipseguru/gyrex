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

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;

public class CustomEventType2SerializerAndDeserializerVariant2 extends AbstractGenericCustomEventType1SerializerAndDeserializer<CustomEventType2> {

	@Override
	public CustomEventType2 deserializeEvent(final byte[] bytes) throws IllegalArgumentException, IllegalStateException {
		checkState(Arrays.equals(CustomEventType2.class.getName().getBytes(), bytes));
		return new CustomEventType2();
	}

	@Override
	public byte[] serializeEvent(final CustomEventType2 value) throws IllegalArgumentException, IllegalStateException {
		return CustomEventType2.class.getName().getBytes();
	}

}
