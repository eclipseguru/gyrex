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

import org.eclipse.gyrex.eventbus.IEventDeserializer;
import org.eclipse.gyrex.eventbus.IEventSerializer;

import org.apache.commons.codec.binary.StringUtils;

public class StringDeSerializer implements IEventSerializer<String>, IEventDeserializer<String> {

	static final StringDeSerializer sharedInstance = new StringDeSerializer();

	@Override
	public String deserializeEvent(final byte[] bytes) throws IllegalArgumentException, IllegalStateException {
		return StringUtils.newStringUtf8(bytes);
	}

	@Override
	public byte[] serializeEvent(final String value) throws IllegalArgumentException, IllegalStateException {
		return StringUtils.getBytesUtf8(value);
	}

}
