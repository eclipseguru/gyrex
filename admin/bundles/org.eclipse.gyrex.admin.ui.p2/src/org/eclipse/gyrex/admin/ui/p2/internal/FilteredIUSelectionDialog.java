/*******************************************************************************
 * Copyright (c) 2010, 2012 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   IBM - Ongoing development
 *   Gunnar Wagenknecht - adapted to Gyrex
 *   Peter Grube - rework new Admin UI
 ******************************************************************************/
package org.eclipse.gyrex.admin.ui.p2.internal;

import java.net.URI;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.gyrex.admin.ui.internal.widgets.FilteredItemsSelectionDialog;
import org.eclipse.gyrex.p2.internal.repositories.RepoUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteredIUSelectionDialog extends FilteredItemsSelectionDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	class IUItemsFilter extends ItemsFilter {

		boolean latest = false;

		public IUItemsFilter() {
			latest = fShowLatestVersionOnly;
		}

		@Override
		public boolean equalsFilter(final ItemsFilter obj) {
			if (latest != ((IUItemsFilter) obj).latest) {
				return false;
			}
			return super.equals(obj);
		}

		@Override
		public boolean isConsistentItem(final Object item) {
			return true;
		}

		public boolean isIUMatch(final IInstallableUnit iu) {
			if (iu.getFragments() != null && iu.getFragments().size() > 0) {
				return false;
			}

			final String id = iu.getId();

			// exclude source IUs
			if (StringUtils.endsWith(id, ".source") || StringUtils.endsWith(id, ".source.feature.group")) {
				return false;
			}

			// get name
			String name = iu.getProperty(IInstallableUnit.PROP_NAME, null);
			if (name == null || name.startsWith("%")) {
				name = ""; //$NON-NLS-1$
			}

			// match id or name
			if (patternMatcher.matches(id) || patternMatcher.matches(name)) {
				return true;
			}

			return false;
		}

		@Override
		public boolean isSubFilter(final ItemsFilter filter) {
			if (latest != ((IUItemsFilter) filter).latest) {
				return false;
			}
			return super.isSubFilter(filter);
		}

		@Override
		public boolean matchItem(final Object item) {
			if (item instanceof IInstallableUnit) {
				return isIUMatch((IInstallableUnit) item);
			}

			return false;
		}
	}

	private static final class SynchReposJob extends Job {

		private SynchReposJob() {
			super("Synchronizing Repositories");
			setSystem(false);
			setPriority(LONG);
		}

		@Override
		protected IStatus run(final IProgressMonitor progressMonitor) {
			IProvisioningAgent agent = null;
			try {
				// get agent
				agent = P2UiActivator.getInstance().getService(IProvisioningAgentProvider.class).createAgent(null);
				if (agent == null) {
					LOG.warn("The current system has not been provisioned using p2. Unable to acquire provisioning agent.");
					return Status.CANCEL_STATUS;
				}

				final IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
				if (manager == null) {
					LOG.warn("The provision system is broken. Unable to acquire metadata repository service.");
					return Status.CANCEL_STATUS;
				}

				// sync repos
				RepoUtil.configureRepositories(manager, (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME));

				// load repos
				final URI[] knownRepositories = manager.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
				final SubMonitor monitor = SubMonitor.convert(progressMonitor, knownRepositories.length);
				for (final URI uri : knownRepositories) {
					if (P2UiDebug.debug) {
						LOG.debug("Loading repository {}...", uri);
					}
					manager.loadRepository(uri, monitor.newChild(1));
				}
			} catch (final ProvisionException e) {
				LOG.warn("Error synchronizing repositories. {}", e.getMessage());
				return Status.CANCEL_STATUS;
			} catch (final OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} finally {
				if (null != agent) {
					agent.stop();
				}
			}

			return Status.OK_STATUS;
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(FilteredIUSelectionDialog.class);

	private Button fShowLatestVersionOnlyButton;
	private boolean fShowLatestVersionOnly = true;

//	private static final String S_PLUGINS = "showPlugins"; //$NON-NLS-1$
//	private static final String S_FEATURES = "showFeatures"; //$NON-NLS-1$
//	private static final String S_PACKAGES = "showPackages"; //$NON-NLS-1$
//
//	private static final int TYPE_PLUGIN = 0;
//	private static final int TYPE_FEATURE = 1;
//	private static final int TYPE_PACKAGE = 2;

	private final IQuery<IInstallableUnit> query;

	private final ILabelProvider fLabelProvider = new P2UiLabelProvider();

	public FilteredIUSelectionDialog(final Shell shell, final IQuery<IInstallableUnit> query) {
		super(shell, true);
		this.query = query;
		setTitle("Add Artifact to Software Package");
		setMessage("&Select an artifact to add to your software package (? = any character, * = any string):");
		setListLabelProvider(fLabelProvider);
		setDetailsLabelProvider(fLabelProvider);

		new SynchReposJob().schedule();
	}

	@Override
	protected Control createExtendedContentArea(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		fShowLatestVersionOnlyButton = new Button(composite, SWT.CHECK);
		fShowLatestVersionOnlyButton.setSelection(true);
		fShowLatestVersionOnlyButton.setText("Show latest version only");
		fShowLatestVersionOnlyButton.addSelectionListener(new SelectionAdapter() {

			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				fShowLatestVersionOnly = fShowLatestVersionOnlyButton.getSelection();
				applyFilter();
			}
		});
		return composite;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new IUItemsFilter();
	}

	@Override
	protected void fillContentProvider(final AbstractContentProvider contentProvider, final ItemsFilter itemsFilter, final IProgressMonitor progressMonitor) throws CoreException {
		// get agent
		final IProvisioningAgent agent = P2UiActivator.getInstance().getService(IProvisioningAgentProvider.class).createAgent(null);
		if (agent == null) {
			final String message = "The current system has not been provisioned using p2. Unable to acquire provisioning agent.";
			throw new CoreException(new Status(IStatus.ERROR, P2UiActivator.SYMBOLIC_NAME, message));
		}

		try {

			final IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
			if (manager == null) {
				final String message = "The provision system is broken. Unable to acquire metadata repository service.";
				throw new CoreException(new Status(IStatus.ERROR, P2UiActivator.SYMBOLIC_NAME, message));
			}

			// create query
			IQuery<IInstallableUnit> pipedQuery;
			if (fShowLatestVersionOnly) {
				pipedQuery = QueryUtil.createPipeQuery(query, QueryUtil.createLatestIUQuery());
			} else {
				pipedQuery = query;
			}

			// execute
			if (P2UiDebug.debug) {
				LOG.debug("Performing search...");
			}
			final Iterator iter = manager.query(pipedQuery, progressMonitor).iterator();
			while (iter.hasNext()) {
				final IInstallableUnit iu = (IInstallableUnit) iter.next();
				if (P2UiDebug.debug) {
					LOG.debug("Found IU: {}", iu);
				}
				contentProvider.add(iu, itemsFilter);
			}
		} finally {
			if (agent != null) {
				agent.stop();
			}
		}
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return new DialogSettings("org.eclipse.gyrex.admin.ui.p2.internal.FilteredIUSelectionDialog"); //$NON-NLS-1$
	}

	@Override
	public String getElementName(final Object item) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Comparator getItemsComparator() {
		return new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				String id1 = null;
				String id2 = null;

				if (o1 instanceof IInstallableUnit) {
					id1 = ((IInstallableUnit) o1).getId();
				} else {
					return 0;
				}

				if (o2 instanceof IInstallableUnit) {
					id2 = ((IInstallableUnit) o2).getId();
				} else {
					return 0;
				}

				return id1.compareTo(id2);
			}
		};
	}

	@Override
	protected IStatus validateItem(final Object item) {
		return Status.OK_STATUS;
	}

}
