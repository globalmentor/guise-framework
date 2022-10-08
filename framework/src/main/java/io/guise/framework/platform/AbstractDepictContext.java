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

package io.guise.framework.platform;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static java.util.Objects.*;

import static java.util.Collections.*;

import com.globalmentor.beans.BoundPropertyObject;
import com.globalmentor.net.URIPath;

import io.guise.framework.*;
import io.guise.framework.theme.Theme;

/**
 * Abstract encapsulation of information related to the current depiction.
 * @author Garret Wilson
 */
public abstract class AbstractDepictContext extends BoundPropertyObject implements DepictContext {

	/** The Guise user session of which this context is a part. */
	private final GuiseSession session;

	@Override
	public GuiseSession getSession() {
		return session;
	}

	/** The platform on which Guise objects are depicted. */
	private final Platform platform;

	@Override
	public Platform getPlatform() {
		return platform;
	}

	/** The destination with which this context is associated. */
	private final Destination destination;

	@Override
	public Destination getDestination() {
		return destination;
	}

	/** The URIs of the styles for this context, in order. */
	private final Iterable<URI> styles;

	@Override
	public Iterable<URI> getStyles() {
		return styles;
	}

	/**
	 * Guise session constructor.
	 * @param session The Guise user session of which this context is a part.
	 * @param destination The destination with which this context is associated.
	 * @throws NullPointerException if the given session and/or destination is null.
	 * @throws IOException If there was an I/O error loading a needed resource.
	 */
	public AbstractDepictContext(final GuiseSession session, final Destination destination) throws IOException {
		this.session = requireNonNull(session, "Session cannot be null."); //save the Guise session
		this.destination = requireNonNull(destination, "Destination cannot be null."); //save the destination
		this.platform = session.getPlatform(); //save the platform
		final GuiseApplication application = session.getApplication(); //get the application
		final List<URI> styleURIs = new ArrayList<URI>(); //create a new list to hold styles
		//theme styles
		/*TODO re-implement loading theme styles.
		Theme theme = session.getTheme(); //get the current session theme TODO this currently can't be null; finalize the API
		while(theme != null) { //while there are themes
			for(final URFResource style : theme.getStyles()) { //get the styles
				final URI styleURI = style.getURI(); //get this style's URI
				if(styleURI != null) { //if this style has a URI
					styleURIs.add(0, styleURI); //add this style URI to the list at the beginning so that resolving parent styles get listed first
				}
			}
			theme = theme.getParent(); //get the theme's parent, if any
		}
		*/
		//application style
		final URI applicationStyleURI = application.getStyleURI(); //get the application style
		if(applicationStyleURI != null) { //if there is a style TODO make sure the style is a CSS style
			styleURIs.add(applicationStyleURI); //add the application style
		}
		if(destination instanceof ComponentDestination) { //if this is a component destination TODO improve; should we require a component destination for all context instances?
			//destination style
			final URI destinationStyleURI = ((ComponentDestination)destination).getStyle(); //get the destination style, if any
			if(destinationStyleURI != null) { //if there is a style TODO make sure the style is a CSS style
				styleURIs.add(destinationStyleURI); //add the destination style
			}
		}
		this.styles = unmodifiableList(styleURIs); //save an unmodifiable version of the style URIs
	}

	@Override
	public URI getDepictionURI(final URIPath navigationPath, final String... suffixes) {
		return getSession().getDepictionURI(navigationPath, suffixes); //ask the session for the depiction URI
	}

	@Override
	public URI getDepictionURI(final URI navigationURI, final String... suffixes) {
		return getSession().getDepictionURI(navigationURI, suffixes); //ask the session for the depiction URI
	}

}
