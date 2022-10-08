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

package io.guise.framework.prototype;

import java.net.URI;

import io.guise.framework.model.Enableable;

/**
 * Contains enableable prototype information, appropriate for a control, for example.
 * @author Garret Wilson
 */
public abstract class AbstractEnableablePrototype extends AbstractPrototype implements Enableable {

	/** Whether the control is enabled and can receive user input. */
	private boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(final boolean newEnabled) {
		if(enabled != newEnabled) { //if the value is really changing
			final boolean oldEnabled = enabled; //get the old value
			enabled = newEnabled; //actually change the value
			firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled)); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public AbstractEnableablePrototype() {
		this(null); //construct the class with no label
	}

	/**
	 * Label constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public AbstractEnableablePrototype(final String label) {
		this(label, null); //construct the label model with no icon
	}

	/**
	 * Label and icon constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	 */
	public AbstractEnableablePrototype(final String label, final URI icon) {
		super(label, icon); //construct the parent class
	}
}
