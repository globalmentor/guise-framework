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

package io.guise.framework.model;

import java.net.URI;
import java.util.Objects;

/**
 * A default implementation of a model for an image.
 * @author Garret Wilson
 */
public class DefaultImageModel extends AbstractModel implements ImageModel {

	/** The image URI, which may be a resource URI, or <code>null</code> if there is no image URI. */
	private URI imageURI;

	@Override
	public URI getImageURI() {
		return imageURI;
	}

	@Override
	public void setImageURI(final URI newImageURI) {
		if(!Objects.equals(imageURI, newImageURI)) { //if the value is really changing
			final URI oldImageURI = imageURI; //get the old value
			imageURI = newImageURI; //actually change the value
			firePropertyChange(IMAGE_URI_PROPERTY, oldImageURI, newImageURI); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public DefaultImageModel() {
		this(null); //construct the class with no image
	}

	/**
	 * Image URI constructor.
	 * @param imageURI The image URI, which may be a resource URI, or <code>null</code> if there is no image URI.
	 */
	public DefaultImageModel(final URI imageURI) {
		this.imageURI = imageURI; //save the image URI
	}

}
