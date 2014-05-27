package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;

import org.junit.Test;

public class ByteBufferDeSerializerTest {

	@Test
	public void rountrip() throws Exception {
		assertNotNull(ByteArrayDeSerializer.sharedInstance);
		assertEquals(ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }), ByteBufferDeSerializer.sharedInstance.deserializeEvent(ByteBufferDeSerializer.sharedInstance.serializeEvent(ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }))));
	}

}
