/*******************************************************************************
 * Copyright (c) 2013 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.jobs.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import org.apache.commons.lang.StringUtils;

final class TimeZoneProposals implements IContentProposalProvider {
	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	@Override
	public IContentProposal[] getProposals(final String contents, final int position) {
		final List<IContentProposal> resultList = new ArrayList<IContentProposal>();

		final String patternString = StringUtils.trimToNull(StringUtils.substring(contents, 0, position));

		final String[] availableIDs = TimeZone.getAvailableIDs();
		Arrays.sort(availableIDs);
		for (final String id : availableIDs) {
			final TimeZone timeZone = TimeZone.getTimeZone(id);
			if ((null == patternString) || StringUtils.contains(StringUtils.lowerCase(timeZone.getID()), StringUtils.lowerCase(patternString))) {
				resultList.add(new ContentProposal(timeZone.getID(), id + " - " + timeZone.getDisplayName(Locale.US)));
			}
		}

		return resultList.toArray(new IContentProposal[resultList.size()]);
	}
}