package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ByteArrayDeSerializerTest {

	@Test
	public void rountrip() throws Exception {
		assertNotNull(ByteArrayDeSerializer.sharedInstance);
		assertArrayEquals(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, ByteArrayDeSerializer.sharedInstance.deserializeEvent(ByteArrayDeSerializer.sharedInstance.serializeEvent(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })));
	}

}
