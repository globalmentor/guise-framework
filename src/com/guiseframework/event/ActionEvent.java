package com.guiseframework.event;

import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.beans.TargetedEvent;

/**An event indicating an action should take place.
@author Garret Wilson
*/
public class ActionEvent extends AbstractGuiseEvent implements TargetedEvent
{

	/**The default action option.*/
	public final static int DEFAULT_OPTION=0;

	/**The default action force.*/
	public final static int DEFAULT_FORCE=1;

	/**The commands that can be represented by an action.*/
	public enum Command
	{
		/**The action requests an item to be selected.
		Traditionally this is indicated by a left mouse button single click.
		*/
		SELECT,
		/**The action requests contextual information.
		Traditionally this is indicated by a right mouse button single click.
		*/
		INFO,
		/**The action requests activitation,
		Traditionally this is indicated by a left mouse button double click.
		*/
		ACTIVATE;
	};
	
	/**The target of the event, or <code>null</code> if the event target is not known.*/
	private final Object target;
	
		/**Returns the object to which the event applies.
		This may be a different than <dfn>source</dfn>, which is the object that generated this event instance.
		@return The target of the event.
		*/
		public Object getTarget() {return target;}

	/**The zero-based option indicated by this action.*/
	private final int option;

		/**Returns the option indicated by this action.
		The option is zero-based and represents any alternate option indicated by the user.
		If the action was initiated by a mouse click, for instance, the left button traditionally will indicate the default option (0),
		while the right button will indicate a secondary option (1).
		*/
		public int getOption() {return option;}

	/**The force with which the action was initiated.*/
	private final int force;

		/**Returns the force with which the action was initiated.
		A force of zero indicates no force.
		A mouse single click should generate a force of 1, while a double single click should generate a force of 2.
		*/
		public int getForce() {return force;}

	/**Determines the conventional command represented by this action.
	@return The conventional command represented by this action.
	 */
	public Command getCommand()
	{
		switch(getForce())	//check the force
		{
			case DEFAULT_FORCE:	//if the default force (0) is used
				switch(getOption())	//see which option was requested
				{
					case DEFAULT_OPTION:	//if the default option (0) was requested
						return Command.SELECT;	//a simple selection is intended
					default:	//if any other option was used
						return Command.INFO;	//information was requested
				}
			default:	//if a stronger force is used
				return Command.ACTIVATE;	//activate was intended
		}
	}

	/**Source constructor with a default force and option.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source)
	{
		this(source, DEFAULT_FORCE, DEFAULT_OPTION);	//construct the class with the default force and option
	}

	/**Source, force, and option constructor.
	The target will be set to be the same as the given source.
	@param source The object on which the event initially occurred.
	@param force The zero-based force, such as 0 for no force or 1 for a mouse single click.
	@param option The zero-based option, such as 0 for a mouse left button click or 1 for a mouse right button click.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	@exception IllegalArgumentException if the given force and/or option is negative.
	*/
	public ActionEvent(final Object source, final int force, final int option)
	{
		this(source, source, force, option);	//construct the class with the same target as the source
	}

	/**Source, target, force, and option constructor.
	@param source The object on which the event initially occurred.
	@param target The target of the event.
	@param force The zero-based force, such as 0 for no force or 1 for a mouse single click.
	@param option The zero-based option, such as 0 for a mouse left button click or 1 for a mouse right button click.
	@exception NullPointerException if the given source and/or target is <code>null</code>.
	@exception IllegalArgumentException if the given force and/or option is negative.
	*/
	public ActionEvent(final Object source, final Object target, final int force, final int option)
	{
		super(source);	//construct the parent class
		this.target=checkInstance(target, "Event target object cannot be null.");	//save the target
		if(force<0)	//if the force is negative
		{
			throw new IllegalArgumentException("Force cannot be negative: "+force);
		}
		this.force=force;	//save the option
		if(option<0)	//if the option is negative
		{
			throw new IllegalArgumentException("Option cannot be negative: "+option);
		}
		this.option=option;	//save the option
	}

	/**Copy constructor that specifies a different source.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source, final ActionEvent actionEvent)
	{
		this(source, actionEvent.getTarget(), actionEvent.getForce(), actionEvent.getOption());	//construct the class with the same target		
	}
}
