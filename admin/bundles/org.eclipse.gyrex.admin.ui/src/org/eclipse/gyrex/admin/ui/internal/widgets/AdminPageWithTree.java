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
package org.eclipse.gyrex.admin.ui.internal.widgets;

import org.eclipse.gyrex.admin.ui.internal.application.AdminUiUtil;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.lang.StringUtils;

public abstract class AdminPageWithTree extends AdminPage {
	private final class ChangeSortColumnListener extends SelectionAdapter {
		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private final SortableColumnsComparator comparator;
		private final TreeViewerColumn column;
		private final int sortIndex;

		private ChangeSortColumnListener(final SortableColumnsComparator comparator, final int sortIndex, final TreeViewerColumn column) {
			this.comparator = comparator;
			this.sortIndex = sortIndex;
			this.column = column;
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			if (comparator.getColumn() == sortIndex) {
				comparator.setReverse(!comparator.isReverse());
			} else {
				comparator.setColumn(sortIndex);
				treeViewer.getTree().setSortColumn(column.getColumn());
			}
			treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);
			treeViewer.refresh();
		}
	}

	private final class SortableColumnsComparator extends ViewerComparator {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		private int column;
		private boolean reverse;

		public SortableColumnsComparator() {
			super();
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compare(final Viewer viewer, final Object e1, final Object e2) {
			final String t1 = StringUtils.trimToEmpty(getElementLabel(e1, getColumn()));
			final String t2 = StringUtils.trimToEmpty(getElementLabel(e2, getColumn()));
			if (isReverse())
				return getComparator().compare(t2, t1);
			else
				return getComparator().compare(t1, t2);
		}

		public int getColumn() {
			return column;
		}

		public boolean isReverse() {
			return reverse;
		}

		public void setColumn(final int column) {
			this.column = column;

			// setting an index resets the reverse flag
			reverse = false;
		}

		public void setReverse(final boolean reverse) {
			this.reverse = reverse;
		}

	}

	private final int numberOfColumns;

	private Composite composite;
	private TreeViewer treeViewer;
	private ISelectionChangedListener updateButtonsListener;

	public AdminPageWithTree(final int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	@Override
	public void activate() {
		super.activate();

		if (treeViewer == null)
			return;

		treeViewer.setInput(getViewerInput());
		updateButtonsListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(final SelectionChangedEvent event) {
				updateButtons();
			}
		};
		treeViewer.addSelectionChangedListener(updateButtonsListener);

		refresh();
	}

	protected abstract void createButtons(final Composite parent);

	protected abstract ITreeContentProvider createContentProvider();

	@Override
	public Control createControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		final Control header = createHeader(composite);
		if (header != null) {
			final GridData gd = AdminUiUtil.createHorzFillData();
			gd.verticalIndent = 10;
			header.setLayoutData(gd);
			if ((composite.getChildren().length != 1) || (header != composite.getChildren()[0]))
				throw new IllegalStateException("Please wrap header into its own composite!");
		}

		final Composite treeContainerWithButtons = new Composite(composite, SWT.NONE);
		final GridData gd = AdminUiUtil.createFillData();
		gd.verticalIndent = 10;
		treeContainerWithButtons.setLayoutData(gd);
		treeContainerWithButtons.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(2, false));

		final Control filteredTree = createTree(treeContainerWithButtons);
		filteredTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite buttons = new Composite(treeContainerWithButtons, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, true));
		buttons.setLayout(new RowLayout(SWT.VERTICAL));
		createButtons(buttons);

		return composite;
	}

	protected abstract Control createHeader(Composite parent);

	protected PatternFilter createPatternFilter() {
		return new PatternFilter();
	}

	protected FilteredTree createTree(final Composite parent) {
		final FilteredTree filteredTree = new FilteredTree(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, createPatternFilter(), true);

		treeViewer = filteredTree.getViewer();
		treeViewer.getTree().setHeaderVisible(true);
		final TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(50, 50));
		layout.addColumnData(new ColumnWeightData(60, 50));
		layout.addColumnData(new ColumnWeightData(30, 50));
		treeViewer.getTree().setLayout(layout);
		treeViewer.setUseHashlookup(true);
		treeViewer.setContentProvider(createContentProvider());
		final SortableColumnsComparator comparator = new SortableColumnsComparator();
		treeViewer.setComparator(comparator);
		treeViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(final OpenEvent event) {
				openSelectedElement();
			}
		});

		// create columns
		final int columns = getNumberOfColumns();
		for (int i = 0; i < columns; i++) {
			final int column = i;
			final TreeViewerColumn viewerColumn = new TreeViewerColumn(treeViewer, SWT.LEFT);
			viewerColumn.getColumn().setText(getColumnLabel(column));
			viewerColumn.setLabelProvider(new ColumnLabelProvider() {
				/** serialVersionUID */
				private static final long serialVersionUID = 1L;

				@Override
				public String getText(final Object element) {
					return getElementLabel(element, column);
				}
			});

			if (isColumnSortable(column)) {
				viewerColumn.getColumn().addSelectionListener(new ChangeSortColumnListener(comparator, column, viewerColumn));
				treeViewer.getTree().setSortColumn(viewerColumn.getColumn());
				treeViewer.getTree().setSortDirection(comparator.isReverse() ? SWT.UP : SWT.DOWN);
			}
		}

		return filteredTree;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		if (treeViewer != null) {
			if (updateButtonsListener != null) {
				treeViewer.removeSelectionChangedListener(updateButtonsListener);
				updateButtonsListener = null;
			}
			if (treeViewer.getTree().isDisposed()) {
				treeViewer.setInput(null);
			}
		}
	}

	protected abstract String getColumnLabel(final int column);

	protected abstract String getElementLabel(final Object element, final int column);

	protected String getElementTextForSorting(final Object element, final int column) {
		return getElementLabel(element, column);
	}

	protected final int getNumberOfColumns() {
		return numberOfColumns;
	}

	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	protected abstract Object getViewerInput();

	final void internalRefresh() {
		refresh();
		treeViewer.refresh();
		updateButtons();
	}

	protected abstract boolean isColumnSortable(final int column);

	protected void openSelectedElement() {
		// no-op
	}

	protected void refresh() {
		// no-op
	}

	protected abstract void updateButtons();

}