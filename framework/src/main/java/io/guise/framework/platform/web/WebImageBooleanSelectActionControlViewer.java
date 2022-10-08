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
import java.net.URI;

import io.guise.framework.component.*;
import io.guise.framework.event.NavigateActionListener;

/**
 * Strategy for rendering an image select action control as an XHTML <code>&lt;img&gt;</code> inside a <code>&lt;a&gt;</code> element. If a link has a
 * {@link NavigateActionListener} as one of its action listeners, the generated <code>href</code> URI will be that of the listener, and a <code>target</code>
 * attribute will be set of the listener specifies a viewport ID.
 * <p>
 * This view uses the following attributes which are not in XHTML:
 * </p>
 * <ul>
 * <li><code>guise:originalSrc</code></li>
 * <li><code>guise:rolloverSrc</code></li>
 * </ul>
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebImageBooleanSelectActionControlViewer<C extends ImageBooleanSelectActionControl> extends WebImageActionControlDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the selected image if the component is selected and there is a selected image.
	 * </p>
	 * @see ImageComponent#getImageURI()
	 * @see ImageBooleanSelectActionControl#isSelected()
	 * @see ImageBooleanSelectActionControl#getRolloverImageURI()
	 */
	@Override
	protected URI getImageURI() {
		final C component = getDepictedObject(); //get the component
		URI image = null; //we'll determine the image
		final boolean isSelected = component.getValue().booleanValue(); //see if the component is selected
		if(isSelected) { //if the component is selected
			image = component.getSelectedImageURI(); //use the selected image
			if(image == null) { //if there is no selected image
				image = component.getRolloverImageURI(); //use the rollover image
			}
		}
		if(image == null) { //if the component is not selected, or there is no selected or rollover image image
			image = component.getImageURI(); //get the normal component image
		}
		return image; //return the image we determined
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the component's rollover image.
	 * </p>
	 * @see ImageBooleanSelectActionControl#getRolloverImageURI()
	 */
	@Override
	protected URI getRolloverImageURI() {
		return getDepictedObject().getRolloverImageURI(); //get the component rollover image
	}

	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		//TODO del		context.writeAttribute(null, "hidefocus", "true");	//hidefocus="true"	//TODO add to DTD; put in JavaScript to do this dynamically
	}

}
