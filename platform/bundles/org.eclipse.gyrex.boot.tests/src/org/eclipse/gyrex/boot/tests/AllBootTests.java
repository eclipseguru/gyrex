/*******************************************************************************
 * Copyright (c) 2014 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Konrad Schergaut - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.boot.tests;

import org.eclipse.gyrex.server.settings.SystemSettingTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SystemSettingTest.class })
public class AllBootTests {

}
