/*******************************************************************************
 * Copyright (c) 2008 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.apache.solr.common.SolrDocument;

/**
 *
 */
public class StoredDocument extends BaseDocument {

	private final SolrDocument doc;
	private final Map<String, IDocumentAttribute> attributes = new HashMap<String, IDocumentAttribute>();
	private boolean dirty;
	private boolean initializedFully;

	/**
	 * Creates a new instance.
	 * 
	 * @param doc
	 */
	public StoredDocument(final SolrDocument doc) {
		this.doc = doc;
	}

	@Override
	public boolean contains(final String attributeId) {
		return doc.containsKey(attributeId);
	}

	private StoredDocumentAttribute createAttribute(final String attributeId, final Collection<Object> fieldValues) {
		if (fieldValues == null) {
			return null;
		}
		return new StoredDocumentAttribute<Object>(attributeId, fieldValues, this);
	}

	private void ensureInitialized(final String attributeId) {
		// lazy populate attributes map until full initialization is complete
		if (!initializedFully && !attributes.containsKey(attributeId)) {
			attributes.put(attributeId, createAttribute(attributeId, doc.getFieldValues(attributeId)));
		}
	}

	private void ensureInitializedFully() {
		if (!initializedFully) {
			for (final String key : doc.keySet()) {
				ensureInitialized(key);
			}
			initializedFully = true;
		}
	}

	@Override
	public IDocumentAttribute<?> get(final String attributeId) {
		ensureInitialized(attributeId);
		return attributes.get(attributeId);
	}

	@Override
	public Map<String, IDocumentAttribute> getAttributes() {
		ensureInitializedFully();
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public IDocumentAttribute<?> getOrCreate(final String attributeId) {
		ensureInitializedFully();
		IDocumentAttribute attribute = attributes.get(attributeId);
		if (attribute == null) {
			attribute = new BaseDocumentAttribute(attributeId);
			attributes.put(attributeId, attribute);
			setDirty(true);
		}
		return attribute;
	}

	@Override
	public Object getValue(final String attributeId) {
		ensureInitialized(attributeId);
		final IDocumentAttribute attribute = attributes.get(attributeId);
		if (attribute != null) {
			return attribute.getValue();
		}
		return null;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isTransient() {
		return false; // not transient
	}

	@Override
	public IDocumentAttribute<?> remove(final String attributeId) {
		ensureInitializedFully();
		// rely on full initialization flag which ensures that original doc values will be ignored after full init complete
		// thus, we only need to remove it from our attributes map
		final IDocumentAttribute removed = attributes.remove(attributeId);
		setDirty(removed != null);
		return removed;
	}

	void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}

	@Override
	public String toString() {
		ensureInitializedFully();
		return (dirty ? "StoredDocument[DIRTY]{" : "StoredDocument{") + attributes.toString().replace('\n', '|').replace('\r', ' ') + "}";
	}

}
