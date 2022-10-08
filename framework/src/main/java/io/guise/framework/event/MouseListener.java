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
 * An object that listens for mouse events.
 * @author Garret Wilson
 */
public interface MouseListener extends GuiseEventListener {

	/**
	 * Called when the mouse clicks the target.
	 * @param mouseClickEvent The event providing mouse information
	 */
	public void mouseClicked(final MouseClickEvent mouseClickEvent);

	/**
	 * Called when the mouse enters the target.
	 * @param mouseEnterEvent The event providing mouse information
	 */
	public void mouseEntered(final MouseEnterEvent mouseEnterEvent);

	/**
	 * Called when the mouse exits the target.
	 * @param mouseExitEvent The event providing mouse information
	 */
	public void mouseExited(final MouseExitEvent mouseExitEvent);

}
