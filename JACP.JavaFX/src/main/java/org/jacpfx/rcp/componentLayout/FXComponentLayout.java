/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [FX2ComponentLayout.java]
 * JACPFX Project (https://github.com/JacpFX/JacpFX/)
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacpfx.rcp.componentLayout;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jacpfx.api.componentLayout.BaseLayout;
import org.jacpfx.api.util.ToolbarPosition;
import org.jacpfx.rcp.components.menuBar.JACPMenuBar;
import org.jacpfx.rcp.components.toolBar.JACPToolBar;

import java.util.Collections;
import java.util.Map;

/**
 * A FX2ComponentLayout acts as an wrapper to the references of the main menu
 * and the defined bar entries; The menu and the bars are defined in the
 * workbench instance at application startup.
 *
 * @author Andy Moncsek
 */
public class FXComponentLayout implements BaseLayout<Node> {
    private final Map<ToolbarPosition, JACPToolBar> registeredToolBars;
    private final JACPMenuBar menu;
    private final Pane glassPane;
    private final String parentId;
    private final String componentId;

    public FXComponentLayout(final JACPMenuBar menu,
                             final Map<ToolbarPosition, JACPToolBar> registeredToolBars,
                             final Pane glassPane) {
        this(menu,registeredToolBars,glassPane,null,null);


    }

    public FXComponentLayout(final JACPMenuBar menu,
                             final Map<ToolbarPosition, JACPToolBar> registeredToolBars,
                             final Pane glassPane,
                             final String parentId,
                             final String componentId) {
        this.menu = menu;
        this.registeredToolBars = registeredToolBars;
        this.glassPane = glassPane;
        this.parentId = parentId;
        this.componentId = componentId;
    }

    public FXComponentLayout(final FXWorkbenchLayout layout,final String componentId) {
        this(layout.getMenu(), layout.getRegisteredToolBars(), layout
                .getGlassPane(),null,componentId);
    }

    @Override
    public JACPToolBar getRegisteredToolBar(final ToolbarPosition position) {
        return this.registeredToolBars.get(position);
    }

    @Override
    public Map<ToolbarPosition, JACPToolBar> getRegisteredToolBars() {
        return Collections.unmodifiableMap(this.registeredToolBars);
    }

    @Override
    public final JACPMenuBar getMenu() {
        return this.menu;
    }

    @Override
    public final Pane getGlassPane() {
        return this.glassPane;
    }

}
