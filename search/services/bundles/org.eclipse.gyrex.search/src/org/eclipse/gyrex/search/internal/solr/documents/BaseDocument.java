/*******************************************************************************
 * Copyright (c) 2010 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.Collection;

import org.eclipse.gyrex.cds.documents.IDocument;

import org.eclipse.core.runtime.PlatformObject;

/**
 * Base {@link IDocument} implementation
 */
public abstract class BaseDocument extends PlatformObject implements IDocument {

	@Override
	public String getDescription() {
		return (String) getValue(ATTRIBUTE_DESCRIPTION);
	}

	@Override
	public long getEnd() {
		final Long value = (Long) getValue(ATTRIBUTE_END);
		if ((value == null) || (value.longValue() < 0)) {
			return 0;
		}
		return value;
	}

	@Override
	public String getId() {
		return (String) getValue(ATTRIBUTE_ID);
	}

	@Override
	public long getLastModified() {
		return 0; // no last modify for transient doc
	}

	@Override
	public String getName() {
		return (String) getValue(ATTRIBUTE_NAME);
	}

	@Override
	public long getStart() {
		final Long value = (Long) getValue(ATTRIBUTE_START);
		if ((value == null) || (value.longValue() < 0)) {
			return 0;
		}
		return value;
	}

	@Override
	public String getSummary() {
		return (String) getValue(ATTRIBUTE_SUMMARY);
	}

	@Override
	public Collection<String> getTags() {
		return getOrCreate(ATTRIBUTE_TAGS).ofType(String.class).getValues();
	}

	@Override
	public String getTitle() {
		return (String) getValue(ATTRIBUTE_TITLE);
	}

	@Override
	public String getUriPath() {
		return (String) getValue(ATTRIBUTE_URI_PATH);
	}

	@Override
	public void setDescription(final String description) {
		setOrRemove(ATTRIBUTE_DESCRIPTION, description);
	}

	@Override
	public void setEnd(final long end) {
		if (end > 0) {
			getOrCreate(ATTRIBUTE_END).ofType(Long.class).set(end);
		} else {
			remove(ATTRIBUTE_END);
		}
	}

	@Override
	public void setId(final String id) {
		setOrRemove(ATTRIBUTE_ID, id);
	}

	@Override
	public void setName(final String name) {
		setOrRemove(ATTRIBUTE_NAME, name);
	}

	void setOrRemove(final String attributeId, final String value) {
		if (value != null) {
			getOrCreate(attributeId).ofType(String.class).set(value);
		} else {
			remove(attributeId);
		}
	}

	@Override
	public void setStart(final long start) {
		if (start > 0) {
			getOrCreate(ATTRIBUTE_START).ofType(Long.class).set(start);
		} else {
			remove(ATTRIBUTE_START);
		}
	}

	@Override
	public void setSummary(final String summary) {
		setOrRemove(ATTRIBUTE_SUMMARY, summary);
	}

	@Override
	public void setTitle(final String title) {
		setOrRemove(ATTRIBUTE_TITLE, title);
	}

	@Override
	public void setUriPath(final String uriPath) {
		setOrRemove(ATTRIBUTE_URI_PATH, uriPath);
	}

}
