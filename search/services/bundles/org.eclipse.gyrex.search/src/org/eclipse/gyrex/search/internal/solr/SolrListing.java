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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.apache.solr.common.SolrDocument;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.PlatformObject;

import org.eclipse.gyrex.cds.documents.Document;
import org.eclipse.gyrex.cds.documents.IDocument;
import org.eclipse.gyrex.cds.documents.IDocumentAttribute;

/**
 * 
 */
public class SolrListing extends PlatformObject implements IDocument {

	private final SolrDocument document;
	private IDocumentAttribute[] attributes;

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
	public IDocumentAttribute getAttribute(final String name) {
		if (name.equals(Document.ID) || name.equals(Document.NAME) || name.equals(Document.TITLE) || name.equals(Document.DESCRIPTION) || name.equals(Document.URI_PATH)) {
			return null;
		}
		final Collection<Object> values = document.getFieldValues(name);
		if (null == values) {
			return null;
		}

		return new SolrListingAttribute(name, values);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getAttributes()
	 */
	@Override
	public IDocumentAttribute[] getAttributes() {
		if (null != attributes) {
			return attributes;
		}

		final List<IDocumentAttribute> attributes = new ArrayList<IDocumentAttribute>();
		final Collection<String> fieldNames = document.getFieldNames();
		for (final String name : fieldNames) {
			if (name.equals(Document.ID) || name.equals(Document.NAME) || name.equals(Document.TITLE) || name.equals(Document.DESCRIPTION) || name.equals(Document.URI_PATH)) {
				continue;
			}
			final Collection<Object> values = document.getFieldValues(name);
			attributes.add(new SolrListingAttribute(name, values));
		}
		return this.attributes = attributes.toArray(new IDocumentAttribute[attributes.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getDescription()
	 */
	@Override
	public String getDescription() {
		return (String) document.getFirstValue(Document.DESCRIPTION);
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
		return (String) document.getFirstValue(Document.ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getName()
	 */
	@Override
	public String getName() {
		return (String) document.getFirstValue(Document.NAME);
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
	public String[] getTags() {
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getTitle()
	 */
	@Override
	public String getTitle() {
		return (String) document.getFirstValue(Document.TITLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gyrex.cds.model.IListing#getUriPath()
	 */
	@Override
	public String getUriPath() {
		return (String) document.getFirstValue(Document.URI_PATH);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SolrListing{ " + document.toString().replace('\n', '|').replace('\r', ' ') + " }";
	}
}
