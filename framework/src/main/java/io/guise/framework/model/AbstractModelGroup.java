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

package io.guise.framework.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Objects.*;

/**
 * An abstract implementation of a group of similar models for providing such functions as communication or mutual exclusion. This class is thread safe.
 * @param <M> The type of model contained in the group.
 * @author Garret Wilson.
 */
public abstract class AbstractModelGroup<M extends Model> implements ModelGroup<M> {

	/** The set of models. */
	private final Set<M> modelSet = new CopyOnWriteArraySet<M>(); //create a thread-safe set that is very efficient on reads because it works on a copy of the set (which we don't mind; changing to the set occurs infrequently)

	/** @return The set of models. */
	protected Set<M> getModelSet() {
		return modelSet;
	}

	@Override
	public boolean contains(final Model model) {
		return modelSet.contains(requireNonNull(model, "Model cannot be null.")); //see if the set of models contains this model TODO check for class cast exception
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to {@link #addImpl(Model)}.
	 * </p>
	 */
	@Override
	public void add(final M model) {
		if(!contains(model)) { //if the group doesn't already contain the model
			addImpl(model); ///actually add the model to the model set
		}
	}

	/**
	 * Actual implementation of adding a model to the group.
	 * @param model The model to add to the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	protected void addImpl(final M model) {
		modelSet.add(requireNonNull(model, "Model cannot be null.")); //add this model to the model set
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version delegates to {@link #removeImpl(Model)}.
	 * </p>
	 */
	@Override
	public void remove(final M model) {
		if(contains(model)) { //if the group contains the model
			removeImpl(model); ///actually remove the model from the model set
		}
	}

	/**
	 * Actual implementation of removing a model from the group.
	 * @param model The model to remove from the group.
	 * @throws NullPointerException if the given model is <code>null</code>.
	 */
	protected void removeImpl(final M model) {
		modelSet.remove(requireNonNull(model, "Model cannot be null.")); //remove this model from the model set
	}

	/**
	 * Model constructor.
	 * @param models Zero or more models with which to initially place in the group.
	 * @throws NullPointerException if one of the models is <code>null</code>.
	 */
	public AbstractModelGroup(final M... models) {
		for(final M model : models) { //for each model
			add(model); //add this model to the group
		}
	}
}
