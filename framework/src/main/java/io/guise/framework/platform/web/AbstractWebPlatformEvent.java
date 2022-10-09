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

package io.guise.framework.platform.web;

import io.guise.framework.platform.AbstractPlatformEvent;

/**
 * The base class for events to or from the web platform.
 * @author Garret Wilson
 */
public abstract class AbstractWebPlatformEvent extends AbstractPlatformEvent implements WebPlatformEvent {

	private static final long serialVersionUID = 3792795749758123644L;

	/**
	 * Source constructor.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public AbstractWebPlatformEvent(final Object source) {
		super(source); //construct the parent class
	}

}
