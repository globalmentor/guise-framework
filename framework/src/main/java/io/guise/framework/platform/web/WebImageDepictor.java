/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import com.globalmentor.html.spec.HTML;

import static com.globalmentor.css.spec.CSS.*;
import static com.globalmentor.html.spec.HTML.*;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.geometry.Axis;
import io.guise.framework.model.AbstractModel;

/**
 * Strategy for rendering an image component an XHTML <code>&lt;img&gt;</code> element. The {@link ImageComponent#getLabel()} or
 * {@link ImageComponent#getDescription()} will be used as the {@value HTML#ELEMENT_IMG_ATTRIBUTE_ALT} attribute if available.
 * <p>
 * This depictor supports {@link PendingImageComponent}.
 * </p>
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebImageDepictor<C extends ImageComponent> extends AbstractSimpleWebComponentDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version requests a poll interval if the image is pending.
	 * </p>
	 */
	@Override
	public void installed(final C component) {
		super.installed(component); //perform the default installation
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending()) { //if the image is pending
			getPlatform().requestPollInterval(component, 2000); //indicate that polling should occur for this image TODO use a constant			
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version requests any poll interval.
	 * </p>
	 */
	@Override
	public void uninstalled(final C component) {
		if(component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending()) { //if the image is pending
			getPlatform().discontinuePollInterval(component); //indicate that polling should no longer occur for this image; another depictor can request polling if necessary
		}
		super.uninstalled(component); //perform the default uninstallation
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation requests or discontinues a poll interval when the pending state changes.
	 * </p>
	 * @see PendingImageComponent#isImagePending()
	 */
	@Override
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent) {
		super.depictedObjectPropertyChange(propertyChangeEvent); //do the default property change functionality
		final C component = getDepictedObject(); //get the depicted object
		if(propertyChangeEvent.getSource() == getDepictedObject() && component instanceof PendingImageComponent
				&& PendingImageComponent.IMAGE_PENDING_PROPERTY.equals(propertyChangeEvent.getPropertyName())) { //if the image pending property is changing
			final WebPlatform webPlatform = getPlatform(); //get the web platform
			if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue())) { //if the image is now pending
				webPlatform.requestPollInterval(component, 2000); //indicate that polling should occur for this image TODO use a constant
			} else { //if the image is no longer pending
				webPlatform.discontinuePollInterval(component); //indicate that polling should no longer occur for this image
			}
		}
	}

	/** Default constructor using the XHTML <code>&lt;label&gt;</code> element. */
	public WebImageDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //represent <xhtml:img>, creating an empty element
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This adds layout fixes for images within tables.
	 * </p>
	 */
	@Override
	protected Map<String, Object> getBodyStyles() {
		final Map<String, Object> styles = super.getBodyStyles(); //get the default body styles
		final C component = getDepictedObject(); //get the component
		final Orientation orientation = component.getComponentOrientation(); //get this component's orientation
		if((orientation.getAxis(Flow.LINE) == Axis.X ? component.getLineExtent() : component.getPageExtent()) == null) { //if there is no preferred width and this image is within a fixed layout, set the maximum width of the image to keep large images from forcing a column width to be very large
			CompositeComponent parent = component.getParent(); //get this component's parent
			if(parent instanceof Container) { //if the parent is a container
				final Layout<?> layout = ((Container)parent).getLayout(); //get the container layout
				if(layout instanceof RegionLayout && ((RegionLayout)layout).isFixed()) { //if the container layout is a fixed region layout
					styles.put(CSS_PROP_MAX_WIDTH, "100%"); //indicate a maximum width of 100%				
				}
			}
		}
		return styles; //return the updated body styles
	}

	/**
	 * Determines the image URI to use for this component. If the delegate image is a {@link PendingImageComponent} with a pending image, this version return the
	 * {@link PendingImageComponent#getPendingImageURI()} value. Otherwise, this version returns the delegate image's {@link ImageComponent#getImageURI()} value.
	 * @return The image to use for the component, or <code>null</code> if there should not be an image.
	 */
	protected URI getImageURI() {
		final C component = getDepictedObject(); //get the component
		return component instanceof PendingImageComponent && ((PendingImageComponent)component).isImagePending() ? ((PendingImageComponent)component)
				.getPendingImageURI() : component.getImageURI(); //get the image URI to use, using the pending image URI if appropriate
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final GuiseSession session = getSession(); //get the session
		final C component = getDepictedObject(); //get the component
		final URI imageURI = getImageURI(); //get the component image URI
		if(imageURI != null) { //if there is an image URI
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(imageURI).toString()); //src="imageURI"
		}
		String alt = component.getLabel(); //get the component label, if there is one
		if(alt != null) { //if there is a label
			alt = AbstractModel.getPlainText(session.dereferenceString(alt), component.getLabelContentType()); //resolve the label and get plain text
		} else { //if there is no label
			alt = component.getDescription(); //get the component description, if there is one
			if(alt != null) {
				alt = AbstractModel.getPlainText(session.dereferenceString(alt), component.getDescriptionContentType()); //resolve the description and get plain text
			} else { //if there is no description
				alt = ""; //resort to an empty string
			}
		}
		depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, alt); //alt="alt"
	}

}
