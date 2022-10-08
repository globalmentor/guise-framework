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
 * Strategy for rendering an action control as an XHTML <code>&lt;a&gt;</code> element that can be selected.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebSelectLinkDepictor<C extends SelectActionControl> extends WebLinkDepictor<C> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version writes the selected icon, if available.
	 * </p>
	 */
	@Override
	protected void writeSupplementaryIcons() throws IOException {
		super.writeSupplementaryIcons(); //write the default suppementary icons TODO i18n direction
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final C component = getDepictedObject(); //get the component
		final URI selectedIcon = component.isSelected() ? component.getSelectedGlyphURI() : component.getUnselectedGlyphURI(); //we'll determine the selected icon based upon selected state
		if(selectedIcon != null) { //if there is a selected icon
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true); //<xhtml:img>
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(selectedIcon).toString()); //src="icon"
			//TODO fix to use description or something else, and always write an alt, even if there is no information
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, "selected"); //alt="selected" TODO i18n
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG); //</html:img>
		}
	}

}
