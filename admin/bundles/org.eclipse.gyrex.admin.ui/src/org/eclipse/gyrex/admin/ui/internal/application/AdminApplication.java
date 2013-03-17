/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Mike Tschierschke - initial API and implementation
 *     Gunnar Wagenknecht - rework to new console look (based on RAP Examples)
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.AdminPageRegistry;
import org.eclipse.gyrex.admin.ui.internal.pages.registry.PageContribution;
import org.eclipse.gyrex.admin.ui.pages.AdminPage;
import org.eclipse.gyrex.admin.ui.pages.FilteredAdminPage;
import org.eclipse.gyrex.admin.ui.pages.IAdminUi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

import org.osgi.framework.Version;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class AdminApplication implements EntryPoint, IAdminUi {

	private static final String HISTORY_TOKEN_SEPARATOR = "--";
	private static final String GYREX_WEBSITE_URL = "http://eclipse.org/gyrex/";
	private static final int CONTENT_MIN_HEIGHT = 800;
	private static final int CENTER_AREA_WIDTH = 998;

	private static final Logger LOG = LoggerFactory.getLogger(AdminApplication.class);

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

	public static Image getImage(final Display display, final String path) {
		final ImageDescriptor imageDescriptor = AdminUiActivator.getImageDescriptor("img/" + path);
		return imageDescriptor.createImage(display);
	}

	private static String getVersion() {
		final Version version = AdminUiActivator.getInstance().getBundleVersion();
		final StringBuilder resultBuffer = new StringBuilder(20);
		resultBuffer.append(version.getMajor());
		resultBuffer.append('.');
		resultBuffer.append(version.getMinor());
		resultBuffer.append('.');
		resultBuffer.append(version.getMicro());
		resultBuffer.append(" (Build ");
		resultBuffer.append(version.getQualifier());
		resultBuffer.append(')');
		return resultBuffer.toString();
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

	private Composite centerArea;
	private NavigationBar navigation;
	private Composite navBar;
	private final Map<String, AdminPage> pagesById = new HashMap<String, AdminPage>();
	private AdminPage currentPage;
	private Image logo;
	private Composite filterContainer;
	private Composite headerCenterArea;

	private void activate(final AdminPage page, final PageContribution contribution, String[] args) {
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
		createPage(page, contribution, centerArea);

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
					final PageContribution contribution = AdminPageRegistry.getInstance().getPage(tokens[0]);
					if (contribution != null) {
						openPage(contribution, tokens);
					}
				}
			});
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
		logo = getImage(scrolledArea.getDisplay(), "gyrex/gyrex-juno.png");
		final Composite comp = new Composite(scrolledArea, SWT.NONE);
		comp.setLayout(new FormLayout());
		final Composite header = createHeader(comp);
		header.setLayoutData(createHeaderFormData(logo.getBounds().height));
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
		label.setText("Admin Console " + getVersion());
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
		logoLabel.setImage(logo);
		logoLabel.setLayoutData(createLogoFormData(logo));
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

		final NavigationBar navigation = new NavigationBar(navBar) {
			/** serialVersionUID */
			private static final long serialVersionUID = 1L;

			@Override
			protected void openPage(final PageContribution page) {
				AdminApplication.this.openPage(page, null);
			}
		};

		return navigation;
	}

	private void createPage(final AdminPage page, final PageContribution contribution, final Composite parent) {
		final Composite pageComp = new Composite(parent, SWT.NONE);
		pageComp.setLayout(AdminUiUtil.createGridLayoutWithoutMargin(1, false));

		if (page instanceof FilteredAdminPage) {
			((FilteredAdminPage) page).createFilterControls(filterContainer);
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
		contentComp.setLayoutData(AdminUiUtil.createFillData());
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

		openInitialPage();

		display.disposeExec(new Runnable() {
			@Override
			public void run() {
				if (null != currentPage) {
					deactivate(currentPage);
				}
			}
		});

		return 0;
	}

	private void deactivate(final AdminPage page) {
		page.deactivate();
	}

	private AdminPage getPage(final PageContribution contribution) throws CoreException {
		if (!pagesById.containsKey(contribution.getId())) {
			final AdminPage page = contribution.createPage();
			page.setAdminUi(this);
			pagesById.put(contribution.getId(), page);
		}
		return pagesById.get(contribution.getId());
	}

	private void openInitialPage() {
		final PageContribution contribution = navigation.findInitialPage();
		if (contribution != null) {
			openPage(contribution, null);
		}
	}

	private void openPage(final PageContribution contribution, final String[] args) {
		try {
			final AdminPage page = getPage(contribution);
			if (null == page) {
				Policy.getStatusHandler().show(new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, String.format("Page '%s' not found!", contribution.getId())), "Error Opening Page");
				return;
			}

			if (page == currentPage)
				// don't do anything if it's the same page
				return;
			else if (null != currentPage) {
				// deactivate old page first
				deactivate(currentPage);
			}

			currentPage = page;
			navigation.selectNavigationEntry(contribution);
			activate(page, contribution, args);
		} catch (final Exception | LinkageError | AssertionError e) {
			Policy.getStatusHandler().show(e instanceof CoreException ? ((CoreException) e).getStatus() : new Status(IStatus.ERROR, AdminUiActivator.SYMBOLIC_NAME, String.format("Unable to open page '%s (id %s)'. Please check the server logs.", contribution.getName(), contribution.getId()), e), "Error Opening Page");
		}
	}

	@Override
	public void openPage(final String pageId, final String[] args) {
		if (StringUtils.isBlank(pageId))
			throw new IllegalArgumentException("invalid page id");
		final PageContribution contribution = AdminPageRegistry.getInstance().getPage(pageId);
		if (contribution == null)
			return;

		final String[] argsWithPageId;
		if (args != null) {
			argsWithPageId = new String[args.length + 1];
			argsWithPageId[0] = pageId;
			System.arraycopy(args, 0, argsWithPageId, 1, args.length);
		} else {
			argsWithPageId = new String[] { pageId };
		}

		openPage(contribution, argsWithPageId);
	}
}
