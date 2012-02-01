/*******************************************************************************
 * Copyright (c) 2010, 2012 AGETO Service GmbH and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *      Mike Tschierschke - initial API and implementation
 *******************************************************************************/
package org.eclipse.gyrex.admin.ui.internal.application;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.commands.ICommandService;

/**
 * Creates, adds and disposes actions for the menus and action bars of each
 * workbench window.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction introAction;
	private Action showViewAction;

	public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillCoolBar(final ICoolBarManager coolBar) {
		final IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main")); //$NON-NLS-1$
		if (null != introAction) {
			toolbar.add(introAction);
		}
		if (null != showViewAction) {
			toolbar.add(showViewAction);
		}
	}

	@Override
	protected void fillMenuBar(final IMenuManager menuBar) {
		final MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);

		if (null != showViewAction) {
			fileMenu.add(showViewAction);
		}
	}

	private ParameterizedCommand getShowViewCommand(final ICommandService commandService, final boolean makeFast) {
		final Command c = commandService.getCommand(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
		Parameterization[] parms = null;
		if (makeFast) {
			try {
				final IParameter parmDef = c.getParameter(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_FASTVIEW);
				parms = new Parameterization[] { new Parameterization(parmDef, "true") //$NON-NLS-1$
				};
			} catch (final NotDefinedException e) {
				// this should never happen
			}
		}
		return new ParameterizedCommand(c, parms);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them. Registering also
		// provides automatic disposal of the actions when the window is closed.

//		introAction = ActionFactory.INTRO.create(window);
//		register(introAction);

//		final IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
//		final ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
//		final ParameterizedCommand cmd = getShowViewCommand(commandService, true);
//
//		showViewAction = new Action("Show View") {
//			@Override
//			public void run() {
//				try {
//					handlerService.executeCommand(cmd, null);
//				} catch (final ExecutionException e) {
//					// Do nothing.
//				} catch (final NotDefinedException e) {
//					// Do nothing.
//				} catch (final NotEnabledException e) {
//					// Do nothing.
//				} catch (final NotHandledException e) {
//					// Do nothing.
//				}
//			}
//		};
//		showViewAction.setId(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW);
//		register(showViewAction);
	}
}
