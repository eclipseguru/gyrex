/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * This is a copy of FilteredItemsSelectionDialog without use of workbench and memento
 *
 * Contributors:
 *  IBM Corporation - initial API and implementation
 *  Willian Mitsuda <wmitsuda@gmail.com>
 *     - Fix for bug 196553 - [Dialogs] Support IColorProvider/IFontProvider in FilteredItemsSelectionDialog
 *  Peter Friese <peter.friese@gentleware.com>
 *     - Fix for bug 208602 - [Dialogs] Open Type dialog needs accessible labels
 *  Simon Muschel <smuschel@gmx.de> - bug 258493
 *  Peter Grube - fork for Gyrex Admin UI
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.AdminUiImages;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rap.rwt.lifecycle.UICallBack;
import org.eclipse.rap.rwt.widgets.DialogCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Shows a list of items to the user with a text entry field for a string
 * pattern used to filter the list of items.
 * 
 * @since 1.1
 */
public abstract class FilteredItemsSelectionDialog extends SelectionStatusDialog {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/**
	 * An interface to content providers for
	 * <code>FilterItemsSelectionDialog</code>.
	 */
	protected abstract class AbstractContentProvider {
		/**
		 * Adds the item to the content provider if the filter matches the item.
		 * Otherwise does nothing.
		 * 
		 * @param item
		 *            the item to add
		 * @param itemsFilter
		 *            the filter
		 * @see FilteredItemsSelectionDialog.ItemsFilter#matchItem(Object)
		 */
		public abstract void add(Object item, ItemsFilter itemsFilter);
	}

	/**
	 * Collects filtered elements. Contains one synchronized, sorted set for
	 * collecting filtered elements. All collected elements are sorted using
	 * comparator. Comparator is returned by getElementComparator() method.
	 * Implementation of <code>ItemsFilter</code> is used to filter elements.
	 * The key function of filter used in to filtering is
	 * <code>matchElement(Object item)</code>.
	 * <p>
	 * The <code>ContentProvider</code> class also provides item filtering
	 * methods. The filtering has been moved from the standard TableView
	 * <code>getFilteredItems()</code> method to content provider, because
	 * <code>ILazyContentProvider</code> and virtual tables are used. This class
	 * is responsible for adding a separator below history items and marking
	 * each items as duplicate if its name repeats more than once on the
	 * filtered list.
	 */
	private class ContentProvider extends AbstractContentProvider implements IStructuredContentProvider, ILazyContentProvider {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		private SelectionHistory selectionHistory;

		/**
		 * Raw result of the searching (unsorted, unfiltered).
		 * <p>
		 * Standard object flow:
		 * <code>items -> lastSortedItems -> lastFilteredItems</code>
		 */
		private final Set<Object> items;

		/**
		 * Items that are duplicates.
		 */
		private final Set<Object> duplicates;

		/**
		 * List of <code>ViewerFilter</code>s to be used during filtering
		 */
		private List<ViewerFilter> filters;

		/**
		 * Result of the last filtering.
		 * <p>
		 * Standard object flow:
		 * <code>items -> lastSortedItems -> lastFilteredItems</code>
		 */
		private List<Object> lastFilteredItems;

		/**
		 * Result of the last sorting.
		 * <p>
		 * Standard object flow:
		 * <code>items -> lastSortedItems -> lastFilteredItems</code>
		 */
		private final List<Object> lastSortedItems;

		/**
		 * Used for <code>getFilteredItems()</code> method canceling (when the
		 * job that invoked the method was canceled).
		 * <p>
		 * Method canceling could be based (only) on monitor canceling
		 * unfortunately sometimes the method <code>getFilteredElements()</code>
		 * could be run with a null monitor, the <code>reset</code> flag have to
		 * be left intact.
		 */
		private boolean reset;

		/**
		 * Creates new instance of <code>ContentProvider</code>.
		 */
		public ContentProvider() {
			items = Collections.synchronizedSet(new HashSet<Object>(2048));
			duplicates = Collections.synchronizedSet(new HashSet<Object>(256));
			lastFilteredItems = new ArrayList<Object>();
			lastSortedItems = Collections.synchronizedList(new ArrayList<Object>(2048));
		}

		/**
		 * Adds filtered item.
		 * 
		 * @param item
		 * @param itemsFilter
		 */
		@Override
		public void add(final Object item, final ItemsFilter itemsFilter) {
			if (itemsFilter == filter) {
				if (itemsFilter != null) {
					if (itemsFilter.matchItem(item)) {
						items.add(item);
					}
				} else {
					items.add(item);
				}
			}
		}

		/**
		 * Adds a filter to this content provider. For an example usage of such
		 * filters look at the project <code>org.eclipse.ui.ide</code>, class
		 * <code>org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog.CustomWorkingSetFilter</code>
		 * .
		 * 
		 * @param filter
		 *            the filter to be added
		 */
		public void addFilter(final ViewerFilter filter) {
			if (filters == null) {
				filters = new ArrayList<ViewerFilter>();
			}
			filters.add(filter);
			// currently filters are only added when dialog is restored
			// if it is changed, refreshing the whole TableViewer should be
			// added
		}

		/**
		 * Adds item to history and refresh view.
		 * 
		 * @param item
		 *            to add
		 */
		public void addHistoryElement(final Object item) {
			if (selectionHistory != null) {
				selectionHistory.accessed(item);
			}
			if (filter == null || !filter.matchItem(item)) {
				items.remove(item);
				duplicates.remove(item);
				lastSortedItems.remove(item);
			}
			synchronized (lastSortedItems) {
				Collections.sort(lastSortedItems, getHistoryComparator());
			}
			refresh();
		}

		/**
		 * Add all history items to <code>contentProvider</code>.
		 * 
		 * @param itemsFilter
		 */
		public void addHistoryItems(final ItemsFilter itemsFilter) {
			if (selectionHistory != null) {
				final Object[] items = selectionHistory.getHistoryItems();
				for (final Object item : items) {
					if (itemsFilter == filter) {
						if (itemsFilter != null) {
							if (itemsFilter.matchItem(item)) {
								if (itemsFilter.isConsistentItem(item)) {
									this.items.add(item);
								} else {
									selectionHistory.remove(item);
								}
							}
						}
					}
				}
			}
		}

		private void checkDuplicates(final IProgressMonitor monitor) {
			synchronized (lastFilteredItems) {
				IProgressMonitor subMonitor = null;
				final int reportEvery = lastFilteredItems.size() / 20;
				if (monitor != null) {
					subMonitor = new SubProgressMonitor(monitor, 100);
					subMonitor.beginTask(WidgetMessages.get(display).FilteredItemsSelectionDialog_cacheRefreshJob_checkDuplicates, 5);
				}
				final HashMap<String, Object> helperMap = new HashMap<String, Object>();
				for (int i = 0; i < lastFilteredItems.size(); i++) {
					if (reset || subMonitor != null && subMonitor.isCanceled()) {
						return;
					}
					final Object item = lastFilteredItems.get(i);

					if (!(item instanceof ItemsListSeparator)) {
						final Object previousItem = helperMap.put(getElementName(item), item);
						if (previousItem != null) {
							setDuplicateElement(previousItem, true);
							setDuplicateElement(item, true);
						} else {
							setDuplicateElement(item, false);
						}
					}

					if (subMonitor != null && reportEvery != 0 && (i + 1) % reportEvery == 0) {
						subMonitor.worked(1);
					}
				}
				helperMap.clear();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		@Override
		public Object[] getElements(final Object inputElement) {
			return lastFilteredItems.toArray();
		}

		/**
		 * Returns an array of items filtered using the provided
		 * <code>ViewerFilter</code>s with a separator added.
		 * 
		 * @param parent
		 *            the parent
		 * @param monitor
		 *            progress monitor, can be <code>null</code>
		 * @return an array of filtered items
		 */
		protected Object[] getFilteredItems(final Object parent, IProgressMonitor monitor) {
			int ticks = 100;
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}

			monitor.beginTask(WidgetMessages.get().FilteredItemsSelectionDialog_cacheRefreshJob_getFilteredElements, ticks);
			if (filters != null) {
				ticks /= filters.size() + 2;
			} else {
				ticks /= 2;
			}

			// get already sorted array
			Object[] filteredElements = getSortedItems();

			monitor.worked(ticks);

			// filter the elements using provided ViewerFilters
			if (filters != null && filteredElements != null) {
				for (final ViewerFilter f : filters) {
					filteredElements = f.filter(list, parent, filteredElements);
					monitor.worked(ticks);
				}
			}

			if (filteredElements == null || monitor.isCanceled()) {
				monitor.done();
				return new Object[0];
			}

			final ArrayList<Object> preparedElements = new ArrayList<Object>();
			boolean hasHistory = false;

			if (filteredElements.length > 0) {
				if (isHistoryElement(filteredElements[0])) {
					hasHistory = true;
				}
			}

			final int reportEvery = filteredElements.length / ticks;

			// add separator
			for (int i = 0; i < filteredElements.length; i++) {
				final Object item = filteredElements[i];

				if (hasHistory && !isHistoryElement(item)) {
					preparedElements.add(itemsListSeparator);
					hasHistory = false;
				}

				preparedElements.add(item);

				if (reportEvery != 0 && (i + 1) % reportEvery == 0) {
					monitor.worked(1);
				}
			}

			monitor.done();

			return preparedElements.toArray();
		}

		public int getNumberOfElements() {
			return lastFilteredItems.size();
		}

		/**
		 * @return Returns the selectionHistory.
		 */
		public SelectionHistory getSelectionHistory() {
			return selectionHistory;
		}

		/**
		 * Gets sorted items.
		 * 
		 * @return sorted items
		 */
		private Object[] getSortedItems() {
			if (lastSortedItems.size() != items.size()) {
				synchronized (lastSortedItems) {
					lastSortedItems.clear();
					lastSortedItems.addAll(items);
					Collections.sort(lastSortedItems, getHistoryComparator());
				}
			}
			return lastSortedItems.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}

		/**
		 * Indicates whether given item is a duplicate.
		 * 
		 * @param item
		 *            item to check
		 * @return <code>true</code> if item is duplicate
		 */
		public boolean isDuplicateElement(final Object item) {
			return duplicates.contains(item);
		}

		/**
		 * @param item
		 * @return <code>true</code> if given item is part of the history
		 */
		public boolean isHistoryElement(final Object item) {
			if (selectionHistory != null) {
				return selectionHistory.contains(item);
			}
			return false;
		}

//[gyrex]	we don't need memento in gyrex
		/**
		 * Load history from memento.
		 * 
		 * @param memento
		 *            memento from which the history will be retrieved
		 */
//		public void loadHistory(IMemento memento) {
//			if (selectionHistory != null) {
//				selectionHistory.load(memento);
//			}
//		}

		/**
		 * Refresh dialog.
		 */
		public void refresh() {
			scheduleRefresh();
		}

		/**
		 * Main method responsible for getting the filtered items and checking
		 * for duplicates. It is based on the
		 * {@link FilteredItemsSelectionDialog.ContentProvider#getFilteredItems(Object, IProgressMonitor)}
		 * .
		 * 
		 * @param checkDuplicates
		 *            <code>true</code> if data concerning elements duplication
		 *            should be computed - it takes much more time than standard
		 *            filtering
		 * @param monitor
		 *            progress monitor
		 */
		public void reloadCache(final boolean checkDuplicates, final IProgressMonitor monitor) {

			reset = false;

			if (monitor != null) {
				// the work is divided into two actions of the same length
				final int totalWork = checkDuplicates ? 200 : 100;

				monitor.beginTask(WidgetMessages.get(display).FilteredItemsSelectionDialog_cacheRefreshJob, totalWork);
			}

			// the TableViewer's root (the input) is treated as parent

			lastFilteredItems = Arrays.asList(getFilteredItems(list.getInput(), monitor != null ? new SubProgressMonitor(monitor, 100) : null));

			if (reset || monitor != null && monitor.isCanceled()) {
				if (monitor != null) {
					monitor.done();
				}
				return;
			}

			if (checkDuplicates) {
				checkDuplicates(monitor);
			}
			if (monitor != null) {
				monitor.done();
			}
		}

		/**
		 * Remember result of filtering.
		 * 
		 * @param itemsFilter
		 */
		public void rememberResult(final ItemsFilter itemsFilter) {
			final List<Object> itemsList = Collections.synchronizedList(Arrays.asList(getSortedItems()));
			// synchronization
			if (itemsFilter == filter) {
				lastCompletedFilter = itemsFilter;
				lastCompletedResult = itemsList;
			}

		}

		/**
		 * Removes items from history and refreshes the view.
		 * 
		 * @param item
		 *            to remove
		 * @return removed item
		 */
		public Object removeHistoryElement(final Object item) {
			if (selectionHistory != null) {
				selectionHistory.remove(item);
			}
			if (filter == null || filter.getPattern().length() == 0) {
				items.remove(item);
				duplicates.remove(item);
				lastSortedItems.remove(item);
			}

			synchronized (lastSortedItems) {
				Collections.sort(lastSortedItems, getHistoryComparator());
			}
			return item;
		}

		/**
		 * Removes all content items and resets progress message.
		 */
		public void reset() {
			reset = true;
			items.clear();
			duplicates.clear();
			lastSortedItems.clear();
		}

//[gyrex]	we don't need memento in gyrex
		/**
		 * Save history to memento.
		 * 
		 * @param memento
		 *            memento to which the history will be added
		 */
//		public void saveHistory(IMemento memento) {
//			if (selectionHistory != null) {
//				selectionHistory.save(memento);
//			}
//		}

		/**
		 * Sets/unsets given item as duplicate.
		 * 
		 * @param item
		 *            item to change
		 * @param isDuplicate
		 *            duplicate flag
		 */
		public void setDuplicateElement(final Object item, final boolean isDuplicate) {
			if (items.contains(item)) {
				if (isDuplicate) {
					duplicates.add(item);
				} else {
					duplicates.remove(item);
				}
			}
		}

		/**
		 * Sets selection history.
		 * 
		 * @param selectionHistory
		 *            The selectionHistory to set.
		 */
		public void setSelectionHistory(final SelectionHistory selectionHistory) {
			this.selectionHistory = selectionHistory;
		}

		/**
		 * Stops reloading cache - <code>getFilteredItems()</code> method.
		 */
		public void stopReloadingCache() {
			reset = true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
		 */
		@Override
		public void updateElement(final int index) {

			list.replace(lastFilteredItems.size() > index ? lastFilteredItems.get(index) : null, index);

		}

	}

	/**
	 * DetailsContentViewer objects are wrappers for labels.
	 * DetailsContentViewer provides means to change label's image and text when
	 * the attached LabelProvider is updated.
	 */
	private class DetailsContentViewer extends ContentViewer {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		private final CLabel label;

		/**
		 * Unfortunately, it was impossible to delegate displaying border to
		 * label. The <code>ViewForm</code> is used because <code>CLabel</code>
		 * displays shadow when border is present.
		 */
		private final ViewForm viewForm;

		/**
		 * Constructs a new instance of this class given its parent and a style
		 * value describing its behavior and appearance.
		 * 
		 * @param parent
		 *            the parent component
		 * @param style
		 *            SWT style bits
		 */
		public DetailsContentViewer(final Composite parent, final int style) {
			viewForm = new ViewForm(parent, style);
			final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			viewForm.setLayoutData(gd);
			label = new CLabel(viewForm, SWT.FLAT);
			label.setFont(parent.getFont());
			viewForm.setContent(label);
			hookControl(label);
		}

		/**
		 * Sets the given text and image to the label.
		 * 
		 * @param text
		 *            the new text or null
		 * @param image
		 *            the new image
		 */
		private void doRefresh(String text, final Image image) {
			if (text != null) {
				text = LegacyActionTools.escapeMnemonics(text);
			}
			label.setText(text);
			label.setImage(image);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#getControl()
		 */
		@Override
		public Control getControl() {
			return label;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#getSelection()
		 */
		@Override
		public ISelection getSelection() {
			// not supported
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ContentViewer#handleLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
		 */
		@Override
		protected void handleLabelProviderChanged(final LabelProviderChangedEvent event) {
			if (event != null) {
				refresh(event.getElements());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,
		 *      java.lang.Object)
		 */
		@Override
		protected void inputChanged(final Object input, final Object oldInput) {
			if (oldInput == null) {
				if (input == null) {
					return;
				}
				refresh();
				return;
			}

			refresh();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#refresh()
		 */
		@Override
		public void refresh() {
			final Object input = getInput();
			if (input != null) {
				final ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
				doRefresh(labelProvider.getText(input), labelProvider.getImage(input));
			} else {
				doRefresh(null, null);
			}
		}

		/**
		 * Refreshes the label if currently chosen element is on the list.
		 * 
		 * @param objs
		 *            list of changed object
		 */
		private void refresh(final Object[] objs) {
			if (objs == null || getInput() == null) {
				return;
			}
			final Object input = getInput();
			for (final Object obj : objs) {
				if (obj.equals(input)) {
					refresh();
					break;
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection,
		 *      boolean)
		 */
		@Override
		public void setSelection(final ISelection selection, final boolean reveal) {
			// not supported
		}

		/**
		 * Shows/hides the content viewer.
		 * 
		 * @param visible
		 *            if the content viewer should be visible.
		 */
		public void setVisible(final boolean visible) {
			final GridData gd = (GridData) viewForm.getLayoutData();
			gd.exclude = !visible;
			viewForm.getParent().layout();
		}
	}

	/**
	 * Filters items history and schedule filter job.
	 */
	private class FilterHistoryJob extends Job {

		/**
		 * Filter used during the filtering process.
		 */
		private ItemsFilter itemsFilter;

		/**
		 * Creates new instance of receiver.
		 */
		public FilterHistoryJob() {
			super(WidgetMessages.get().FilteredItemsSelectionDialog_jobLabel);
			setSystem(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(final IProgressMonitor monitor) {

			itemsFilter = filter;

			contentProvider.reset();

			refreshWithLastSelection = false;

			contentProvider.addHistoryItems(itemsFilter);

			if (!(lastCompletedFilter != null && lastCompletedFilter.isSubFilter(itemsFilter))) {
				contentProvider.refresh();
			}

			filterJob.schedule();

			return Status.OK_STATUS;
		}

	}

	/**
	 * Filters items in indicated set and history. During filtering, it
	 * refreshes the dialog (progress monitor and elements list). Depending on
	 * the filter, <code>FilterJob</code> decides which kind of search will be
	 * run inside <code>filterContent</code>. If the last filtering is done
	 * (last completed filter), is not null, and the new filter is a sub-filter
	 * (
	 * {@link FilteredItemsSelectionDialog.ItemsFilter#isSubFilter(FilteredItemsSelectionDialog.ItemsFilter)}
	 * ) of the last, then <code>FilterJob</code> only filters in the cache. If
	 * it is the first filtering or the new filter isn't a sub-filter of the
	 * last one, a full search is run.
	 */
	private class FilterJob extends Job {

		/**
		 * Filter used during the filtering process.
		 */
		protected ItemsFilter itemsFilter;

		/**
		 * Creates new instance of FilterJob
		 */
		public FilterJob() {
			super(WidgetMessages.get().FilteredItemsSelectionDialog_jobLabel);
			setSystem(true);
		}

		/**
		 * Executes job using the given filtering progress monitor. A hook for
		 * subclasses.
		 * 
		 * @param monitor
		 *            progress monitor
		 * @return result of the execution
		 */
		protected IStatus doRun(final GranualProgressMonitor monitor) {
			try {
				internalRun(monitor);
			} catch (final CoreException e) {
				cancel();
				return new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, WidgetMessages.get(display).FilteredItemsSelectionDialog_jobError, e);
			}
			return Status.OK_STATUS;
		}

		/**
		 * Filters items.
		 * 
		 * @param monitor
		 *            for monitoring progress
		 * @throws CoreException
		 */
		protected void filterContent(final GranualProgressMonitor monitor) throws CoreException {

			if (lastCompletedFilter != null && lastCompletedFilter.isSubFilter(itemsFilter)) {

				final int length = lastCompletedResult.size() / 500;
				monitor.beginTask(WidgetMessages.get(display).FilteredItemsSelectionDialog_cacheSearchJob_taskName, length);

				for (int pos = 0; pos < lastCompletedResult.size(); pos++) {

					final Object item = lastCompletedResult.get(pos);
					if (monitor.isCanceled()) {
						break;
					}
					contentProvider.add(item, itemsFilter);

					if (pos % 500 == 0) {
						monitor.worked(1);
					}
				}

			} else {

				lastCompletedFilter = null;
				lastCompletedResult = null;

				SubProgressMonitor subMonitor = null;
				if (monitor != null) {
					monitor.beginTask(WidgetMessages.get(display).FilteredItemsSelectionDialog_searchJob_taskName, 100);
					subMonitor = new SubProgressMonitor(monitor, 95);

				}

				fillContentProvider(contentProvider, itemsFilter, subMonitor);

				if (monitor != null && !monitor.isCanceled()) {
					monitor.worked(2);
					contentProvider.rememberResult(itemsFilter);
					monitor.worked(3);
				}
			}

		}

		/**
		 * Main method for the job.
		 * 
		 * @param monitor
		 * @throws CoreException
		 */
		private void internalRun(final GranualProgressMonitor monitor) throws CoreException {
			try {
				if (monitor.isCanceled()) {
					return;
				}

				itemsFilter = filter;

				if (filter.getPattern().length() != 0) {
					filterContent(monitor);
				}

				if (monitor.isCanceled()) {
					return;
				}

				contentProvider.refresh();
			} finally {
				monitor.done();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected final IStatus run(final IProgressMonitor parent) {
			final GranualProgressMonitor monitor = new GranualProgressMonitor(parent);
			return doRun(monitor);
		}

	}

	/**
	 * GranualProgressMonitor is used for monitoring progress of filtering
	 * process. It is used by <code>RefreshProgressMessageJob</code> to refresh
	 * progress message. State of this monitor illustrates state of filtering or
	 * cache refreshing process.
	 */
	private class GranualProgressMonitor extends ProgressMonitorWrapper {

		private String name;

		private String subName;

		private int totalWork;

		private double worked;

		private boolean done;

		/**
		 * Creates instance of <code>GranualProgressMonitor</code>.
		 * 
		 * @param monitor
		 *            progress to be wrapped
		 */
		public GranualProgressMonitor(final IProgressMonitor monitor) {
			super(monitor);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#beginTask(java.lang.String,
		 *      int)
		 */
		@Override
		public void beginTask(final String name, final int totalWork) {
			super.beginTask(name, totalWork);
			if (name == null) {
				this.name = name;
			}
			this.totalWork = totalWork;
			refreshProgressMessageJob.scheduleProgressRefresh(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#done()
		 */
		@Override
		public void done() {
			done = true;
			super.done();
		}

		private String getMessage() {
			if (done) {
				return ""; //$NON-NLS-1$
			}

			String message;

			if (name == null) {
				message = subName == null ? "" : subName; //$NON-NLS-1$
			} else {
				message = subName == null ? name : NLS.bind(WidgetMessages.get(display).FilteredItemsSelectionDialog_subtaskProgressMessage, new Object[] { name, subName });
			}
			if (totalWork == 0) {
				return message;
			}

			return NLS.bind(WidgetMessages.get(display).FilteredItemsSelectionDialog_taskProgressMessage, new Object[] { message, new Integer((int) (worked * 100 / totalWork)) });

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#internalWorked(double)
		 */
		@Override
		public void internalWorked(final double work) {
			worked = worked + work;
		}

		/**
		 * Checks if filtering has been done
		 * 
		 * @return true if filtering work has been done false in other way
		 */
		public boolean isDone() {
			return done;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setCanceled(boolean)
		 */
		@Override
		public void setCanceled(final boolean b) {
			done = b;
			super.setCanceled(b);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#setTaskName(java.lang.String)
		 */
		@Override
		public void setTaskName(final String name) {
			super.setTaskName(name);
			this.name = name;
			subName = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#subTask(java.lang.String)
		 */
		@Override
		public void subTask(final String name) {
			super.subTask(name);
			subName = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ProgressMonitorWrapper#worked(int)
		 */
		@Override
		public void worked(final int work) {
			super.worked(work);
			internalWorked(work);
		}

	}

	/**
	 * Compares items according to the history.
	 */
	private class HistoryComparator<T> implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final Object o1, final Object o2) {
			if (isHistoryElement(o1) && isHistoryElement(o2) || !isHistoryElement(o1) && !isHistoryElement(o2)) {
				return getItemsComparator().compare(o1, o2);
			}

			if (isHistoryElement(o1)) {
				return -2;
			}
			if (isHistoryElement(o2)) {
				return +2;
			}

			return 0;
		}

	}

	/**
	 * Filters elements using SearchPattern by comparing the names of items with
	 * the filter pattern.
	 */
	protected abstract class ItemsFilter {

		protected SearchPattern patternMatcher;

		/**
		 * Creates new instance of ItemsFilter.
		 */
		public ItemsFilter() {
			this(new SearchPattern());
		}

		/**
		 * Creates new instance of ItemsFilter.
		 * 
		 * @param searchPattern
		 *            the pattern to be used when filtering
		 */
		public ItemsFilter(final SearchPattern searchPattern) {
			patternMatcher = searchPattern;
			String stringPattern = ""; //$NON-NLS-1$
			if (pattern != null/* && !pattern.getText().equals("*")*/) {
				stringPattern = pattern.getText();
			}
			patternMatcher.setPattern(stringPattern);
		}

		/**
		 * Checks whether the provided filter is equal to the current filter.
		 * The default implementation checks if <code>SearchPattern</code> from
		 * current filter is equal to the one from provided filter.
		 * 
		 * @param filter
		 *            filter to be checked, or <code>null</code>
		 * @return <code>true</code> if the given filter is equal to current
		 *         filter, <code>false</code> if given filter isn't equal to
		 *         current one or if it is <code>null</code>
		 * @see org.eclipse.ui.dialogs.SearchPattern#equalsPattern(org.eclipse.ui.dialogs.SearchPattern)
		 */
		public boolean equalsFilter(final ItemsFilter filter) {
			if (filter != null && filter.patternMatcher.equalsPattern(patternMatcher)) {
				return true;
			}
			return false;
		}

		/**
		 * Returns the rule to apply for matching keys.
		 * 
		 * @return an implementation-specific match rule
		 * @see SearchPattern#getMatchRule() for match rules returned by the
		 *      default implementation
		 */
		public int getMatchRule() {
			return patternMatcher.getMatchRule();
		}

		/**
		 * Returns the pattern string.
		 * 
		 * @return pattern for this filter
		 * @see SearchPattern#getPattern()
		 */
		public String getPattern() {
			return patternMatcher.getPattern();
		}

		/**
		 * Checks whether the pattern's match rule is camel case.
		 * 
		 * @return <code>true</code> if pattern's match rule is camel case,
		 *         <code>false</code> otherwise
		 */
		public boolean isCamelCasePattern() {
			return patternMatcher.getMatchRule() == SearchPattern.RULE_CAMELCASE_MATCH;
		}

		/**
		 * Checks consistency of an item. Item is inconsistent if was changed or
		 * removed.
		 * 
		 * @param item
		 * @return <code>true</code> if item is consistent, <code>false</code>
		 *         if item is inconsistent
		 */
		public abstract boolean isConsistentItem(Object item);

		/**
		 * Check if the given filter is a sub-filter of this filter. The default
		 * implementation checks if the <code>SearchPattern</code> from the
		 * given filter is a sub-pattern of the one from this filter.
		 * <p>
		 * <i>WARNING: This method is <b>not</b> defined in reading order, i.e.
		 * <code>a.isSubFilter(b)</code> is <code>true</code> iff <code>b</code>
		 * is a sub-filter of <code>a</code>, and not vice-versa. </i>
		 * </p>
		 * 
		 * @param filter
		 *            the filter to be checked, or <code>null</code>
		 * @return <code>true</code> if the given filter is sub-filter of this
		 *         filter, <code>false</code> if the given filter isn't a
		 *         sub-filter or is <code>null</code>
		 * @see org.eclipse.ui.dialogs.SearchPattern#isSubPattern(org.eclipse.ui.dialogs.SearchPattern)
		 */
		public boolean isSubFilter(final ItemsFilter filter) {
			if (filter != null) {
				return patternMatcher.isSubPattern(filter.patternMatcher);
			}
			return false;
		}

		/**
		 * Matches text with filter.
		 * 
		 * @param text
		 *            the text to match with the filter
		 * @return <code>true</code> if text matches with filter pattern,
		 *         <code>false</code> otherwise
		 */
		protected boolean matches(final String text) {
			return patternMatcher.matches(text);
		}

		/**
		 * General method for matching raw name pattern. Checks whether current
		 * pattern is prefix of name provided item.
		 * 
		 * @param item
		 *            item to check
		 * @return <code>true</code> if current pattern is a prefix of name
		 *         provided item, <code>false</code> if item's name is shorter
		 *         than prefix or sequences of characters don't match.
		 */
		public boolean matchesRawNamePattern(final Object item) {
			final String prefix = patternMatcher.getPattern();
			final String text = getElementName(item);

			if (text == null) {
				return false;
			}

			final int textLength = text.length();
			final int prefixLength = prefix.length();
			if (textLength < prefixLength) {
				return false;
			}
			for (int i = prefixLength - 1; i >= 0; i--) {
				if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(text.charAt(i))) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Matches an item against filter conditions.
		 * 
		 * @param item
		 * @return <code>true<code> if item matches against filter conditions, <code>false</code>
		 *         otherwise
		 */
		public abstract boolean matchItem(Object item);

	}

	// RAP [rh] StyledCellLabelProvider not supported	
//	private class ItemsListLabelProvider extends StyledCellLabelProvider
	private class ItemsListLabelProvider extends CellLabelProvider implements ILabelProviderListener {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		private ILabelProvider provider;

		private ILabelDecorator selectionDecorator;

		// Need to keep our own list of listeners
		private final ListenerList listeners = new ListenerList();

		/**
		 * Creates a new instance of the class.
		 * 
		 * @param provider
		 *            the label provider for all items, not <code>null</code>
		 * @param selectionDecorator
		 *            the decorator for selected items, can be <code>null</code>
		 */
		public ItemsListLabelProvider(final ILabelProvider provider, final ILabelDecorator selectionDecorator) {
			Assert.isNotNull(provider);
			this.provider = provider;
			this.selectionDecorator = selectionDecorator;

// RAP [rh] IStyledLabelProvider not supported
//			setOwnerDrawEnabled(provider instanceof IStyledLabelProvider);

			provider.addListener(this);

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		@Override
		public void addListener(final ILabelProviderListener listener) {
			listeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		@Override
		public void dispose() {
			provider.removeListener(this);
			provider.dispose();

			if (selectionDecorator != null) {
				selectionDecorator.removeListener(this);
				selectionDecorator.dispose();
			}

			super.dispose();
		}

		private Color getBackground(final Object element) {
			if (element instanceof ItemsListSeparator) {
				return null;
			}
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getBackground(element);
			}
			return null;
		}

		private Font getFont(final Object element) {
			if (element instanceof ItemsListSeparator) {
				return null;
			}
			if (provider instanceof IFontProvider) {
				return ((IFontProvider) provider).getFont(element);
			}
			return null;
		}

		private Color getForeground(final Object element) {
			if (element instanceof ItemsListSeparator) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			}
			if (provider instanceof IColorProvider) {
				return ((IColorProvider) provider).getForeground(element);
			}
			return null;
		}

		private Image getImage(final Object element) {
			if (element instanceof ItemsListSeparator) {
				return AdminUiImages.getImage(AdminUiImages.IMG_VIEW_MENU);
			}

			return provider.getImage(element);
		}

// RAP [rh] IStyledLabelProvider not supported		
//		private StyledString getStyledText(Object element,
//				IStyledLabelProvider provider) {
//			StyledString string = provider.getStyledText(element);
//
//			if (selectionDecorator != null && isSelected(element)) {
//				String decorated = selectionDecorator.decorateText(string
//						.getString(), element);
//				return new StyledString(decorated);
//				// no need to add colors when element is selected
//			}
//			return string;
//		}

		/**
		 * Gets selection decorator.
		 * 
		 * @return the label decorator for selected items in the list
		 */
		public ILabelDecorator getSelectionDecorator() {
			return selectionDecorator;
		}

		private String getSeparatorLabel(final String separatorLabel) {
			final Rectangle rect = list.getTable().getBounds();

			final int borderWidth = list.getTable().computeTrim(0, 0, 0, 0).width;

			final int imageWidth = AdminUiActivator.getInstance().getImageRegistry().get(AdminUiImages.IMG_VIEW_MENU).getBounds().width;

			final int width = rect.width - borderWidth - imageWidth;

			final GC gc = new GC(list.getTable());
			gc.setFont(list.getTable().getFont());

//RAP [rh] GC#getAdvanceWidth missing			
//			int fSeparatorWidth = gc.getAdvanceWidth('-');
			final int fSeparatorWidth = gc.getCharWidth('-');
			final int fMessageLength = gc.textExtent(separatorLabel).x;

			gc.dispose();

			final StringBuffer dashes = new StringBuffer();
			final int chars = (width - fMessageLength) / fSeparatorWidth / 2 - 2;
			for (int i = 0; i < chars; i++) {
				dashes.append('-');
			}

			final StringBuffer result = new StringBuffer();
			result.append(dashes);
			result.append(" " + separatorLabel + " "); //$NON-NLS-1$//$NON-NLS-2$
			result.append(dashes);
			return result.toString().trim();
		}

		private String getText(final Object element) {
			if (element instanceof ItemsListSeparator) {
				return getSeparatorLabel(((ItemsListSeparator) element).getName());
			}

			final String str = provider.getText(element);
			if (selectionDecorator != null && isSelected(element)) {
				return selectionDecorator.decorateText(str.toString(), element);
			}

			return str;
		}

		@Override
		public boolean isLabelProperty(final Object element, final String property) {
			if (provider.isLabelProperty(element, property)) {
				return true;
			}
			if (selectionDecorator != null && selectionDecorator.isLabelProperty(element, property)) {
				return true;
			}
			return false;
		}

		private boolean isSelected(final Object element) {
			if (element != null && currentSelection != null) {
				for (final Object element2 : currentSelection) {
					if (element.equals(element2)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public void labelProviderChanged(final LabelProviderChangedEvent event) {
			final Object[] l = listeners.getListeners();
			for (int i = 0; i < listeners.size(); i++) {
				((ILabelProviderListener) l[i]).labelProviderChanged(event);
			}
		}

		@Override
		public void removeListener(final ILabelProviderListener listener) {
			listeners.remove(listener);
		}

		/**
		 * Sets new label provider.
		 * 
		 * @param newProvider
		 *            new label provider for items in the list, not
		 *            <code>null</code>
		 */
		public void setProvider(final ILabelProvider newProvider) {
			Assert.isNotNull(newProvider);
			provider.removeListener(this);
			provider.dispose();

			provider = newProvider;

			if (provider != null) {
				provider.addListener(this);
			}

// RAP [rh] IStyledLabelProvider not supported
//			setOwnerDrawEnabled(provider instanceof IStyledLabelProvider);
		}

		/**
		 * Sets new selection decorator.
		 * 
		 * @param newSelectionDecorator
		 *            new label decorator for selected items in the list
		 */
		public void setSelectionDecorator(final ILabelDecorator newSelectionDecorator) {
			if (selectionDecorator != null) {
				selectionDecorator.removeListener(this);
				selectionDecorator.dispose();
			}

			selectionDecorator = newSelectionDecorator;

			if (selectionDecorator != null) {
				selectionDecorator.addListener(this);
			}
		}

		@Override
		public void update(final ViewerCell cell) {
			final Object element = cell.getElement();

// RAP [rh] IStyledLabelProvider not supported
//			if (!(element instanceof ItemsListSeparator)
//					&& provider instanceof IStyledLabelProvider) {
//				IStyledLabelProvider styledLabelProvider = (IStyledLabelProvider) provider;
//				StyledString styledString = getStyledText(element,
//						styledLabelProvider);
//
//				cell.setText(styledString.getString());
//				cell.setStyleRanges(styledString.getStyleRanges());
//				cell.setImage(styledLabelProvider.getImage(element));
//			} else {
			cell.setText(getText(element));
			cell.setImage(getImage(element));
//			}
			cell.setFont(getFont(element));
			cell.setForeground(getForeground(element));
			cell.setBackground(getBackground(element));

// RAP [rh] Obsolete because of changed inheritance
//			super.update(cell);
		}
	}

	/**
	 * Used in ItemsListContentProvider, separates history and non-history
	 * items.
	 */
	private class ItemsListSeparator {

		private final String name;

		/**
		 * Creates a new instance of the class.
		 * 
		 * @param name
		 *            the name of the separator
		 */
		public ItemsListSeparator(final String name) {
			this.name = name;
		}

		/**
		 * Returns the name of this separator.
		 * 
		 * @return the name of the separator
		 */
		public String getName() {
			return name;
		}
	}

	/**
	 * A content provider that does nothing.
	 */
	private class NullContentProvider implements IContentProvider {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		}

	}

	/**
	 * A job responsible for computing filtered items list presented using
	 * <code>RefreshJob</code>.
	 * 
	 * @see FilteredItemsSelectionDialog.RefreshJob
	 */
	private class RefreshCacheJob extends Job {

		private final RefreshJob refreshJob = new RefreshJob();

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshCacheJob() {
			super(WidgetMessages.get().FilteredItemsSelectionDialog_cacheRefreshJob);
			setSystem(true);
		}

		/**
		 * Stops the job and all sub-jobs.
		 */
		public void cancelAll() {
			cancel();
			refreshJob.cancel();
		}

		@Override
		protected void canceling() {
			super.canceling();
			contentProvider.stopReloadingCache();
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return new Status(IStatus.CANCEL, AdminUiActivator.SYMBOLIC_NAME, IStatus.CANCEL, EMPTY_STRING, null);
			}

			if (FilteredItemsSelectionDialog.this != null) {
// RAP [if] fake context
				UICallBack.runNonUIThreadWithFakeContext(display, new Runnable() {
					@Override
					public void run() {
						final GranualProgressMonitor wrappedMonitor = new GranualProgressMonitor(monitor);
						reloadCache(true, wrappedMonitor);
					}
				});
			}

			if (!monitor.isCanceled()) {
				refreshJob.schedule();
			}

			return new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
		}

	}

	/**
	 * Only refreshes UI on the basis of an already sorted and filtered set of
	 * items.
	 * <p>
	 * Standard invocation scenario:
	 * <ol>
	 * <li>filtering job (<code>FilterJob</code> class extending
	 * <code>Job</code> class)</li>
	 * <li>cache refresh without checking for duplicates (
	 * <code>RefreshCacheJob</code> class extending <code>Job</code> class)</li>
	 * <li>UI refresh (<code>RefreshJob</code> class extending
	 * <code>UIJob</code> class)</li>
	 * <li>cache refresh with checking for duplicates
	 * (<cod>CacheRefreshJob</code> class extending <code>Job</code> class)</li>
	 * <li>UI refresh (<code>RefreshJob</code> class extending
	 * <code>UIJob</code> class)</li>
	 * </ol>
	 * The scenario is rather complicated, but it had to be applied, because:
	 * <ul>
	 * <li>refreshing cache is rather a long action and cannot be run in the UI
	 * - cannot be run in a UIJob</li>
	 * <li>refreshing cache checking for duplicates is twice as long as
	 * refreshing cache without checking for duplicates; results of the search
	 * could be displayed earlier</li>
	 * <li>refreshing the UI have to be run in a UIJob</li>
	 * </ul>
	 * 
	 * @see org.eclipse.FilteredItemsSelectionDialog.dialogs.UpdatedFilteredItemsSelectionDialog.FilterJob
	 * @see org.eclipse.FilteredItemsSelectionDialog.dialogs.UpdatedFilteredItemsSelectionDialog.RefreshJob
	 * @see org.eclipse.FilteredItemsSelectionDialog.dialogs.UpdatedFilteredItemsSelectionDialog.RefreshCacheJob
	 */
	private class RefreshJob extends UiJob {

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshJob() {
			super(getParentShell().getDisplay(), WidgetMessages.get().FilteredItemsSelectionDialog_refreshJob);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
			}

			if (FilteredItemsSelectionDialog.this != null) {
				refresh();
			}

			return new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
		}

	}

	/**
	 * Refreshes the progress message cyclically with 500 milliseconds delay.
	 * <code>RefreshProgressMessageJob</code> is strictly connected with
	 * <code>GranualProgressMonitor</code> and use it to to get progress message
	 * and to decide about break of cyclical refresh.
	 */
	private class RefreshProgressMessageJob extends UiJob {

		private GranualProgressMonitor progressMonitor;

		/**
		 * Creates a new instance of the class.
		 */
		public RefreshProgressMessageJob() {
			super(getParentShell().getDisplay(), WidgetMessages.get().FilteredItemsSelectionDialog_progressRefreshJob);
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(final IProgressMonitor monitor) {

			if (!progressLabel.isDisposed()) {
				progressLabel.setText(progressMonitor != null ? progressMonitor.getMessage() : EMPTY_STRING);
			}

			if (progressMonitor == null || progressMonitor.isDone()) {
				return new Status(IStatus.CANCEL, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
			}

			// Schedule cyclical with 500 milliseconds delay
			schedule(500);

			return new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
		}

		/**
		 * Schedule progress refresh job.
		 * 
		 * @param progressMonitor
		 *            used during refresh progress label
		 */
		public void scheduleProgressRefresh(final GranualProgressMonitor progressMonitor) {
			this.progressMonitor = progressMonitor;
			// Schedule with initial delay to avoid flickering when the user
			// types quickly
			schedule(200);
		}

	}

	private class RemoveHistoryItemAction extends Action {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new instance of the class.
		 */
		public RemoveHistoryItemAction() {
			super(WidgetMessages.get().FilteredItemsSelectionDialog_removeItemsFromHistoryAction);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			@SuppressWarnings("unchecked")
			final List<Object> selectedElements = ((StructuredSelection) list.getSelection()).toList();
			removeSelectedItems(selectedElements);
		}
	}

//[gyrex]	we don't need history functions with memento
	/**
	 * History stores a list of key, object pairs. The list is bounded at a
	 * certain size. If the list exceeds this size the oldest element is removed
	 * from the list. An element can be added/renewed with a call to
	 * <code>accessed(Object)</code>.
	 * <p>
	 * The history can be stored to/loaded from an XML file.
	 */
	protected static abstract class SelectionHistory {

		private static final String DEFAULT_ROOT_NODE_NAME = "historyRootNode"; //$NON-NLS-1$

		private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$

		private static final int MAX_HISTORY_SIZE = 60;

		private final List<Object> historyList;

//		private final String rootNodeName;

//		private final String infoNodeName;

		/**
		 * Creates new instance of <code>SelectionHistory</code>.
		 */
		public SelectionHistory() {
			this(DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
		}

		private SelectionHistory(final String rootNodeName, final String infoNodeName) {

			historyList = Collections.synchronizedList(new LinkedList<Object>() {

				private static final long serialVersionUID = 0L;

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.util.LinkedList#add(java.lang.Object)
				 */
				@Override
				public boolean add(final Object arg0) {
					if (size() >= MAX_HISTORY_SIZE) {
						removeFirst();
					}
					if (!contains(arg0)) {
						return super.add(arg0);
					}
					return false;
				}

			});

//			this.rootNodeName = rootNodeName;
//			this.infoNodeName = infoNodeName;
		}

		/**
		 * Adds object to history.
		 * 
		 * @param object
		 *            the item to be added to the history
		 */
		public synchronized void accessed(final Object object) {
			historyList.add(object);
		}

		/**
		 * Returns <code>true</code> if history contains object.
		 * 
		 * @param object
		 *            the item for which check will be executed
		 * @return <code>true</code> if history contains object
		 *         <code>false</code> in other way
		 */
		public synchronized boolean contains(final Object object) {
			return historyList.contains(object);
		}

		/**
		 * Gets array of history items.
		 * 
		 * @return array of history elements
		 */
		public synchronized Object[] getHistoryItems() {
			return historyList.toArray();
		}

		/**
		 * Returns <code>true</code> if history is empty.
		 * 
		 * @return <code>true</code> if history is empty
		 */
		public synchronized boolean isEmpty() {
			return historyList.isEmpty();
		}

		/**
		 * Load history elements from memento.
		 * 
		 * @param memento
		 *            memento from which the history will be retrieved
		 */
//		public void load(IMemento memento) {
//
//			XMLMemento historyMemento = (XMLMemento) memento.getChild(rootNodeName);
//
//			if (historyMemento == null) {
//				return;
//			}
//
//			IMemento[] mementoElements = historyMemento.getChildren(infoNodeName);
//			for (int i = 0; i < mementoElements.length; ++i) {
//				IMemento mementoElement = mementoElements[i];
//				Object object = restoreItemFromMemento(mementoElement);
//				if (object != null) {
//					historyList.add(object);
//				}
//			}
//		}

		/**
		 * Remove element from history.
		 * 
		 * @param element
		 *            to remove form the history
		 * @return <code>true</code> if this list contained the specified
		 *         element
		 */
		public synchronized boolean remove(final Object element) {
			return historyList.remove(element);
		}

		/**
		 * Creates an object using given memento.
		 * 
		 * @param memento
		 *            memento used for creating new object
		 * @return the restored object
		 */
//		protected abstract Object restoreItemFromMemento(IMemento memento);

		/**
		 * Save history elements to memento.
		 * 
		 * @param memento
		 *            memento to which the history will be added
		 */
//		public void save(IMemento memento) {
//
//			IMemento historyMemento = memento.createChild(rootNodeName);
//
//			Object[] items = getHistoryItems();
//			for (int i = 0; i < items.length; i++) {
//				Object item = items[i];
//				IMemento elementMemento = historyMemento.createChild(infoNodeName);
//				storeItemToMemento(item, elementMemento);
//			}
//
//		}

		/**
		 * Store object in <code>IMemento</code>.
		 * 
		 * @param item
		 *            the item to store
		 * @param memento
		 *            the memento to store to
		 */
//		protected abstract void storeItemToMemento(Object item, IMemento memento);

	}

	private class ToggleStatusLineAction extends Action {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new instance of the class.
		 */
		public ToggleStatusLineAction() {
			super(WidgetMessages.get().FilteredItemsSelectionDialog_toggleStatusAction, IAction.AS_CHECK_BOX);
		}

		@Override
		public void run() {
			details.setVisible(isChecked());
		}
	}

	private final AtomicReference<DialogCallback> callbackRef = new AtomicReference<DialogCallback>();

	private static final String DIALOG_BOUNDS_SETTINGS = "DialogBoundsSettings"; //$NON-NLS-1$

//	private static final String SHOW_STATUS_LINE = "ShowStatusLine"; //no use in gyrex

//	private static final String HISTORY_SETTINGS = "History"; //we don't use history in gyrex

	private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

	private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$

	/**
	 * Represents an empty selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int NONE = 0;

	/**
	 * Pattern input field selection where caret is at the beginning (used only
	 * for initial pattern).
	 */
	public static final int CARET_BEGINNING = 1;

	/**
	 * Represents a full selection in the pattern input field (used only for
	 * initial pattern).
	 */
	public static final int FULL_SELECTION = 2;

	// RAP [rh] display used to access NLS messages	
	private final Display display;

	private Text pattern;

	private TableViewer list;

	private DetailsContentViewer details;

	/**
	 * It is a duplicate of a field in the CLabel class in DetailsContentViewer.
	 * It is maintained, because the <code>setDetailsLabelProvider()</code>
	 * could be called before content area is created.
	 */
	private ILabelProvider detailsLabelProvider;

	private ItemsListLabelProvider itemsListLabelProvider;

	private MenuManager menuManager;

	private MenuManager contextMenuManager;

	private final boolean multi;

	private ToolBar toolBar;

	private ToolItem toolItem;

	private Label progressLabel;

	private ToggleStatusLineAction toggleStatusLineAction;

//	private IHandlerActivation showViewHandler; //we don't need this in gyrex

	private RemoveHistoryItemAction removeHistoryItemAction;

	private ActionContributionItem removeHistoryActionContributionItem;

	private IStatus status;

	private final RefreshCacheJob refreshCacheJob;

	private final RefreshProgressMessageJob refreshProgressMessageJob = new RefreshProgressMessageJob();

	private Object[] currentSelection;

	private final ContentProvider contentProvider;

	private final FilterHistoryJob filterHistoryJob;

	private final FilterJob filterJob;

	private ItemsFilter filter;

	private List<Object> lastCompletedResult;

	private ItemsFilter lastCompletedFilter;

	private String initialPatternText;

	private int selectionMode;

	private ItemsListSeparator itemsListSeparator;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private boolean refreshWithLastSelection = false;

	/**
	 * Creates a new instance of the class. Created dialog won't allow to select
	 * more than one item.
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 */
	public FilteredItemsSelectionDialog(final Shell shell) {
		this(shell, false);
	}

	/**
	 * Creates a new instance of the class.
	 * 
	 * @param shell
	 *            shell to parent the dialog on
	 * @param multi
	 *            indicates whether dialog allows to select more than one
	 *            position in its list of items
	 */
	public FilteredItemsSelectionDialog(final Shell shell, final boolean multi) {
		super(shell);
// RAP [rh] store display to access NLS messages		
		display = shell.getDisplay();
		this.multi = multi;
		filterHistoryJob = new FilterHistoryJob();
		filterJob = new FilterJob();
		contentProvider = new ContentProvider();
		refreshCacheJob = new RefreshCacheJob();
		itemsListSeparator = new ItemsListSeparator(WidgetMessages.get().FilteredItemsSelectionDialog_separatorLabel);
		selectionMode = NONE;
	}

	/**
	 * Adds item to history.
	 * 
	 * @param item
	 *            the item to be added
	 */
	protected void accessedHistoryItem(final Object item) {
		contentProvider.addHistoryElement(item);
	}

	/**
	 * Adds viewer filter to the dialog items list.
	 * 
	 * @param filter
	 *            the new filter
	 */
	protected void addListFilter(final ViewerFilter filter) {
		contentProvider.addFilter(filter);
	}

	/**
	 * Applies the filter created by <code>createFilter()</code> method to the
	 * items list. When new filter is different than previous one it will cause
	 * refiltering.
	 */
	protected void applyFilter() {

		final ItemsFilter newFilter = createFilter();

		// don't apply filtering for patterns which mean the same, for example:
		// *a**b and ***a*b
		if (filter != null && filter.equalsFilter(newFilter)) {
			return;
		}

		filterHistoryJob.cancel();
		filterJob.cancel();

		filter = newFilter;

		if (filter != null) {
			filterHistoryJob.schedule();
		}
	}

//[gyrex]	we don't need handler and services
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	@Override
	public boolean close() {
		filterJob.cancel();
		refreshCacheJob.cancel();
		refreshProgressMessageJob.cancel();
//		if (showViewHandler != null) {
//			final IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
//			service.deactivateHandler(showViewHandler);
//			showViewHandler.getHandler().dispose();
//			showViewHandler = null;
//		}
		if (menuManager != null) {
			menuManager.dispose();
		}
		if (contextMenuManager != null) {
			contextMenuManager.dispose();
		}
//		storeDialog(getDialogSettings());

		final boolean closed = super.close();
		if (closed) {
			final DialogCallback callback = callbackRef.getAndSet(null);
			if (null != callback) {
				callback.dialogClosed(getReturnCode());
			}
		}
		return closed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void computeResult() {

		final List<Object> selectedElements = ((StructuredSelection) list.getSelection()).toList();

		final List<Object> objectsToReturn = new ArrayList<Object>();

		Object item = null;

		for (final Iterator<Object> it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();

			if (!(item instanceof ItemsListSeparator)) {
				accessedHistoryItem(item);
				objectsToReturn.add(item);
			}
		}

		setResult(objectsToReturn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#create()
	 */
	@Override
	public void create() {
		super.create();
		pattern.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);

		final Composite content = new Composite(dialogArea, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		content.setLayoutData(gd);

		final GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		content.setLayout(layout);

// RAP [rh] workaround "unused variable" compile error
//		final Label headerLabel = createHeader(content);
		createHeader(content);

		pattern = new Text(content, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
// RAP [rh] Accessibility API missing
//		pattern.getAccessible().addAccessibleListener(new AccessibleAdapter() {
//			public void getName(AccessibleEvent e) {
//				e.result = LegacyActionTools.removeMnemonics(headerLabel
//						.getText());
//			}
//		});
		gd = new GridData(GridData.FILL_HORIZONTAL);
		pattern.setLayoutData(gd);

// RAP [rh] wor around "unused variable" compile error    
//		final Label listLabel = createLabels(content);
		createLabels(content);

		list = new TableViewer(content, (multi ? SWT.MULTI : SWT.SINGLE) | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
// RAP [rh] Accessibility API missing
//		list.getTable().getAccessible().addAccessibleListener(
//				new AccessibleAdapter() {
//					public void getName(AccessibleEvent e) {
//						e.result = LegacyActionTools.removeMnemonics(listLabel
//								.getText());
//					}
//				});
		list.setContentProvider(contentProvider);
		list.setLabelProvider(getItemsListLabelProvider());
		list.setInput(new Object[0]);
		list.setItemCount(contentProvider.getNumberOfElements());
		gd = new GridData(GridData.FILL_BOTH);
		applyDialogFont(list.getTable());
		gd.heightHint = list.getTable().getItemHeight() * 15;
		list.getTable().setLayoutData(gd);

		createPopupMenu();

		pattern.addModifyListener(new ModifyListener() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void modifyText(final ModifyEvent e) {
				applyFilter();
			}
		});

// RAP [rh] Key events missing		
//		pattern.addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//				if (e.keyCode == SWT.ARROW_DOWN) {
//					if (list.getTable().getItemCount() > 0) {
//						list.getTable().setFocus();
//					}
//				}
//			}
//		});

		list.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				final StructuredSelection selection = (StructuredSelection) event.getSelection();
				handleSelected(selection);
			}
		});

		list.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

// RAP [rh] missing key events
//		list.getTable().addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//
//				if (e.keyCode == SWT.DEL) {
//
//					List selectedElements = ((StructuredSelection) list
//							.getSelection()).toList();
//
//					Object item = null;
//					boolean isSelectedHistory = true;
//
//					for (Iterator it = selectedElements.iterator(); it
//							.hasNext();) {
//						item = it.next();
//						if (item instanceof ItemsListSeparator
//								|| !isHistoryElement(item)) {
//							isSelectedHistory = false;
//							break;
//						}
//					}
//					if (isSelectedHistory)
//						removeSelectedItems(selectedElements);
//
//				}
//
//				if (e.keyCode == SWT.ARROW_UP && (e.stateMask & SWT.SHIFT) != 0
//						&& (e.stateMask & SWT.CTRL) != 0) {
//					StructuredSelection selection = (StructuredSelection) list
//							.getSelection();
//
//					if (selection.size() == 1) {
//						Object element = selection.getFirstElement();
//						if (element.equals(list.getElementAt(0))) {
//							pattern.setFocus();
//						}
//						if (list.getElementAt(list.getTable()
//								.getSelectionIndex() - 1) instanceof ItemsListSeparator)
//							list.getTable().setSelection(
//									list.getTable().getSelectionIndex() - 1);
//						list.getTable().notifyListeners(SWT.Selection,
//								new Event());
//
//					}
//				}
//
//				if (e.keyCode == SWT.ARROW_DOWN
//						&& (e.stateMask & SWT.SHIFT) != 0
//						&& (e.stateMask & SWT.CTRL) != 0) {
//
//					if (list
//							.getElementAt(list.getTable().getSelectionIndex() + 1) instanceof ItemsListSeparator)
//						list.getTable().setSelection(
//								list.getTable().getSelectionIndex() + 1);
//					list.getTable().notifyListeners(SWT.Selection, new Event());
//				}
//
//			}
//		});

		createExtendedContentArea(content);

		details = new DetailsContentViewer(content, SWT.BORDER | SWT.FLAT);
		details.setVisible(toggleStatusLineAction.isChecked());
		details.setContentProvider(new NullContentProvider());
		details.setLabelProvider(getDetailsLabelProvider());

		applyDialogFont(content);

//		restoreDialog(getDialogSettings());

		if (initialPatternText != null) {
			pattern.setText(initialPatternText);
		}

		switch (selectionMode) {
			case CARET_BEGINNING:
				pattern.setSelection(0, 0);
				break;
			case FULL_SELECTION:
				pattern.setSelection(0, initialPatternText.length());
				break;
		}

		// apply filter even if pattern is empty (display history)
		applyFilter();

		return dialogArea;
	}

	/**
	 * Creates an extra content area, which will be located above the details.
	 * 
	 * @param parent
	 *            parent to create the dialog widgets in
	 * @return an extra content area
	 */
	protected abstract Control createExtendedContentArea(Composite parent);

	/**
	 * Creates an instance of a filter.
	 * 
	 * @return a filter for items on the items list. Can be <code>null</code>,
	 *         no filtering will be applied then, causing no item to be shown in
	 *         the list.
	 */
	protected abstract ItemsFilter createFilter();

	/**
	 * Create a new header which is labelled by headerLabel.
	 * 
	 * @param parent
	 * @return Label the label of the header
	 */
	private Label createHeader(final Composite parent) {
		final Composite header = new Composite(parent, SWT.NONE);

		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		header.setLayout(layout);

		final Label headerLabel = new Label(header, SWT.NONE);
		headerLabel.setText(getMessage() != null && getMessage().trim().length() > 0 ? getMessage() : WidgetMessages.get().FilteredItemsSelectionDialog_patternLabel);
// RAP [rh] Traverse events missing		
//		headerLabel.addTraverseListener(new TraverseListener() {
//			public void keyTraversed(TraverseEvent e) {
//				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
//					e.detail = SWT.TRAVERSE_NONE;
//					pattern.setFocus();
//				}
//			}
//		});

		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		headerLabel.setLayoutData(gd);

		createViewMenu(header);
		header.setLayoutData(gd);
		return headerLabel;
	}

	/**
	 * Create the labels for the list and the progress. Return the list label.
	 * 
	 * @param parent
	 * @return Label
	 */
	private Label createLabels(final Composite parent) {
		final Composite labels = new Composite(parent, SWT.NONE);

		final GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		labels.setLayout(layout);

		final Label listLabel = new Label(labels, SWT.NONE);
		listLabel.setText(WidgetMessages.get().FilteredItemsSelectionDialog_listLabel);

// RAP [rh] Traverse events missing
//		listLabel.addTraverseListener(new TraverseListener() {
//			public void keyTraversed(TraverseEvent e) {
//				if (e.detail == SWT.TRAVERSE_MNEMONIC && e.doit) {
//					e.detail = SWT.TRAVERSE_NONE;
//					list.getTable().setFocus();
//				}
//			}
//		});

		final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		listLabel.setLayoutData(gd);

		progressLabel = new Label(labels, SWT.RIGHT);
		progressLabel.setLayoutData(gd);

		labels.setLayoutData(gd);
		return listLabel;
	}

	private void createPopupMenu() {
		removeHistoryItemAction = new RemoveHistoryItemAction();
		removeHistoryActionContributionItem = new ActionContributionItem(removeHistoryItemAction);

		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void menuAboutToShow(final IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		final Table table = list.getTable();
		final Menu menu = contextMenuManager.createContextMenu(table);
		table.setMenu(menu);
	}

	private void createViewMenu(final Composite parent) {
		toolBar = new ToolBar(parent, SWT.FLAT);
		toolItem = new ToolItem(toolBar, SWT.PUSH, 0);

		final GridData data = new GridData();
		data.horizontalAlignment = GridData.END;
		toolBar.setLayoutData(data);

		toolBar.addMouseListener(new MouseAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseDown(final MouseEvent e) {
				showViewMenu();
			}
		});

		toolItem.setImage(AdminUiActivator.getInstance().getImageRegistry().get(AdminUiImages.IMG_VIEW_MENU));
		toolItem.setToolTipText(WidgetMessages.get().FilteredItemsSelectionDialog_menu);
		toolItem.addSelectionListener(new SelectionAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent e) {
				showViewMenu();
			}
		});

		menuManager = new MenuManager();

		fillViewMenu(menuManager);

//[gyrex] we don't need handler and services in gyrex

//		final IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
//		final IHandler handler = new AbstractHandler() {
////			public Object execute(final ExecutionEvent event) {
////				showViewMenu();
////				return null;
////			}
//
//			@Override
//			public Object execute(final ExecutionEvent event) throws ExecutionException {
//				showViewMenu();
//				return null;
//			}
//		};
//		showViewHandler = service.activateHandler(IWorkbenchCommandConstants.WINDOW_SHOW_VIEW_MENU, handler, new ActiveShellExpression(getShell()));
	}

	/**
	 * Fills the content provider with matching items.
	 * 
	 * @param contentProvider
	 *            collector to add items to.
	 *            {@link FilteredItemsSelectionDialog.AbstractContentProvider#add(Object, FilteredItemsSelectionDialog.ItemsFilter)}
	 *            only adds items that pass the given <code>itemsFilter</code>.
	 * @param itemsFilter
	 *            the items filter
	 * @param progressMonitor
	 *            must be used to report search progress. The state of this
	 *            progress monitor reflects the state of the filtering process.
	 * @throws CoreException
	 */
	protected abstract void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException;

	/**
	 * Hook that allows to add actions to the context menu.
	 * <p>
	 * Subclasses may extend in order to add other actions.
	 * </p>
	 * 
	 * @param menuManager
	 *            the context menu manager
	 * @since 1.4
	 */
	@SuppressWarnings("unchecked")
	protected void fillContextMenu(final IMenuManager menuManager) {
		final List<Object> selectedElements = ((StructuredSelection) list.getSelection()).toList();

		Object item = null;

		for (final Iterator<Object> it = selectedElements.iterator(); it.hasNext();) {
			item = it.next();
			if (item instanceof ItemsListSeparator || !isHistoryElement(item)) {
				return;
			}
		}

		if (selectedElements.size() > 0) {
			removeHistoryItemAction.setText(WidgetMessages.get().FilteredItemsSelectionDialog_removeItemsFromHistoryAction);

			menuManager.add(removeHistoryActionContributionItem);

		}
	}

	/**
	 * Fills the menu of the dialog.
	 * 
	 * @param menuManager
	 *            the menu manager
	 */
	protected void fillViewMenu(final IMenuManager menuManager) {
		toggleStatusLineAction = new ToggleStatusLineAction();
		menuManager.add(toggleStatusLineAction);
	}

	private ILabelProvider getDetailsLabelProvider() {
		if (detailsLabelProvider == null) {
			detailsLabelProvider = new LabelProvider();
		}
		return detailsLabelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		final IDialogSettings settings = getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_BOUNDS_SETTINGS);
		if (section == null) {
			section = settings.addNewSection(DIALOG_BOUNDS_SETTINGS);
			section.put(DIALOG_HEIGHT, 500);
			section.put(DIALOG_WIDTH, 600);
		}
		return section;
	}

	/**
	 * Returns the dialog settings. Returned object can't be null.
	 * 
	 * @return return dialog settings for this dialog
	 */
	protected abstract IDialogSettings getDialogSettings();

	/**
	 * Returns name for then given object.
	 * 
	 * @param item
	 *            an object from the content provider. Subclasses should pay
	 *            attention to the passed argument. They should either only pass
	 *            objects of a known type (one used in content provider) or make
	 *            sure that passed parameter is the expected one (by type
	 *            checking like <code>instanceof</code> inside the method).
	 * @return name of the given item
	 */
	public abstract String getElementName(Object item);

	/**
	 * Returns a history comparator.
	 * 
	 * @return decorated comparator
	 */
	@SuppressWarnings("unchecked")
	private Comparator<Object> getHistoryComparator() {
		return new HistoryComparator<Object>();
	}

	/**
	 * Gets initial pattern.
	 * 
	 * @return initial pattern, or <code>null</code> if initial pattern is not
	 *         set
	 */
	protected String getInitialPattern() {
		return initialPatternText;
	}

	/**
	 * Returns comparator to sort items inside content provider. Returned object
	 * will be probably created as an anonymous class. Parameters passed to the
	 * <code>compare(java.lang.Object, java.lang.Object)</code> are going to be
	 * the same type as the one used in the content provider.
	 * 
	 * @return comparator to sort items content provider
	 */
	protected abstract Comparator<Object> getItemsComparator();

	/**
	 * Returns the item list label provider.
	 * 
	 * @return the item list label provider
	 */
	private ItemsListLabelProvider getItemsListLabelProvider() {
		if (itemsListLabelProvider == null) {
			itemsListLabelProvider = new ItemsListLabelProvider(new LabelProvider(), null);
		}
		return itemsListLabelProvider;
	}

	/**
	 * Returns the label decorator for selected items in the list.
	 * 
	 * @return the label decorator for selected items in the list
	 */
	private ILabelDecorator getListSelectionLabelDecorator() {
		return getItemsListLabelProvider().getSelectionDecorator();
	}

	/**
	 * Get the control where the search pattern is entered. Any filtering should
	 * be done using an {@link ItemsFilter}. This control should only be
	 * accessed for listeners that wish to handle events that do not affect
	 * filtering such as custom traversal.
	 * 
	 * @return Control or <code>null</code> if the pattern control has not been
	 *         created.
	 */
	public Control getPatternControl() {
		return pattern;
	}

	/**
	 * Returns the current selection.
	 * 
	 * @return the current selection
	 */
	@SuppressWarnings("unchecked")
	protected StructuredSelection getSelectedItems() {

		final StructuredSelection selection = (StructuredSelection) list.getSelection();

		final List<Object> selectedItems = selection.toList();
		Object itemToRemove = null;

		for (final Iterator it = selection.iterator(); it.hasNext();) {
			final Object item = it.next();
			if (item instanceof ItemsListSeparator) {
				itemToRemove = item;
				break;
			}
		}

		if (itemToRemove == null) {
			return new StructuredSelection(selectedItems);
		}
		// Create a new selection without the collision
		final List<Object> newItems = new ArrayList<Object>(selectedItems);
		newItems.remove(itemToRemove);
		return new StructuredSelection(newItems);

	}

	/**
	 * Returns the history of selected elements.
	 * 
	 * @return history of selected elements, or <code>null</code> if it is not
	 *         set
	 */
	protected SelectionHistory getSelectionHistory() {
		return contentProvider.getSelectionHistory();
	}

	/**
	 * This method is a hook for subclasses to override default dialog behavior.
	 * The <code>handleDoubleClick()</code> method handles double clicks on the
	 * list of filtered elements.
	 * <p>
	 * Current implementation makes double-clicking on the list do the same as
	 * pressing <code>OK</code> button on the dialog.
	 */
	protected void handleDoubleClick() {
		okPressed();
	}

	/**
	 * Handle selection in the items list by updating labels of selected and
	 * unselected items and refresh the details field using the selection.
	 * 
	 * @param selection
	 *            the new selection
	 */
	@SuppressWarnings("unchecked")
	protected void handleSelected(final StructuredSelection selection) {
		IStatus status = new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);

		final Object[] lastSelection = currentSelection;

		currentSelection = selection.toArray();

		if (selection.size() == 0) {
			status = new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, EMPTY_STRING, null);

			if (lastSelection != null && getListSelectionLabelDecorator() != null) {
				list.update(lastSelection, null);
			}

			currentSelection = null;

		} else {
			status = new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, IStatus.ERROR, EMPTY_STRING, null);

			final List<Object> items = selection.toList();

			Object item = null;
			IStatus tempStatus = null;

			for (final Object o : items) {
				if (o instanceof ItemsListSeparator) {
					continue;
				}

				item = o;
				tempStatus = validateItem(item);

				if (tempStatus.isOK()) {
					status = new Status(IStatus.OK, AdminUiActivator.SYMBOLIC_NAME, IStatus.OK, EMPTY_STRING, null);
				} else {
					status = tempStatus;
					// if any selected element is not valid status is set to
					// ERROR
					break;
				}
			}

			if (lastSelection != null && getListSelectionLabelDecorator() != null) {
				list.update(lastSelection, null);
			}

			if (getListSelectionLabelDecorator() != null) {
				list.update(currentSelection, null);
			}
		}

		refreshDetails();
		updateStatus(status);
	}

	/**
	 * Indicates whether the given item is a duplicate.
	 * 
	 * @param item
	 *            the item to be investigated
	 * @return <code>true</code> if the item is duplicate, <code>false</code>
	 *         otherwise
	 */
	public boolean isDuplicateElement(final Object item) {
		return contentProvider.isDuplicateElement(item);
	}

	/**
	 * Indicates whether the given item is a history item.
	 * 
	 * @param item
	 *            the item to be investigated
	 * @return <code>true</code> if the given item exists in history,
	 *         <code>false</code> otherwise
	 */
	public boolean isHistoryElement(final Object item) {
		return contentProvider.isHistoryElement(item);
	}

	/*
	 * @see Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (status != null && (status.isOK() || status.getCode() == IStatus.INFO)) {
			super.okPressed();
		}
	}

	/**
	 * Opens this window, creating it first if it has not yet been created.
	 * <p>
	 * The window will be configured to not block on open. The specified
	 * callback will be set and (if not <code>null</code>) will be called when
	 * the windows is closed. Clients may use {@link #getReturnCode()} to obtain
	 * the return code that {@link #open()} returns in blocking mode.
	 * </p>
	 * 
	 * @see #create()
	 */
	@Override
	public void openNonBlocking(final DialogCallback callback) {
		if (!callbackRef.compareAndSet(null, callback)) {
			throw new IllegalStateException("Concurrent operation not supported!");
		}

		setBlockOnOpen(false);
		super.open();
	}

	/**
	 * Refreshes the dialog - has to be called in UI thread.
	 */
	@SuppressWarnings("unchecked")
	public void refresh() {
		if (list != null && !list.getTable().isDisposed()) {

			final List<Object> lastRefreshSelection = ((StructuredSelection) list.getSelection()).toList();
			list.getTable().deselectAll();

			list.setItemCount(contentProvider.getNumberOfElements());
			list.refresh();

			if (list.getTable().getItemCount() > 0) {
				// preserve previous selection
				if (refreshWithLastSelection && lastRefreshSelection != null && lastRefreshSelection.size() > 0) {
					list.setSelection(new StructuredSelection(lastRefreshSelection));
				} else {
					refreshWithLastSelection = true;
					list.getTable().setSelection(0);
					list.getTable().notifyListeners(SWT.Selection, new Event());
				}
			} else {
				list.setSelection(StructuredSelection.EMPTY);
			}

		}

		scheduleProgressMessageRefresh();
	}

	/**
	 * Refreshes the details field according to the current selection in the
	 * items list.
	 */
	private void refreshDetails() {
		final StructuredSelection selection = getSelectedItems();

		switch (selection.size()) {
			case 0:
				details.setInput(null);
				break;
			case 1:
				details.setInput(selection.getFirstElement());
				break;
			default:
				details.setInput(NLS.bind(WidgetMessages.get().FilteredItemsSelectionDialog_nItemsSelected, new Integer(selection.size())));
				break;
		}

	}

	/**
	 * Notifies the content provider - fires filtering of content provider
	 * elements. During the filtering, a separator between history and workspace
	 * matches is added.
	 * <p>
	 * This is a long running operation and should be called in a job.
	 * 
	 * @param checkDuplicates
	 *            <code>true</code> if data concerning elements duplication
	 *            should be computed - it takes much more time than the standard
	 *            filtering
	 * @param monitor
	 *            a progress monitor or <code>null</code> if no monitor is
	 *            available
	 */
	public void reloadCache(final boolean checkDuplicates, final IProgressMonitor monitor) {
		if (list != null && !list.getTable().isDisposed() && contentProvider != null) {
			contentProvider.reloadCache(checkDuplicates, monitor);
		}
	}

	/**
	 * Removes an item from history.
	 * 
	 * @param item
	 *            an item to remove
	 * @return removed item
	 */
	protected Object removeHistoryItem(final Object item) {
		return contentProvider.removeHistoryElement(item);
	}

	/**
	 * Removes selected items from history.
	 * 
	 * @param items
	 *            items to be removed
	 */
	private void removeSelectedItems(final List<Object> items) {
		for (final Object item : items) {
			removeHistoryItem(item);
		}
		refreshWithLastSelection = false;
		contentProvider.refresh();
	}

//[gyrex]	we don't need history with memento in gyrex
	/**
	 * Restores dialog using persisted settings. The default implementation
	 * restores the status of the details line and the selection history.
	 * 
	 * @param settings
	 *            settings used to restore dialog
	 */
//	protected void restoreDialog(final IDialogSettings settings) {
//		boolean toggleStatusLine = true;
//
//		if (settings.get(SHOW_STATUS_LINE) != null) {
//			toggleStatusLine = settings.getBoolean(SHOW_STATUS_LINE);
//		}
//
//		toggleStatusLineAction.setChecked(toggleStatusLine);
//
//		details.setVisible(toggleStatusLine);
//
//		final String setting = settings.get(HISTORY_SETTINGS);
//		if (setting != null) {
////			try {
//////				IMemento memento = XMLMemento.createReadRoot(new StringReader(setting));
//////				contentProvider.loadHistory(memento);
////			} catch (WorkbenchException e) {
////				// Simply don't restore the settings
////				StatusManager.getManager().handle(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, FilteredItemsSelectionDialog_restoreError, e));
////			}
//		}
//	}

	/**
	 * Schedules progress message refresh.
	 */
	public void scheduleProgressMessageRefresh() {
		if (filterJob.getState() != Job.RUNNING && refreshProgressMessageJob.getState() != Job.RUNNING) {
			refreshProgressMessageJob.scheduleProgressRefresh(null);
		}
	}

	// RAP [bm] not used
//	private static boolean showColoredLabels() {
//		return PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.USE_COLORED_LABELS);
//	}

	/**
	 * Schedule refresh job.
	 */
	public void scheduleRefresh() {
// RAP [rh] fake context	  
		UICallBack.runNonUIThreadWithFakeContext(display, new Runnable() {
			@Override
			public void run() {
				refreshCacheJob.cancelAll();
				refreshCacheJob.schedule();
			}
		});
	}

	/**
	 * Sets label provider for the details field. For a single selection, the
	 * element sent to {@link ILabelProvider#getImage(Object)} and
	 * {@link ILabelProvider#getText(Object)} is the selected object, for
	 * multiple selection a {@link String} with amount of selected items is the
	 * element.
	 * 
	 * @see #getSelectedItems() getSelectedItems() can be used to retrieve
	 *      selected items and get the items count.
	 * @param detailsLabelProvider
	 *            the label provider for the details field
	 */
	public void setDetailsLabelProvider(final ILabelProvider detailsLabelProvider) {
		this.detailsLabelProvider = detailsLabelProvider;
		if (details != null) {
			details.setLabelProvider(detailsLabelProvider);
		}
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. A full selection is used in the pattern
	 * input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @see FilteredItemsSelectionDialog#FULL_SELECTION
	 */
	public void setInitialPattern(final String text) {
		setInitialPattern(text, FULL_SELECTION);
	}

	/**
	 * Sets the initial pattern used by the filter. This text is copied into the
	 * selection input on the dialog. The <code>selectionMode</code> is used to
	 * choose selection type for the input field.
	 * 
	 * @param text
	 *            initial pattern for the filter
	 * @param selectionMode
	 *            one of: {@link FilteredItemsSelectionDialog#NONE},
	 *            {@link FilteredItemsSelectionDialog#CARET_BEGINNING},
	 *            {@link FilteredItemsSelectionDialog#FULL_SELECTION}
	 */
	public void setInitialPattern(final String text, final int selectionMode) {
		initialPatternText = text;
		this.selectionMode = selectionMode;
	}

	// RAP [rh] JavaDoc: IStyledLabelProvider not supported
	/**
	 * Sets a new label provider for items in the list. <!-- If the label
	 * provider also implements
	 * {@link org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider}
	 * , the style text labels provided by it will be used. -->
	 * 
	 * @param listLabelProvider
	 *            the label provider for items in the list
	 */
	public void setListLabelProvider(final ILabelProvider listLabelProvider) {
		getItemsListLabelProvider().setProvider(listLabelProvider);
	}

	/**
	 * Sets the label decorator for selected items in the list.
	 * 
	 * @param listSelectionLabelDecorator
	 *            the label decorator for selected items in the list
	 */
	public void setListSelectionLabelDecorator(final ILabelDecorator listSelectionLabelDecorator) {
		getItemsListLabelProvider().setSelectionDecorator(listSelectionLabelDecorator);
	}

	/**
	 * Sets new history.
	 * 
	 * @param selectionHistory
	 *            the history
	 */
	protected void setSelectionHistory(final SelectionHistory selectionHistory) {
		if (contentProvider != null) {
			contentProvider.setSelectionHistory(selectionHistory);
		}
	}

	/**
	 * Sets separator label
	 * 
	 * @param separatorLabel
	 *            the label showed on separator
	 */
	public void setSeparatorLabel(final String separatorLabel) {
		itemsListSeparator = new ItemsListSeparator(separatorLabel);
	}

	private void showViewMenu() {
		final Menu menu = menuManager.createContextMenu(getShell());
		final Rectangle bounds = toolItem.getBounds();
		Point topLeft = new Point(bounds.x, bounds.y + bounds.height);
		topLeft = toolBar.toDisplay(topLeft);
		menu.setLocation(topLeft.x, topLeft.y);
		menu.setVisible(true);
	}

//[gyrex]	we don't need history with memento in gyrex
	/**
	 * Stores dialog settings.
	 * 
	 * @param settings
	 *            settings used to store dialog
	 */
//	protected void storeDialog(final IDialogSettings settings) {
//		settings.put(SHOW_STATUS_LINE, toggleStatusLineAction.isChecked());
//
////		XMLMemento memento = XMLMemento.createWriteRoot(HISTORY_SETTINGS);
////		contentProvider.saveHistory(memento);
//		final StringWriter writer = new StringWriter();
////		try {
////			memento.save(writer);
//		settings.put(HISTORY_SETTINGS, writer.getBuffer().toString());
////		} catch (IOException e) {
////			// Simply don't store the settings
////			StatusManager.getManager().handle(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, FilteredItemsSelectionDialog_storeError, e));
////		}
//	}

	/**
	 * Updates the progress label.
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void updateProgressLabel() {
		scheduleProgressMessageRefresh();
	}

	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#updateStatus(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	protected void updateStatus(final IStatus status) {
		this.status = status;
		super.updateStatus(status);
	}

	/**
	 * Validates the item. When items on the items list are selected or
	 * deselected, it validates each item in the selection and the dialog status
	 * depends on all validations.
	 * 
	 * @param item
	 *            an item to be checked
	 * @return status of the dialog to be set
	 */
	protected abstract IStatus validateItem(Object item);

}
