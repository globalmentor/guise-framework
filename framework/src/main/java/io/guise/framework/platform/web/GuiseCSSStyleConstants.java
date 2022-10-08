/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform.web;

/**
 * Constants to be used with the Guise stylesheet.
 * @author Garret Wilson
 */
public class GuiseCSSStyleConstants {

	//style IDs

	/** The CSS class for panels meant to visually group components. */
	public static final String GROUP_PANEL_CLASS = "groupPanel";
	/** The CSS class for license information. */
	public static final String LICENSE_CLASS = "license";

	//components

	/** The CSS class suffix for the layout of a layout component. */
	public static final String COMPONENT_LAYOUT_CLASS_SUFFIX = "-layout";
	/** The CSS class suffix for the label part of a component. */
	public static final String COMPONENT_LABEL_CLASS_SUFFIX = "-label";
	/** The CSS class suffix for the description part of a component. */
	public static final String COMPONENT_DESCRIPTION_CLASS_SUFFIX = "-description";
	/** The CSS class suffix for the body part of a component. */
	public static final String COMPONENT_BODY_CLASS_SUFFIX = "-body";
	/** The CSS class suffix for the container part of a component. */
	public static final String COMPONENT_CONTAINER_CLASS_SUFFIX = "-container";
	/** The CSS class suffix for the link part of a component. */
	public static final String COMPONENT_LINK_CLASS_SUFFIX = "-link";
	/** The CSS class suffix for a single child of a component. */
	public static final String COMPONENT_CHILD_CLASS_SUFFIX = "-child";
	/** The CSS class suffix for the list of children of a component. */
	public static final String COMPONENT_CHILDREN_CLASS_SUFFIX = "-children";
	/** The CSS class suffix for the error part of a component. */
	public static final String COMPONENT_ERROR_CLASS_SUFFIX = "-error";

	/** The CSS class indicating the X axis. */
	public static final String AXIS_X_CLASS = "axisX";
	/** The CSS class indicating the Y axis. */
	public static final String AXIS_Y_CLASS = "axisY";
	/** The CSS class indicating the Z axis. */
	public static final String AXIS_Z_CLASS = "axisZ";
	/** The CSS class indicating a busy component. */
	//TODO transfer to common style ID class	public static final String BUSY_CLASS="busy";
	/** The CSS class indicating left-to-right line direction. */
	public static final String DIR_LTR_CLASS = "dirLTR";
	/** The CSS class indicating right-to-left line direction. */
	public static final String DIR_RTL_CLASS = "dirRTL";
	/** The CSS class indicating disabled state. */
	public static final String DISABLED_CLASS = "disabled";
	/** The CSS class indicating error status. */
	public static final String ERROR_CLASS = "error";
	/** The CSS class indicating invalid contents. */
	public static final String INVALID_CLASS = "invalid";
	/** The CSS class indicating open state. */
	public static final String OPEN_CLASS = "open";
	/** The CSS class indicating rollover state. */
	public static final String ROLLOVER_CLASS = "rollover";
	/** The CSS class indicating selected state. */
	public static final String SELECTED_CLASS = "selected";
	/** The CSS class indicating warning status. */
	public static final String WARNING_CLASS = "warning";

	//images
	/** The CSS class suffix identifying the image caption. */
	public static final String IMAGE_CAPTION_CLASS_SUFFIX = "-caption";

	//tree nodes
	/** The CSS class for a tree node. */
	public static final String TREE_NODE_CLASS = "treeNode";
	/** The CSS class indicating that a tree node is collapsed. */
	public static final String TREE_NODE_COLLAPSED_CLASS = "collapsed";
	/** The CSS class indicating that a tree node is expanded. */
	public static final String TREE_NODE_EXPANDED_CLASS = "expanded";
	/** The CSS class indicating that a tree node is a leaf node. */
	public static final String TREE_NODE_LEAF_CLASS = "leaf";

	//tabbed panel
	/** The CSS class suffix for the tab part of a tabbed panel. */
	public static final String TABBED_PANEL_TAB_CLASS_SUFFIX = "-tab";
	/** The CSS class suffix for the tab set part of a tabbed panel. */
	public static final String TABBED_PANEL_TABS_CLASS_SUFFIX = "-tabs";

	//frames
	/** The CSS class for a closed frame. */
	public static final String FRAME_CLOSED_CLASS = "frameClosed";
	/** The CSS class for an open, nonmodal frame. */
	public static final String FRAME_NONMODAL_CLASS = "frameNonmodal";
	/** The CSS class suffix identifying the frame menu. */
	public static final String FRAME_MENU_CLASS_SUFFIX = "-menu";
	/** The CSS class for an open, modal frame. */
	public static final String FRAME_MODAL_CLASS = "frameModal";
	/** The CSS class suffix identifying the frame tether. */
	public static final String FRAME_TETHER_CLASS_SUFFIX = "-tether";
	/** The CSS class suffix identifying the frame title. */
	public static final String FRAME_TITLE_CLASS_SUFFIX = "-title";
	/** The CSS class suffix identifying the frame title controls. */
	public static final String FRAME_TITLE_CONTROLS_CLASS_SUFFIX = "-titleControls";
	/** The CSS class suffix identifying the frame close control. */
	public static final String FRAME_CLOSE_CLASS_SUFFIX = "-close";

	//slider
	/** The CSS class suffix identifying the slider track. */
	public static final String SLIDER_TRACK_CLASS_SUFFIX = "-track";
	/** The CSS class suffix identifying the slider thumb. */
	public static final String SLIDER_THUMB_CLASS_SUFFIX = "-thumb";
	/** The CSS class indicating slider sliding state. */
	public static final String SLIDER_SLIDING_CLASS = "sliding";

	//drag and drop
	/** The potential source of a drag and drop operation. */
	public static final String DRAG_SOURCE_CLASS = "dragSource";
	/** The handle of a drag source. */
	public static final String DRAG_HANDLE_CLASS = "dragHandle";
	/** The potential target of a drag and drop operation. */
	public static final String DROP_TARGET_CLASS = "dropTarget";

	//layout

	/** The CSS class for any enclosing element needed for region layout. */
	public static final String LAYOUT_REGION_CLASS = "layout-region";
	/** The CSS class for the bottom region layout. */
	public static final String LAYOUT_REGION_BOTTOM_CLASS = "layout-region-bottom";
	/** The CSS class for the center region layout. */
	public static final String LAYOUT_REGION_CENTER_CLASS = "layout-region-center";
	/** The CSS class for the left region layout. */
	public static final String LAYOUT_REGION_LEFT_CLASS = "layout-region-left";
	/** The CSS class for the right region layout. */
	public static final String LAYOUT_REGION_RIGHT_CLASS = "layout-region-right";
	/** The CSS class for the top region layout. */
	public static final String LAYOUT_REGION_TOP_CLASS = "layout-region-top";

	/** The CSS class for any enclosing element needed for horizontal flow layout. */
	public static final String LAYOUT_FLOW_X_CLASS = "layout-flow-x";
	/** The CSS class for horizontal flow layout children. */
	public static final String LAYOUT_FLOW_X_CHILD_CLASS = "layout-flow-x-child";

	/** The CSS class for any enclosing element needed for vertical flow layout. */
	public static final String LAYOUT_FLOW_Y_CLASS = "layout-flow-y";
	/** The CSS class for vertical flow layout children. */
	public static final String LAYOUT_FLOW_Y_CHILD_CLASS = "layout-flow-y-child";

	//action
	/** A component that can send back an action. */
	public static final String ACTION_CLASS = "action";

	//mouse
	/** A component that listens for mouse events. */
	public static final String MOUSE_LISTENER_CLASS = "mouseListener";
	/** The part of a component that senses mouse events. */
	//TODO del if not needed	public static final String MOUSE_SENSOR_CLASS="mouseSensor";

}
