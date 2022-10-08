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

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static java.util.Objects.*;

import com.globalmentor.model.NameValuePair;
import com.globalmentor.model.TaskState;

import io.guise.framework.Bookmark;
import io.guise.framework.component.*;
import io.guise.framework.platform.*;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.java.Objects.*;
import static io.guise.framework.platform.web.WebPlatform.*;

/**
 * Strategy for rendering a resource collect control as an XHTML <code>&lt;input&gt;</code> element with type="file".
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebResourceCollectDepictor<C extends ResourceCollectControl> extends AbstractDecoratedWebComponentDepictor<C>
		implements ResourceCollectControl.Depictor<C> {

	/** The web commands for controlling a resource collect control. */
	public enum ResourceCollectCommand implements WebPlatformCommand {
		/**
		 * The command to start receiving resources. parameters: <code>{{@value #DESTINATION_URI_PROPERTY}:"<var>destinationURI</var>"}</code>
		 */
		RESOURCE_COLLECT_RECEIVE,

		/** The command to complete receiving resources. */
		RESOURCE_COLLECT_COMPLETE,

		/** The command to cancel a resource transfer. */
		RESOURCE_COLLECT_CANCEL;

		/** The property for specifying the destination URI of the resources to receive. */
		public static final String DESTINATION_URI_PROPERTY = "destinationURI";
	}

	/** Default constructor using the XHTML <code>&lt;input&gt;</code> element. */
	public WebResourceCollectDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true); //represent <xhtml:input>, allowing an empty element if possible
		getIgnoredProperties().add(ResourceCollectControl.RESOURCE_PATHS_PROPERTY); //TODO fix; temporary for development
	}

	@Override
	public void receive(URI destinationURI, final Bookmark destinationBookmark) {
		URI receiveResourceURI = getSession().getApplication().resolveURI(requireNonNull(destinationURI, "Destination URI cannot be null.")); //resolve the URI
		if(destinationBookmark != null) { //if a bookmark was provided
			receiveResourceURI = URI.create(receiveResourceURI.toString() + destinationBookmark.toString()); //append the bookmark query
		}
		getPlatform().getSendMessageQueue()
				.add(new WebCommandDepictEvent<ResourceCollectCommand>(getDepictedObject(), ResourceCollectCommand.RESOURCE_COLLECT_RECEIVE,
						new NameValuePair<String, Object>(ResourceCollectCommand.DESTINATION_URI_PROPERTY, receiveResourceURI))); //send a command for the control to start receiving		
	}

	@Override
	public void cancel() {
		final C control = getDepictedObject(); //get the resource collect control
		getPlatform().getSendMessageQueue()
				.add(new WebCommandDepictEvent<ResourceCollectCommand>(getDepictedObject(), ResourceCollectCommand.RESOURCE_COLLECT_CANCEL)); //send a resource collect cancel command to the platform
		control.setState(TaskState.CANCELED); //tell the control that the transfer has been cancelled
		control.clearResourcePaths(); //clear all the resource paths
	}

	@Override
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebChangeDepictEvent) { //if a property changed
			final WebChangeDepictEvent webChangeEvent = (WebChangeDepictEvent)event; //get the web change event
			final C component = getDepictedObject(); //get the depicted object
			if(webChangeEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties = webChangeEvent.getProperties(); //get the new properties
			asInstance(properties.get("resourcePath"), String.class).ifPresent(resourcePath -> { //get the added resource path; if there is a resource path TODO use a constant
				if(component.isEnabled()) { //if the component is enabled
					component.addResourcePath(resourcePath); //add this resource path to the control
				}
			});
		} else if(event instanceof WebProgressDepictEvent) { //if this is a progress event
			final WebProgressDepictEvent webProgressEvent = (WebProgressDepictEvent)event; //get the progress event
			final C component = getDepictedObject(); //get the depicted object
			if(webProgressEvent.getDepictedObject() != component) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webProgressEvent.getDepictedObject());
			}
			if(component.isEnabled()) { //if the component is enabled
				final TaskState taskState = webProgressEvent.getTaskState(); //get the new task state
				component.fireProgressed(webProgressEvent.getTask(), taskState, webProgressEvent.getProgress(), webProgressEvent.getGoal()); //tell the control that progress has been made
				if(webProgressEvent.getTask() == null) { //if this is a progress indication for the entire transfer
					component.setState(taskState); //update the control with the new state
				}
			}
		}
		super.processEvent(event); //do the default event processing
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation marks the property as being modified if the property is not an ignored property.
	 * </p>
	 */
	@Override
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
		super.depictedObjectPropertyChange(propertyChangeEvent); //do the default processing
		final C control = getDepictedObject(); //get the control
		if(control == propertyChangeEvent.getSource() //if our control's property changed
				&& ResourceCollectControl.STATE_PROPERTY.equals(propertyChangeEvent.getPropertyName()) //if the state property changed
				&& TaskState.COMPLETE == propertyChangeEvent.getNewValue()) { //if the control completed a transfer
			getPlatform().getSendMessageQueue()
					.add(new WebCommandDepictEvent<ResourceCollectCommand>(getDepictedObject(), ResourceCollectCommand.RESOURCE_COLLECT_COMPLETE)); //send a resource collect complete command to the platform
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version write additional patching attributes.
	 * </p>
	 */
	@Override
	protected void writeDecoratorBegin() throws IOException {
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, "patchType", "none"); //guise:patchType="none" never patch this component TODO use constants
		super.writeDecoratorBegin(); //write the default decorator beginning
	}

	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID())); //write the component ID in the XHTML name attribute
		depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_FILE); //type="file"
		if(!component.isEnabled()) { //if the component's model is not enabled
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED); //disabled="disabled"			
		}
		/*TODO fix
				if(!component.isEditable()) {	//if the component's model is not editable
					context.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY);	//readonly="readonly"			
				}
				final Validator<ResourceImport> validator=component.getValidator();	//get the component's validator
				if(validator instanceof ResourceImportValidator) {	//if the validator is a resource import validator
					final Set<ContentType> acceptedContentTypes=((ResourceImportValidator)validator).getAcceptedContentTypes();	//get the accepted content types
					if(acceptedContentTypes!=null) {	//if accepted content types are specified
						final StringBuilder acceptStringBuilder=new StringBuilder();	//create a string builder for constructing the accept string
						for(final ContentType contentType:acceptedContentTypes) {	//for each accepted content type
							if(acceptStringBuilder.length()>0) {	//if this is not the first accepted content type
								acceptStringBuilder.append(COMMA_CHAR);	//separate the accepted content types
							}
							acceptStringBuilder.append(contentType.toBaseTypeString());	//append the base accepted content type
						}
						context.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_ACCEPT, acceptStringBuilder.toString());	//accept="acceptedContentTypes"							
					}
				}
		*/
		/*TODO fix if we want to
				final ResourceImport encodedValue=getEncodedValue();	//get the encoded resource import, if there is one
				if(encodedValue!=null) {	//if there is a value
					final String name=encodedValue.getName();	//get the resource import name, if there is one; use the full name that was given originally, so we can match the original value round-trip
					if(name!=null) {	//if we have a resource import name
						context.writeAttribute(null, ATTRIBUTE_VALUE, name);	//value="name"
					}
				}
		*/
	}

}
