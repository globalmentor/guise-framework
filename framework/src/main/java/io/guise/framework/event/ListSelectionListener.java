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
 * An object that listens for list selection modification events.
 * @param <V> The type of values selected.
 * @author Garret Wilson
 */
public interface ListSelectionListener<V> extends GuiseEventListener {

	/**
	 * Called when a selection changes.
	 * @param selectionEvent The event indicating the source of the event and the selectionmodifications.
	 */
	public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent);

}
