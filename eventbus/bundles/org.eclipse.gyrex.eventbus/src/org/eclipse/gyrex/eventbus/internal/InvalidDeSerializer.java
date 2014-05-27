package org.eclipse.gyrex.eventbus.internal;

import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;

/**
 * Helper for annotation defaults.
 */
public final class InvalidDeSerializer implements IEventSerializer<Object>, IEventDeserializer<Object> {

	@Override
	public Object deserializeEvent(final byte[] bytes) throws IllegalArgumentException, IllegalStateException {
		throw new IllegalStateException("Do not use this deserializer!");
	}

	@Override
	public byte[] serializeEvent(final Object value) throws IllegalArgumentException, IllegalStateException {
		throw new IllegalStateException("Do not use this serializer!");
	}
}