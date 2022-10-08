/*
 * Copyright © 2011 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.platform.web.facebook;

import java.io.IOException;
import java.net.URI;

import com.globalmentor.net.Host;
import com.globalmentor.net.URIPath;
import com.globalmentor.net.URIQueryParameter;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.facebook.LikeButton;
import io.guise.framework.platform.web.AbstractSimpleWebComponentDepictor;
import io.guise.framework.platform.web.WebDepictContext;
import io.guise.framework.platform.web.WebUserAgentProduct;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.HTTP.*;
import static io.guise.framework.platform.web.facebook.Facebook.*;

/**
 * Strategy for rendering a Facebook like button as an XHTML <code>&lt;iframe&gt;</code> element.
 * @author Garret Wilson
 */
public class WebIFrameLikeButtonDepictor extends AbstractSimpleWebComponentDepictor<LikeButton> {

	/** Default constructor using the XHTML <code>&lt;iframe&gt;</code> element. */
	public WebIFrameLikeButtonDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_IFRAME); //represent <xhtml:iframe>
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final GuiseSession session = getSession(); //get the session
		final LikeButton component = getDepictedObject(); //get the component
		final URIPath navigationPath = component.getNavigationPath(); //get the navigation path
		if(navigationPath != null) { //if there is a path to like
			URI depictionURI = session.getDepictionRootURI().resolve(depictContext.getDepictionURI(navigationPath)); //get the absolute depiction URI
			//Facebook doesn't like the Like button to use localhost
			if(Host.LOCALHOST.getName().equals(depictionURI.getHost()) && session.getApplication().isDebug()) { //when on localhost in debug mode
				depictionURI = changeHost(depictionURI, Host.EXAMPLE_COM.getName()); //switch to using example.com, just so the examples will show up
			}
			final URI facebookLikeBaseURI = matchSchemeSecurity(FACEBOOK_PLUGIN_LIKE_URI, session.getDepictionRootURI()); //get the base URI for Facebook, matching the scheme security of the depiction URI
			final URI srcURI = appendQueryParameters(facebookLikeBaseURI, new URIQueryParameter("href", depictionURI.toASCIIString()), new URIQueryParameter("send",
					Boolean.toString(true)), new URIQueryParameter("layout", "standard"), new URIQueryParameter("width", Integer.toString(450)), new URIQueryParameter(
					"show_faces", Boolean.toString(true)), new URIQueryParameter("action", "like"), new URIQueryParameter("colorscheme", "light"), new URIQueryParameter(
					"font", null), new URIQueryParameter("height", Integer.toString(80))); //TODO use constants; allow configuration
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_SRC, srcURI.toASCIIString()); //src
		}
		depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_SCROLLING, IFRAME_SCROLLING_NO); //scrolling="no"
		depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_FRAMEBORDER, "0"); //frameborder="0"
		depictContext.writeAttribute(null, ATTRIBUTE_STYLE, "border:none; overflow:hidden; width:450px; height:80px;"); //TODO don't hard-code; allow flexible sizes
		if(getPlatform().getClientProduct().getBrand() == WebUserAgentProduct.Brand.INTERNET_EXPLORER) { //if the user agent is IE, use the special attributes
			depictContext.writeAttribute(null, ELEMENT_IFRAME_ATTRIBUTE_ALLOW_TRANSPARENCY, Boolean.toString(true)); //allowTransparency="true"
		}

		/*TODO del
				<iframe src="http://www.facebook.com/plugins/like.php?href=http%3A%2F%2Fwww.garretwilson.com%2F&amp;send=true&amp;layout=standard&amp;width=450&amp;show_faces=true&amp;action=like&amp;" +
						"colorscheme=light&amp;font&amp;height=80" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:450px; height:80px;" allowTransparency="true"></iframe>
		*/
	}
}
