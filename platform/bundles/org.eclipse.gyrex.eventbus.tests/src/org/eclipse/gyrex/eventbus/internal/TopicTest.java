package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.cloud.services.events.EventMessage;
import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;

import org.osgi.framework.FrameworkUtil;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TopicTest {

	private final String id = "123";

	private EventService eventService;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private Topic topic;

	@Test
	public void getDeserializers() throws Exception {
		final Collection<?> deserializers = topic.getDeserializers(new EventMessage("doesntmatter", String.class.getName(), ByteBuffer.wrap(new byte[0])));
		assertNotNull(deserializers);
		assertTrue(deserializers.size() > 0);
		assertTrue(deserializers.contains(StringDeSerializer.sharedInstance));
	}

	@Test
	public void getEventService() throws Exception {
		assertSame(eventService, topic.getEventService());
	}

	@Test
	public void getId() throws Exception {
		assertSame(id, topic.getId());
	}

	@Test
	public void getSerializer() throws Exception {
		assertSame(StringDeSerializer.sharedInstance, topic.getSerializer(String.class));
	}

	@Test
	public void sendEvent() throws Exception {
		Assume.assumeFalse("cannot run in OSGi due to class visibility issues with Mockito", FrameworkUtil.getBundle(Topic.class) != null);

		final Topic spyedTopic = spy(topic);

		final String event = "randomString-" + System.nanoTime();
		spyedTopic.sendEvent(event);

		verify(spyedTopic).createEventMessage(eq(String.class), eq(StringDeSerializer.sharedInstance.serializeEvent(event)));
		verify(eventService).newEventId();
		verify(eventService).queueEvent(eq(id), any(EventMessage.class));
	}

	@Before
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setUp() throws Exception {
		eventService = spy(new EventService("test", mock(IServiceProxy.class, RETURNS_MOCKS)));

		final Map<Class<?>, IEventSerializer<Object>> serializers = new HashMap<>();
		final Map<Class<?>, IEventDeserializer<Object>> deserializers = new HashMap<>();

		serializers.put(String.class, (IEventSerializer) StringDeSerializer.sharedInstance);
		deserializers.put(String.class, (IEventDeserializer) StringDeSerializer.sharedInstance);

		final Map<String, Object> properties = Collections.emptyMap();
		topic = new Topic(id, properties, serializers, deserializers, eventService);
	}

	@After
	public void validate() {
		validateMockitoUsage();
	}
}
