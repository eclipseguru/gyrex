package org.eclipse.gyrex.eventbus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;

import org.eclipse.gyrex.common.services.IServiceProxy;
import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1SerializerAndDeserializerVariant1;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType1SerializerAndDeserializerVariant2;
import org.eclipse.gyrex.eventbus.internal.examples.CustomEventType2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TopicBuilderTest {

	private final String id = "123";

	private TopicBuilder topicBuilder;

	private EventService eventService;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void addDeserializer_failsForEmpty() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no deserializer specified");

		topicBuilder.addDeserializer(new IEventDeserializer<?>[] {});
	}

	@Test
	public void addDeserializer_failsForNoarg() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no deserializer specified");

		topicBuilder.addDeserializer();
	}

	@Test
	public void addDeserializer_failsForNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no deserializer specified");

		topicBuilder.addDeserializer((IEventDeserializer<?>[]) null);
	}

	@Test
	public void addDeserializer_failsWithNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("null deserializer not allowed");

		topicBuilder.addDeserializer(new IEventDeserializer<?>[] { null });
	}

	@Test
	public void addDeserializerInternal_failsForDuplicateType() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("duplicate");
		exception.expectMessage(CustomEventType1.class.getName());

		topicBuilder.addDeserializerInternal(new CustomEventType1SerializerAndDeserializerVariant1());
		topicBuilder.addDeserializerInternal(new CustomEventType1SerializerAndDeserializerVariant2());
	}

	@Test
	public void addDeserializerInternal1() throws Exception {

		topicBuilder.addDeserializerInternal(new CustomEventType1SerializerAndDeserializerVariant1());

		assertEquals(1, topicBuilder.deserializers.size());
		assertNotNull(topicBuilder.deserializers.get(CustomEventType1.class));
		assertNull(topicBuilder.deserializers.get(CustomEventType2.class));
		assertNull(topicBuilder.deserializers.get(Object.class));
	}

	@Test
	public void addSerializer_failsForEmpty() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no serializer specified");

		topicBuilder.addSerializer(new IEventSerializer<?>[] {});
	}

	@Test
	public void addSerializer_failsForNoarg() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no serializer specified");

		topicBuilder.addSerializer();
	}

	@Test
	public void addSerializer_failsForNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("no serializer specified");

		topicBuilder.addSerializer((IEventSerializer<?>[]) null);
	}

	@Test
	public void addSerializer_failsWithNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("null serializer not allowed");

		topicBuilder.addSerializer(new IEventSerializer<?>[] { null });
	}

	@Test
	public void addSerializerInternal_failsForDuplicateType() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("duplicate");
		exception.expectMessage(CustomEventType1.class.getName());

		topicBuilder.addSerializerInternal(new CustomEventType1SerializerAndDeserializerVariant1());
		topicBuilder.addSerializerInternal(new CustomEventType1SerializerAndDeserializerVariant2());
	}

	@Test
	public void addSerializerInternal1() throws Exception {

		topicBuilder.addSerializerInternal(new CustomEventType1SerializerAndDeserializerVariant1());

		assertEquals(1, topicBuilder.serializers.size());
		assertNotNull(topicBuilder.serializers.get(CustomEventType1.class));
		assertNull(topicBuilder.serializers.get(CustomEventType2.class));
		assertNull(topicBuilder.serializers.get(Object.class));
	}

	/**
	 * Test method for
	 * {@link org.eclipse.gyrex.eventbus.internal.TopicBuilder#build()}.
	 */
	@Test
	public void build() throws Exception {
		final Topic topic = topicBuilder.build();
		assertNotNull(topic);
		assertSame(id, topic.getId());
	}

	@Test
	public void newTopicBuilder() throws Exception {
		assertSame(id, topicBuilder.id);
	}

	@Test
	public void newTopicBuilder_failsForInvalidId() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("invalid id");

		new TopicBuilder("invalid id äÖá^/\\", eventService);
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		eventService = new EventService("test", mock(IServiceProxy.class, RETURNS_MOCKS));
		topicBuilder = new TopicBuilder(id, eventService);
	}
}
