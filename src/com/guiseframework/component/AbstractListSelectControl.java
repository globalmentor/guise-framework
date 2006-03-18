package com.guiseframework.component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.Converter;
import com.guiseframework.converter.DefaultStringLiteralConverter;
import com.guiseframework.event.ListEvent;
import com.guiseframework.event.ListListener;
import com.guiseframework.event.ListSelectionEvent;
import com.guiseframework.event.ListSelectionListener;
import com.guiseframework.event.PostponedListEvent;
import com.guiseframework.event.PostponedListSelectionEvent;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;
import com.guiseframework.validator.Validator;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a control to allow selection by the user of a value from a list.
The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. 
This implementation does not yet fully support elements that appear more than once in the model.
@param <V> The type of values to select.
@author Garret Wilson
*/
public abstract class AbstractListSelectControl<V, C extends ListSelectControl<V, C>> extends AbstractControl<C> implements ListSelectControl<V, C>
{

	/**The list select model used by this component.*/
	private final ListSelectModel<V> listSelectModel;

		/**@return The list select model used by this component.*/
		protected ListSelectModel<V> getListSelectModel() {return listSelectModel;}

	/**The strategy used to generate a component to represent each value in the model.*/
	private ValueRepresentationStrategy<V> valueRepresentationStrategy;

		/**@return The strategy used to generate a component to represent each value in the model.*/
		public ValueRepresentationStrategy<V> getValueRepresentationStrategy() {return valueRepresentationStrategy;}

		/**Sets the strategy used to generate a component to represent each value in the model.
		This is a bound property
		@param newValueRepresentationStrategy The new strategy to create components to represent this model's values.
		@exception NullPointerException if the provided value representation strategy is <code>null</code>.
		@see SelectControl#VALUE_REPRESENTATION_STRATEGY_PROPERTY
		*/
		public void setValueRepresentationStrategy(final ValueRepresentationStrategy<V> newValueRepresentationStrategy)
		{
			if(valueRepresentationStrategy!=newValueRepresentationStrategy)	//if the value is really changing
			{
				final ValueRepresentationStrategy<V> oldValueRepresentationStrategy=valueRepresentationStrategy;	//get the old value
				valueRepresentationStrategy=checkNull(newValueRepresentationStrategy, "Value representation strategy cannot be null.");	//actually change the value
				firePropertyChange(VALUE_REPRESENTATION_STRATEGY_PROPERTY, oldValueRepresentationStrategy, newValueRepresentationStrategy);	//indicate that the value changed
			}
		}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param listSelectModel The component data model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractListSelectControl(final GuiseSession session, final String id, final ListSelectModel<V> listSelectModel, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		super(session, id);	//construct the parent class
		this.valueRepresentationStrategy=checkNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
		this.listSelectModel=checkNull(listSelectModel, "List select model cannot be null.");	//save the list select model
		this.listSelectModel.addPropertyChangeListener(getRepeaterPropertyChangeListener());	//listen and repeat all property changes of the value model
		this.listSelectModel.addListListener(new ListListener<V>()	//install a repeater list listener to listen to the decorated model
				{
					public void listModified(final ListEvent<V> listEvent)	//if the list is modified
					{
						fireListModified(listEvent.getIndex(), listEvent.getAddedElement(), listEvent.getRemovedElement());	//repeat the event, indicating the component as the source of the event
					}
				});
		this.listSelectModel.addListSelectionListener(new ListSelectionListener<V>()	//install a repeater list selection listener to listen to the decorated model
				{
					public void listSelectionChanged(final ListSelectionEvent<V> selectionEvent)	//if the list selection changes
					{
						fireSelectionChanged(selectionEvent.getAddedElement(), selectionEvent.getRemovedElement());	//repeat the event, indicating the component as the source of the event
					}		
				});
	}

	/**Reports that a bound property has changed.
	This version first updates the valid status if the value is reported as being changed.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	*/
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue)
	{
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName))	//if the value property or the validator property is being reported as changed
		{
			updateValid();	//update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue);	//fire the property change event normally
	}

	/**Checks the state of the component for validity.
	This version checks the validity of the list select model.
	@return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	*/ 
	protected boolean determineValid()
	{
		if(!super.determineValid())	//if we don't pass the default validity checks
		{
			return false;	//the component isn't valid
		}
		return getListSelectModel().isValidValue();	//the component is valid if the list select model has a valid value
	}

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version validates the associated model.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		super.validate();	//validate the parent class
		try
		{
			getListSelectModel().validateValue();	//validate the value model
		}
		catch(final ComponentException componentException)	//if there is a component error
		{
			componentException.setComponent(this);	//make sure the exception knows to which component it relates
			addError(componentException);	//add this error to the component
			throw new ComponentExceptions(componentException);	//throw a new component exception list exception
		}
	}

		//ValueModel delegations

	/**@return The default value.*/
	public V getDefaultValue() {return getListSelectModel().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public V getValue() {return getListSelectModel().getValue();}

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final V newValue) throws ValidationException {getListSelectModel().setValue(newValue);}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	@see ValueModel#VALUE_PROPERTY
	*/
	public void clearValue() {getListSelectModel().clearValue();}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	@see #VALUE_PROPERTY
	*/
	public void resetValue() {getListSelectModel().resetValue();}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator() {return getListSelectModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator) {getListSelectModel().setValidator(newValidator);}

	/**Determines whether the value of this model is valid.
	@return Whether the value of this model is valid.
	*/
	public boolean isValidValue() {return getListSelectModel().isValidValue();}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getListSelectModel().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<V> getValueClass() {return getListSelectModel().getValueClass();}

		//SelectModel delegations
	
	/**Replaces the first occurrence in the of the given value with its replacement.
	This method ensures that another thread does not change the model while the search and replace operation occurs.
	@param oldValue The value for which to search.
	@param newValue The replacement value.
	@return Whether the operation resulted in a modification of the model.
	*/
	public boolean replace(final V oldValue, final V newValue) {return getListSelectModel().replace(oldValue, newValue);}

	/**Determines the selected value.
	This method delegates to the selection strategy.
	If more than one value is selected, the lead selected value will be returned.
	@return The value currently selected, or <code>null</code> if no value is currently selected.
	*/
	public V getSelectedValue() {return getListSelectModel().getSelectedValue();}

	/**Determines the selected values.
	This method delegates to the selection strategy.
	@return The values currently selected.
	*/
	public V[] getSelectedValues() {return getListSelectModel().getSelectedValues();}

	/**Sets the selected values.
	If a value occurs more than one time in the model, the first occurrence of the value will be selected.
	Values that do not occur in the select model will be ignored.
	This method delegates to the selection strategy.
	@param values The values to select.
	@exception ValidationException if the provided value is not valid.
	*/
	public void setSelectedValues(final V... values) throws ValidationException {getListSelectModel().setSelectedValues(values);}
	
		//ListSelectModel delegations

	/**@return The selection policy for this model.*/
	public ListSelectionPolicy<V> getSelectionPolicy() {return getListSelectModel().getSelectionPolicy();}

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex() {return getListSelectModel().getSelectedIndex();}
	
	/**Determines the selected indices.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndexes() {return getListSelectModel().getSelectedIndexes();}
	
	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	@param indexes The indices to select.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndexes(int...)
	*/
	public void setSelectedIndexes(int... indexes) throws ValidationException {getListSelectModel().setSelectedIndexes(indexes);}
	
	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	@param indexes The indices to add to the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void addSelectedIndexes(int... indexes) throws ValidationException {getListSelectModel().addSelectedIndexes(indexes);}
	
	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	@param indexes The indices to remove from the selection.
	@exception ValidationException if the provided value is not valid.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void removeSelectedIndexes(int... indexes) throws ValidationException {getListSelectModel().removeSelectedIndexes(indexes);}
	
	/**Determines the enabled status of the first occurrence of a given value.
	@param value The value for which the enabled status is to be determined.
	@return <code>true</code> if the value is enabled, else <code>false</code>.
	@exception IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueEnabled(final V value) {return getListSelectModel().isValueEnabled(value);}

	/**Sets the enabled status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to enable or disable.
	@param newEnabled Whether the value should be enabled.
	@see ValuePropertyChangeEvent
	@see #ENABLED_PROPERTY
	*/
	public void setValueEnabled(final V value, final boolean newEnabled) {getListSelectModel().setValueEnabled(value, newEnabled);}

	/**Determines the enabled status of a given index.
	@param index The index of the value for which the enabled status is to be determined.
	@return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
	*/
	public boolean isIndexEnabled(final int index) {return getListSelectModel().isIndexEnabled(index);}
	
	/**Sets the enabled status of a given index.
	This is a bound value state property.
	@param index The index of the value to enable or disable.
	@param newEnabled Whether the value at the given index should be enabled.
	@see ValuePropertyChangeEvent
	@see #ENABLED_PROPERTY
	@exception IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexEnabled(final int index, final boolean newEnabled) {getListSelectModel().setIndexEnabled(index, newEnabled);}

	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<V> listListener)
	{
		getEventListenerManager().add(ListListener.class, listListener);	//add the listener
	}

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<V> listListener)
	{
		getEventListenerManager().remove(ListListener.class, listListener);	//remove the listener
	}

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener)
	{
		getEventListenerManager().add(ListSelectionListener.class, selectionListener);	//add the listener
	}

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener)
	{
		getEventListenerManager().remove(ListSelectionListener.class, selectionListener);	//remove the listener
	}

	/**Fires an event to all registered list listeners indicating the list was modified.
	@param index The index at which an element was added and/or removed, or -1 if the index is unknown.
	@param addedElement The element that was added to the list, or <code>null</code> if no element was added or it is unknown whether or which elements were added.
	@param removedElement The element that was removed from the list, or <code>null</code> if no element was removed or it is unknown whether or which elements were removed.
	@see ListListener
	@see ListEvent
	*/
	protected void fireListModified(final int index, final V addedElement, final V removedElement)
	{
		if(getEventListenerManager().hasListeners(ListListener.class))	//if there are appropriate listeners registered
		{
			final ListEvent<V> listEvent=new ListEvent<V>(getSession(), this, index, addedElement, removedElement);	//create a new event
			getSession().queueEvent(new PostponedListEvent<V>(getEventListenerManager(), listEvent));	//tell the Guise session to queue the event
		}
	}

	/**Fires an event to all registered selection listeners indicating the selection changed.
	@param addedIndex The index that was added to the selection, or <code>null</code> if no index was added or it is unknown whether or which indices were added.
	@param removedIndex The index that was removed from the list, or <code>null</code> if no index was removed or it is unknown whether or which indices were removed.
	@see ListSelectionListener
	@see ListSelectionEvent
	*/
	protected void fireSelectionChanged(final Integer addedIndex, final Integer removedIndex)
	{
		if(getEventListenerManager().hasListeners(ListSelectionListener.class))	//if there are appropriate listeners registered
		{
			final ListSelectionEvent<V> selectionEvent=new ListSelectionEvent<V>(getSession(), this, addedIndex, removedIndex);	//create a new event
			getSession().queueEvent(new PostponedListSelectionEvent<V>(getEventListenerManager(), selectionEvent));	//tell the Guise session to queue the event
		}
	}

		//List delegations
	
	/**@return The number of values in the model.*/
	public int size() {return getListSelectModel().size();}

	/**@return Whether this model contains no values.*/
	public boolean isEmpty() {return getListSelectModel().isEmpty();}

	/**Determines whether this model contains the specified value.
	@param value The value the presence of which to test.
	@return <code>true</code> if this model contains the specified value.
	*/
	public boolean contains(final Object value) {return getListSelectModel().contains(value);}

	/**@return An iterator over the values in this model.*/
	public Iterator<V> iterator() {return getListSelectModel().iterator();}

	/**@return An array containing all of the values in this model.*/
	public Object[] toArray() {return getListSelectModel().toArray();}

	/**Returns an array containing all of the values in this model.
	@param array The array into which the value of this collection are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
	@return An array containing the values of this model.
	@exception ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every value in this model.
	@exception NullPointerException if the specified array is <code>null</code>.
	*/
	public <T> T[] toArray(final T[] array) {return getListSelectModel().toArray(array);}

	/**Appends the specified value to the end of this model.
	This version delegates to {@link #add(int, Object)}.
	@param value The value to be appended to this model.
	@return <code>true</code>, indicating that the model changed as a result of the operation.
	*/
	public boolean add(final V value) {return getListSelectModel().add(value);}

	/**Removes the first occurrence in this model of the specified value. 
	@param value The value to be removed from this model, if present.
	@return <code>true</code> if this model contained the specified value.
	*/
	public boolean remove(final Object value) {return getListSelectModel().remove(value);}

	/**Determines if this model contains all of the values of the specified collection.
	@param collection The collection to be checked for containment in this model.
	@return <code>true</code> if this model contains all of the values of the specified collection.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #contains(Object)
	*/
	public boolean containsAll(final Collection<?> collection) {return getListSelectModel().containsAll(collection);}

	/**Appends all of the values in the specified collection to the end of this model, in the order that they are returned by the specified collection's iterator.
	@param collection The collection the values of which are to be added to this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #add(Object)
	*/
	public boolean addAll(final Collection<? extends V> collection) {return getListSelectModel().addAll(collection);}

	/**Inserts all of the values in the specified collection into this model at the specified position.
	@param index The index at which to insert first value from the specified collection.
	@param collection The values to be inserted into this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public synchronized boolean addAll(final int index, final Collection<? extends V> collection) {return getListSelectModel().addAll(index, collection);}

	/**Removes from this model all the values that are contained in the specified collection.
	@param collection The collection that defines which values will be removed from this model.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public boolean removeAll(final Collection<?> collection) {return getListSelectModel().removeAll(collection);}

	/**Retains only the values in this model that are contained in the specified collection.
	@param collection The collection that defines which values this model will retain.
	@return <code>true</code> if this model changed as a result of the call.
	@exception NullPointerException if the specified collection is <code>null</code>.
	@see #remove(Object)
	@see #contains(Object)
	*/
	public boolean retainAll(final Collection<?> collection) {return getListSelectModel().retainAll(collection);}

	/**Removes all of the values from this model.*/
	public void clear() {getListSelectModel().clear();}

	/**Returns the value at the specified position in this model.
	@param index The index of the value to return.
	@return The value at the specified position in this model.
	@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public V get(final int index) {return getListSelectModel().get(index);}

	/**Replaces the value at the specified position in this model with the specified value.
	@param index The index of the value to replace.
	@param value The value to be stored at the specified position.
	@return The value at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index<var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public V set(final int index, final V value) {return getListSelectModel().set(index, value);}

	/**Inserts the specified value at the specified position in this model.
	@param index The index at which the specified value is to be inserted.
	@param value The value to be inserted.
	@throws IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public void add(final int index, final V value) {getListSelectModel().add(index, value);}

	/**Removes the value at the specified position in this model.
	@param index The index of the value to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public V remove(final int index) {return getListSelectModel().remove(index);}

  /**Returns the index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	@param value The value for which to search.
	@return The index in this model of the first occurrence of the specified value, or -1 if this model does not contain this value.
	*/
	public int indexOf(final Object value) {return getListSelectModel().indexOf(value);}

	/**Returns the index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
	@param value The value for which to search.
	@return The index in this model of the last occurrence of the specified value, or -1 if this model does not contain this value.
	*/
	public int lastIndexOf(final Object value) {return getListSelectModel().lastIndexOf(value);}

	/**@return A read-only list iterator of the values in this model (in proper sequence).*/
	public ListIterator<V> listIterator() {return getListSelectModel().listIterator();}

	/**Returns a list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@param index The index of first value to be returned from the list iterator (by a call to the <code>next()</code> method).
	@return A list iterator of the values in this model (in proper sequence), starting at the specified position in this model.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt; <code>size()</code>).
	*/
	public ListIterator<V> listIterator(final int index) {return getListSelectModel().listIterator(index);}

	/**Returns a read-only view of the portion of this model between the specified <var>fromIndex</var>, inclusive, and <var>toIndex</var>, exclusive.
	@param fromIndex The low endpoint (inclusive) of the sub-list.
	@param toIndex The high endpoint (exclusive) of the sub-list.
	@return A view of the specified range within this model.
	@throws IndexOutOfBoundsException for an illegal endpoint index value (<var>fromIndex</var> &lt; 0 || <var>toIndex</var> &gt; <code>size()</code> || <var>fromIndex</var> &gt; <var>toIndex</var>).
	*/
	public List<V> subList(final int fromIndex, final int toIndex) {return getListSelectModel().subList(fromIndex, toIndex);}

	/**A default list select value representation strategy.
	A label component will be generated containing the default string representation of a value.
	The value ID will be generated by appending the hexadecimal representation of the object's hash code to the word "hash".
	@param <VV> The type of value the strategy is to represent.
	@see Label
	@see Object#toString() 
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	public static class DefaultValueRepresentationStrategy<VV> implements ValueRepresentationStrategy<VV>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession getSession() {return session;}

		/**The converter to use for displaying the value as a string.*/
		private final Converter<VV, String> converter;
			
			/**@return The converter to use for displaying the value as a string.*/
			public Converter<VV, String> getConverter() {return converter;}

		/**Session constructor with a default converter.
		This implementation uses a {@link DefaultStringLiteralConverter}.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultValueRepresentationStrategy(final GuiseSession session)
		{
			this(session, new DefaultStringLiteralConverter<VV>(session));	//construct the class with a default string literal converter
		}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@param converter The converter to use for displaying the value as a string.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultValueRepresentationStrategy(final GuiseSession session, final Converter<VV, String> converter)
		{
			this.session=checkNull(session, "Session cannot be null");	//save the session
			this.converter=checkNull(converter, "Converter cannot be null.");	//save the converter
		}

		/**Creates a component for the given list value.
		This implementation returns a label with the string value of the given value using the saved converter.
		@param model The model containing the value.
		@param value The value for which a component should be created.
		@param index The index of the value within the list, or -1 if the value is not in the list (e.g. for representing no selection).
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value.
		@see #getConverter()
		*/
		public Label createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			return new Label(session, new ValueConverterLabelModel<VV>(session, value, getConverter()));	//create a label that will convert the value to a string
		}

		/**Determines an identifier for the given object.
		This implementation returns the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final VV value)	//TODO fix; this can result in duplicate component IDs on the same page
		{
			return value!=null ? "hash"+Integer.toHexString(value.hashCode()) : null;	//if a value is given return the word "hash" followed by a hexadecimal representation of the value's hash code
		}
	}

}
