package org.eclipse.gyrex.cloud.services.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

public class EventMessageTest {

	private static final byte[] BYTES = "hello".getBytes();
	private static final String MYTYPE = "mytype";
	private static final String MYID = "myid";

	@Test
	public void EventMessage() throws Exception {
		final EventMessage eventMessage = new EventMessage(MYID, MYTYPE, ByteBuffer.wrap(BYTES));
		assertTrue(eventMessage.getCreated() > 0);
		assertTrue(eventMessage.getCreated() <= System.currentTimeMillis());

		assertSame(MYID, eventMessage.getId());
		assertSame(MYTYPE, eventMessage.getType());
		assertEquals(BYTES, eventMessage.getPayload().array());
	}

}
