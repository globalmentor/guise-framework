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

package io.guise.framework.model;

import static com.globalmentor.java.Classes.*;

/**
 * An object that can be selected.
 * @author Garret Wilson
 */
public interface Selectable {

	/** The bound property of whether the object is selected. */
	public static final String SELECTED_PROPERTY = getPropertyName(Selectable.class, "selected");

	/** @return Whether the object is selected. */
	public boolean isSelected();

	/**
	 * Sets whether the object is selected. This is a bound property of type <code>Boolean</code>.
	 * @param newSelected <code>true</code> if the object should be selected, else <code>false</code>.
	 * @see #SELECTED_PROPERTY
	 */
	public void setSelected(final boolean newSelected);

}
