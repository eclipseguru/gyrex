package org.eclipse.gyrex.eventbus.websocket.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.eventbus.websocket.internal.EventMessageReceiver.IEventMessageCallback;

import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public abstract class AbstractEventMessageSenderReceiverTest {

	protected final class ReceivedMessage {
		public final String topic;
		public final EventMessage message;

		public ReceivedMessage(final String topic, final EventMessage message) {
			this.topic = topic;
			this.message = message;
		}
	}

	protected void assertExpectedMessage(final String expectedTopic, final String expectedId, final String expectedType, final byte[] expectedPayload, final ReceivedMessage receivedMessage) {
		assertNotNull(receivedMessage);
		assertNotNull(receivedMessage.topic);
		assertNotNull(receivedMessage.message);
		assertEquals(expectedTopic, receivedMessage.topic);
		assertEquals(expectedId, receivedMessage.message.getId());
		assertEquals(expectedType, receivedMessage.message.getType());
		assertArrayEquals(expectedPayload, BufferUtil.toArray(receivedMessage.message.getPayload()));
	}

	protected EventMessageReceiver newReceiver(final ArrayDeque<ReceivedMessage> receivedMessages) {
		final EventMessageReceiver receiver = new EventMessageReceiver("receiver", new IEventMessageCallback() {

			@Override
			public void onEventMessage(final String topicId, final EventMessage message) {
				receivedMessages.add(new ReceivedMessage(topicId, message));
			}
		});
		return receiver;
	}

	protected EventMessageSender newSenderWiredToReceiver(final EventMessageReceiver receiver) {
		final EventMessageSender sender = new EventMessageSender("sender");

		// init sender with session that will forward sent buffers to receiver
		final Session senderSession = mock(Session.class, RETURNS_DEEP_STUBS);
		final RemoteEndpoint senderSessionRemote = mock(RemoteEndpoint.class);
		when(senderSession.getRemote()).thenReturn(senderSessionRemote);
		when(senderSession.getPolicy().getMaxBinaryMessageSize()).thenReturn(64 * 1024);

		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation) throws Throwable {
				final ByteBuffer buffer = (ByteBuffer) invocation.getArguments()[0];
				final byte[] array = BufferUtil.toArray(buffer);
				receiver.onWebSocketBinary(array, 0, array.length);
				return null;
			}
		}).when(senderSessionRemote).sendBytes(any(ByteBuffer.class), any(WriteCallback.class));
		sender.onWebSocketConnect(senderSession);
		return sender;
	}

}