/*******************************************************************************
 * Copyright (c) 2011, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.cloud.internal.zookeeper;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.gyrex.cloud.internal.zk.IZooKeeperLayout;
import org.eclipse.gyrex.cloud.internal.zk.ZooKeeperGate;

import org.eclipse.core.runtime.IPath;

import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.zookeeper.data.Stat;

/**
 *
 */
public class ZooKeeperData {

	private static final Object PROP_PATH = new Object();
	private static final Object PROP_VERSION = new Object();
	private static final Object PROP_CVERSION = new Object();
	private static final Object PROP_DATA_LENGTH = new Object();
	private static final Object PROP_EPHEMERAL_OWNER = new Object();
	private static final Object PROP_DATA = new Object();

	private final IPath path;
	private Stat stat;

	private Object[] children;
	private final ZooKeeperData parent;

	/**
	 * Creates a new instance.
	 * 
	 * @param append
	 */
	public ZooKeeperData(final IPath path, final ZooKeeperData parent) {
		this.path = path;
		this.parent = parent;
	}

	private String asString(final byte[] data) {
		if (null == data) {
			return StringUtils.EMPTY;
		}

		try {
			return new String(data, CharEncoding.UTF_8);
		} catch (final UnsupportedEncodingException e) {
			return ExceptionUtils.getRootCauseMessage(e);
		}
	}

	/**
	 * Returns the children.
	 * 
	 * @return the children
	 */
	public Object[] getChildren() {
		if (null == children) {
			load();
		}
		return children;
	}

	private Object getData() {
		if (null == stat) {
			load();
		}

		try {
			final byte[] data = ZooKeeperGate.get().readRecord(path, stat);

			if (null == data) {
				return null;
			}

			// read known paths as properties
			if (IZooKeeperLayout.PATH_PREFERENCES_ROOT.isPrefixOf(path)) {
				final Properties prop = new Properties();
				prop.load(new ByteArrayInputStream(data));
				return prop;
			}

			return WordUtils.wrap(asString(data), 80);
		} catch (final Exception e) {
			return ExceptionUtils.getRootCauseMessage(e);
		}

	}

	public String getLabel() {
		final Stat stat = getStat();
		if (stat.getEphemeralOwner() != 0) {
			return String.format("%s (ephemeral, v%d)", path.segmentCount() > 0 ? path.lastSegment() : "/", stat.getVersion());
		} else if (stat.getDataLength() > 0) {
			return String.format("%s (v%d, c%d, %d bytes)", path.segmentCount() > 0 ? path.lastSegment() : "/", stat.getVersion(), stat.getCversion(), stat.getDataLength());
		} else {
			return String.format("%s (v%d, c%d)", path.segmentCount() > 0 ? path.lastSegment() : "/", stat.getVersion(), stat.getCversion());
		}
	}

	/**
	 * Returns the parent.
	 * 
	 * @return the parent
	 */
	public ZooKeeperData getParent() {
		return parent;
	}

	public Object getPropertyValue(final Object id) {
		if (id == PROP_PATH) {
			return path;
		}
		if (id == PROP_VERSION) {
			return getStat().getVersion();
		}
		if (id == PROP_CVERSION) {
			return getStat().getCversion();
		}
		if (id == PROP_EPHEMERAL_OWNER) {
			return "0x" + Long.toHexString(getStat().getEphemeralOwner());
		}
		if (id == PROP_DATA_LENGTH) {
			return getStat().getDataLength();
		}
		if (id == PROP_DATA) {
			return getData();
		}
		return null;
	}

	public Stat getStat() {
		if (null == stat) {
			load();
		}
		return stat;
	}

	public boolean hasChildren() {
		return getChildren().length > 0;
	}

	private void load() {
		stat = new Stat();
		try {
			final Collection<String> names = ZooKeeperGate.get().readChildrenNames(path, stat);
			final List<ZooKeeperData> children = new ArrayList<ZooKeeperData>(names.size());
			for (final String name : names) {
				children.add(new ZooKeeperData(path.append(name), this));
			}
			this.children = children.toArray();
		} catch (final Exception e) {
			children = new String[] { ExceptionUtils.getRootCauseMessage(e) };
		}
	}

}
