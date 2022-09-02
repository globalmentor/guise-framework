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

package io.guise.framework.component;

import static com.globalmentor.java.Classes.*;

import io.guise.framework.converter.AbstractStringLiteralConverter;
import io.guise.framework.model.*;

/**
 * Control to allow selection of one or more values from a list.
 * @param <V> The type of values to select.
 * @author Garret Wilson
 */
public class ListControl<V> extends AbstractListSelectControl<V> {

	/** The row count bound property. */
	public static final String ROW_COUNT_PROPERTY = getPropertyName(ListControl.class, "rowCount");

	/** The estimated number of rows requested to be visible, or -1 if no row count is specified. */
	private int rowCount;

	/** @return The estimated number of rows requested to be visible, or -1 if no row count is specified. */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * Sets the estimated number of rows requested to be visible. This is a bound property of type <code>Integer</code>.
	 * @param newRowCount The new requested number of visible rows, or -1 if no row count is specified.
	 * @see #ROW_COUNT_PROPERTY
	 */
	public void setRowCount(final int newRowCount) {
		if(rowCount != newRowCount) { //if the value is really changing
			final int oldRowCount = rowCount; //get the old value
			rowCount = newRowCount; //actually change the value
			firePropertyChange(ROW_COUNT_PROPERTY, oldRowCount, newRowCount); //indicate that the value changed
		}
	}

	/**
	 * Value class constructor with a default data model to represent a given type with multiple selection.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass) {
		this(valueClass, true); //construct the class with multiple selection enabled
	}

	/**
	 * Value class constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param multipleSelection <code>true</code> if the list control should allow multiple selections, else <code>false</code> if only a single selection is
	 *          allowed.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass, final boolean multipleSelection) {
		this(valueClass, multipleSelection ? new MultipleListSelectionPolicy<V>() : new SingleListSelectionPolicy<V>()); //construct the class with an appropriate selection policy
	}

	/**
	 * Value class and row count constructor with a default data model to represent a given type with multiple selection.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param rowCount The requested number of visible rows, or -1 if no row count is specified.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass, final int rowCount) {
		this(valueClass, true, rowCount); //construct the class with multiple selection enabled
	}

	/**
	 * Value class and row count constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param multipleSelection <code>true</code> if the list control should allow multiple selections, else <code>false</code> if only a single selection is
	 *          allowed.
	 * @param rowCount The requested number of visible rows, or -1 if no row count is specified.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass, final boolean multipleSelection, final int rowCount) {
		this(valueClass, multipleSelection ? new MultipleListSelectionPolicy<V>() : new SingleListSelectionPolicy<V>(), rowCount); //construct the class with an appropriate selection policy and the row count
	}

	/**
	 * Value class and selection strategy constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @throws NullPointerException if the given value class and/or selection strategy is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy)); //construct the class with a default model
	}

	/**
	 * Value class, selection strategy, and row count constructor with a default data model to represent a given type.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param selectionStrategy The strategy for selecting values in the model.
	 * @param rowCount The requested number of visible rows, or -1 if no row count is specified.
	 * @throws NullPointerException if the given value class and/or selection strategy is <code>null</code>.
	 */
	public ListControl(final Class<V> valueClass, final ListSelectionPolicy<V> selectionStrategy, final int rowCount) {
		this(new DefaultListSelectModel<V>(valueClass, selectionStrategy), rowCount); //construct the class with a default model and the row count
	}

	/**
	 * List select model constructor.
	 * @param listSelectModel The component list select model.
	 * @throws NullPointerException if the given list select model is <code>null</code>.
	 */
	public ListControl(final ListSelectModel<V> listSelectModel) {
		this(listSelectModel, -1); //construct the class with no row count
	}

	/**
	 * List select model and row count constructor.
	 * @param listSelectModel The component list select model.
	 * @param rowCount The requested number of visible rows, or -1 if no row count is specified.
	 * @throws NullPointerException if the given list select model is <code>null</code>.
	 */
	public ListControl(final ListSelectModel<V> listSelectModel, final int rowCount) {
		this(listSelectModel, new DefaultValueRepresentationStrategy<V>(AbstractStringLiteralConverter.getInstance(listSelectModel.getValueClass())), rowCount); //construct the class with a default representation strategy
	}

	/**
	 * List select model and value representation strategy constructor.
	 * @param listSelectModel The component list select model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @throws NullPointerException if the given list select model and/or value representation strategy is <code>null</code>.
	 */
	public ListControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy) {
		this(listSelectModel, valueRepresentationStrategy, -1); //construct the class with no row count
	}

	/**
	 * List select model, value representation strategy, and row count constructor.
	 * @param listSelectModel The component list select model.
	 * @param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	 * @param rowCount The requested number of visible rows, or -1 if no row count is specified.
	 * @throws NullPointerException if the given list select model and/or value representation strategy is <code>null</code>.
	 */
	public ListControl(final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy, final int rowCount) {
		super(listSelectModel, valueRepresentationStrategy); //construct the parent class
		this.rowCount = rowCount; //save the row count
	}

}
