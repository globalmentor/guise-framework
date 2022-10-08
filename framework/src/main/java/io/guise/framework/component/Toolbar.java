/*
 * Copyright © 2005-2013 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.globalmentor.beans.GenericPropertyChangeListener;

import io.guise.framework.component.layout.*;
import io.guise.framework.prototype.*;

/**
 * A panel that holds components used as tools.
 * <p>
 * This component's {@link LabelDisplayableComponent} implementation refers to whether tool components will have their icon and/or labels displayed (if they in
 * turn implement {@link LabelDisplayableComponent}).
 * </p>
 * <p>
 * This component by default has {@link #isIconDisplayed()} set to <code>true</code> and {@link #isLabelDisplayed()} set to <code>false</code>.
 * </p>
 * @author Garret Wilson
 */
public class Toolbar extends AbstractPanel implements LabelDisplayableComponent {

	/** Whether the icon is displayed. */
	private boolean iconDisplayed = true;

	@Override
	public boolean isIconDisplayed() {
		return iconDisplayed;
	}

	@Override
	public void setIconDisplayed(final boolean newIconDisplayed) {
		if(iconDisplayed != newIconDisplayed) { //if the value is really changing
			final boolean oldIconDisplayed = iconDisplayed; //get the current value
			iconDisplayed = newIconDisplayed; //update the value
			firePropertyChange(ICON_DISPLAYED_PROPERTY, Boolean.valueOf(oldIconDisplayed), Boolean.valueOf(newIconDisplayed));
		}
	}

	/** Whether the label is displayed. */
	private boolean labelDisplayed = false;

	@Override
	public boolean isLabelDisplayed() {
		return labelDisplayed;
	}

	@Override
	public void setLabelDisplayed(final boolean newLabelDisplayed) {
		if(labelDisplayed != newLabelDisplayed) { //if the value is really changing
			final boolean oldLabelDisplayed = labelDisplayed; //get the current value
			labelDisplayed = newLabelDisplayed; //update the value
			firePropertyChange(LABEL_DISPLAYED_PROPERTY, Boolean.valueOf(oldLabelDisplayed), Boolean.valueOf(newLabelDisplayed));
		}
	}

	/** Default constructor with a default horizontal flow layout. */
	public Toolbar() {
		this(new FlowLayout(Flow.LINE)); //default to flowing horizontal
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public Toolbar(final Layout<?> layout) {
		super(layout); //construct the parent class
		//create a listener that will update all child components' icon/label displayed status
		final GenericPropertyChangeListener<Boolean> labelDisplayedPropertyChangeListener = new AbstractGenericPropertyChangeListener<Boolean>() {

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) {
				for(final Component childComponent : getChildComponents()) { //update the displayed status of all child components
					if(childComponent instanceof LabelDisplayableComponent) { //if this component can modify its label displayed status
						((LabelDisplayableComponent)childComponent).setIconDisplayed(isIconDisplayed()); //turn the icon on or off
						((LabelDisplayableComponent)childComponent).setLabelDisplayed(isLabelDisplayed()); //turn the label on or off
					}
				}
			}

		};
		//update the child components' icon/label displayed status when the toolbar's icon and/or label displayed status changes
		addPropertyChangeListener(ICON_DISPLAYED_PROPERTY, labelDisplayedPropertyChangeListener);
		addPropertyChangeListener(LABEL_DISPLAYED_PROPERTY, labelDisplayedPropertyChangeListener);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Creates a component appropriate for the context of this component from the given prototype. This version creates the following components, in order of
	 * priority:
	 * </p>
	 * <dl>
	 * <dt>{@link ActionPrototype}</dt>
	 * <dd>{@link ToolButton}</dd>
	 * <dt>{@link TogglePrototype}</dt>
	 * <dd>{@link BooleanSelectToolButton}</dd>
	 * </dl>
	 * <p>
	 * After creating a component, this version displays or hides the label as appropriate.
	 * </p>
	 */
	@Override
	public Component createComponent(final Prototype prototype) {
		final Component component;
		if(prototype instanceof ActionPrototype) { //action prototypes
			component = new ToolButton((ActionPrototype)prototype);
		} else if(prototype instanceof TogglePrototype) { //toggle prototypes
			final TogglePrototype togglePrototype = (TogglePrototype)prototype; //get the toggle prototype
			final BooleanSelectToolButton booleanSelectToolButton = new BooleanSelectToolButton(togglePrototype); //create a boolean select tool button
			booleanSelectToolButton.setToggle(true); //turn on toggling
			component = booleanSelectToolButton; //use the button
		} else { //for all other components
			component = super.createComponent(prototype); //create a default component
		}
		if(component instanceof LabelDisplayableComponent) { //if this component can modify its label displayed status
			((LabelDisplayableComponent)component).setIconDisplayed(isIconDisplayed()); //turn the icon on or off
			((LabelDisplayableComponent)component).setLabelDisplayed(isLabelDisplayed()); //turn the label on or off
		}
		return component; //return the component
	}
}
