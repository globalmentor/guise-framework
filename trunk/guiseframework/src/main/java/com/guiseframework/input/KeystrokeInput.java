/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.input;

import com.globalmentor.java.Objects;

import static com.globalmentor.java.Objects.*;

/**An encapsulation of key stroke input.
@author Garret Wilson
*/
public class KeystrokeInput extends AbstractGestureInput implements KeyboardInput
{

	/**The key that was pressed.*/
	private final Key key;

		/**The key that was pressed.*/
		public Key getKey() {return key;}

	/**Key and keys constructor.
	@parma key The key that was pressed.
	@param keys The keys that were pressed when this input occurred.
	@exception NullPointerException if the given key and/or keys is <code>null</code>.
	*/
	public KeystrokeInput(final Key key, final Key... keys)
	{
		super(keys);	//construct the parent class
		this.key=checkInstance(key, "Key cannot be null.");	//save the key
	}

	/**Returns the hash code of this object.
	This version extends the hash code of the underlying objects with the the key.
	@return The hash code of this object.
	*/
	public int hashCode()
	{
		return Objects.getHashCode(super.hashCode(), key);	//extend the hash code with the key
	}
	
	/**Determines if this object equals another object.
	Besides the default checks, this version ensures that the keys are equal.
	@param object The object to compare with this object.
	@return <code>true</code> if the given object is considered equal to this object.
	*/
	public boolean equals(final Object object)
	{
		return super.equals(object) && getKey()==((KeystrokeInput)object).getKey();	//if the default checks pass, the object is of the correct type; compare keys
	}

	/**@return A string representation of this object.*/
	public String toString()
	{
		return getKey().toString()+' '+super.toString();	//add the key representation to the string
	}
}
