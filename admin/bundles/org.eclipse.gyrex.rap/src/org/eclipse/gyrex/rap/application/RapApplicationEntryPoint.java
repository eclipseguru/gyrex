/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.rap.application;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.rap.internal.RapActivator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Policy;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.BrowserNavigationEvent;
import org.eclipse.rap.rwt.client.service.BrowserNavigationListener;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for a Gyrex RAP application.
 * <p>
 * Clients my extend this class and customize it to suite their needs.
 * </p>
 */
public class RapApplicationEntryPoint implements EntryPoint {

	private static Label createHeadlineLabel(final Composite parent, final String text) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(text.replace("&", "&&"));
		label.setData(RWT.CUSTOM_VARIANT, "pageHeadline");
		return label;
	}

	private static FormData createLogoFormData(final Image logo) {
		final FormData data = new FormData();
		data.left = new FormAttachment(0);
		data.top = new FormAttachment(0);
		return data;
	}

	private static FormData createNavBarFormData() {
		final FormData data = new FormData();
		data.bottom = new FormAttachment(100, 5);
		data.right = new FormAttachment(100, 0);
		return data;
	}

	private static void makeLink(final Label control, final String url) {
		control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		control.addMouseListener(new MouseAdapter() {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			public void mouseDown(final MouseEvent e) {
				final JavaScriptExecutor service = RWT.getClient().getService(JavaScriptExecutor.class);
				if (service != null) {
					service.execute("window.location.href = '" + url + "'");
				}
			}
		});
	}

	private PageProvider pageProvider;

	private static final int CONTENT_MIN_HEIGHT = 800;

	private static final int CENTER_AREA_WIDTH = 998;

	private static final String HISTORY_TOKEN_SEPARATOR = "--";

	private static final String GYREX_WEBSITE_URL = "http://eclipse.org/gyrex/";

	private static final Logger LOG = LoggerFactory.getLogger(RapApplicationEntryPoint.class);

	private final IApplicationService applicationService = new IApplicationService() {
		@Override
		public void openPage(final String pageId, final String... args) {
			RapApplicationEntryPoint.this.open(pageId, args);
		}
	};

	private Composite centerArea;

	private NavigationBar navigation;

	private Composite navBar;

	private final Map<String, Page> pagesById = new HashMap<String, Page>();

	private Page current;

	private Composite filterContainer;

	private Composite headerCenterArea;

	private Image logo;

	private void activate(final Page page, final PageHandle contribution, String[] args) {
		// TODO: should switch to using a StackLayout and not disposing children every time
		// however, disposal might be necessary if input changes
		for (final Control child : centerArea.getChildren()) {
			child.dispose();
		}
		for (final Control child : filterContainer.getChildren()) {
			child.dispose();
		}

		// initialize arguments (allows safe use within this method as well as for API contract)
		if ((args == null) || (args.length == 0)) {
			args = new String[] { contribution.getId() };
		}

		// update page with input
		page.setArguments(args);

		// create history entry
		final String historyText = StringUtils.isNotBlank(page.getTitleToolTip()) ? String.format("%s - %s - Gyrex Admin", contribution.getName(), page.getTitleToolTip()) : String.format("%s - Gyrex Admin", contribution.getName());
		final BrowserNavigation browserNavigation = RWT.getClient().getService(BrowserNavigation.class);
		if (browserNavigation != null) {
			browserNavigation.pushState(StringUtils.join(args, HISTORY_TOKEN_SEPARATOR), historyText);
		}

		// create page
		create(page, contribution, centerArea);

		// re-layout
		headerCenterArea.layout(true, true);
		centerArea.layout(true, true);

		// activate
		page.activate();
	}

	private void attachHistoryListener() {
		final BrowserNavigation browserNavigation = RWT.getClient().getService(BrowserNavigation.class);
		if (browserNavigation != null) {
			browserNavigation.addBrowserNavigationListener(new BrowserNavigationListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void navigated(final BrowserNavigationEvent event) {
					final String[] tokens = StringUtils.splitByWholeSeparator(event.getState(), HISTORY_TOKEN_SEPARATOR);
					final PageHandle contribution = getPageProvider().getPage(tokens[0]);
					if (contribution != null) {
						open(contribution, tokens);
					}
				}
			});
		}
	}

	private void create(final Page page, final PageHandle contribution, final Composite parent) {
		final Composite pageComp = new Composite(parent, SWT.NONE);
		pageComp.setLayout(GridLayoutFactory.fillDefaults().create());

		if (page instanceof FilteredPage) {
			((FilteredPage) page).createFilterControls(filterContainer);
		}

		String title = page.getTitle();
		if (StringUtils.isBlank(title)) {
			title = contribution.getName();
		}
		if (StringUtils.isNotBlank(title)) {
			final Label label = createHeadlineLabel(pageComp, page.getTitle());
			final GridData layoutData = new GridData();
			layoutData.verticalIndent = 30;
//			layoutData.horizontalIndent = DEFAULT_SPACE;
			label.setLayoutData(layoutData);
		}

		final Composite contentComp = new Composite(pageComp, SWT.NONE);
		contentComp.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		contentComp.setLayout(new FillLayout());
		page.createControl(contentComp);
		// sanity check
		for (final Control child : contentComp.getChildren()) {
			if (null != child.getLayoutData()) {
				LOG.warn("Programming error in page {}: child composites ({}) should not make any assumptions about the parent layout!", contribution.getId(), child);
				child.setLayoutData(null);
			}
		}
	}

	private Composite createCenterArea(final Composite parent, final Control topControl, final Control bottomControl) {
		final Composite centerArea = new Composite(parent, SWT.NONE);
		centerArea.setLayout(new FillLayout());
		centerArea.setLayoutData(createCenterAreaFormData(topControl, bottomControl));
		centerArea.setData(RWT.CUSTOM_VARIANT, "centerArea");
		return centerArea;
	}

	private FormData createCenterAreaFormData(final Control topAttachment, final Control bottomAttachment) {
		final FormData data = new FormData();
		data.top = new FormAttachment(topAttachment, 0, SWT.BOTTOM);
		data.bottom = new FormAttachment(bottomAttachment, -10, SWT.TOP);
		data.left = new FormAttachment(50, (-CENTER_AREA_WIDTH / 2) + 10);
		data.width = CENTER_AREA_WIDTH - 10;
		return data;
	}

	private Composite createContent(final ScrolledComposite scrolledArea) {
		final Composite comp = new Composite(scrolledArea, SWT.NONE);
		comp.setLayout(new FormLayout());
		final Composite header = createHeader(comp);
		header.setLayoutData(createHeaderFormData(getLogo().getBounds().height));
		createContentBody(comp, header);
		return comp;
	}

	private void createContentBody(final Composite parent, final Composite header) {

		// FIXME: the separator is a hack to work around missing "border-top" (or -bottom) in RAP (bug 283872)
		final Composite separator = new Composite(parent, SWT.NONE);
		separator.setData(RWT.CUSTOM_VARIANT, "mainContentAreaHeaderSeparator");
		final FormData data = new FormData();
		data.top = new FormAttachment(header, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.height = 2;
		separator.setLayoutData(data);

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setData(RWT.CUSTOM_VARIANT, "mainContentArea");
		composite.setLayout(new FormLayout());
		composite.setLayoutData(createContentBodyFormData(separator));
		final Composite footer = createFooter(composite);
		centerArea = createCenterArea(composite, separator, footer);
	}

	private FormData createContentBodyFormData(final Control topControlToAttachTo) {
		final FormData data = new FormData();
		data.top = new FormAttachment(topControlToAttachTo, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		return data;
	}

	private Composite createFilterContainer(final Composite parent, final Control left) {
		final Composite filterContainer = new Composite(parent, SWT.NONE);
		filterContainer.setData(RWT.CUSTOM_VARIANT, "filter-container");

		final FormData data = new FormData();
		data.bottom = new FormAttachment(100, 5);
		data.left = new FormAttachment(left, 5);
		filterContainer.setLayoutData(data);

		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		filterContainer.setLayout(layout);

		return filterContainer;
	}

	private Composite createFooter(final Composite contentComposite) {
		final Composite footer = new Composite(contentComposite, SWT.NONE);
		footer.setLayout(new FormLayout());
		footer.setData(RWT.CUSTOM_VARIANT, "footer");
		footer.setLayoutData(createFooterFormData());
		footer.setBackgroundMode(SWT.INHERIT_FORCE);
		final Label label = new Label(footer, SWT.NONE);
		label.setData(RWT.CUSTOM_VARIANT, "footerLabel");
		label.setText(getFooterText());
		label.setLayoutData(createFooterLabelFormData(footer));
		return footer;
	}

	private FormData createFooterFormData() {
		final FormData data = new FormData();
		data.left = new FormAttachment(50, -CENTER_AREA_WIDTH / 2);
		data.top = new FormAttachment(100, -40);
		data.bottom = new FormAttachment(100);
		data.width = CENTER_AREA_WIDTH - 10 - 2;
		return data;
	}

	private FormData createFooterLabelFormData(final Composite footer) {
		final FormData data = new FormData();
		data.top = new FormAttachment(50, -10);
		data.right = new FormAttachment(100, -15);
		return data;
	}

	private Composite createHeader(final Composite parent) {
		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setData(RWT.CUSTOM_VARIANT, "header");
		comp.setBackgroundMode(SWT.INHERIT_DEFAULT);
		comp.setLayout(new FormLayout());
		headerCenterArea = createHeaderCenterArea(comp);

		final Label logoLabel = new Label(headerCenterArea, SWT.NONE);
		logoLabel.setImage(getLogo());
		logoLabel.setLayoutData(createLogoFormData(getLogo()));
		makeLink(logoLabel, GYREX_WEBSITE_URL);

//		final Label title = new Label(headerCenterArea, SWT.NONE);
//		title.setText("Admin Console");
//		title.setData(RWT.CUSTOM_VARIANT, "title");
//		final FormData titleFormData = new FormData();
//		titleFormData.bottom = new FormAttachment(100, -18);
//		titleFormData.left = new FormAttachment(logo, 0);
//		title.setLayoutData(titleFormData);

		filterContainer = createFilterContainer(headerCenterArea, logoLabel);
		navigation = createNavigation(headerCenterArea);

		return comp;
	}

	private Composite createHeaderCenterArea(final Composite parent) {
		final Composite headerCenterArea = new Composite(parent, SWT.NONE);
		headerCenterArea.setLayout(new FormLayout());
		headerCenterArea.setLayoutData(createHeaderCenterAreaFormData());
		return headerCenterArea;
	}

	private FormData createHeaderCenterAreaFormData() {
		final FormData data = new FormData();
		data.left = new FormAttachment(50, -CENTER_AREA_WIDTH / 2);
		data.top = new FormAttachment(0);
		data.bottom = new FormAttachment(100);
		data.width = CENTER_AREA_WIDTH;
		return data;
	}

	private FormData createHeaderFormData(final int height) {
		final FormData data = new FormData();
		data.top = new FormAttachment(0);
		data.left = new FormAttachment(0);
		data.right = new FormAttachment(100);
		data.height = height;
		return data;
	}

	private Shell createMainShell(final Display display) {
		final Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setMaximized(true);
		shell.setData(RWT.CUSTOM_VARIANT, "mainshell");
		return shell;
	}

	private NavigationBar createNavigation(final Composite parent) {
		navBar = new Composite(parent, SWT.NONE);
		navBar.setLayoutData(createNavBarFormData());
		navBar.setData(RWT.CUSTOM_VARIANT, "nav-bar");

		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		navBar.setLayout(layout);

		final NavigationBar navigation = new NavigationBar(navBar, getPageProvider()) {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			protected void openPage(final PageHandle page) {
				RapApplicationEntryPoint.this.open(page, null);
			}
		};

		return navigation;
	}

	private ScrolledComposite createScrolledArea(final Composite parent) {
		final ScrolledComposite scrolledComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComp.setMinHeight(CONTENT_MIN_HEIGHT);
		scrolledComp.setMinWidth(CENTER_AREA_WIDTH);
		scrolledComp.setExpandVertical(true);
		scrolledComp.setExpandHorizontal(true);
		return scrolledComp;
	}

	@Override
	public int createUI() {
		final Display display = new Display();
		final Shell shell = createMainShell(display);
		shell.setLayout(new FillLayout());
		final ScrolledComposite scrolledArea = createScrolledArea(shell);
		final Composite content = createContent(scrolledArea);
		scrolledArea.setContent(content);
		attachHistoryListener();
		shell.open();

		openInitial();

		display.disposeExec(new Runnable() {
			@Override
			public void run() {
				if (null != current) {
					deactivate(current);
				}
			}
		});

		return 0;
	}

	private void deactivate(final Page page) {
		page.deactivate();
	}

	private Page get(final PageHandle pageHandle) throws Exception {
		if (!pagesById.containsKey(pageHandle.getId())) {
			final Page page = getPageProvider().createPage(pageHandle);
			page.setApplicationService(applicationService);
			pagesById.put(pageHandle.getId(), page);
		}
		return pagesById.get(pageHandle.getId());
	}

	/**
	 * Returns a text to be displayed in the footer.
	 * <p>
	 * Subclasses should override and return a meaningful footer text.
	 * </p>
	 * <p>
	 * Note, subsequent invocations are expected to return the same text.
	 * Dynamic changes to the page are not supported.
	 * </p>
	 *
	 * @return a footer text
	 */
	protected String getFooterText() {
		return "Gyrex RAP Application";
	}

	/**
	 * Returns the image to be used in the header.
	 * <p>
	 * Subclasses should override and provide a custom logo.
	 * </p>
	 * <p>
	 * Note, subsequent invocations must return the same object. Changing the
	 * logo at runtime is not supported.
	 * </p>
	 *
	 * @return
	 */
	protected Image getLogo() {
		if (logo == null) {
			final ImageDescriptor imageDescriptor = RapActivator.getImageDescriptor("webresources/images/logo.png");
			logo = imageDescriptor.createImage(Display.getCurrent());
		}
		return logo;
	}

	/**
	 * Returns the page provider.
	 *
	 * @return the page provider (never <code>null</code>)
	 * @throws IllegalStateException
	 *             is no page provider was set
	 */
	public PageProvider getPageProvider() {
		final PageProvider provider = pageProvider;
		checkState(provider != null, "No page provioder set. Please set a page provider before using the application.");
		return provider;
	}

	/**
	 * Opens the specified page using the given arguments.
	 * <p>
	 * If the page is already opened, it will be re-loaded.
	 * </p>
	 *
	 * @param contribution
	 * @param args
	 */
	private void open(final PageHandle contribution, final String[] args) {
		try {
			final Page page = get(contribution);
			if (null == page) {
				Policy.getStatusHandler().show(new Status(IStatus.ERROR, RapActivator.SYMBOLIC_NAME, String.format(" '%s' not found!", contribution.getId())), "Error Opening ");
				return;
			}

			if (null != current) {
				// deactivate old page first
				deactivate(current);
			}

			current = page;
			navigation.selectNavigationEntry(contribution);
			activate(page, contribution, args);
		} catch (final Exception | LinkageError | AssertionError e) {
			Policy.getStatusHandler().show(e instanceof CoreException ? ((CoreException) e).getStatus() : new Status(IStatus.ERROR, RapActivator.SYMBOLIC_NAME, String.format("Unable to open page '%s (id %s)'. Please check the server logs.", contribution.getName(), contribution.getId()), e), "Error Opening ");
		}
	}

	void open(final String pageId, final String... args) {
		if (StringUtils.isBlank(pageId))
			throw new IllegalArgumentException("invalid page id");
		final PageHandle contribution = getPageProvider().getPage(pageId);
		if (contribution == null)
			return;

		final String[] argsWithId;
		if (args != null) {
			argsWithId = new String[args.length + 1];
			argsWithId[0] = pageId;
			System.arraycopy(args, 0, argsWithId, 1, args.length);
		} else {
			argsWithId = new String[] { pageId };
		}

		open(contribution, argsWithId);
	}

	private void openInitial() {
		final PageHandle contribution = navigation.findInitialPage();
		if (contribution != null) {
			open(contribution, null);
		}
	}

	/**
	 * Sets the page provider to use.
	 *
	 * @param pageProvider
	 *            the pageProvider to set (must not be <code>null</code>)
	 */
	public void setPageProvider(final PageProvider pageProvider) {
		checkArgument(pageProvider != null, "PageProvider must not be null");
		this.pageProvider = pageProvider;
	}

}