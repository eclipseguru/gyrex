/*******************************************************************************
 * Copyright (c) 2010, 2011 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *  
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.configuration;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * An ICreationHelper should support developers in easier and faster
 * creating of common widgets for the forms ui.
 * <p>
 * The created widgets are ready layouted an easy to integrate.
 */
public interface ICreationHelper {

	/**
	 * Creates an wrapped label with the width of one column
	 * with a given text. The associated {@link LayoutData} is of type
	 * {@link TableWrapData}
	 * 
	 * @param parent
	 * @param text 
	 * @return the created label
	 */
	public Label createDefaultLabel(Composite parent, String text);
	
	/**
	 * Creates an input text field with the width of one column
	 * with an optional initial text. The associated {@link LayoutData} is of type
	 * {@link TableWrapData}
	 * 
	 * @param parent
	 * @param text 
	 * @return the created text
	 */
	public Text createSinglelineText(Composite parent, String text);
	
	/**
	 * Creates an multi line input text field with the width of one column
	 * with an optional initial text. The associated {@link LayoutData} is of type
	 * {@link TableWrapData}
	 * 
	 * @param parent
	 * @param text 
	 * @return the created text
	 */
	public Text createMultilineText(Composite parent, String text);
	
	/**
	 * Creates an read-only drop down list with the width of one column
	 * with an optional initial array of values. The associated {@link LayoutData} is of type
	 * {@link TableWrapData}
	 * 
	 * @param parent
	 * @param options 
	 * @return the created combo
	 */
	public CCombo createCombo(Composite parent, String[] options);
	
	/**
	 * Creates an drop down list with the width of one column
	 * with an optional initial array of values and a given style.
	 * The associated {@link LayoutData} is of type {@link TableWrapData}
	 * 
	 * @param parent
	 * @param options
	 * @param style 
	 * @return the created combo
	 */
	public CCombo createCombo(Composite parent, String[] options,int style);
	
	/**
	 * Creates a button with the width of one column
	 * with a given label and its style of type {@link SWT#PUSH}, {@link SWT#RADIO},
	 * {@link SWT#TOGGLE} or {@link SWT#CHECK}.
	 * The associated {@link LayoutData} is of type {@link TableWrapData}
	 * 
	 * @param parent
	 * @param text 
	 * @param style
	 * @return the created button
	 */
	public Button createButton(Composite parent, String text, int style);
	
	/**
	 * Creates an empty Composite with the width of one column.
	 * The associated {@link LayoutData} is of type {@link TableWrapData}
	 * 
	 * @param parent
	 * @return the created button
	 */
	public Composite createComposite(Composite parent);
	
	/**
	 * Creates an {@link Section} with the width of a specified number of columns,
	 * a specified title, an optional description and an optional style.
	 * 
	 * @param parent the parent composite
	 * @param title the sections title
	 * @param description optional
	 * @param style the style of the section, when the param is -1, an default style will be applied
	 * @param form the surrounding form
	 * @param numColumns 
	 * @return the body composite of the created section
	 */
	public Composite createSection(Composite parent, String title, String description, int style, final ScrolledForm form, int numColumns);

	/**
	 * Creates an {@link Section} with the width of one column,
	 * a specified title, an optional description and an optional style.
	 * 
	 * @param parent the parent composite
	 * @param title the sections title
	 * @param description optional
	 * @param style the style of the section, when the param is -1, an default style will be applied
	 * @param form the surrounding form
	 * @return the body composite of the created section
	 */
	public Composite createSection(Composite parent, String title, String description, int style, final ScrolledForm form);

}
