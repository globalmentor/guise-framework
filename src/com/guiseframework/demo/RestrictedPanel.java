package com.guiseframework.demo;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Flow;
import com.guiseframework.component.layout.FlowLayout;
import com.guiseframework.component.layout.RegionLayout;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;

/**Restricted Guise demonstration panel.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates restricted access to navigation paths and user logout.
@author Garret Wilson
*/
public class RestrictedPanel extends DefaultNavigationPanel
{

	/**The notice to display on the restricted panel.*/
	protected final static String NOTICE=
			"Note: This page may be accessed either by logging in via the login panel or by accessing this page directly using the browser's HTTP digest authentication capabilities. "+
			"Once a user logs out from this page, by default the browser attempts to authenticate the user because the same page is attempting to be loaded with no Guise session principal set. "+
			"This illustrates how login panels and browser-based HTTP digest authentication can be used interchangeably. "+
			"If a login panel is always desired, the logout functionality could navigate to the login panel, or a default login panel could be set.";
	
	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public RestrictedPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Restricted");	//set the panel title
		
		final LayoutPanel restrictionPanel=new LayoutPanel(session, new FlowLayout(session, Flow.PAGE));	//create the authorization panel flowing vertically
		
			//heading
		final Heading heading=new Heading(session, 0);	//create a top-level heading
		heading.setLabel("Access Granted.");	//set the text of the heading, using its model
		restrictionPanel.add(heading);	//add the heading to the panel

			//notice
		final Message notice=new Message(session);	//create a new message
		notice.setMessage(NOTICE);	//set the text of the notice
		restrictionPanel.add(notice);	//add the notice to the panel

			//logout button
		final Button logoutButton=new Button(session);	//create a button for logging out
		logoutButton.setLabel("Log out");	//set the button label
		logoutButton.addActionListener(new ActionListener()	//when the logout button is pressed
				{
					public void actionPerformed(final ActionEvent actionEvent)	//set the session's user to null
					{
						session.setPrincipal(null);	//log out the user
					}
				});
		restrictionPanel.add(logoutButton);	//add the button to the panel
		

		add(restrictionPanel);	//add the panel to the panel in the default center
	}

}
