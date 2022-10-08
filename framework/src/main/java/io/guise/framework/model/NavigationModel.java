/*
 * Copyright © 2005-2011 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

import com.globalmentor.net.URIPath;

import static com.globalmentor.java.Classes.*;

/**
 * A model for identifying a navigation path.
 * @author Garret Wilson
 */
public interface NavigationModel extends Model {

	/** The navigation path bound property, of type {@link URIPath}. */
	public static final String NAVIGATION_PATH_PROPERTY = getPropertyName(NavigationModel.class, "navigationPath");

	/** @return The navigation path, or <code>null</code> if the navigation path is not available. */
	public URIPath getNavigationPath();

	/**
	 * Sets the navigation path This is a bound property.
	 * @param newNavigationPath The new navigation path, or <code>null</code> if the navigation path is not available.
	 * @see #NAVIGATION_PATH_PROPERTY
	 */
	public void setNavigationPath(final URIPath newNavigationPath);

}
