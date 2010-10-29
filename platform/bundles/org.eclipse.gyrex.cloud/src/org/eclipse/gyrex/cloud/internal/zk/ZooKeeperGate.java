/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cloud.internal.zk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.cloud.internal.CloudDebug;

import org.eclipse.core.runtime.IPath;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central class within Gyrex to be used for all ZooKeeper related
 * communication.
 * <p>
 * Currently, this class capsulates the connection management. However, it might
 * be better to have a more de-coupled model and use events (connected --> gate
 * up; disconnected --> gate down).
 * </p>
 * <p>
 * On the other hand, each caller has to deal with gate up/down situations. It
 * might likely be necessary to allow registering of arbitrary gate-up/connected
 * listeners and maybe clean-up/disconnected listeners.
 * </p>
 */
public class ZooKeeperGate {

	public static interface GateListener {
		void gateDown();
	}

	private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperGate.class);
	private static final AtomicReference<ZooKeeperGate> instanceRef = new AtomicReference<ZooKeeperGate>();

	/**
	 * Returns the current active gate.
	 * 
	 * @return the active gate
	 * @throws IllegalStateException
	 *             if the gate is DOWN
	 */
	public static ZooKeeperGate get() throws IllegalStateException {
		final ZooKeeperGate gate = instanceRef.get();
		if (gate == null) {
			throw new IllegalStateException("ZooKeeper Gate is DOWN.");
		}
		return gate;
	}

	static ZooKeeperGate getAndSet(final ZooKeeperGate gate) {
		return instanceRef.getAndSet(gate);
	}

	private final GateListener listener;
	private final ZooKeeper zooKeeper;

	private final Watcher connectionMonitor = new Watcher() {
		@Override
		public void process(final WatchedEvent event) {
			if (CloudDebug.zooKeeperGateLifecycle) {
				LOG.debug("Connection event: {}", event);
			}

			if (event.getState() == KeeperState.SyncConnected) {
				LOG.info("ZooKeeper Gate is now UP. Connection to cloud established.");
			} else {
				LOG.info("ZooKeeper Gate is now DOWN. Connection to cloud lost ({}).", event.getState());
			}
		}
	};

	ZooKeeperGate(final ZooKeeperGateConfig config, final GateListener listener) throws IOException {
		zooKeeper = new ZooKeeper(config.getConnectString(), config.getSessionTimeout(), connectionMonitor);
		this.listener = listener;
	}

	private void create(final IPath path, final CreateMode createMode, final byte[] data) throws InterruptedException, KeeperException, IOException {
		if (path == null) {
			throw new IllegalArgumentException("path must not be null");
		}
		if (createMode == null) {
			throw new IllegalArgumentException("createMode must not be null");
		}

		// create all parents
		for (int i = path.segmentCount() - 1; i > 0; i--) {
			final IPath parentPath = path.removeLastSegments(i);
			try {
				ensureConnected().create(parentPath.toString(), null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (final KeeperException e) {
				if (e.code() != KeeperException.Code.NODEEXISTS) {
					// rethrow
					throw e;
				}
			}
		}

		// create node itself
		ensureConnected().create(path.toString(), data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
	}

	/**
	 * Creates a path in ZooKeeper
	 * <p>
	 * If the path parents don't exist they will be created using
	 * {@link CreateMode#PERSISTENT}.
	 * </p>
	 * 
	 * @param path
	 *            the path to create
	 * @param createMode
	 *            the creation mode
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void createPath(final IPath path, final CreateMode createMode) throws KeeperException, InterruptedException, IOException {
		create(path, createMode, null);
	}

	/**
	 * Creates a record at the specified path in ZooKeeper.
	 * <p>
	 * If the path parents don't exist they will be created using
	 * {@link CreateMode#PERSISTENT}.
	 * </p>
	 * 
	 * @param path
	 *            the path to create
	 * @param createMode
	 *            the creation mode
	 * @param recordData
	 *            the record data
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void createRecord(final IPath path, final CreateMode createMode, final String recordData) throws KeeperException, InterruptedException, IOException {
		if (recordData == null) {
			throw new IllegalArgumentException("recordData must not be null");
		}
		try {
			create(path, createMode, recordData.getBytes(CharEncoding.UTF_8));
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException("JVM does not support UTF-8.", e);
		}

	}

	/**
	 * Removes a path in ZooKeeper.
	 * <p>
	 * If the path parents don't exist they will be created using
	 * {@link CreateMode#PERSISTENT}.
	 * </p>
	 * 
	 * @param path
	 *            the path to create
	 * @param createMode
	 *            the creation mode
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void deletePath(final IPath path) throws KeeperException, InterruptedException, IOException {
		if (path == null) {
			throw new IllegalArgumentException("path must not be null");
		}

		// delete all children
		final List<String> children = ensureConnected().getChildren(path.toString(), false);
		for (final String child : children) {
			deletePath(path.append(child));
		}

		// delete node itself
		ensureConnected().delete(path.toString(), -1);
	}

	public void dumpTree(final String path, final int indent, final StrBuilder string) throws Exception {
		final byte[] data = ensureConnected().getData(path, false, null);
		final List<String> children = ensureConnected().getChildren(path, false);
		final StringBuilder spaces = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			spaces.append(" ");
		}
		string.append(spaces).append(path).append(" (").append(children.size()).appendln(")");
		if (data != null) {
			String dataString = new String(data, CharEncoding.UTF_8);
			dataString = CharSetUtils.delete(dataString, "" + CharUtils.CR);
			dataString = StringUtils.replace(dataString, "" + CharUtils.LF, SystemUtils.LINE_SEPARATOR + spaces + "  ");
			string.append(spaces).append("D:").appendln(dataString);
		}

		for (final String child : children) {
			dumpTree(path + (path.equals("/") ? "" : "/") + child, indent + 1, string);
		}

	}

	final ZooKeeper ensureConnected() {
		if (!zooKeeper.getState().isAlive()) {
			throw new IllegalStateException("ZooKeeper Gate is DOWN.");
		}
		return zooKeeper;
	}

	/**
	 * Reads a record from the specified path in ZooKeeper if it exists.
	 * 
	 * @param path
	 *            the path to the record
	 * @return the record data (maybe <code>null</code> if it doesn't exist)
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public byte[] readRecord(final IPath path) throws KeeperException, InterruptedException, IOException {
		if (path == null) {
			throw new IllegalArgumentException("path must not be null");
		}
		try {
			return ensureConnected().getData(path.toString(), false, null);
		} catch (final KeeperException e) {
			if (e.code() == KeeperException.Code.NONODE) {
				return null;
			}
			throw e;
		}
	}

	/**
	 * Reads a record from the specified path in ZooKeeper if it exists.
	 * 
	 * @param path
	 *            the path to the record
	 * @param defaultValue
	 *            a default value to return the record does not exist.
	 * @return the record data (maybe <code>null</code> if
	 *         <code>defaultValue</code> was <code>null</code>)
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String readRecord(final IPath append, final String defaultValue) throws KeeperException, InterruptedException, IOException {
		final byte[] data = readRecord(append);
		if (data == null) {
			return defaultValue;
		}
		try {
			return new String(data, CharEncoding.UTF_8);
		} catch (final UnsupportedEncodingException e) {
			throw new IllegalStateException("JVM does not support UTF-8.", e);
		}
	}

	/**
	 * Closes the gate.
	 */
	public void shutdown() {
		if (CloudDebug.zooKeeperGateLifecycle) {
			LOG.debug("Received stop signal for ZooKeeper Gate.");
		}

		try {
			zooKeeper.close();
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (final Exception e) {
			// ignored shutdown exceptions
			e.printStackTrace();
		}

		// notify listeners
		listener.gateDown();
	}
}
