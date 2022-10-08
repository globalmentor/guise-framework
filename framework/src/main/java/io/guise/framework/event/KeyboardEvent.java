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

import io.guise.framework.input.*;

/**
 * An event providing information on input from a keyboard.
 * @author Garret Wilson
 */
public interface KeyboardEvent extends GestureInputEvent, FocusedInputEvent {

	/** @return The key that was pressed. */
	public Key getKey();

	/** @return The input associated with this event, or <code>null</code> if there is no input associated with this event. */
	public KeystrokeInput getInput();

}
