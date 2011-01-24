/**
 * Copyright (c) 2011 Gunnar Wagenknecht and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 */
package org.eclipse.gyrex.admin.ui.internal.configuration;

import org.eclipse.rwt.widgets.ExternalBrowser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class WelcomeSection extends SectionPart {

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 * @param page
	 */
	public WelcomeSection(final Composite parent, final GeneralPage page) {
		super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		createContent(getSection(), page.getManagedForm().getToolkit());
	}

	private void createContent(final Section section, final FormToolkit toolkit) {
		section.setText("Gyrex System Admin");
		section.setDescription("Welcome to Gyrex");

		final Composite client = toolkit.createComposite(section);
		section.setClient(client);
		client.setLayout(new TableWrapLayout());

		final FormText welcomeText = toolkit.createFormText(client, false);
		welcomeText.setText("<text><p>There is currently not much content here. The system is running and that's what we wanted to show you. After all, it's a showcase which demonstrates some concepts. If you like our vision, please follow our <a href=\"http://www.eclipse.org/gyrex/\" alt=\"Open the Gyrex blog.\">blog</a>.</p></text>", true, true);
		welcomeText.addHyperlinkListener(new HyperlinkAdapter() {

			@Override
			public void linkActivated(final HyperlinkEvent e) {
				final Object href = e.getHref();
				if ((href instanceof String) && (StringUtils.startsWith((String) href, "http://") || StringUtils.startsWith((String) href, "https://"))) {
					ExternalBrowser.open("_blank", (String) href, 0);
				}
			}
		});
	}
}
