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

package io.guise.framework.event;

import java.net.URI;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;

/**
 * An abstract that listens for action events and keeps information for navigating in response.
 * @author Garret Wilson
 */
public abstract class AbstractNavigateActionListener implements ActionListener {

	/** The requested navigation URI. */
	private final URI navigationURI;

	/** @return The requested navigation URI. */
	public URI getNavigationURI() {
		return navigationURI;
	}

	/** The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport. */
	private final String viewportID;

	/** @return The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.. */
	public String getViewportID() {
		return viewportID;
	}

	/**
	 * Constructs a listener to navigate to the provided path.
	 * @param navigationPath A path that is either relative to the application context path or is absolute.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case
	 *           {@link #AbstractNavigateActionListener(URI)} should be used instead).
	 */
	public AbstractNavigateActionListener(final URIPath navigationPath) {
		this(navigationPath.toURI()); //construct the class with the URI form of the path
	}

	/**
	 * Constructs a listener to navigate to the provided URI.
	 * @param navigationURI The URI for navigation when the action occurs.
	 * @throws NullPointerException if the given navigation URI is <code>null</code>.
	 */
	public AbstractNavigateActionListener(final URI navigationURI) {
		this(navigationURI, null); //construct the class with navigation to the current viewport 
	}

	/**
	 * Constructs a listener to navigate to the provided URI in the identified viewport.
	 * @param navigationURI The URI for navigation when the action occurs.
	 * @param viewportID The ID of the viewport in which navigation should occur, or <code>null</code> if navigation should occur in the current viewport.
	 * @throws NullPointerException if the given navigation URI is <code>null</code>.
	 */
	public AbstractNavigateActionListener(final URI navigationURI, final String viewportID) {
		this.navigationURI = requireNonNull(navigationURI, "Navigation URI cannot be null.");
		this.viewportID = viewportID;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation requests navigation from the session.
	 * </p>
	 */
	@Override
	public void actionPerformed(final ActionEvent actionEvent) {
		actionEvent.getSession().navigate(getNavigationURI(), getViewportID()); //request that the session navigate to the configured URI in the identified viewport, if any
	}

}
