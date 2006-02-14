package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.Converter;
import com.guiseframework.converter.DefaultStringLiteralConverter;
import com.guiseframework.model.*;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a control to allow selection by the user of a value from a list.
This implementation does not yet fully support elements that appear more than once in the model.
@param <V> The type of values to select.
@author Garret Wilson
*/
public abstract class AbstractListSelectControl<V, C extends ListSelectControl<V, C>> extends AbstractControl<C> implements ListSelectControl<V, C>
{

	/**@return The data model used by this component.*/
	@SuppressWarnings("unchecked")
	public ListSelectModel<V> getModel() {return (ListSelectModel<V>)super.getModel();}

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
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create controls to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractListSelectControl(final GuiseSession session, final String id, final ListSelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		super(session, id, model);	//construct the parent class
		this.valueRepresentationStrategy=checkNull(valueRepresentationStrategy, "Value representation strategy cannot be null.");
	}

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
		public Component<?> createComponent(final ListSelectModel<VV> model, final VV value, final int index, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			return new ValueConverterLabel<VV>(session, value, getConverter());	//create a label that will convert the value to a string
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
