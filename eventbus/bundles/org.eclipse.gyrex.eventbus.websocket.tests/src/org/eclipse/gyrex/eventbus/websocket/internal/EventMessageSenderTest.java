package org.eclipse.gyrex.eventbus.websocket.internal;

import static org.junit.Assert.assertSame;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.gyrex.cloud.services.events.EventMessage;

import org.eclipse.jetty.util.BufferUtil;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Queues;

public class EventMessageSenderTest extends AbstractEventMessageSenderReceiverTest {

	private void assertExpectedMessage(final String topic, final EventMessage message, final ReceivedMessage receivedMessage) {
		assertExpectedMessage(topic, message.getId(), message.getType(), BufferUtil.toArray(message.getPayload()), receivedMessage);
	}

	/**
	 * Simulates sending of the same message multiple times (to multiple
	 * receivers)
	 *
	 * @throws Exception
	 */
	@Test
	public void sendSameEventMessageMultipleTimes() throws Exception {
		// given
		final ArrayDeque<ReceivedMessage> receivedMessages = Queues.newArrayDeque();
		final List<EventMessageSender> senders = new ArrayList<>();
		for (int i = 0; i < 20; i++)
			senders.add(newSenderWiredToReceiver(newReceiver(receivedMessages)));

		// when
		final EventMessage message = new EventMessage("test", "test", BufferUtil.toBuffer("some string".getBytes(Charsets.ISO_8859_1)));
		for (final EventMessageSender sender : senders)
			sender.sendEvent("topic", message);

		// then
		assertSame(senders.size(), receivedMessages.size());
		for (final ReceivedMessage receivedMessage : receivedMessages)
			assertExpectedMessage("topic", message, receivedMessage);
	}
}
