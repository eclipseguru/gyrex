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

import java.nio.ByteBuffer;

import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;

public class ByteBufferDeSerializer implements IEventSerializer<ByteBuffer>, IEventDeserializer<ByteBuffer> {

	static final ByteBufferDeSerializer sharedInstance = new ByteBufferDeSerializer();

	@Override
	public ByteBuffer deserializeEvent(final byte[] bytes) throws IllegalArgumentException, IllegalStateException {
		return ByteBuffer.wrap(bytes);
	}

	@Override
	public byte[] serializeEvent(final ByteBuffer value) throws IllegalArgumentException, IllegalStateException {
		if (value.hasArray())
			return value.array();
		final byte[] result = new byte[value.remaining()];
		value.get(result);
		return result;
	}

}
