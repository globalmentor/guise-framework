package com.javaguise.platform.web;

/**Constants to be used with the Guise stylesheet.
@author Garret Wilson
*/
public class GuiseCSSStyleConstants
{

	//style IDs
	
	/**The CSS class for panels meant to visually group components.*/
	public final static String GROUP_PANEL_CLASS="groupPanel";
	/**The CSS class for license information.*/
	public final static String LICENSE_CLASS="license";

	//components
	
	/**The CSS class suffix for the label part of a component.*/
	public final static String COMPONENT_LABEL_CLASS_SUFFIX="-label";
	/**The CSS class suffix for the message part of a component.*/
	public final static String COMPONENT_MESSAGE_CLASS_SUFFIX="-message";
	/**The CSS class suffix for the body part of a component.*/
	public final static String COMPONENT_BODY_CLASS_SUFFIX="-body";
	/**The CSS class suffix for a single child of a component.*/
	public final static String COMPONENT_CHILD_CLASS_SUFFIX="-child";
	/**The CSS class suffix for the list of children of a component.*/
	public final static String COMPONENT_CHILDREN_CLASS_SUFFIX="-children";
	/**The CSS class suffix for the error part of a component.*/
	public final static String COMPONENT_ERROR_CLASS_SUFFIX="-error";

	/**The CSS class suffix indicating the X axis.*/
	public final static String COMPONENT_X_AXIS_CLASS_SUFFIX="-x";
	/**The CSS class suffix indicating the Y axis.*/
	public final static String COMPONENT_Y_AXIS_CLASS_SUFFIX="-y";

	/**The CSS class suffix indicating left-to-right line direction.*/
	public final static String COMPONENT_LTR_CLASS_SUFFIX="-ltr";
	/**The CSS class suffix indicating right-to-left line direction.*/
	public final static String COMPONENT_RTL_CLASS_SUFFIX="-rtl";

		//images
	/**The CSS class suffix identifying the image caption.*/
	public final static String IMAGE_CAPTION_CLASS_SUFFIX="-caption";

		//tree nodes
	/**The CSS class for a tree node.*/
	public final static String TREE_NODE_CLASS="treeNode";
	/**The CSS class suffix indicating that a tree node is collapsed.*/
	public final static String TREE_NODE_COLLAPSED_CLASS_SUFFIX="-collapsed";
	/**The CSS class suffix indicating that a tree node is expanded.*/
	public final static String TREE_NODE_EXPANDED_CLASS_SUFFIX="-expanded";
	/**The CSS class suffix indicating that a tree node is a leaf node and is neither expanded nor collapsed.*/
	public final static String TREE_NODE_LEAF_CLASS_SUFFIX="-leaf";

		//tabbed panel
	/**The CSS class suffix for the tab part of a tabbed panel.*/
	public final static String TABBED_PANEL_TAB_CLASS_SUFFIX="-tab";
	/**The CSS class suffix for a selected tab part of a tabbed panel.*/
	public final static String TABBED_PANEL_TAB_SELECTED_CLASS_SUFFIX="-tab-selected";
	/**The CSS class suffix for the tab set part of a tabbed panel.*/
	public final static String TABBED_PANEL_TABS_CLASS_SUFFIX="-tabs";

		//frames
	/**The CSS class for a closed frame.*/
	public final static String FRAME_CLOSED_CLASS="frameClosed";
	/**The CSS class for an open, nonmodal frame.*/
	public final static String FRAME_NONMODAL_CLASS="frameNonmodal";
	/**The CSS class for an open, modal frame.*/
	public final static String FRAME_MODAL_CLASS="frameModal";
	/**The CSS class suffix identifying the frame title.*/
	public final static String FRAME_TITLE_CLASS_SUFFIX="-title";
	/**The CSS class suffix identifying the frame title controls.*/
	public final static String FRAME_TITLE_CONTROLS_CLASS_SUFFIX="-titleControls";
	/**The CSS class suffix identifying the frame close control.*/
	public final static String FRAME_CLOSE_CLASS_SUFFIX="-close";

		//slider
	/**The CSS class suffix identifying the slider track.*/
	public final static String SLIDER_TRACK_CLASS_SUFFIX="-track";
	/**The CSS class suffix identifying the slider thumb.*/
	public final static String SLIDER_THUMB_CLASS_SUFFIX="-thumb";

		//drag and drop
	/**The potential source of a drag and drop operation.*/
	public final static String DRAG_SOURCE_CLASS="dragSource";
	/**The handle of a drag source.*/
	public final static String DRAG_HANDLE_CLASS="dragHandle";
	/**The potential target of a drag and drop operation.*/
	public final static String DROP_TARGET_CLASS="dropTarget";
	
		//layout

	/**The CSS class for any enclosing element needed for region layout.*/
	public final static String LAYOUT_REGION_CLASS="layout-region";
	/**The CSS class for the bottom region layout.*/
	public final static String LAYOUT_REGION_BOTTOM_CLASS="layout-region-bottom";
	/**The CSS class for the center region layout.*/
	public final static String LAYOUT_REGION_CENTER_CLASS="layout-region-center";
	/**The CSS class for the left region layout.*/
	public final static String LAYOUT_REGION_LEFT_CLASS="layout-region-left";
	/**The CSS class for the right region layout.*/
	public final static String LAYOUT_REGION_RIGHT_CLASS="layout-region-right";
	/**The CSS class for the top region layout.*/
	public final static String LAYOUT_REGION_TOP_CLASS="layout-region-top";

	/**The CSS class for any enclosing element needed for horizontal flow layout.*/
	public final static String LAYOUT_FLOW_X_CLASS="layout-flow-x";
	/**The CSS class for horizontal flow layout children.*/
	public final static String LAYOUT_FLOW_X_CHILD_CLASS="layout-flow-x-child";

	/**The CSS class for any enclosing element needed for vertical flow layout.*/
	public final static String LAYOUT_FLOW_Y_CLASS="layout-flow-y";
	/**The CSS class for vertical flow layout children.*/
	public final static String LAYOUT_FLOW_Y_CHILD_CLASS="layout-flow-y-child";
}