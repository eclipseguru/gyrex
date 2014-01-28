/*******************************************************************************
 * Copyright (c) 2012, 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.pages.overview;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.internal.pages.OverviewPageItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.exception.ExceptionUtils;

public class SystemStatus extends OverviewPageItem {

	@Override
	public Control createControl(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, true));
		composite.setLayoutData(AdminUiUtil.createHorzFillData());

		AdminUiUtil.createHeading(composite, "System Status", 1);

		final Label desc = new Label(composite, SWT.WRAP);
		desc.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
		desc.setLayoutData(AdminUiUtil.createHorzFillData());

		final IStatus status = AdminUiActivator.getInstance().getSystemStatus();
		if (!status.isOK()) {
			try {
				final StringWriter writer = new StringWriter();
				final IStatus[] children = status.getChildren();
				// print details for all children of the system status
				for (final IStatus child : children) {
					writeStatus(child, writer);
				}
				desc.setText(writer.toString());
			} catch (final IOException e) {
				desc.setText(ExceptionUtils.getRootCauseMessage(e));
			}
		} else {
			desc.setText("System is running.");
		}

		return composite;
	}

	private String getStatusImage(final IStatus status) {
		switch (status.getSeverity()) {
			case IStatus.CANCEL:
			case IStatus.ERROR:
				return "/static/error.gif";
			case IStatus.WARNING:
				return "/static/warning.gif";
			case IStatus.INFO:
			default:
				return "/static/information.gif";
		}
	}

	final void writeEscaped(final Writer writer, final Object object) throws IOException {
		if (object == null)
			return;

		final String string = object.toString();
		for (int i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);

			switch (c) {
				case '&':
					writer.write("&amp;");
					break;
				case '<':
					writer.write("&lt;");
					break;
				case '>':
					writer.write("&gt;");
					break;

				default:
					if (Character.isISOControl(c) && !Character.isWhitespace(c)) {
						writer.write('?');
					} else {
						writer.write(c);
					}
			}
		}
	}

	private void writeStatus(final IStatus status, final Writer writer) throws IOException, UnsupportedEncodingException {
		// ignore OK status
		if (status.isOK())
			return;

		/*
		 * sometimes we have a multi status with no message but only children;
		 * in this case we just print out all children
		 */
		final String statusMessage = status.getMessage();
		if (status.isMultiStatus() && ((statusMessage == null) || (statusMessage.trim().length() == 0))) {
			// write only children if a multi status has no message
			final IStatus[] children = status.getChildren();
			for (final IStatus child : children) {
				writeStatusItem(child, writer, 0);
			}
		} else {
			writeStatusItem(status, writer, 0);
		}
	}

	private void writeStatusItem(final IStatus status, final Writer writer, final int identSize) throws IOException {
		// ignore OK status
		if (status.isOK())
			return;

		// message
		writer.write("<img src=\"" + getStatusImage(status) + "\" height=\"16\" width=\"16\"/>  ");
		writeEscaped(writer, status.getMessage());
		writer.write(" <small><code>(");
		writeEscaped(writer, status.getPlugin());
		writer.write(", code ");
		writeEscaped(writer, String.valueOf(status.getCode()));
		writer.write(")</code></small>");

		if (status.isMultiStatus()) {
			writer.write("<br/>");
			final IStatus[] children = status.getChildren();
			for (final IStatus child : children) {
				writeStatusItem(child, writer, identSize + 4);
			}
		}
	}
}
