/******************************************************************************* 
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.gyrex.admin.ui.internal.design.business;

import org.eclipse.gyrex.admin.ui.internal.design.managers.ContribItem;
import org.eclipse.gyrex.admin.ui.internal.design.managers.CoolBarManager;
import org.eclipse.gyrex.admin.ui.internal.design.managers.MenuBarManager;
import org.eclipse.gyrex.admin.ui.internal.design.managers.PartMenuManager;
import org.eclipse.gyrex.admin.ui.internal.design.managers.ToolBarManager;
import org.eclipse.gyrex.admin.ui.internal.design.managers.ViewToolBarManager;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.jface.internal.provisional.action.IToolBarManager2;
import org.eclipse.rap.ui.interactiondesign.IWindowComposer;
import org.eclipse.rap.ui.interactiondesign.PresentationFactory;


public class BusinessPresentationFactory extends PresentationFactory {


  public ICoolBarManager2 createCoolBarManager() {
    return new CoolBarManager();
  }

  public MenuManager createMenuBarManager() {
    return new MenuBarManager();
  }

  public MenuManager createPartMenuManager() {
    return new PartMenuManager();
  }

  public IToolBarContributionItem createToolBarContributionItem( 
    final IToolBarManager toolBarManager,
    final String id )
  {
    return new ContribItem( toolBarManager, id );
  }

  public IToolBarManager2 createToolBarManager() {
    return new ToolBarManager();
  }

  public IToolBarManager2 createViewToolBarManager() {
    return new ViewToolBarManager();
  }

  public IWindowComposer createWindowComposer() {    
    return new BusinessWindowComposer();
  }
  
}
