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

package io.guise.framework.input;

import java.util.Set;

import static java.util.Collections.*;
import static java.util.Objects.*;

import com.globalmentor.model.AbstractHashObject;

import static com.globalmentor.java.Enums.*;

/**
 * An abstract encapsulation of user input from a gesture.
 * @author Garret Wilson
 */
public abstract class AbstractGestureInput extends AbstractHashObject implements GestureInput {

	/** The keys that were pressed when this input occurred. */
	private final Set<Key> keys;

	@Override
	public Set<Key> getKeys() {
		return keys;
	}

	@Override
	public boolean hasAltKey() {
		return getKeys().contains(Key.ALT_LEFT) || getKeys().contains(Key.ALT_RIGHT); //see if an Alt key is included in the key set
	}

	@Override
	public boolean hasControlKey() {
		return getKeys().contains(Key.CONTROL_LEFT) || getKeys().contains(Key.CONTROL_RIGHT); //see if a Control key is included in the key set
	}

	@Override
	public boolean hasShiftKey() {
		return getKeys().contains(Key.SHIFT_LEFT) || getKeys().contains(Key.SHIFT_RIGHT); //see if a Shift key is included in the key set
	}

	/**
	 * Keys constructor.
	 * @param keys The keys that were pressed when this input occurred.
	 * @throws NullPointerException if the given keys is <code>null</code>.
	 */
	public AbstractGestureInput(final Key... keys) {
		super((Object[])keys); //construct the parent class
		this.keys = unmodifiableSet(createEnumSet(Key.class, requireNonNull(keys, "Keys cannot be null."))); //save a read-only set of the keys
	}

}
