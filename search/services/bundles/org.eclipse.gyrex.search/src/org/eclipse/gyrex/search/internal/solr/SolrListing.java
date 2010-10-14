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
package org.eclipse.gyrex.cds.solr.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

import org.apache.solr.common.SolrDocument;

/**
 *
 */
public class SolrListing extends PlatformObject implements IDocument {

	private final SolrDocument document;
	private Map<String, IDocumentAttribute> attributes;

	/**
	 * Creates a new instance.
	 * 
	 * @param next
	 */
	public SolrListing(final SolrDocument document) {
		this.document = document;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getAttribute(java.lang.String)
	 */
	@Override
	public IDocumentAttribute get(final String name) {
		if (name.equals(IDocument.ATTRIBUTE_ID) || name.equals(IDocument.ATTRIBUTE_NAME) || name.equals(IDocument.ATTRIBUTE_TITLE) || name.equals(IDocument.ATTRIBUTE_DESCRIPTION) || name.equals(IDocument.ATTRIBUTE_URI_PATH)) {
			return null;
		}
		final Collection<Object> values = document.getFieldValues(name);
		if (null == values) {
			return null;
		}

		return new SolrListingAttribute(name, values);
	}

	@Override
	public Map<String, IDocumentAttribute> getAttributes() {
		if (null != attributes) {
			return Collections.unmodifiableMap(attributes);
		}

		final Map<String, IDocumentAttribute> attributes = new HashMap<String, IDocumentAttribute>();
		final Collection<String> fieldNames = document.getFieldNames();
		for (final String name : fieldNames) {
			if (name.equals(IDocument.ATTRIBUTE_ID) || name.equals(IDocument.ATTRIBUTE_NAME) || name.equals(IDocument.ATTRIBUTE_TITLE) || name.equals(IDocument.ATTRIBUTE_DESCRIPTION) || name.equals(IDocument.ATTRIBUTE_URI_PATH)) {
				continue;
			}
			final Collection<Object> values = document.getFieldValues(name);
			attributes.put(name, new SolrListingAttribute(name, values));
		}
		this.attributes = attributes;
		return Collections.unmodifiableMap(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getDescription()
	 */
	@Override
	public String getDescription() {
		return (String) document.getFirstValue(IDocument.ATTRIBUTE_DESCRIPTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getEnd()
	 */
	@Override
	public long getEnd() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getId()
	 */
	@Override
	public String getId() {
		return (String) document.getFirstValue(IDocument.ATTRIBUTE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.model.common.contracts.IModificationAware#getLastModified()
	 */
	@Override
	public long getLastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getName()
	 */
	@Override
	public String getName() {
		return (String) document.getFirstValue(IDocument.ATTRIBUTE_NAME);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getPaths()
	 */
	@Override
	public IPath[] getPaths() {
		return new IPath[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getStart()
	 */
	@Override
	public long getStart() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getTags()
	 */
	@Override
	public Collection<String> getTags() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getTitle()
	 */
	@Override
	public String getTitle() {
		return (String) document.getFirstValue(IDocument.ATTRIBUTE_TITLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getUriPath()
	 */
	@Override
	public String getUriPath() {
		return (String) document.getFirstValue(IDocument.ATTRIBUTE_URI_PATH);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.model.common.contracts.IModifiableInMemory#isDirty()
	 */
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.model.common.contracts.IModifiableInMemory#isTransient()
	 */
	@Override
	public boolean isTransient() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SolrListing{ " + document.toString().replace('\n', '|').replace('\r', ' ') + " }";
	}
}
