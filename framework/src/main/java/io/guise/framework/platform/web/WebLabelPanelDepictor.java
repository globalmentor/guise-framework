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

import io.guise.framework.component.LayoutComponent;

import static com.globalmentor.html.spec.HTML.*;
import static io.guise.framework.platform.web.GuiseCSSStyleConstants.*;

/**
 * Strategy for rendering a labeled panel as a series of XHTML elements. This controller can be substituted for {@link WebFieldsetDepictor} for a different
 * rendering of group panels.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 * @see WebFieldsetDepictor
 */
public class WebLabelPanelDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C> {

	/** Default constructor using the XHTML <code>&lt;div&gt;</code> element. */
	public WebLabelPanelDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV); //represent <xhtml:div>
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version wraps the component in a decorator element.
	 * </p>
	 */
	@Override
	protected void depictBegin() throws IOException {
		super.depictBegin(); //do the default beginning rendering
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		writeIDClassAttributes(null, null); //write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute(); //write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX)); //write the label for the body, if there is a label				
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV); //<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX); //write the ID and class for the body element
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version closes the decorator elements.
	 * </p>
	 */
	@Override
	protected void depictEnd() throws IOException {
		getDepictContext().writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV); //</xhtml:div> (component-body)
		writeErrorMessage(); //write the error message, if any
		super.depictEnd(); //do the default ending rendering
	}

}
