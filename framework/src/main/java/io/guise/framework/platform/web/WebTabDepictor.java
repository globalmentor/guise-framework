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

import java.io.IOException;
import java.util.*;

import com.globalmentor.net.URIQueryParameter;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.Flow;
import io.guise.framework.platform.*;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.net.URIs.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a tabbed control as an XHTML <code>&lt;ol&gt;</code> element containing tabs. This view supports {@link TabControl} and
 * {@link TabContainerControl}.
 * @param <V> The type of values to select.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebTabDepictor<V, C extends ListSelectControl<V>> extends AbstractDecoratedWebComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;ol&gt;</code> element. */
	public WebTabDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_OL); //represent <xhtml:ol>
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebActionDepictEvent) { //if this is an action event
			final WebActionDepictEvent webActionEvent = (WebActionDepictEvent)event; //get the web action event
			final C component = getDepictedObject(); //get the depicted object
			if(webActionEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webActionEvent.getDepictedObject());
			}
			if(component.isEnabled()) { //if the component is enabled
				final WebPlatform platform = getPlatform(); //get the platform
				final String componentID = getPlatform().getDepictIDString(component.getDepictID()); //get the component's ID
				final String targetID = webActionEvent.getTargetID(); //get the target ID, if any
				if(targetID != null && targetID.startsWith(componentID + '-')) { //if the target starts with the component ID TODO use a constant
					final String childComponentIDString = targetID.substring((componentID + '-').length()); //get the child component ID string TODO use a constant
					WebSelectDepictor.processSelectedIDs(component, new String[] { childComponentIDString }); //process the selected IDs				
				}
			}
		} else if(event instanceof WebFormEvent) { //if this is a form submission TODO refactor all this
			final WebFormEvent formEvent = (WebFormEvent)event; //get the form submit event
			final WebPlatform platform = getPlatform(); //get the platform
			final C component = getDepictedObject(); //get the component
			if(component.isEnabled()) { //if the component is enabled
				final String componentID = getPlatform().getDepictIDString(component.getDepictID()); //get the component's ID
				final List<?> selectedIDs = formEvent.getParameterListMap().get(componentID); //get the value IDs reported for this component
				if(selectedIDs != null) { //if there are values
					WebSelectDepictor.processSelectedIDs(getDepictedObject(), selectedIDs.toArray(new String[selectedIDs.size()])); //process the selected IDs				
				}
			}
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds attributes based upon the tab control axis and orientation.
	 * </p>
	 * @see AbstractWebComponentDepictor#addFlowStyleIDs(Set, Flow)
	 */
	@Override
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix) {
		final Set<String> baseStyleIDs = super.getBaseStyleIDs(prefix, suffix); //get the default base style IDs
		final Flow flow; //we'll determine the flow TODO eventually create some sort of parent class or interface that returns the axis rather than special-casing the component type
		final C component = getDepictedObject(); //get the component
		if(component instanceof TabControl) { //if this is a tab control
			flow = ((TabControl<?>)component).getAxis(); //get the flow from the tab control	TODO find out why JDK 1.5 required the double cast; maybe switch to TabControl.class.cast()
		} else if(component instanceof TabContainerControl) { //if this is a tab container control
			flow = ((TabContainerControl)component).getAxis(); //get the flow from the tab control
		} else { //if we don't recognize the component type
			flow = Flow.LINE; //default to line flow
		}
		addFlowStyleIDs(baseStyleIDs, flow); //add style IDs related to flow
		return baseStyleIDs; //return the new style IDs
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final String componentID = getPlatform().getDepictIDString(component.getDepictID()); //get the ID of the tabbed panel
		final V[] selectedValues = component.getSelectedValues(); //get the selected values
		final Set<String> selectedIDs = new HashSet<String>(selectedValues.length); //create a set to contain all selected IDs
		for(final V selectedValue : selectedValues) { //for each selected value
			selectedIDs.add(getPlatform().getDepictIDString(component.getComponent(selectedValue).getDepictID())); //get the ID of this value's representation component
		}
		synchronized(component) { //don't allow the model to be modified while we access it
			int index = -1; //keep track of each index in the model
			for(final V value : component) { //for each value in the model
				++index; //increment the index
				final Component representationComponent = component.getComponent(value); //create a component to represent the value
				final String valueID = getPlatform().getDepictIDString(representationComponent.getDepictID()); //get the ID of this value's representation component
				final boolean selected = selectedIDs != null && selectedIDs.contains(valueID); //see if this value is selected
				final boolean enabled = component.isIndexEnabled(index); //see if this value is enabled
				final Set<String> tabStyleIDs = getBaseStyleIDs(null, TABBED_PANEL_TAB_CLASS_SUFFIX); //get the set of tab style IDs
				if(selected) { //if this tab is selected
					tabStyleIDs.add(SELECTED_CLASS); //add the selected class ID
				}
				if(!enabled) { //if this tab is disabled
					tabStyleIDs.add(DISABLED_CLASS); //add the disabled class ID
				}
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LI); //<xhtml:li> (component-tab)
				writeDirectionAttribute(); //write the component direction, if this component specifies a direction
				writeClassAttribute(tabStyleIDs); //write the base style IDs with a "-tab" (or "-tab-selected") suffix
				depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_A); //<xhtml:a> (component-tab)
				writeIDAttribute(null, "-" + valueID); //id="componentID-valueID"
				tabStyleIDs.add(ACTION_CLASS); //allow the tab link to be an action
				writeClassAttribute(tabStyleIDs); //write the base style IDs with a "-tab" (or "-tab-selected") suffix
				writeDirectionAttribute(); //write the component direction, if this component specifies a direction
				/*TODO fix selected
								if(selected) {	//if we have this component's ID in the list of selected IDs
									context.writeAttribute(null, ELEMENT_OPTION_ATTRIBUTE_SELECTED, OPTION_SELECTED_SELECTED);	//selected="selected"			
								}
				*/
				final String query = constructQuery(new URIQueryParameter(componentID, valueID)); //construct a query in the form "tabControlID=tabID"
				//	TODO del				final URI panelComponentURI=resolveFragment(null, childComponentID);	//create a fragment URI link to the component, even if it isn't showing
				depictContext.writeAttribute(null, ELEMENT_A_ATTRIBUTE_HREF, query); //write the href attribute to the tab component
				representationComponent.depict(); //tell the representation component to update its view
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_A); //</xhtml:a> (component-tab)
				depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LI); //</xhtml:li> (component-tab)
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version does nothing, because if a tab control is a composite component the child controls have already been rendered as values in
	 * {@link #depictBody()}.
	 * </p>
	 */
	@Override
	protected void depictChildren() throws IOException {
	}

}
