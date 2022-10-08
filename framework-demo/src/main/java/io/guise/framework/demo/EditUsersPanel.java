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

package io.guise.framework.demo;

import java.beans.PropertyVetoException;
import java.util.*;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.validator.*;

/**
 * Edit Users Guise demonstration panel. Copyright © 2005-2007 GlobalMentor, Inc. Demonstrates list controls with default representation, thread-safe select
 * model access, sorting list control models, listening for select model list changes, retrieving navigation panels, invoking modal panels, retrieving modal
 * panel results, disabled control models, notification dialog frames, and accessing a custom Guise application.
 * @author Garret Wilson
 */
public class EditUsersPanel extends LayoutPanel {

	/** Default constructor. */
	public EditUsersPanel() {
		super(new FlowLayout(Flow.LINE)); //construct the parent class flowing horizontally
		setLabel("Guise\u2122 Demonstration: Edit Users"); //set the panel title	

		final ListControl<DemoUser> userListControl = new ListControl<DemoUser>(DemoUser.class, new SingleListSelectionPolicy<DemoUser>()); //create a list control allowing only single selections
		userListControl.setValidator(new ValueRequiredValidator<DemoUser>()); //require a value to be selected
		userListControl.setLabel("Users"); //set the list control label
		userListControl.setRowCount(8); //request eight visible rows in the list
		final List<DemoUser> applicationUserList = ((DemoApplication)getSession().getApplication()).getUsers(); //get the application's list of users
		synchronized(applicationUserList) { //don't allow others to modify the application user list while we iterate over it
			userListControl.addAll(applicationUserList); //add all the users from the application
		}

		synchronized(userListControl) { //don't allow the user select model to be changed by another thread while we sort it
			Collections.sort(userListControl); //sort the user list model (each user implements Comparable)
		}

		final LayoutPanel buttonPanel = new LayoutPanel(new FlowLayout(Flow.LINE)); //create the button panel flowing horizontally
		//add button
		final Button addButton = new Button(); //create the add button
		addButton.setLabel("Add User"); //set the text of the add button
		addButton.addActionListener(new ActionListener() { //if the add button was pressed

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				getSession().navigateModal(DemoApplication.EDIT_USER_PANEL_NAVIGATION_PATH, new ModalNavigationListener() { //navigate modally to the edit user panel

					@Override
					public void modalBegan(final ModalEvent modalEvent) { //when modal editing begins
						final String newUserID = ((DemoApplication)getSession().getApplication()).generateUserID(); //ask the application to generate a new user ID
						((EditUserPanel)modalEvent.getSource()).setNewUser(newUserID); //initialize the panel for a new user
					}

					@Override
					public void modalEnded(final ModalEvent modalEvent) { //when modal editing is finished
						final DemoUser newUser = ((EditUserPanel)modalEvent.getSource()).getResult(); //get the modal result
						if(newUser != null) { //if a new user was created
							userListControl.add(newUser); //add the new user to the list
							synchronized(userListControl) { //don't allow the user select model to be changed by another thread while we sort it
								Collections.sort(userListControl); //sort the user list model (each user implements Comparable)
							}
							try {
								userListControl.setSelectedValues(newUser); //select the new user
							} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
							}
						}
					}

				});
			}

		});
		buttonPanel.add(addButton); //add the button to the button panel
		//edit button	
		final Button editButton = new Button(); //create the edit button
		editButton.setLabel("Edit"); //set the text of the edit button
		editButton.addActionListener(new ActionListener() { //if the edit button was pressed

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final DemoUser user = userListControl.getSelectedValue(); //get the selected user
				if(user != null) { //if a user is selected
					getSession().navigateModal(DemoApplication.EDIT_USER_PANEL_NAVIGATION_PATH, new ModalNavigationListener() { //navigate modally to the edit user panel

						@Override
						public void modalBegan(final ModalEvent modalEvent) { //when modal editing begins
							((EditUserPanel)modalEvent.getSource()).setUser(user); //initialize the panel with this user
						}

						@Override
						public void modalEnded(final ModalEvent modalEvent) { //when modal editing is finished
							final DemoUser newUser = ((EditUserPanel)modalEvent.getSource()).getResult(); //get the modal result
							if(newUser != null) { //if a new user was created
								userListControl.replace(user, newUser); //replace the user with the new user
								synchronized(userListControl) { //don't allow the user select model to be changed by another thread while we sort it
									Collections.sort(userListControl); //sort the user list model (each user implements Comparable)
								}
								try {
									userListControl.setSelectedValues(newUser); //select the edited user
								} catch(final PropertyVetoException propertyVetoException) { //if the change was vetoed, ignore the exception
								}
							}
						}

					});
				}
			}

		});
		buttonPanel.add(editButton); //add the button to the button panel
		//remove button	
		final Button removeButton = new Button(); //create the remove button
		removeButton.setLabel("Remove"); //set the text of the remove button
		removeButton.addActionListener(new ActionListener() { //if the remove button was pressed

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final int selectedIndex = userListControl.getSelectedIndex(); //get the selected index
				if(selectedIndex >= 0) { //if an index is selected
					final DemoUser user = userListControl.get(selectedIndex); //get the selected user
					//create a confirmation dialog
					final NotificationOptionDialogFrame confirmationDialog = new NotificationOptionDialogFrame(
							"Are you sure you want to remove user " + user.getFirstName() + " " + user.getLastName() + "?", Notification.Option.YES, Notification.Option.NO); //present "yes" and "no" options to the user
					confirmationDialog.open(new AbstractGenericPropertyChangeListener<Frame.Mode>() { //ask for confirmation		

						@Override
						public void propertyChange(final GenericPropertyChangeEvent<Frame.Mode> propertyChangeEvent) { //when the modal dialog mode changes
							//if the notification dialog is no longer modal and the selected option is "yes"
							if(confirmationDialog.getMode() == null && confirmationDialog.getValue() == Notification.Option.YES) {
								userListControl.remove(selectedIndex); //remove the user at the given index												
							}
						}

					});
				}
			}

		});
		buttonPanel.add(removeButton); //add the button to the button panel

		add(userListControl); //add the list control to the panel
		add(buttonPanel); //add the button panel to the panel

		//disable the add and remove buttons whenever there are no users 
		userListControl.addListListener(new ListListener<DemoUser>() { //listen for the list being modified

			@Override
			public void listModified(final ListEvent<DemoUser> listEvent) { //if the list is modified
				final boolean listEmpty = userListControl.isEmpty(); //see if the list is empty
				editButton.setEnabled(!listEmpty); //only enable the edit button if there are users to edit
				removeButton.setEnabled(!listEmpty); //only enable the remove button if there are users to remove
				final List<DemoUser> applicationUserList = ((DemoApplication)getSession().getApplication()).getUsers(); //get the application's list of users
				synchronized(applicationUserList) { //don't allow others to modify the application user list while we modify it
					applicationUserList.clear(); //clear all the application users
					applicationUserList.addAll(userListControl); //update the application users with the ones we are editing						
				}
			}

		});
	}

}
