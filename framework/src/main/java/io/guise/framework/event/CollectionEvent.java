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
 * An event indicating a collection has been modified. If a single element was replaced both an added and removed element will be provided. If neither an added
 * nor a removed element are provided, the event represents a general collection modification.
 * @param <E> The type of elements contained in the collection.
 * @author Garret Wilson
 */
public class CollectionEvent<E> extends AbstractGuiseEvent {

	private static final long serialVersionUID = 857542877255183785L;

	/** The element that was added to the collection, or <code>null</code> if no element was added or it is unknown whether or which elements were added. */
	private E addedElement;

	/** @return The element that was added to the collection, or <code>null</code> if no element was added or it is unknown whether or which elements were added. */
	public E getAddedElement() {
		return addedElement;
	}

	/** The element that was removed from the collection, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed. */
	private E removedElement;

	/**
	 * @return The element that was removed from the collection, or <code>null</code> if no element was added or it is unknown whether or which elements were
	 *         removed.
	 */
	public E getRemovedElement() {
		return removedElement;
	}

	/**
	 * Source constructor for general collection modification.
	 * @param source The object on which the event initially occurred.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public CollectionEvent(final Object source) {
		this(source, null, null); //construct the class with no known modification values
	}

	/**
	 * Source constructor for an added and/or removed element.
	 * @param source The object on which the event initially occurred.
	 * @param addedElement The element that was added to the collection, or <code>null</code> if no element was added or it is unknown whether or which elements
	 *          were added.
	 * @param removedElement The element that was removed from the collection, or <code>null</code> if no element was removed or it is unknown whether or which
	 *          elements were removed.
	 * @throws NullPointerException if the given source is <code>null</code>.
	 */
	public CollectionEvent(final Object source, final E addedElement, final E removedElement) {
		super(source); //construct the parent class
		this.addedElement = addedElement;
		this.removedElement = removedElement;
	}
}
