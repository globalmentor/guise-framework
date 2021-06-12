/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

import com.globalmentor.net.MediaType;

import static com.globalmentor.java.Classes.*;

/**
 * A model for a label and descriptive information.
 * @author Garret Wilson
 */
public interface InfoModel extends LabelModel {

	/** The description bound property. */
	public static final String DESCRIPTION_PROPERTY = getPropertyName(InfoModel.class, "description");
	/** The description content type bound property. */
	public static final String DESCRIPTION_CONTENT_TYPE_PROPERTY = getPropertyName(InfoModel.class, "descriptionContentType");
	/** The info bound property. */
	public static final String INFO_PROPERTY = getPropertyName(InfoModel.class, "info");
	/** The info content type bound property. */
	public static final String INFO_CONTENT_TYPE_PROPERTY = getPropertyName(InfoModel.class, "infoContentType");

	/** @return The description text, such as might appear in a flyover, or <code>null</code> if there is no description. */
	public String getDescription();

	/**
	 * Sets the description text, such as might appear in a flyover. This is a bound property.
	 * @param newDescription The new text of the description, such as might appear in a flyover.
	 * @see #DESCRIPTION_PROPERTY
	 */
	public void setDescription(final String newDescription);

	/** @return The content type of the description text. */
	public MediaType getDescriptionContentType();

	/**
	 * Sets the content type of the description text. This is a bound property.
	 * @param newDescriptionContentType The new description text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #DESCRIPTION_CONTENT_TYPE_PROPERTY
	 */
	public void setDescriptionContentType(final MediaType newDescriptionContentType);

	/** @return The advisory information text, such as might appear in a tooltip, or <code>null</code> if there is no advisory information. */
	public String getInfo();

	/**
	 * Sets the advisory information text, such as might appear in a tooltip. This is a bound property.
	 * @param newInfo The new text of the advisory information, such as might appear in a tooltip.
	 * @see #INFO_PROPERTY
	 */
	public void setInfo(final String newInfo);

	/** @return The content type of the advisory information text. */
	public MediaType getInfoContentType();

	/**
	 * Sets the content type of the advisory information text. This is a bound property.
	 * @param newInfoContentType The new advisory information text content type.
	 * @throws NullPointerException if the given content type is <code>null</code>.
	 * @throws IllegalArgumentException if the given content type is not a text content type.
	 * @see #INFO_CONTENT_TYPE_PROPERTY
	 */
	public void setInfoContentType(final MediaType newInfoContentType);

}
