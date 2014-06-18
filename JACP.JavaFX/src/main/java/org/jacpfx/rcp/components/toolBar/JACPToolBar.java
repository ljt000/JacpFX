/************************************************************************
 *
 * Copyright (C) 2010 - 2014
 *
 * [JACPToolBar.java]
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
package org.jacpfx.rcp.components.toolBar;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.*;
import org.jacpfx.api.component.Perspective;
import org.jacpfx.api.component.SubComponent;
import org.jacpfx.api.util.CustomSecurityManager;
import org.jacpfx.rcp.util.CSSUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static javafx.geometry.Orientation.HORIZONTAL;
import static javafx.geometry.Orientation.VERTICAL;
import static org.jacpfx.rcp.util.CSSUtil.CSSConstants.CLASS_JACP_TOOL_BAR;

/**
 * The Class JACPToolBar.
 *
 * @author: Patrick Symmangk (pete.jacp@gmail.com)
 */
public class JACPToolBar extends ToolBar implements ChangeListener<Orientation>, ListChangeListener<Node> {

    private final static CustomSecurityManager customSecurityManager = new CustomSecurityManager();

    private final Map<String, List<Region>> regionMap = new HashMap<>();

    private final List<String> manualIds = new ArrayList<>();


    private static final int FIRST_PLACE = 0;


    /**
     * The horizontal tool bar.
     */
    private HBox horizontalToolBar;

    /**
     * The vertical tool bar.
     */
    private VBox verticalToolBar;

    /**
     * stores the current toolbar containers
     */
    private ConcurrentHashMap<JACPToolBarPosition, Pane> toolBarContainer;


    /**
     * Instantiates a new jACP tool bar.
     */
    public JACPToolBar() {
        super();

        this.getStyleClass().add(CLASS_JACP_TOOL_BAR);
        this.orientationProperty().addListener(this);
        this.getItems().addListener(this);

        if (this.getOrientation() == VERTICAL) {
            this.initVerticalToolBar();
        } else {
            this.initHorizontalToolBar();
        }
    }


    private void internalAdd(String id, final Region region) {
        this.addRegion(id, region, JACPToolBarPosition.START, false);
    }


    public void add(final Region region) {
        this.internalAdd(customSecurityManager.getCallerClassName(), region);
    }

    /**
     * Adds the.
     *
     * @param id     an unique id
     * @param region the region
     */
    public void add(String id, final Region region) {
        this.manualIds.add(id);
        this.internalAdd(id, region);
    }


    public void removeForId(String id) {
        final List<Region> tmp = this.getNodes(id);
        this.remove(tmp.toArray(new Region[tmp.size()]));
    }

    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear in the first place of the toolbar
     * <p>
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAll(String id, Region... regions)}
     *
     * @param regions the regions to add
     */
    public void addAll(final Region... regions) {
        this.internalAddAll(customSecurityManager.getCallerClassName(), regions);
    }


    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear in the first place of the toolbar
     *
     * @param id      - the id the regions will refer to
     * @param regions the regions to add
     */
    public void addAll(String id, Region... regions) {
        this.manualIds.add(id);
        this.internalAddAll(id, regions);

    }


    private void internalAddAll(String id, Region... regions) {
        for (final Region region : regions) {
            this.internalAdd(id, region);
        }
    }


    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear on the end of the toolbar
     * Means right hand side for {@link org.jacpfx.api.util.ToolbarPosition#NORTH} and {@link org.jacpfx.api.util.ToolbarPosition#SOUTH}
     * and on the bottom for {@link org.jacpfx.api.util.ToolbarPosition#EAST} and {@link org.jacpfx.api.util.ToolbarPosition#WEST}
     * <p>
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAllOnEnd(String id, Region... regions)}
     *
     * @param regions the regions to add
     */
    public void addAllOnEnd(final Region... regions) {
        this.internalAddAllOnEnd(customSecurityManager.getCallerClassName(), regions);
    }


    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear on the end of the toolbar
     * Means right hand side for {@link org.jacpfx.api.util.ToolbarPosition#NORTH} and {@link org.jacpfx.api.util.ToolbarPosition#SOUTH}
     * and on the bottom for {@link org.jacpfx.api.util.ToolbarPosition#EAST} and {@link org.jacpfx.api.util.ToolbarPosition#WEST}
     *
     * @param id      self managed id for the given regions
     * @param regions the regions to add
     */
    public void addAllOnEnd(final String id, final Region... regions) {
        this.manualIds.add(id);
        this.internalAddAllOnEnd(id, regions);

    }


    private void internalAddAllOnEnd(final String id, final Region... regions) {
        for (final Region region : regions) {
            this.internalAddOnEnd(id, region);
        }
    }


    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear in the middle of the toolbar
     * <p>
     * The id is the name of the calling component by default.
     * For self-managed ids see {@link #addAllOnEnd(String id, Region... regions)}
     *
     * @param regions the regions to add
     */
    public void addAllToCenter(final Region... regions) {
        this.internalAddAllToCenter(customSecurityManager.getCallerClassName(), regions);
    }

    /**
     * Add multiple regions to the toolbar.
     * Those regions are added by id and will appear in the middle of the toolbar
     *
     * @param id      self managed id for the given regions
     * @param regions the regions to add
     */
    public void addAllToCenter(final String id, final Region... regions) {
        this.manualIds.add(id);
        this.internalAddAllToCenter(id, regions);
    }

    private void internalAddAllToCenter(final String id, final Region... regions) {
        for (final Region region : regions) {
            this.internalAddToCenter(id, region);
        }
    }


    private void internalAddToCenter(String id, final Region region) {
        this.addRegion(id, region, JACPToolBarPosition.MIDDLE);
    }

    /**
     * Adds the on end.
     * @param id an unique id
     * @param region the region
     */
    public void addToCenter(final String id, final Region region) {
        this.manualIds.add(id);
        this.internalAddToCenter(id, region);
    }

    /**
     * Adds the on end.
     *
     * @param region the region
     */
    public void addToCenter(final Region region) {
        this.internalAddToCenter(customSecurityManager.getCallerClassName(), region);
    }


    private void internalAddOnEnd(String id, final Region region) {
        this.addRegion(id, region, JACPToolBarPosition.END);
    }

    /**
     * Adds the on end.
     * @param id an unique id
     * @param region the region
     */
    public void addOnEnd(final String id, final Region region) {
        this.manualIds.add(id);
        this.internalAddOnEnd(id, region);
    }

    /**
     * Adds the on end.
     *
     * @param region the region
     */
    public void addOnEnd(final Region region) {
        this.internalAddOnEnd(customSecurityManager.getCallerClassName(), region);
    }


    /**
     * Removes the.
     *
     * @param regions the region
     */
    public void remove(final Region... regions) {
        this.toolBarContainer
                .values()
                .forEach(toolBarItem -> toolBarItem.getChildren()
                        .removeAll(regions));
    }

    /**
     * hide Toolbar Buttons by a given Id
     *
     * @param id -the given id
     */
    public void hideButtons(final String id) {
        this.handleButtons(id, false);
    }


    /**
     * show Toolbar Buttons by a given Id
     *
     * @param id -the given id
     */
    public void showButtons(final String id) {
        this.handleButtons(id, true);
    }


    private void handleButtons(final String id, final boolean visible) {
        for (final Region region : this.getInternalNodes(id)) {
            region.setVisible(visible);
            if (visible) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                this.setInsets(region);

            } else {
                region.setMaxSize(0, 0);
                region.setMinSize(0, 0);
                this.clearInsets(region);
            }
        }
    }


    public void hideButtons(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        this.handleButtons(perspective.getPerspective().getClass().getName(), false);
        for (final SubComponent<EventHandler<Event>, Event, Object> sub : perspective.getSubcomponents()) {
            this.hideButtons(sub);
        }
    }

    public void hideButtons(final SubComponent<EventHandler<Event>, Event, Object> subcomponent) {
        this.handleButtons(subcomponent.getComponent().getClass().getName(), false);
    }

    public void showButtons(final Perspective<EventHandler<Event>, Event, Object> perspective) {
        if(perspective==null || perspective.getSubcomponents() == null) return;
        this.handleButtons(perspective.getPerspective().getClass().getName(), true);
        for (final SubComponent<EventHandler<Event>, Event, Object> sub : perspective.getSubcomponents()) {
            this.showButtons(sub);
        }
    }

    public void showButtons(final SubComponent<EventHandler<Event>, Event, Object> subcomponent) {
        this.handleButtons(subcomponent.getComponent().getClass().getName(), true);
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue
     * , java.lang.Object, java.lang.Object)
     */
    @Override
    public void changed(final ObservableValue<? extends Orientation> arg0, final Orientation oldOrientation, final Orientation newOrientation) {

        if (newOrientation == VERTICAL) {
            this.initVerticalToolBar();
        } else {
            this.initHorizontalToolBar();
        }
        this.unbind();

    }

    /*
     * (non-Javadoc)
     *
     * @see javafx.collections.ListChangeListener#onChanged(javafx.collections.
     * ListChangeListener.Change)
     */
    @Override
    public void onChanged(final javafx.collections.ListChangeListener.Change<? extends Node> arg0) {
        if (this.getItems().size() > 1) {
            this.unbind();
        }

    }

    /**
     * returns all the regions for the given id.
     *
     * @param id - the custom id, which was provided on add .
     * @return A list of regions, if any or an empty list.
     */
    public List<Region> getNodes(String id) {
        return Collections.unmodifiableList(this.getInternalNodes(id));
    }

    /*
        adds a region for a given Id on the given Positon and updates all bindings
     */
    private void addRegion(final String id, final Region region, final JACPToolBarPosition position) {
        this.addRegion(id, region, position, true);
    }

    /*
        adds a region for a given Id on the given Positon and updates all bindings, as needed.
     */
    private void addRegion(final String id, final Region region, final JACPToolBarPosition position, boolean bind) {
        // regions are by default invisible and will be visible if the Perspective/Component etc. will be activated
        this.toolBarContainer.get(position).getChildren().add(region);
        this.getInternalNodes(id).add(region);
        if (!this.manualIds.contains(id)) {
            this.handleButtons(id, false);
        }
        if (bind) {
            this.bind();
        }
    }

    @Override
    protected ObservableList<Node> getChildren() {
        return super.getChildren();
    }

    /*
         *   get the Internal Regions to add some more Regions.
         */
    private List<Region> getInternalNodes(String id) {
        if (this.regionMap.containsKey(id)) {
            return this.regionMap.get(id);
        }
        List<Region> currentList = new ArrayList<>();
        this.regionMap.put(id, currentList);

        return currentList;
    }

    /*
     * Bind the needed Properties to fit the width or the height
     */
    private void bind() {

        double toolbarPadding = 20;

        if (this.getOrientation() == HORIZONTAL) {
            if (this.horizontalToolBar != null) {
                this.horizontalToolBar.maxWidthProperty().bind(this.widthProperty().subtract(toolbarPadding));
                this.horizontalToolBar.minWidthProperty().bind(this.widthProperty().subtract(toolbarPadding));
            }
        } else {
            if (this.verticalToolBar != null) {
                this.verticalToolBar.maxHeightProperty().bind(this.heightProperty().subtract(toolbarPadding));
                this.verticalToolBar.minHeightProperty().bind(this.heightProperty().subtract(toolbarPadding));
            }
        }
    }


    /*
        add Insets to the buttons as needed
     */
    private void setInsets(final Region region) {
        int INSET = 2;
        int ZERO = 0;
        if (this.getOrientation() == HORIZONTAL) {
            HBox.setMargin(region, new Insets(ZERO, INSET, ZERO, INSET));
        } else {
            VBox.setMargin(region, new Insets(INSET, ZERO, INSET, ZERO));
        }
    }

    private void clearInsets(final Region region) {
        if (this.getOrientation() == HORIZONTAL) {
            HBox.setMargin(region, new Insets(0));
        } else {
            VBox.setMargin(region, new Insets(0));
        }
    }


    /**
     * /**
     * Unbind.
     */
    private void unbind() {
        if (this.getOrientation() == HORIZONTAL) {
            if (this.horizontalToolBar != null) {
                this.horizontalToolBar.maxWidthProperty().unbind();
                this.horizontalToolBar.minWidthProperty().unbind();
            }
        } else {
            if (this.verticalToolBar != null) {
                this.verticalToolBar.maxHeightProperty().unbind();
                this.verticalToolBar.minHeightProperty().unbind();
            }
        }
    }

    /**
     * Clear.
     */
    private void clear() {
        if (!this.getItems().isEmpty()) {
            final Node node = this.getItems().get(FIRST_PLACE);
            if (node instanceof HBox || node instanceof VBox) {
                this.getItems().remove(node);
            }
        }
        // reset "cache"
        this.toolBarContainer = new ConcurrentHashMap<>();
    }


    /**
     * Inits the horizontal tool bar.
     */
    private void initHorizontalToolBar() {
        /*
         * ----------------------------------------------------------------------
         * |left hand side buttons| | centered buttons| |right hand side buttons|
         * ----------------------------------------------------------------------
         */
        this.clear();
        // the main box for the toolbar
        // holds the left hand side and the right hand side buttons!
        // the buttons are separated by a spacer box, that fills the remaining
        // width
        this.horizontalToolBar = new HBox();
        // the place for the buttons on the left hand side
        HBox leftButtons = new HBox();
        leftButtons.setAlignment(Pos.CENTER_LEFT);
        this.toolBarContainer.put(JACPToolBarPosition.START, leftButtons);
        // the spacer that fills the remaining width between the buttons
        HBox centerButtons = new HBox();
        centerButtons.setAlignment(Pos.CENTER);
        this.toolBarContainer.put(JACPToolBarPosition.MIDDLE, centerButtons);

        HBox rightButtons = new HBox();
        rightButtons.setAlignment(Pos.CENTER_RIGHT);
        this.toolBarContainer.put(JACPToolBarPosition.END, rightButtons);

        HBox.setHgrow(centerButtons, Priority.ALWAYS);
        CSSUtil.addCSSClass(CSSUtil.CSSConstants.CLASS_JACP_BUTTON_BAR, this, leftButtons, centerButtons, rightButtons);
        this.horizontalToolBar.getChildren().addAll(leftButtons, centerButtons, rightButtons);
        this.getItems().add(FIRST_PLACE, this.horizontalToolBar);
    }


    /**
     * Inits the vertical tool bar.
     */
    private void initVerticalToolBar() {
        /*
         * --------------------------------------------------------------- |
         * |left hand side buttons| |spacer| |right hand side buttons| |
         * ---------------------------------------------------------------
         */
        this.clear();
        // the main box for the toolbar
        // holds the left hand side and the right hand side buttons!
        // the buttons are separated by a spacer box, that fills the remaining
        // width
        this.verticalToolBar = new VBox();

        // the place for the buttons on the left hand side
        VBox topButtons = new VBox();
        topButtons.setAlignment(Pos.CENTER_LEFT);
        this.toolBarContainer.put(JACPToolBarPosition.START, topButtons);
        // the spacer that fills the remaining width between the buttons

        VBox middleButtons = new VBox();
        this.toolBarContainer.put(JACPToolBarPosition.MIDDLE, middleButtons);

        VBox bottomButtons = new VBox();
        bottomButtons.setAlignment(Pos.CENTER_RIGHT);
        this.toolBarContainer.put(JACPToolBarPosition.END, bottomButtons);

        VBox.setVgrow(middleButtons, Priority.ALWAYS);
        this.verticalToolBar.getChildren().addAll(topButtons, middleButtons, bottomButtons);
        this.getItems().add(FIRST_PLACE, this.verticalToolBar);
    }

    private enum JACPToolBarPosition {
        START, MIDDLE, END;
    }


}