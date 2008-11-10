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
package org.eclipse.cloudfree.listings.model.documents;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cloudfree.listings.model.IListingManager;

/**
 * A listing document can be fed to the {@link IListingManager} to generate
 * listings.
 */
public class Document {

	/** name of the description field (value <code>"description"</code>) */
	public static final String DESCRIPTION = "description";

	/** name of the title field (value <code>"title"</code>) */
	public static final String TITLE = "title";

	/** name of the name field (value <code>"name"</code>) */
	public static final String NAME = "name";

	/** name of the id field (value <code>"id"</code>) */
	public static final String ID = "id";

	/** name of the URI path field (value <code>"uripath"</code>) */
	public static final String URI_PATH = "uripath";

	/** name of the tags field (value <code>"tags"</code>) */
	public static final String TAGS = "tags";

	private final Map<String, Field<?>> fields = new LinkedHashMap<String, Field<?>>();

	/**
	 * Creates a new instance.
	 * 
	 * @param title
	 */
	public Document() {
		addField(new StringField(ID));
		addField(new StringField(NAME));
		addField(new StringField(TITLE));
		addField(new StringField(DESCRIPTION));
		addField(new StringField(URI_PATH));
		addField(new StringField(TAGS));
	}

	/**
	 * Adds a field to the document.
	 * <p>
	 * Note, if a field of the same name is already set an
	 * {@link IllegalArgumentException} will be thrown.
	 * </p>
	 * 
	 * @param field
	 * @throws IllegalArgumentException
	 *             if a field of the same name already exists
	 */
	public void addField(final Field<?> field) throws IllegalArgumentException {
		final String name = field.getName();
		if (fields.containsKey(name)) {
			throw new IllegalArgumentException("field '" + name + "' already added");
		}
		fields.put(name, field);
	}

	@SuppressWarnings("unchecked")
	private <T> T cast(final Field<?> field, final Class<T> type) {
		// use a direct cast for performance reasons
		return (T) field;
	}

	/**
	 * Returns the description which is the first value of the
	 * {@value #DESCRIPTION} field.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return getField(DESCRIPTION, StringField.class).getFirstValue();
	}

	public Field<?> getField(final String name) {
		return fields.get(name);
	}

	public <T extends Field> T getField(final String name, final Class<T> type) {
		final Field<?> field = getField(name);
		if (null == field) {
			return null;
		}
		if (type.isAssignableFrom(field.getClass())) {
			return cast(field, type);
		}
		throw new ClassCastException("field '" + name + "' is not of type " + type.getName() + " but of type " + field.getClass().getName());
	}

	/**
	 * Returns a list of all fields of the document.
	 * 
	 * @return the document fields
	 */
	public Collection<Field<?>> getFields() {
		return Collections.unmodifiableList(new ArrayList<Field<?>>(fields.values()));
	}

	/**
	 * Returns the id which is the first value of the {@value #ID} field.
	 * 
	 * @return the id
	 */
	public String getId() {
		return getField(ID, StringField.class).getFirstValue();
	}

	/**
	 * Returns the name which is the first value of the {@value #NAME} field.
	 * 
	 * @return the name
	 */
	public String getName() {
		return getField(NAME, StringField.class).getFirstValue();
	}

	/**
	 * Returns the document tags.
	 * <p>
	 * Internally, the tags will be stored as {@link StringField} with the field
	 * name {@value #TAGS}.
	 * </p>
	 * 
	 * @return an unmodifiable list of tags
	 */
	public List<String> getTags() {
		return getField(TAGS, StringField.class).getValues();
	}

	/**
	 * Returns the title which is the first value of the {@value #TITLE} field.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return getField(TITLE, StringField.class).getFirstValue();
	}

	/**
	 * Returns the URI path which is the first value of the {@value #URI_PATH}
	 * field.
	 * 
	 * @return the URI path
	 */
	public String getUriPath() {
		return getField(URI_PATH, StringField.class).getFirstValue();
	}

	public boolean hasField(final String name) {
		return fields.containsKey(name);
	}

	public Field<?> removeField(final String name) {
		// TODO: remove is unsafe because of id/name/title get/setters & co
		return fields.remove(name);
	}

	/**
	 * Sets the description.
	 * <p>
	 * Internally, the id will be stored as {@link StringField} with the field
	 * name {@value #DESCRIPTION}.
	 * </p>
	 * 
	 * @param description
	 */
	public Document setDescription(final String description) {
		getField(DESCRIPTION, StringField.class).setValue(description);
		return this;
	}

	/**
	 * Sets the id.
	 * <p>
	 * Internally, the id will be stored as {@link StringField} with the field
	 * name {@value #ID}.
	 * </p>
	 * 
	 * @param id
	 */
	public Document setId(final String id) {
		getField(ID, StringField.class).setValue(id);
		return this;
	}

	/**
	 * Sets the name.
	 * <p>
	 * Internally, the id will be stored as {@link StringField} with the field
	 * name {@value #NAME}.
	 * </p>
	 * 
	 * @param name
	 */
	public Document setName(final String name) {
		getField(NAME, StringField.class).setValue(name);
		return this;
	}

	/**
	 * Sets the document tags.
	 * <p>
	 * Internally, the tags will be stored as {@link StringField} with the field
	 * name {@value #TAGS}.
	 * </p>
	 * 
	 * @param description
	 */
	public Document setTags(final String... tags) {
		getField(TAGS, StringField.class).setValues(tags);
		return this;
	}

	/**
	 * Sets the title.
	 * <p>
	 * Internally, the id will be stored as {@link StringField} with the field
	 * name {@value #TITLE}.
	 * </p>
	 * 
	 * @param title
	 */
	public Document setTitle(final String title) {
		getField(TITLE, StringField.class).setValue(title);
		return this;
	}

	/**
	 * Sets the URI path.
	 * <p>
	 * Internally, the URI path will be stored as {@link StringField} with the
	 * field name {@value #URI_PATH}.
	 * </p>
	 * 
	 * @param description
	 */
	public Document setUriPath(final String path) {
		getField(URI_PATH, StringField.class).setValue(path);
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return MessageFormat.format("Document {0}", fields.values());
	}
}
