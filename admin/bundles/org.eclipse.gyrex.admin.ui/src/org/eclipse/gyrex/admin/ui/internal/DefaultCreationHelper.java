/*******************************************************************************
 * Copyright (c) 2010 AGETO Service GmbH and others.
 * All rights reserved.
 *  
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *  
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal;

import org.eclipse.gyrex.admin.ui.configuration.ICreationHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * A default implementation of {@link ICreationHelper}
 */
public class DefaultCreationHelper implements ICreationHelper {

	FormToolkit toolkit;
	
	public DefaultCreationHelper(FormToolkit toolkit) {
		this.toolkit = toolkit;
	}

	
	public static ICreationHelper create(FormToolkit toolkit){
		return new DefaultCreationHelper(toolkit);
	}
	
	@Override
	public Composite createSection(Composite parent, String title,
			String description, int style, ScrolledForm form) {
		return createSection(parent, title, description, style, form, 1);
	}
	
	public Composite createSection(Composite parent, String title, String description, int style, final ScrolledForm form, int numColumns) {
		if (style == -1) {
			style = Section.TITLE_BAR | Section.EXPANDED | Section.TWISTIE;
		}
		Section section = toolkit.createSection(parent, style | (description != null ? Section.DESCRIPTION : 0));
		section.setText(title);
		section.setDescription(description);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setLayoutData(createGridData(parent));

		Composite panel = toolkit.createComposite(section);
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = numColumns;
		layout.makeColumnsEqualWidth = false;
		panel.setLayout(layout);
		section.setClient(panel);

		return panel;
	}

	protected GridData createGridData(Composite parent) {
		Object layout = parent.getLayout();
		int hSpan = 1;
		if (layout != null && (layout instanceof GridLayout)) {
			hSpan = ((GridLayout) layout).numColumns;
		}
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = hSpan;
		return data;
	}

	protected TableWrapData createTableWrapData(Composite parent) {
		Object layout = parent.getLayout();
		int hSpan = 1;
		if (layout != null && (layout instanceof GridLayout)) {
			hSpan = ((GridLayout) layout).numColumns;
		}
		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, hSpan);
		return data;
	}

	protected TableWrapData createTableWrapData(Composite parent, boolean grab) {
		Object layout = parent.getLayout();
		int hSpan = 1;
		if (layout != null && (layout instanceof GridLayout)) {
			hSpan = ((GridLayout) layout).numColumns;
		}
		TableWrapData data = new TableWrapData(grab ? TableWrapData.FILL_GRAB : TableWrapData.FILL,
				TableWrapData.TOP, 1, hSpan);
		return data;
	}

	public Button createButton(Composite parent, String text, int style) {
		return toolkit.createButton(parent, text, style);
	}

	public CCombo createCombo(Composite parent, String[] options) {
		CCombo combo = new CCombo(parent, SWT.READ_ONLY | SWT.FLAT | SWT.BORDER);

		TableWrapData data = createTableWrapData(parent);
		data.maxWidth = 250;
		combo.setLayoutData(data);
		combo.setItems(options);

		toolkit.adapt(combo, true, true);

		return combo;
	}

	public Label createDefaultLabel(Composite parent, String text) {
		Label label = toolkit.createLabel(parent, text, SWT.LEAD | SWT.WRAP);
		label.setLayoutData(createTableWrapData(parent));
		return label;
	}

	public Text createMultilineText(Composite parent, String text) {
		Text t = toolkit.createText(parent, text, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		TableWrapData d = createTableWrapData(parent);
		d.heightHint = t.getLineHeight() * 3 + 10;
		d.maxWidth = 250;
		// GridData data = createGridData(parent);
		// data.heightHint = 50;
		t.setLayoutData(d);
		return t;
	}

	public Text createSinglelineText(Composite parent, String text) {
		Text t = toolkit.createText(parent, text);

		TableWrapData data = createTableWrapData(parent);
		data.maxWidth = 250;

		t.setLayoutData(data);

		return t;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchronity.dnx.ui.guifactory.editors.CreationHelper#createCombo(org.eclipse.swt.widgets.Composite,
	 *      java.lang.String[], int)
	 */
	public CCombo createCombo(Composite parent, String[] options, int style) {
		CCombo combo = new CCombo(parent, style);

		TableWrapData data = createTableWrapData(parent);
		data.maxWidth = 250;
		combo.setLayoutData(data);
		// combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setItems(options);

		toolkit.adapt(combo, true, true);

		return combo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.synchronity.dnx.ui.guifactory.editors.CreationHelper#createComposite(org.eclipse.swt.widgets.Composite)
	 */
	public Composite createComposite(Composite parent) {
		Composite p = toolkit.createComposite(parent);
		p.setLayoutData(createTableWrapData(parent));
		return p;
	}
}
