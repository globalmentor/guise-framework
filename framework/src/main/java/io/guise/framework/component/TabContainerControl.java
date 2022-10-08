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

package io.guise.framework.component;

import static java.util.Objects.*;

import io.guise.framework.component.layout.Flow;
import io.guise.framework.component.layout.TabLayout;

import static com.globalmentor.java.Classes.*;

/**
 * A container showing its children as tabs. The panel's value model reflects the currently selected component, if any.
 * @author Garret Wilson
 * @see TabLayout
 */
public class TabContainerControl extends AbstractListSelectContainerControl {

	/** The axis bound property. */
	public static final String AXIS_PROPERTY = getPropertyName(TabContainerControl.class, "axis");
	/** The maximum tab count bound property. */
	public static final String MAX_TAB_COUNT_PROPERTY = getPropertyName(TabContainerControl.class, "maxTabCount");

	/** The flow axis. */
	private Flow axis;

	/** @return The flow axis. */
	public Flow getAxis() {
		return axis;
	}

	/**
	 * Sets the flow axis. This is a bound property
	 * @param newAxis The flow axis.
	 * @throws NullPointerException if the given axis is <code>null</code>.
	 * @see #AXIS_PROPERTY
	 */
	public void setAxis(final Flow newAxis) {
		if(axis != requireNonNull(newAxis, "Flow axis cannot be null.")) { //if the value is really changing
			final Flow oldAxis = axis; //get the old value
			axis = newAxis; //actually change the value
			firePropertyChange(AXIS_PROPERTY, oldAxis, newAxis); //indicate that the value changed
		}
	}

	/** The estimated number of tabs requested to be visible, or -1 if no tab count is specified. */
	private int maxTabCount;

	/** @return The estimated number of tabs requested to be visible, or -1 if no tab count is specified. */
	public int getMaxTabCount() {
		return maxTabCount;
	}

	/**
	 * Sets the estimated number of tabs requested to be visible. This is a bound property of type <code>Integer</code>.
	 * @param newMaxTabCount The new requested number of visible tabs, or -1 if no tab count is specified.
	 * @see #MAX_TAB_COUNT_PROPERTY
	 */
	public void setMaxTabCount(final int newMaxTabCount) {
		if(maxTabCount != newMaxTabCount) { //if the value is really changing
			final int oldMaxTabCount = maxTabCount; //get the old value
			maxTabCount = newMaxTabCount; //actually change the value
			firePropertyChange(MAX_TAB_COUNT_PROPERTY, oldMaxTabCount, newMaxTabCount); //indicate that the value changed
		}
	}

	/** Default constructor with default {@link Flow#LINE} axis orientation. */
	public TabContainerControl() {
		this(Flow.LINE); //default to line axis orientation
	}

	/**
	 * Axis constructor.
	 * @param axis The axis along which the tabs are oriented.
	 * @throws NullPointerException if the given axis is <code>null</code>.
	 */
	public TabContainerControl(final Flow axis) {
		this(new TabLayout(), axis); //construct the panel using a default layout
	}

	/**
	 * Layout and axis constructor.
	 * @param layout The layout definition for the container.
	 * @param axis The axis along which the tabs are oriented.
	 * @throws NullPointerException if the given layout, and/or axis is <code>null</code>.
	 */
	public TabContainerControl(final TabLayout layout, final Flow axis) {
		this(layout, axis, -1); //construct the class with no maximum tab count		
	}

	/**
	 * Layout, axis, and maximum tab count constructor.
	 * @param layout The layout definition for the container.
	 * @param axis The axis along which the tabs are oriented.
	 * @param maxTabCount The requested number of visible tabs, or -1 if no maximum tab count is specified.
	 * @throws NullPointerException if the given layout and/or axis is <code>null</code>.
	 */
	public TabContainerControl(final TabLayout layout, final Flow axis, final int maxTabCount) {
		super(layout); //construct the parent class, using the card layout's value model
		this.axis = requireNonNull(axis, "Flow axis cannot be null.");
		this.maxTabCount = maxTabCount; //save the maximum tab count
	}

}
