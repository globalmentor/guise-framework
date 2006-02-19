package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.ActionModel;
import com.guiseframework.model.DefaultActionModel;

/**Control with an action model rendered as a button.
If an image is specified, it will be used instead of the button label, if possible.
@author Garret Wilson
*/
public class Button extends AbstractButtonControl<Button>
{

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Button(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Button(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultActionModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param actionModel The component action model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Button(final GuiseSession session, final ActionModel actionModel)
	{
		this(session, null, actionModel);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param actionModel The component action model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Button(final GuiseSession session, final String id, final ActionModel actionModel)
	{
		super(session, id, actionModel);	//construct the parent class
	}

}