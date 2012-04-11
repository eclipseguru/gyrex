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
package org.eclipse.gyrex.admin.ui.internal.configuration;

import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.Saveable;

/**
 * {@link Saveable} for {@link AdminPage}
 */
public class ConfigurationPageSaveable extends Saveable {

	private final AdminPage page;
	private final ConfigurationPanelView view;

	public ConfigurationPageSaveable(final ConfigurationPanelView view, final AdminPage page) {
		this.view = view;
		this.page = page;

	}

	@Override
	public void doSave(final IProgressMonitor monitor) throws CoreException {
		view.setBusy(true);
		try {
			page.performSave(monitor);
		} finally {
			view.setBusy(false);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ConfigurationPageSaveable other = (ConfigurationPageSaveable) obj;
		if (page == null) {
			if (other.page != null) {
				return false;
			}
		} else if (!page.equals(other.page)) {
			return false;
		}
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		final Image image = page.getTitleImage();
		if (image == null) {
			return null;
		}
		return ImageDescriptor.createFromImage(image);
	}

	@Override
	public String getName() {
		return page.getTitle();
	}

	@Override
	public String getToolTipText() {
		return page.getTitleToolTip();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((page == null) ? 0 : page.hashCode());
		return result;
	}

	@Override
	public boolean isDirty() {
		return page.isDirty();
	}

}
