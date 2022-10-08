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

/**
 * An object that allows the registration of edit listeners.
 * @author Garret Wilson
 */
public interface EditListenable {

	/**
	 * Adds an edit listener.
	 * @param editListener The edit listener to add.
	 */
	public void addEditListener(final EditListener editListener);

	/**
	 * Removes an edit listener.
	 * @param editListener The edit listener to remove.
	 */
	public void removeEditListener(final EditListener editListener);

}
