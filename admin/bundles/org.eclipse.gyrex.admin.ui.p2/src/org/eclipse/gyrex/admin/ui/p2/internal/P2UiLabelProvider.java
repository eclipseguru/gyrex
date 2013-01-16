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
 *     Peter Grube - update return value for RepositoryDefinition
 */
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.util.Locale;

import org.eclipse.gyrex.p2.internal.P2Activator;
import org.eclipse.gyrex.p2.internal.packages.IPackageManager;
import org.eclipse.gyrex.p2.internal.packages.InstallableUnitReference;
import org.eclipse.gyrex.p2.internal.packages.PackageDefinition;
import org.eclipse.gyrex.p2.internal.repositories.RepositoryDefinition;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IProcessingStepDescriptor;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class P2UiLabelProvider extends LabelProvider {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;
	private ResourceManager manager;

	@Override
	public void dispose() {
		if (null != manager) {
			manager.dispose();
			manager = null;
		}
		super.dispose();
	}

	private String getElementText(final IInstallableUnit iu) {
		final StringBuilder label = new StringBuilder();
		final String name = iu.getProperty(IInstallableUnit.PROP_NAME, Locale.getDefault().toString());
		if (StringUtils.isNotBlank(name)) {
			label.append(name).append(" - ");
		}
		label.append(iu.getId());
		return label.toString();
	}

	private String getElementText(final InstallableUnitReference unit) {
		final StringBuilder label = new StringBuilder();
		label.append(unit.getId());
		final Version version = unit.getVersion();
		if (version != null) {
			label.append(" (").append(version.toString()).append(")");
		}
		return label.toString();
	}

	private String getElementText(final IProfile profile) {
		final String name = profile.getProperty(IProfile.PROP_NAME);
		if (name != null && name.length() > 0) {
			return name;
		}
		return profile.getProfileId();
	}

	private String getElementText(final IRepository<?> repository) {
		final String name = repository.getName();
		if (name != null && name.length() > 0) {
			return name;
		}
		return URIUtil.toUnencodedString(repository.getLocation());
	}

	private String getElementText(final PackageDefinition pkg) {
		final StringBuilder label = new StringBuilder();
		label.append(pkg.getId());
		final IPackageManager packageManager = P2Activator.getInstance().getPackageManager();
		if (packageManager.isMarkedForInstall(pkg)) {
			label.append(" (provisioned)");
		}
		if (packageManager.isMarkedForUninstall(pkg)) {
			label.append(" (revoked)");
		}
		return label.toString();
	}

	private String getElementText(final RepositoryDefinition repo) {
		final StringBuilder label = new StringBuilder();
		label.append(repo.getId());
		label.append(" (");
		label.append(repo.getLocation());
		label.append(") ");
		return label.toString();
	}

	@Override
	public Image getImage(final Object element) {
		final ImageDescriptor descriptor = getImageDescriptor(element);
		if (descriptor == null) {
			if (element instanceof IArtifactKey) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
			return null;
		}
		return getResourceManager().createImage(descriptor);
	}

	private ImageDescriptor getImageDescriptor(final Object element) {
		if (element instanceof IProfile) {
			return P2UiImages.getImageDescriptor(P2UiImages.IMG_PROFILE);
		}
		if (element instanceof IInstallableUnit) {

			final IInstallableUnit iu = (IInstallableUnit) element;
			if (Boolean.valueOf(iu.getProperty(InstallableUnitDescription.PROP_TYPE_PATCH))) {
				return P2UiImages.getImageDescriptor(P2UiImages.IMG_PATCH_IU);
			} else if (Boolean.valueOf(iu.getProperty(InstallableUnitDescription.PROP_TYPE_CATEGORY))) {
				return P2UiImages.getImageDescriptor(P2UiImages.IMG_CATEGORY);
			} else {
				return P2UiImages.getImageDescriptor(P2UiImages.IMG_IU);
			}
		}
		if (element instanceof IArtifactRepository) {
			return P2UiImages.getImageDescriptor(P2UiImages.IMG_ARTIFACT_REPOSITORY);
		}
		if (element instanceof IMetadataRepository) {
			return P2UiImages.getImageDescriptor(P2UiImages.IMG_METADATA_REPOSITORY);
		}
		if (element instanceof IRequirement) {
			return P2UiImages.getImageDescriptor(P2UiImages.IMG_IU);
		}
		return null;
	}

	private ResourceManager getResourceManager() {
		if (null == manager) {
			manager = new LocalResourceManager(JFaceResources.getResources());
		}
		return manager;
	}

	@Override
	public String getText(final Object element) {
		if (element instanceof PackageDefinition) {
			return getElementText((PackageDefinition) element);
		}
		if (element instanceof RepositoryDefinition) {
			return getElementText((RepositoryDefinition) element);
		}
		if (element instanceof InstallableUnitReference) {
			return getElementText((InstallableUnitReference) element);
		}
		if (element instanceof IProfile) {
			return getElementText((IProfile) element);
		}
		if (element instanceof IInstallableUnit) {
			return getElementText((IInstallableUnit) element);
		}
		if (element instanceof IRepository) {
			return getElementText((IRepository) element);
		}
		if (element instanceof IArtifactKey) {
			final IArtifactKey key = (IArtifactKey) element;
			return key.getId() + " [" + key.getClassifier() + "]"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (element instanceof IProcessingStepDescriptor) {
			final IProcessingStepDescriptor descriptor = (IProcessingStepDescriptor) element;
			return descriptor.getProcessorId();
		}
		if (element instanceof Node) {
			final Node node = (Node) element;
			final StringBuilder label = new StringBuilder();
			label.append(node.getId());
//			if (StringUtils.isNotBlank(node.getLocation())) {
//				label.append(" (").append(node.getLocation()).append(")");
//			}
			return label.toString();
		}

		return super.getText(element);
	}
}
