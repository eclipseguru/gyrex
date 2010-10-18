/*******************************************************************************
 * Copyright (c) 2008, 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.cds.documents;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.gyrex.model.common.IModelObject;
import org.eclipse.gyrex.model.common.contracts.IModifiableInMemory;
import org.eclipse.gyrex.model.common.contracts.IModificationAware;

/**
 * A document model object.
 * <p>
 * Documents are a core element of the content delivery story. They can
 * represent products for sale on a storefront or items for sale in an auction.
 * However, they are not limited to those two possibilities. Basically, a
 * document can by anything that you want to present in some way to somebody.
 * It's also possible that documents are digital goods which can be downloaded
 * or formatted articles/texts which can be viewed on-line.
 * </p>
 * <p>
 * Therefore, documents do not provide a fixed structure. They are unstructured
 * documents consisting of a bunch of simple name-value attributes. The
 * attributes describe a document further.
 * </p>
 * <p>
 * In order to provide a common infrastructure for working with documents a set
 * of base attributes is defined by this interface.
 * </p>
 * <p>
 * Documents can be navigated. In order to make the navigation as flexible as
 * possible it will be based on attributes as well. A set of common navigational
 * attributes is defined by this interface.
 * </p>
 * <p>
 * Documents are stored in a repository. The kind of repository is defined by
 * the actual model implementation. Typically, a document repository offers rich
 * query capabilities in order to implement full-text as well as faceted search.
 * </p>
 * <p>
 * By definition, documents do <strong>not</strong> implement multi-language
 * behavior. Instead, different languages should be represented by different
 * documents It may also be necessary to organize documents in different
 * languages into different repositories because a single repository may be
 * optimized for a single language only.
 * </p>
 * <p>
 * This interface must be implemented by contributors of a document model
 * implementation. As such it is considered part of a service provider API which
 * may evolve faster than the general API. Please get in touch with the
 * development team through the prefered channels listed on <a
 * href="http://www.eclipse.org/gyrex">the Gyrex website</a> to stay up-to-date
 * of possible changes.
 * </p>
 * <p>
 * Clients may not implement or extend this interface directly. If
 * specialization is desired they should look at the options provided by the
 * model implementation.
 * </p>
 */
public interface IDocument extends IModelObject, IModifiableInMemory, IModificationAware {

	/**
	 * id of {@link #getId() the id attribute} (value <code>"id"</code>, type
	 * {@link String})
	 */
	String ATTRIBUTE_ID = "id";

	/**
	 * id of {@link #getName() the name attribute} (value <code>"name"</code>,
	 * type {@link String})
	 */
	String ATTRIBUTE_NAME = "name";

	/**
	 * id of {@link #getTitle() the title attribute} (value <code>"title"</code>
	 * , type {@link String})
	 */
	String ATTRIBUTE_TITLE = "title";

	/**
	 * id of {@link #getTitle() the summary attribute} (value
	 * <code>"summary"</code> , type {@link String})
	 */
	String ATTRIBUTE_SUMMARY = "summary";

	/**
	 * id of {@link #getDescription() the description attribute} (value
	 * <code>"description"</code>, type {@link String})
	 */
	String ATTRIBUTE_DESCRIPTION = "description";

	/**
	 * id of {@link #getUriPath() the URI path attribute} (value
	 * <code>"uripath"</code>, type {@link String})
	 */
	String ATTRIBUTE_URI_PATH = "uripath";

	/**
	 * id of {@link #getTags() the tags attribute} (value <code>"tags"</code>,
	 * type {@link List} of {@link String})
	 */
	String ATTRIBUTE_TAGS = "tags";

	/**
	 * id of {@link #getStart() the start attribute} <code>"start"</code>, type
	 * {@link Long})
	 */
	String ATTRIBUTE_START = "start";

	/**
	 * id of {@link #getEnd() the end attribute} <code>"end"</code>, type
	 * {@link Long})
	 */
	String ATTRIBUTE_END = "end";

	/**
	 * Indicates if an attribute is set on the document.
	 * <p>
	 * An attribute is considered set if a call to {@link #get(String)} will not
	 * return <code>null</code>, even if the attribute contains no values.
	 * </p>
	 * 
	 * @param attributeId
	 * @return <code>true</code> if an attribute of the specified id is set,
	 *         <code>false</code> otherwise
	 */
	boolean contains(String attributeId);

	/**
	 * Returns the attribute with the specified id.
	 * 
	 * @param attributeId
	 *            the attribute id (may not be <code>null</code>)
	 * @return the attribute (maybe <code>null</code> if an attribute with that
	 *         id is not defined)
	 */
	IDocumentAttribute<?> get(String attributeId);

	/**
	 * Returns a map of all attributes set in the document.
	 * 
	 * @return an unmodifiable map of all document attributes
	 */
	Map<String, IDocumentAttribute> getAttributes();

	/**
	 * Returns a human-readable description.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_DESCRIPTION} as
	 * the attribute id.
	 * </p>
	 * 
	 * @return a human-readable description (maybe <code>null</code> if not set)
	 */
	String getDescription();

	/**
	 * Returns the milliseconds from the Java epoch of
	 * <code>1970-01-01T00:00:00Z</code> when the document should be hidden.
	 * <p>
	 * This allows to control the visibility of documents.
	 * </p>
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_PATHS} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return the milliseconds from the Java epoch of
	 *         <code>1970-01-01T00:00:00Z</code> when the document should be
	 *         hidden, or <code>0</code> if the documents visibility end time is
	 *         not limited (or is not set)
	 */
	long getEnd();

	/**
	 * Returns a unique identifier of a document. It's purpose is to locate a
	 * single specific document if necessary.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_ID} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return a unique identifier of the document (maybe <code>null</code> if
	 *         not set)
	 */
	String getId();

	/**
	 * Returns a human-readable name of a document which is typically an
	 * identifier that is unique and makes sense in a specific context (eg. a
	 * product/sku number).
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_NAME} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return a human-readable name (maybe <code>null</code> if not set)
	 */
	String getName();

	/**
	 * Returns the attribute with the specified id creating one if necessary.
	 * <p>
	 * In contrast to {@link #get(String)} this method will create and return
	 * new, transient attribute if an attribute with the specified id is not
	 * currently set.
	 * </p>
	 * <p>
	 * If a new transient attribute was created it will be added to the document
	 * and returned by {@link #getAttributes()} after this method returns.
	 * </p>
	 * 
	 * @param attributeId
	 *            the attribute id (may not be <code>null</code>)
	 * @return the attribute
	 */
	IDocumentAttribute<?> getOrCreate(String attributeId);

	/**
	 * Returns the milliseconds from the Java epoch of
	 * <code>1970-01-01T00:00:00Z</code> when the document should be visible.
	 * <p>
	 * This allows to control the visibility of documents.
	 * </p>
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_DESCRIPTION} as
	 * the attribute id.
	 * </p>
	 * 
	 * @return the milliseconds from the Java epoch of
	 *         <code>1970-01-01T00:00:00Z</code> when the document should be
	 *         visible, or <code>0</code> if the documents visibility start time
	 *         is not limited (or is not set)
	 */
	long getStart();

	/**
	 * Returns a human-readable summary.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_SUMMARY} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return a human-readable summary (maybe <code>null</code> if not set)
	 */
	String getSummary();

	/**
	 * Returns all tags (aka. labels) attached to a document.
	 * <p>
	 * This allows to navigate documents through a tag cloud or to filter based
	 * on tags.
	 * </p>
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_TAGS} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return a modifiable collection of all tags attached to a document
	 */
	Collection<String> getTags();

	/**
	 * Returns a human-readable title.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_TITLE} as the
	 * attribute id.
	 * </p>
	 * 
	 * @return a human-readable title (maybe <code>null</code> if not set)
	 */
	String getTitle();

	/**
	 * Returns a canonical URI pathname to the document.
	 * <p>
	 * The URI path is useful for building search engine friendly site URLs. The
	 * URI does not need to be unique across the board but should be unique
	 * within the same lookup context (eg. unique across all products of an
	 * online shop). Thus, it must be interpreted relative to the context base.
	 * </p>
	 * <p>
	 * The returned URI path is absolute, will start with a slash and is
	 * guaranteed to not contain relative segments such as <code>"."</code> and
	 * <code>".."</code>.
	 * </p>
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_URI_PATH} as
	 * the attribute id.
	 * </p>
	 * 
	 * @return a canonical URI pathname (maybe <code>null</code> if not set)
	 */
	String getUriPath();

	/**
	 * Returns {@link IDocumentAttribute#getValue() the attribute value} of the
	 * attribute with the specified id.
	 * <p>
	 * This is a convenience method which gets the attribute using
	 * {@link #get(String)} and if an attribute exists just calls
	 * {@link IDocumentAttribute#getValue()} and returns that value.
	 * </p>
	 * 
	 * @param attributeId
	 *            the attribute id (may not be <code>null</code>)
	 * @return the value (maybe <code>null</code> if an attribute with that id
	 *         is not defined or the value is <code>null</code>)
	 * @see IDocumentAttribute#getValue()
	 */
	Object getValue(String attributeId);

	/**
	 * Removes the attribute with the specified id from the document
	 * 
	 * @param attributeId
	 *            the attribute id (may not be <code>null</code>)
	 * @return the attribute that has been removed (maybe <code>null</code> if
	 *         an attribute with that id was not defined)
	 */
	IDocumentAttribute<?> remove(String attributeId);

	/**
	 * Sets the document description.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_DESCRIPTION} as
	 * the attribute id.
	 * </p>
	 * 
	 * @param description
	 *            the description to set (maybe <code>null</code> to unset)
	 */
	void setDescription(String description);

	/**
	 * Sets the end visibility.
	 * 
	 * @param end
	 *            the end time to set
	 * @see #getEnd()
	 */
	void setEnd(long end);

	/**
	 * Sets the document unique identifier.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_ID} as the
	 * attribute id.
	 * </p>
	 * 
	 * @param id
	 *            the id to set (maybe <code>null</code> to unset)
	 */
	void setId(String id);

	/**
	 * Sets the document name
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_NAME} as the
	 * attribute id.
	 * </p>
	 * 
	 * @param name
	 *            the name to set (maybe <code>null</code> to unset)
	 * @param value
	 */
	void setName(String name);

	/**
	 * Sets the start visibility.
	 * 
	 * @param start
	 *            the start time to set
	 * @see #getStart()
	 */
	void setStart(long start);

	/**
	 * Sets the document summary.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_SUMMARY} as the
	 * attribute id.
	 * </p>
	 * 
	 * @param summary
	 *            the summary to set (maybe <code>null</code> to unset)
	 */
	void setSummary(String summary);

	/**
	 * Sets the document title.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_TITLE} as the
	 * attribute id.
	 * </p>
	 * 
	 * @param title
	 *            the title to set (maybe <code>null</code> to unset)
	 */
	void setTitle(String title);

	/**
	 * Sets the document URI path.
	 * <p>
	 * This is a convenience method which uses {@link #ATTRIBUTE_URI_PATH} as
	 * the attribute id.
	 * </p>
	 * 
	 * @param uriPath
	 *            the URI path to set (maybe <code>null</code> to unset)
	 */
	void setUriPath(String uriPath);

}
