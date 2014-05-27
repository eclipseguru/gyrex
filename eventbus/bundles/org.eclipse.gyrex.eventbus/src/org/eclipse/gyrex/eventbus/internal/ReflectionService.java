/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.eventbus.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.gyrex.eventbus.IEventHandler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

/**
 * A service to assist dealing with reflection within the Event service
 * implementation.
 */
public class ReflectionService {

	@VisibleForTesting
	final Set<?> ignoredHierarchyTypes = ImmutableSet.of(Object.class);

	@VisibleForTesting
	final LoadingCache<Class<?>, Set<Class<?>>> classHierarchyByClassCache = CacheBuilder.newBuilder().weakKeys().build(new CacheLoader<Class<?>, Set<Class<?>>>() {
		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Set<Class<?>> load(final Class<?> key) {
			return (Set) ImmutableSet.copyOf(Collections2.filter(TypeToken.of(key).getTypes().rawTypes(), not(in(ignoredHierarchyTypes))));
		}
	});

	public void clearCaches() {
		classHierarchyByClassCache.invalidateAll();
		classHierarchyByClassCache.cleanUp();
	}

	public Set<EventHandler> getEventHandlers(final Object object) {
		if (object instanceof IEventHandler) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			final Set<EventHandler> handlers = getEventHandlersFromIEventHandler((IEventHandler) object);
			checkArgument(handlers.size() == 1, "Invalid event handler object (%s). Only one IEventHandler#handleEvent implementation is allowed.", object);
			return handlers;
		} else {
			final Set<EventHandler> handlers = getEventHandlersFromObjectMethods(object);
			checkArgument(handlers.size() > 0, "Invalid event handler object (%s). No compliant methods found.", object);
			return handlers;
		}
	}

	@VisibleForTesting
	Set<EventHandler> getEventHandlersFromIEventHandler(final IEventHandler<Object> eventHandler) {
		final Set<EventHandler> handlers = new HashSet<>();
		for (final Method method : eventHandler.getClass().getMethods()) {
			if (method.getName().equals("handleEvent") && (method.getParameterTypes().length == 1)) {
				final Class<?> eventType = method.getParameterTypes()[0];
				if (isAllowedEventType(eventType)) {
					handlers.add(EventHandler.of(eventType, eventHandler));
				}
			}
		}
		return handlers;
	}

	@VisibleForTesting
	Set<EventHandler> getEventHandlersFromObjectMethods(final Object object) {
		final Set<EventHandler> handlers = new HashSet<>();
		for (final Method method : object.getClass().getMethods()) {
			if (method.isAnnotationPresent(org.eclipse.gyrex.eventbus.EventHandler.class) && (method.getParameterTypes().length == 1)) {
				final Class<?> eventType = method.getParameterTypes()[0];
				if (isAllowedEventType(eventType)) {
					handlers.add(EventHandler.of(eventType, method, object));
				}
			}
		}
		return handlers;
	}

	/**
	 * Returns all super classes and interfaces implemented by the specified
	 * class.
	 * 
	 * @param clazz
	 *            the class to lookup
	 * @return the flat hierarchy of the specified class
	 */
	public Set<Class<?>> getHierarchy(final Class<?> clazz) {
		try {
			return classHierarchyByClassCache.get(clazz);
		} catch (final ExecutionException e) {
			throw Throwables.propagate(e.getCause());
		}
	}

	public boolean isAllowedEventType(final Class<?> type) {
		return !type.equals(Object.class);
	}

}
