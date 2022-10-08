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

import java.net.URI;
import java.util.*;

import io.guise.framework.platform.DepictContext;
import io.guise.framework.platform.PlatformCommandMessage;

/**
 * A command message to or from the web platform on which objects are being depicted. All parameters with {@link URI} values will are considered to be
 * application-relative; before depiction they will be dereferenced and resolved using {@link DepictContext#getDepictionURI(URI, String...)}
 * @param <C> The type of command.
 * @author Garret Wilson
 */
public interface WebPlatformCommandMessage<C extends Enum<C> & WebPlatformCommand> extends PlatformCommandMessage<C> {

	/** @return The read-only map of parameters. */
	public Map<String, Object> getParameters();

}
