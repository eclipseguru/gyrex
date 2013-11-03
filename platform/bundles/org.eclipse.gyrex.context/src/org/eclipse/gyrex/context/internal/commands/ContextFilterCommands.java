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
package org.eclipse.gyrex.context.internal.commands;

import org.eclipse.gyrex.common.console.SubCommand;

/**
 * Sub command for managing context filters.
 */
public class ContextFilterCommands extends SubCommand {

	/**
	 * Creates a new instance.
	 * 
	 * @param name
	 */
	public ContextFilterCommands() {
		super("filter");

		registerCommand("ls", LsContextFilterCmd.class);

		registerCommand("set", SetContextFilterCmd.class);

//		registerCommand("rm", RemovePool.class);
//		registerCommand("remove", RemovePool.class);
	}

}
