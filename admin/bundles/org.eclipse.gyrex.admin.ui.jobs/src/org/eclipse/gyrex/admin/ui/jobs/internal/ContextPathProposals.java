/*******************************************************************************
 * Copyright (c) 2013 <enter-company-name-here> and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gyrex.context.definitions.ContextDefinition;
import org.eclipse.gyrex.context.definitions.IRuntimeContextDefinitionManager;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import org.apache.commons.lang.StringUtils;

final class ContextPathProposals implements IContentProposalProvider {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {
		final List<IContentProposal> resultList = new ArrayList<IContentProposal>();

		final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));

		final Collection<ContextDefinition> contexts = JobsUiActivator.getInstance().getService(IRuntimeContextDefinitionManager.class).getDefinedContexts();
		for (final ContextDefinition contextDefinition : contexts) {
			if ((null == patternString) || StringUtils.contains(contextDefinition.getPath().toString(), patternString)) {
				resultList.add(new ContentProposal(contextDefinition.getPath().toString(), contextDefinition.toString()));
			}
		}

		return resultList.toArray(new IContentProposal[resultList.size()]);
	}
}