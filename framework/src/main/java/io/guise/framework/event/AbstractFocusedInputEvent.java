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
 * An abstract input event such as a command that is directed towards the component with input focus.
 * @author Garret Wilson
 */
public abstract class AbstractFocusedInputEvent extends AbstractInputEvent implements FocusedInputEvent {

	private static final long serialVersionUID = -2001046083520014709L;

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public AbstractFocusedInputEvent(final Object source) {
		super(source); //construct the parent class
	}

}
