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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.guise.framework.component.layout.*;
import io.guise.framework.model.ValueModel;

import static java.util.Collections.*;

/**
 * Abstract implementation of a frame for communication of an option. An option frame defaults to a single composite child panel with a row of options along the
 * bottom. The contents of an option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}. This
 * implementation does not allow its frame content to be changed.
 * @param <O> The type of options available.
 * @author Garret Wilson
 */
public abstract class AbstractOptionDialogFrame<O> extends AbstractDialogFrame<O> implements OptionDialogFrame<O> {

	@Override
	public void setContent(final Component newContent) {
		if(newContent != getContent()) { //if the content is changing
			throw new IllegalArgumentException("Option dialog frame content cannot be changed.");
		}
	}

	/** @return The container component used to hold content, including the option child component. */
	protected Container getContentContainer() {
		return (Container)super.getContent();
	}

	@Override
	public Component getOptionContent() {
		return ((RegionLayout)getContentContainer().getLayout()).getComponent(Region.CENTER); //return the center component, if there is one
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation adds the option content component to the center region of the child container.
	 * </p>
	 */
	@Override
	public void setOptionContent(final Component newOptionContent) {
		final Component oldOptionContents = getOptionContent(); //get the current component
		if(oldOptionContents != newOptionContent) { //if the value is really changing
			final Container contentsContainer = getContentContainer(); //get our container
			if(oldOptionContents != null) { //if an old content component was present
				contentsContainer.remove(oldOptionContents); //remove the old component
			}
			if(newOptionContent != null) { //if a new content component is given
				contentsContainer.add(newOptionContent, new RegionConstraints(Region.CENTER)); //add the component to the center of the container
			}
		}
	}

	/** The container containing the options. */
	private final Container optionContainer;

	@Override
	public Container getOptionContainer() {
		return optionContainer;
	}

	/** The read-only list of available options in order. */
	private final List<O> options;

	@Override
	public List<O> getOptions() {
		return options;
	}

	/** The map of components representing options. */
	private final Map<O, Component> optionComponentMap = new ConcurrentHashMap<O, Component>();

	@Override
	public Component getOptionComponent(final O option) {
		return optionComponentMap.get(option);
	}

	/**
	 * Value model, component, and options constructor. Duplicate options are ignored.
	 * @param valueModel The frame value model.
	 * @param component The component representing the content of the option dialog frame, or <code>null</code> if there is no content component.
	 * @param options The available options.
	 * @throws NullPointerException if the given value model and/or options is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public AbstractOptionDialogFrame(final ValueModel<O> valueModel, final Component component, final O... options) {
		super(valueModel, new LayoutPanel(new RegionLayout())); //construct the parent class using a layout panel as a container
		final List<O> optionList = new ArrayList<O>(); //create a list of options
		for(final O option : options) { //put all the options in the list without duplicates
			if(!optionList.contains(option)) { //if this option isn't already in the list
				optionList.add(option); //add this option to the list
			}
		}
		this.options = unmodifiableList(optionList); //save the list of options without duplicates
		setOptionContent(component); //set the component, if there is one
		optionContainer = createOptionContainer(); //create the option container
		getContentContainer().add(optionContainer, new RegionConstraints(Region.PAGE_END)); //add the option container at the bottom
		initializeOptionContainer(optionContainer, this.options); //initialize the option container
	}

	/**
	 * Creates a container for holding the options. This implementation creates a horizontal arrange panel.
	 * @return a container for holding the options.
	 * @see ArrangePanel
	 */
	protected Container createOptionContainer() {
		return new ArrangePanel(new FlowLayout(Flow.LINE)); //create a horizontal arrange panel
	}

	/**
	 * Initializes the option container with the available options. Each component is added to the option container and to the map of option components.
	 * @param optionContainer The container to the options.
	 * @param options The available options.
	 */
	protected void initializeOptionContainer(final Container optionContainer, final List<O> options) {
		for(final O option : options) { //for each option
			final Component optionComponent = createOptionComponent(option); //create a component for this option
			optionComponentMap.put(option, optionComponent); //store this component in the map keyed to the component
			optionContainer.add(optionComponent); //add the component to the container
		}
	}

	/**
	 * Creates a component to represent the given option.
	 * @param option The option for which a component should be created.
	 * @return A component to represents the given option.
	 */
	protected abstract Component createOptionComponent(final O option);
}
