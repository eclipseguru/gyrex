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
package org.eclipse.gyrex.eventbus.internal;

import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;

public class ByteArrayDeSerializer implements IEventSerializer<byte[]>, IEventDeserializer<byte[]> {

	static final ByteArrayDeSerializer sharedInstance = new ByteArrayDeSerializer();

	@Override
	public byte[] deserializeEvent(final byte[] bytes) throws IllegalArgumentException, IllegalStateException {
		return bytes;
	}

	@Override
	public byte[] serializeEvent(final byte[] value) throws IllegalArgumentException, IllegalStateException {
		return value;
	}

}
