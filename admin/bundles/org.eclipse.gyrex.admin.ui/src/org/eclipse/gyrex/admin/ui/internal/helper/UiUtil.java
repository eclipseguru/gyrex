/**
 * Copyright (c) 2011, 2012 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.helper;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class UiUtil {

	private static final Logger LOG = LoggerFactory.getLogger(UiUtil.class);

	/**
	 * these activate the content assist; alphanumeric, space plus some expected
	 * special chars
	 */
	private static final char[] VALUE_HELP_ACTIVATIONCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123457890*@ <>".toCharArray(); //$NON-NLS-1$

	/**
	 * Adds little bulb decoration to given control. Bulb will appear in top
	 * left corner of control after giving focus for this control. After
	 * clicking on bulb image text from <code>tooltip</code> will appear.
	 * 
	 * @param control
	 *            instance of {@link Control} object with should be decorated
	 * @param tooltip
	 *            text value which should appear after clicking on bulb image.
	 */
	public static void addBulbDecorator(final Control control, final String tooltip) {
		final ControlDecoration dec = new ControlDecoration(control, SWT.TOP | SWT.LEFT);

		dec.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage());

		dec.setShowOnlyOnFocus(true);
		dec.setShowHover(true);

		dec.setDescriptionText(tooltip);
	}

	/**
	 * Adds content proposal behavior to the specified text field using default
	 * keystroke (M1+SPACE) and a default tooltip.
	 * 
	 * @param textField
	 * @param processor
	 */
	public static ContentProposalAdapter addContentProposalBehavior(final Text textField, final IContentProposalProvider processor) {
		KeyStroke stroke;
		String tooltip;
		try {
			stroke = KeyStroke.getInstance("M1+SPACE"); //$NON-NLS-1$
			tooltip = NLS.bind("Press {0} or begin typing to see a filtered list of previously used values", stroke.format());
		} catch (final ParseException e1) {
			LOG.error("Error constructing key binding.", e1);
			stroke = null;
			tooltip = "Start typing to see a filtered list of previously used values";
		}
		return addContentProposalBehavior(textField, processor, stroke, tooltip);
	}

	public static ContentProposalAdapter addContentProposalBehavior(final Text textField, final IContentProposalProvider processor, final KeyStroke stroke, final String tooltip) {
		if (tooltip != null) {
			addBulbDecorator(textField, tooltip);
		}

		final ContentProposalAdapter adapter = new ContentProposalAdapter(textField, new TextContentAdapter(), processor, stroke, VALUE_HELP_ACTIVATIONCHARS);

		// set the acceptance style to always replace the complete content
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		return adapter;
	}

	/**
	 * @param id
	 *            see {@link FontRegistry#getBold(String)}
	 * @return the font
	 */
	public static Font getBoldFont(final String id) {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().getBold(id);
	}

	/**
	 * @param id
	 *            see {@link FontRegistry#get(String)}
	 * @return the font
	 */
	public static Font getFont(final String id) {
		return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(id);
	}
}
