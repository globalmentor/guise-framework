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

import static com.globalmentor.java.Objects.*;

import com.globalmentor.beans.*;

import io.guise.framework.model.Displayable;

/**
 * A {@link Boolean} property change listener that synchronizes a {@link Displayable} source's {@link Displayable#DISPLAYED_PROPERTY} by calling
 * {@link Displayable#setDisplayed(boolean)} with any new value that it receives. If there is no {@link Displayable} event source or no new value for any given
 * event, no action occurs.
 * @author Garret Wilson
 */
public class SynchronizeDisplayedPropertyChangeListener extends AbstractGenericPropertyChangeListener<Boolean> { //TODO turn this into a singleton

	@Override
	public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) {
		final Displayable displayableSource = asInstance(genericPropertyChangeEvent.getSource(), Displayable.class).orElse(null); //get the source as a Displayable
		if(displayableSource != null) { //if the source is displayable
			final Boolean newValue = genericPropertyChangeEvent.getNewValue(); //get the new value
			if(newValue != null) { //if there is a new value
				displayableSource.setDisplayed(newValue.booleanValue()); //update the displayable's displayed status to match
			}
		}
	}
}
