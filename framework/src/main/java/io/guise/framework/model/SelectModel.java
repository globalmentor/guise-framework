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

import java.beans.PropertyVetoException;
import java.util.Collection;

/**
 * A model for selecting one or more values from a collection. The model must be thread-safe, synchronized on itself. Any iteration over values should include
 * synchronization on the instance of this interface.
 * @param <V> The type of values contained in the model.
 * @author Garret Wilson
 */
public interface SelectModel<V> extends ValueModel<V>, Collection<V> {

	/**
	 * Replaces the first occurrence in the of the given value with its replacement. This method ensures that another thread does not change the model while the
	 * search and replace operation occurs.
	 * @param oldValue The value for which to search.
	 * @param newValue The replacement value.
	 * @return Whether the operation resulted in a modification of the model.
	 */
	public boolean replace(final V oldValue, final V newValue);

	/**
	 * Determines the selected value. This method delegates to the selection strategy. If more than one value is selected, the lead selected value will be
	 * returned.
	 * @return The value currently selected, or <code>null</code> if no value is currently selected.
	 */
	public V getSelectedValue();

	/**
	 * Determines the selected values. This method delegates to the selection strategy.
	 * @return The values currently selected.
	 */
	public V[] getSelectedValues();

	/**
	 * Sets the selected values. If a value occurs more than one time in the model, all occurrences of the value will be selected. Values that do not occur in the
	 * select model will be ignored. If the value change is vetoed by the installed validator, the validation exception will be accessible via
	 * {@link PropertyVetoException#getCause()}. This method delegates to the selection strategy.
	 * @param values The values to select.
	 * @throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	 */
	public void setSelectedValues(final V... values) throws PropertyVetoException;

}
