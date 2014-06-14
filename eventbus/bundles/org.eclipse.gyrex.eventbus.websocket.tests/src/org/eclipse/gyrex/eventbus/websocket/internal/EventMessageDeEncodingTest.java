package org.eclipse.gyrex.eventbus.websocket.internal;

import java.util.ArrayDeque;

import org.eclipse.gyrex.cloud.services.events.EventMessage;

import org.eclipse.jetty.util.BufferUtil;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Queues;
import com.google.common.io.Resources;

public class EventMessageDeEncodingTest extends AbstractEventMessageSenderReceiverTest {

	EventMessageReceiver receiver;
	EventMessageSender sender;
	ArrayDeque<ReceivedMessage> receivedMessages;

	private void assertSendAndReceiveSameMessageContent(final String topic, final String id, final String type, final byte[] payload) {
		// when
		final EventMessage message = new EventMessage(id, type, BufferUtil.toBuffer(payload));
		sender.sendEvent(topic, message);

		// then
		final ReceivedMessage receivedMessage = receivedMessages.poll();
		assertExpectedMessage(topic, message.getId(), message.getType(), payload, receivedMessage);
	}

	@Test
	public void decode_and_encode_rountrip1() {
		// given
		final String topic = "atopic";
		final String id = "myid";
		final String type = "mytype";
		final byte[] payload = "test string".getBytes(Charsets.ISO_8859_1);

		// then
		assertSendAndReceiveSameMessageContent(topic, id, type, payload);
	}

	@Test
	public void decode_and_encode_rountrip2() throws Exception {
		// given
		final String topic = "atopic" + System.nanoTime();
		final String id = "myid" + System.nanoTime();
		final String type = "mytype" + System.nanoTime();
		final byte[] payload = Resources.toByteArray(EventMessageDeEncodingTest.class.getResource("EventMessageDeEncodingTest.class"));

		// then
		assertSendAndReceiveSameMessageContent(topic, id, type, payload);
	}

	@Before
	public void setUp() throws Exception {
		receivedMessages = Queues.newArrayDeque();
		receiver = newReceiver(receivedMessages);
		sender = newSenderWiredToReceiver(receiver);
	}
}
