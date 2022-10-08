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

import static com.globalmentor.java.Classes.*;

import io.guise.framework.model.Enableable;

/**
 * A component that accepts user interaction to manipulate a data model.
 * @author Garret Wilson
 */
public interface Control extends Component, InputFocusableComponent, Enableable {

	/** The input status of a control. */
	public enum Status {
		/** The input is provisionally incorrect. */
		WARNING,

		/** The input is incorrect. */
		ERROR;
	}

	/** The status bound property. */
	public static final String STATUS_PROPERTY = getPropertyName(Control.class, "status");

	/** @return The status of the current user input, or <code>null</code> if there is no status to report. */
	public Status getStatus();

	/** Resets the control to its default value. */
	public void reset();

}
