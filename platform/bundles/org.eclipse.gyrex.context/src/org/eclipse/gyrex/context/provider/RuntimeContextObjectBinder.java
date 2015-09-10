/*******************************************************************************
 * Copyright (c) 2015 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.context.provider;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * A binder for binding object types to implementation classes.
 * <p>
 * The binder allows to bind object types to implementation classes using a
 * fluent binding API (similar to a builder pattern).
 * </p>
 * <p>
 * Note, this class is part of a service provider API which may evolve faster
 * than the general contextual runtime API. Please get in touch with the
 * development team through the preferred channels listed on
 * <a href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay
 * up-to-date of possible changes.
 * </p>
 *
 * @since 1.4
 */
public final class RuntimeContextObjectBinder {

	/**
	 * An object type binding builder.
	 *
	 * @param <T>
	 *            the type being bound
	 */
	public static final class BindingBuilder<T> {

		private final Class<T> type;
		private final RuntimeContextObjectBinder binder;
		private boolean bound;

		BindingBuilder(final Class<T> type, final RuntimeContextObjectBinder runtimeContextObjectBinder) {
			this.type = type;
			this.binder = runtimeContextObjectBinder;
		}

		/**
		 * Specifies the implementation class to bind this binding to.
		 * <p>
		 * Note, the binding is immediately built as part of this method.
		 * Calling it more then once will result in an
		 * {@link IllegalStateException} being thrown.
		 * </p>
		 *
		 * @param implementationClass
		 *            the implementation class
		 * @return the binder
		 */
		public final RuntimeContextObjectBinder toImplementationClass(final Class<? extends T> implementationClass) {
			checkState(!bound, "already bound");
			bound = true;
			checkArgument(nonNull(implementationClass), "No implementation class provided!");
			binder.addBinding(type, implementationClass);
			return binder;
		}
	}

	private final ImmutableMap.Builder<Class<?>, Class<?>> bindings = ImmutableMap.builder();

	RuntimeContextObjectBinder() {
		// empty
	}

	void addBinding(final Class<?> type, final Class<?> implementationClass) {
		bindings.put(type, implementationClass);
	}

	/**
	 * Binds the object type
	 *
	 * @param type
	 *            the class of the type to bind (must not be <code>null</code>)
	 * @return a new binding to bind the given type
	 */
	public final <T> BindingBuilder<T> bindType(final Class<T> type) {
		checkArgument(nonNull(type), "A type must be provided.");
		return new RuntimeContextObjectBinder.BindingBuilder<T>(type, this);
	}

	Map<Class<?>, Class<?>> getBindings() {
		return bindings.build();
	}

}