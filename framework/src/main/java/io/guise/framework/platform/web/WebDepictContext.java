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

import java.net.URI;
import java.util.*;

import com.globalmentor.css.spec.CSS;

import io.guise.framework.component.layout.Orientation;
import io.guise.framework.geometry.Extent;
import io.guise.framework.model.ui.PresentationModel;
import io.guise.framework.platform.XHTMLDepictContext;
import io.guise.framework.style.*;

/**
 * Information related to the current depiction on the web platform.
 * @author Garret Wilson
 */
public interface WebDepictContext extends XHTMLDepictContext {

	/** @return The web platform on which Guise objects are depicted. */
	public WebPlatform getPlatform();

	/** @return Whether quirks mode is being used. */
	public boolean isQuirksMode();

	/**
	 * Returns a string representation of the provided style declarations. This method performs special processing on the following properties, including
	 * generating user-agent-specific styles to allow proper display on certain browsers:
	 * <ul>
	 * <li>{@value CSS#CSS_PROP_COLOR} with a value of {@link Color} and an alpha less than 1.0.</li>
	 * <li>{@value CSS#CSS_PROP_CURSOR} with a value of {@link URI}, interpreted as a predefined cursor (one of {@link Cursor#getURI()}) or as a URI to a
	 * custom cursor; URI references are allowed in either.</li>
	 * <li>{@value CSS#CSS_PROP_DISPLAY} with a value of {@value CSS#CSS_DISPLAY_INLINE_BLOCK}.</li>
	 * <li>{@value CSS#CSS_PROP_FONT_WEIGHT} with a value of {@link Number}, interpreted in terms of {@link PresentationModel#FONT_WEIGHT_NORMAL} and
	 * {@link PresentationModel#FONT_WEIGHT_BOLD}.</li>
	 * <li>{@value CSS#CSS_PROP_MAX_WIDTH} or {@value CSS#CSS_PROP_MAX_HEIGHT} with a pixel value of {@link Extent}.</li>
	 * <li>{@value CSS#CSS_PROP_OPACITY} with a value of {@link Number}.</li>
	 * </ul>
	 * These styles include the CSS property {@value CSS#CSS_PROP_DISPLAY} with a value of {@value CSS#CSS_DISPLAY_INLINE_BLOCK}. This method supports
	 * values of the following types:
	 * <ul>
	 * <li>{@link Color}</li>
	 * <li>{@link Cursor}</li>
	 * <li>{@link Extent}</li>
	 * <li>{@link FontStyle}</li>
	 * <li>{@link LineStyle}</li>
	 * <li>{@link List}</li>
	 * <li>{@link URI} with URI references allowed</li>
	 * <li>An array of objects, each of which will be serialized and separated by spaces.</li>
	 * </ul>
	 * All other values will be added using {@link Object#toString()}.
	 * @param styles The map of styles to write, each keyed to a CSS style property.
	 * @param orientation The orientation of the component for which the style is being produced.
	 * @return A string containing the given CSS properties and styles.
	 */
	public String getCSSStyleString(final Map<String, Object> styles, final Orientation orientation);
}
