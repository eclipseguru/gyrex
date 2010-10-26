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
package org.eclipse.gyrex.cds.solr.internal.documents;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

/**
 * Transient {@link IDocument} implementation.
 */
public class TransientDocument extends BaseDocument {

	private final Map<String, IDocumentAttribute> attributes = new LinkedHashMap<String, IDocumentAttribute>();

	@Override
	public boolean contains(final String attributeId) {
		return attributes.containsKey(attributeId);
	}

	@Override
	public IDocumentAttribute<?> get(final String attributeId) {
		return attributes.get(attributeId);
	}

	@Override
	public Map<String, IDocumentAttribute> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	@Override
	public IDocumentAttribute<?> getOrCreate(final String attributeId) {
		IDocumentAttribute attribute = attributes.get(attributeId);
		if (attribute == null) {
			attribute = new BaseDocumentAttribute(attributeId);
			attributes.put(attributeId, attribute);
		}
		return attribute;
	}

	@Override
	public Object getValue(final String attributeId) {
		final IDocumentAttribute attribute = attributes.get(attributeId);
		if (attribute != null) {
			return attribute.getValue();
		}
		return null;
	}

	@Override
	public boolean isDirty() {
		return true; // always dirty
	}

	@Override
	public boolean isTransient() {
		return true; // always transient
	}

	@Override
	public IDocumentAttribute<?> remove(final String attributeId) {
		return attributes.remove(attributeId);
	}

	@Override
	public String toString() {
		return "TransientDocument{" + attributes.toString().replace('\n', '|').replace('\r', ' ') + "}";
	}
}
