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
package org.eclipse.gyrex.persistence.mongodb.internal.commands;

import org.eclipse.gyrex.common.console.SubCommand;

/**
 * Sub command for managing MongoDB pools.
 */
public class PoolCommands extends SubCommand {

	/**
	 * Creates a new instance.
	 * 
	 * @param name
	 */
	public PoolCommands() {
		super("pool");

		registerCommand("ls", LsPool.class);

		registerCommand("create", ConfigurePool.class);

		registerCommand("rm", RemovePool.class);
		registerCommand("remove", RemovePool.class);
	}

}
