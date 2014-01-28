/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Gunnar Wagenknecht - fork for Gyrex Admin UI 
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import java.util.Comparator;

import org.eclipse.core.runtime.Assert;

/**
 * Quick sort to sort key-value pairs. The keys and arrays are specified in
 * separate arrays.
 * 
 * @since 1.1
 */
/* package */class TwoArrayQuickSorter {

	/**
	 * Default comparator.
	 */
	public static final class StringComparator implements Comparator<Object> {
		private final boolean fIgnoreCase;

		StringComparator(final boolean ignoreCase) {
			fIgnoreCase = ignoreCase;
		}

		@Override
		public int compare(final Object left, final Object right) {
			return fIgnoreCase ? ((String) left).compareToIgnoreCase((String) right) : ((String) left).compareTo((String) right);
		}
	}

	/*
	 * Swaps x[a] with x[b].
	 */
	private static final void swap(final Object x[], final int a, final int b) {
		final Object t = x[a];
		x[a] = x[b];
		x[b] = t;
	}

	private final Comparator<Object> fComparator;

	/**
	 * Creates a sorter with default string comparator. The keys are assumed to
	 * be strings.
	 * 
	 * @param ignoreCase
	 *            specifies whether sorting is case sensitive or not.
	 */
	public TwoArrayQuickSorter(final boolean ignoreCase) {
		fComparator = new StringComparator(ignoreCase);
	}

	/**
	 * Creates a sorter with a comparator.
	 * 
	 * @param comparator
	 *            the comparator to order the elements. The comparator must not
	 *            be <code>null</code>.
	 */
	public TwoArrayQuickSorter(final Comparator<Object> comparator) {
		fComparator = comparator;
	}

	private void internalSort(final Object[] keys, final Object[] values, int left, int right) {
		final int original_left = left;
		final int original_right = right;

		final Object mid = keys[(left + right) / 2];
		do {
			while (fComparator.compare(keys[left], mid) < 0) {
				left++;
			}

			while (fComparator.compare(mid, keys[right]) < 0) {
				right--;
			}

			if (left <= right) {
				swap(keys, left, right);
				swap(values, left, right);
				left++;
				right--;
			}
		} while (left <= right);

		if (original_left < right) {
			internalSort(keys, values, original_left, right);
		}

		if (left < original_right) {
			internalSort(keys, values, left, original_right);
		}
	}

	/**
	 * Sorts keys and values in parallel.
	 * 
	 * @param keys
	 *            the keys to use for sorting.
	 * @param values
	 *            the values associated with the keys.
	 */
	public void sort(final Object[] keys, final Object[] values) {
		if ((keys == null) || (values == null)) {
			Assert.isTrue(false, "Either keys or values in null"); //$NON-NLS-1$
			return;
		}

		if (keys.length <= 1)
			return;

		internalSort(keys, values, 0, keys.length - 1);
	}

}
