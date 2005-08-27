package com.javaguise.demo;

import java.util.Arrays;

import com.javaguise.component.*;
import com.javaguise.component.layout.*;
import com.javaguise.event.*;
import com.javaguise.model.ActionModel;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.*;

/**Login Guise demonstration frame.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates on-the-fly validation, on-the-fly error reporting,
	resetting control values, and setting Guise session user.
@author Garret Wilson
*/
public class LoginFrame extends DefaultFrame
{

	/**Guise session constructor.
	@param session The Guise session that owns this frame.
	*/
	public LoginFrame(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class, defaulting to a region layout
		getModel().setLabel("Guise\u2122 Demonstration: Login");	//set the frame title
		
		final LayoutPanel loginPanel=new LayoutPanel(session, new FlowLayout(Orientation.Flow.PAGE));	//create the authorization panel flowing vertically
		
			//heading
		final Heading heading=new Heading(session, 0);	//create a top-level heading
		heading.getModel().setLabel("Login");	//set the text of the heading, using its model
		loginPanel.add(heading);	//add the heading to the panel

		final LayoutPanel userPanel=new LayoutPanel(session, new FlowLayout(Orientation.Flow.LINE));	//create the user panel flowing horizontally

			//ID
		final TextControl<String> idControl=new TextControl<String>(session, String.class);	//create the ID input control
		idControl.getModel().setLabel("User ID *");	//set the ID control label
		idControl.getModel().setValidator(new RegularExpressionStringValidator(session, ".+", true));	//require at least a single character
		userPanel.add(idControl);	//add the ID control to the panel

			//password
		final TextControl<char[]> passwordControl=new TextControl<char[]>(session, char[].class);	//create the password input control
		passwordControl.getModel().setLabel("Password *");	//set the password control label
		passwordControl.setMasked(true);	//mask the password input
		passwordControl.getModel().setValidator(new RegularExpressionCharArrayValidator(session, ".+", true));	//require at least a single character
		userPanel.add(passwordControl);	//add the password control to the panel

		loginPanel.add(userPanel);	//add the user panel to the login panel

			//login button
		final Button loginButton=new Button(session);	//create a button for logging in
		loginButton.getModel().setLabel("Log in");	//set the button label
		loginButton.getModel().addActionListener(new ActionListener<ActionModel>()	//when the login button is pressed
				{
					public void actionPerformed(ActionEvent<ActionModel> actionEvent)	//get the user, verify the password, and set the new session user
					{
						if(isValid())	//if the form information is valid
						{
							final char[] password=passwordControl.getModel().getValue();	//get the password entered by the user
							passwordControl.getModel().resetValue();	//reset the password value so that it won't be available on subsequent accesses
							final DemoApplication demoApplication=(DemoApplication)session.getApplication();	//get a reference to the demo application
							final DemoUser user=demoApplication.getPrincipal(idControl.getModel().getValue());	//get the user by ID
							if(user!=null)	//if a valid user was entered
							{
								if(Arrays.equals(user.getPassword(), password))	//if the entered password matches that of the user
								{
									session.setPrincipal(user);	//log in the user
									session.navigate(DemoApplication.RESTRICTED_FRAME_NAVIGATION_PATH);	//navigate to the restricted frame
								}
								else	//if the password doesn't match
								{
									final ValidationException validationException=new ValidationException(passwordControl, "Invalid password");	//create an invalid password exception for the password control
									idControl.addError(validationException);	//indicate that the ID control has a validation exception
									addError(validationException);	//tell the frame the error so that it can be reported to the user									
								}
							}
							else	//if the user ID is not valid
							{
								final ValidationException validationException=new ValidationException(idControl, "Invalid user ID");	//create an invalid user ID exception for the ID control
								idControl.addError(validationException);	//indicate that the ID control has a validation exception
								addError(validationException);	//tell the frame the error so that it can be reported to the user
							}
						}
					}
				});
		loginPanel.add(loginButton);	//add the button to the panel
	
		add(loginPanel);	//add the panel to the frame in the default center
	}


}