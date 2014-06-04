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

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Charsets;
import com.google.common.collect.Queues;
import com.google.common.io.Resources;

public class EventMessageDeEncodingTest {

	final class ReceivedMessage {
		public final String topic;
		public final EventMessage message;

		public ReceivedMessage(final String topic, final EventMessage message) {
			this.topic = topic;
			this.message = message;
		}
	}

	private EventMessageReceiver receiver;
	private EventMessageSender sender;
	private ArrayDeque<ReceivedMessage> receivedMessages;

	private void assertSendAndReceiveSameMessageContent(final String topic, final String id, final String type, final byte[] payload) {
		// when
		final EventMessage message = new EventMessage(id, type, BufferUtil.toBuffer(payload));
		sender.sendEvent(topic, message);

		// then
		final ReceivedMessage receivedMessage = receivedMessages.poll();
		assertNotNull(receivedMessage);
		assertNotNull(receivedMessage.topic);
		assertNotNull(receivedMessage.message);
		assertEquals(topic, receivedMessage.topic);
		assertEquals(message.getId(), receivedMessage.message.getId());
		assertEquals(message.getType(), receivedMessage.message.getType());
		assertArrayEquals(payload, BufferUtil.toArray(receivedMessage.message.getPayload()));
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
		receiver = new EventMessageReceiver("receiver", new IEventMessageCallback() {

			@Override
			public void onEventMessage(final String topicId, final EventMessage message) {
				receivedMessages.add(new ReceivedMessage(topicId, message));
			}
		});

		sender = new EventMessageSender("sender");

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
	}
}
