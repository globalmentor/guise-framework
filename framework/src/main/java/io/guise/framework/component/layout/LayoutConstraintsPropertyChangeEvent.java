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

package io.guise.framework.component.layout;

import static java.util.Objects.*;

import com.globalmentor.beans.GenericPropertyChangeEvent;

import io.guise.framework.component.Component;

/**
 * An event indicating that a property of layout constraints changed. The source is always the layout object. The component and constraints are also provided.
 * @param <T> The type of layout constraints associated with the component.
 * @param <V> The type of property value.
 * @author Garret Wilson
 */
public class LayoutConstraintsPropertyChangeEvent<T extends Constraints, V> extends GenericPropertyChangeEvent<V> { //TODO maybe cast the source to a layout in the class if needed by a use case

	private static final long serialVersionUID = 5297568761000291693L;

	/** The component for which a constraint value changed. */
	private final Component component;

	/** @return The component for which a constraint value changed. */
	public Component getComponent() {
		return component;
	}

	/** The constraints for which a value changed. */
	private final T constraints;

	/** @return The constraints for which a value changed. */
	public T getConstraints() {
		return constraints;
	}

	/**
	 * Source, component, constraint, property name, with old and new value constructor.
	 * @param source The layout that fired the event.
	 * @param component The component for which a constraint value changed.
	 * @param constraints The constraints for which a value changed.
	 * @param propertyName The programmatic name of the property that was changed.
	 * @param oldValue The old value of the property, or <code>null</code> if no old value is not available.
	 * @param newValue The new value of the property, or <code>null</code> if the new value is not available.
	 * @throws NullPointerException if the given component and/or constraints is <code>null</code>.
	 */
	public LayoutConstraintsPropertyChangeEvent(final Layout<T> source, final Component component, final T constraints, final String propertyName,
			final V oldValue, V newValue) {
		super(source, propertyName, oldValue, newValue); //construct the parent class
		this.component = requireNonNull(component, "Component cannot be null."); //TODO remove checkNull(), as this is now checked in the call to getSession()
		this.constraints = requireNonNull(constraints, "Constraints cannot be null.");
	}

}
