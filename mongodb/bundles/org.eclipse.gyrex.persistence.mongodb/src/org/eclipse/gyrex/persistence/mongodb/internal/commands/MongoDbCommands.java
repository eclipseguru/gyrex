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

import org.eclipse.gyrex.common.console.BaseCommandProvider;
import org.eclipse.gyrex.persistence.mongodb.internal.MongoDbDebug;

import org.eclipse.osgi.framework.console.CommandInterpreter;

/**
 * All MongoDB commands
 */
public class MongoDbCommands extends BaseCommandProvider {

	/**
	 * Creates a new instance.
	 */
	public MongoDbCommands() {
		registerCommand(PoolCommands.class);
	}

	public void _mongo(final CommandInterpreter ci) {
		printStackTraces = MongoDbDebug.debug;
		execute(ci);
	}

	public void _mongodb(final CommandInterpreter ci) {
		_mongo(ci);
	}

	@Override
	protected String getCommandName() {
		return "mongo";
	}

}
