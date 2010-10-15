/*******************************************************************************
 * Copyright (c) 2008, 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.internal;

import org.eclipse.gyrex.common.runtime.BaseBundleActivator;

public class ContentDeliveryActivator extends BaseBundleActivator {

	private static final String SYMBOLIC_NAME = "org.eclipse.gyrex.cds";

	/**
	 * Creates a new instance.
	 */
	public ContentDeliveryActivator() {
		super(SYMBOLIC_NAME);
	}
}
