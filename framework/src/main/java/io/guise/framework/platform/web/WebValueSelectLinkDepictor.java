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

import static com.globalmentor.html.spec.HTML.*;

/**
 * Strategy for rendering an action model control as an XHTML <code>&lt;a&gt;</code> element that can be selected, with values represented by icons.
 * @param <V> The type of value represented by the component.
 * @param <C> The type of component being controlled.
 * @author Garret Wilson
 */
public class WebValueSelectLinkDepictor<V, C extends SelectActionControl & ActionValueControl<V>> extends WebSelectLinkDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version writes the value icon, if available.
	 * </p>
	 */
	@Override
	protected void writeSupplementaryIcons() throws IOException {
		super.writeSupplementaryIcons(); //write the default suppementary icons
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final V value = component.getValue(); //get the selected value
		final URI valueIcon = component.getValueGlyphURI(value); //get the value icon, if any
		if(valueIcon != null) { //if there is a selected icon
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(valueIcon).toString()); //src="icon"
			//TODO fix to use description or something else, and always write an alt, even if there is no information
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, "value"); //alt="value" TODO i18n
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</html:img>
		}
	}
}
