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

package io.guise.framework.component;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.layout.Layout;
import io.guise.framework.component.layout.RegionLayout;

/**
 * Default implementation of a panel that represents a point of modal navigation with default region layout.
 * @param <R> The type of modal result this modal panel produces.
 * @author Garret Wilson
 * @see RegionLayout
 */
public class DefaultModalNavigationPanel<R> extends AbstractModalNavigationPanel<R> {

	/** Default constructor with a default region layout. */
	public DefaultModalNavigationPanel() {
		this(new RegionLayout()); //default to a region layout
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	public DefaultModalNavigationPanel(final Layout<?> layout) {
		super(layout); //construct the parent class
	}

	@Override
	public void endModal(final R result) {
		setResult(result); //update the result
		getSession().endModalNavigation(this); //end modal navigation for this modal frame
	}

}
