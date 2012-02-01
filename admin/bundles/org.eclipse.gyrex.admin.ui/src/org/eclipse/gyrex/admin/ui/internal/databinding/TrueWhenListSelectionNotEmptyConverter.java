/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.databinding;

import org.eclipse.core.databinding.conversion.Converter;

import org.apache.commons.lang.StringUtils;

public class TrueWhenListSelectionNotEmptyConverter extends Converter {
	/**
	 * Creates a new instance.
	 */
	public TrueWhenListSelectionNotEmptyConverter() {
		super(String.class, Boolean.TYPE);
	}

	@Override
	public Object convert(final Object fromObject) {
		return (fromObject instanceof String) && (StringUtils.isNotEmpty((String) fromObject));
	}
}