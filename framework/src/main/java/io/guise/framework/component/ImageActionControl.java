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

package io.guise.framework.component;

import static java.util.Objects.*;

import java.util.Objects;

import com.globalmentor.beans.*;

import io.guise.framework.event.*;
import io.guise.framework.model.*;
import io.guise.framework.prototype.ActionPrototype;

/**
 * An image component that is also an action control.
 * @author Garret Wilson
 */
public class ImageActionControl extends AbstractImageComponent implements ActionControl {

	/** The action model used by this component. */
	private final ActionModel actionModel;

	/** @return The action model used by this component. */
	protected ActionModel getActionModel() {
		return actionModel;
	}

	/** The enableable object decorated by this component. */
	private final Enableable enableable;

	/** @return The enableable object decorated by this component. */
	protected Enableable getEnableable() {
		return enableable;
	}

	/** Whether the component is in a rollover state. */
	private boolean rollover = false;

	@Override
	public boolean isRollover() {
		return rollover;
	}

	@Override
	public void setRollover(final boolean newRollover) {
		if(rollover != newRollover) { //if the value is really changing
			final boolean oldRollover = rollover; //get the current value
			rollover = newRollover; //update the value
			firePropertyChange(ROLLOVER_PROPERTY, Boolean.valueOf(oldRollover), Boolean.valueOf(newRollover));
		}
	}

	/** The status of the current user input, or <code>null</code> if there is no status to report. */
	private Status status = null;

	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of the current user input. This is a bound property.
	 * @param newStatus The new status of the current user input, or <code>null</code> if there is no status to report.
	 * @see #STATUS_PROPERTY
	 */
	protected void setStatus(final Status newStatus) {
		if(status != newStatus) { //if the value is really changing
			final Status oldStatus = status; //get the current value
			status = newStatus; //update the value
			firePropertyChange(STATUS_PROPERTY, oldStatus, newStatus);
		}
	}

	/**
	 * Rechecks user input status of this component, and updates the status.
	 * @see #setStatus(Control.Status)
	 */
	protected void updateStatus() {
		setStatus(determineStatus()); //update the status after rechecking it
	}

	/**
	 * Checks the user input status of the control. If the component has a notification of {@link Notification.Severity#WARN}, the status is determined to be
	 * {@link Status#WARNING}. If the component has a notification of {@link Notification.Severity#ERROR}, the status is determined to be {@link Status#ERROR}.
	 * Otherwise, this version returns <code>null</code>. If the control is disabled <code>null</code> is returned.
	 * @return The current user input status of the control.
	 */
	@SuppressWarnings("incomplete-switch")
	protected Status determineStatus() {
		if(isEnabled()) { //if the control is enabled
			final Notification notification = getNotification(); //get the current notification
			if(notification != null) { //if there is a notification
				switch(notification.getSeverity()) { //see how severe the notification is
					case WARN:
						return Status.WARNING;
					case ERROR:
						return Status.ERROR;
				}
			}
		}
		return null; //default to no status to report
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version also updates the status.
	 * </p>
	 * @see #updateStatus()
	 */
	@Override
	protected void updateValid() {
		super.updateValid(); //update validity normally
		updateStatus(); //update user input status
	}

	@Override
	public void setNotification(final Notification newNotification) {
		final Notification oldNotification = getNotification(); //get the old notification
		super.setNotification(newNotification); //update the old notification normally
		if(!Objects.equals(oldNotification, newNotification)) { //if the notification changed
			updateStatus(); //update the status			
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version clears any notification.
	 * </p>
	 * @see #setNotification(Notification)
	 */
	@Override
	public void reset() {
		setNotification(null); //clear any notification
	}

	/** Default constructor. */
	public ImageActionControl() {
		this(new DefaultInfoModel(), new DefaultImageModel(), new DefaultActionModel(), new DefaultEnableable()); //construct the class with default models
	}

	/**
	 * Image model constructor.
	 * @param imageModel The component image model.
	 */
	public ImageActionControl(final ImageModel imageModel) {
		this(new DefaultInfoModel(), imageModel, new DefaultActionModel(), new DefaultEnableable()); //construct the class with an image model and other default models
	}

	/**
	 * Info model, image model, action model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param imageModel The component image model.
	 * @param actionModel The component action model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, image model, action model, and/or enableable object is <code>null</code>.
	 */
	public ImageActionControl(final InfoModel infoModel, final ImageModel imageModel, final ActionModel actionModel, final Enableable enableable) {
		super(infoModel, imageModel); //construct the parent class
		this.actionModel = requireNonNull(actionModel, "Action model cannot be null."); //save the action model
		if(actionModel != infoModel && actionModel != imageModel) { //if the action model isn't the same as another model (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
			this.actionModel.addActionListener(new ActionListener() { //create an action repeater to forward events to this component's listeners

				@Override
				public void actionPerformed(final ActionEvent actionEvent) { //if the action is performed
					final ActionEvent repeatActionEvent = new ActionEvent(ImageActionControl.this, actionEvent); //copy the action event with this class as its source
					fireActionPerformed(repeatActionEvent); //fire the repeated action
				}

			});
		}
		this.enableable = requireNonNull(enableable, "Enableable object cannot be null."); //save the enableable object
		if(enableable != infoModel) { //if the enableable and the info model are two different objects (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
			this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the enableable object
		}
		addPropertyChangeListener(ENABLED_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() { //listen for the "enabled" property changing

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) { //if the "enabled" property changes
				setNotification(null); //clear any notification
				updateValid(); //update the valid status, which depends on the enabled status					
			}

		});
	}

	/**
	 * Prototype constructor.
	 * @param actionPrototype The prototype on which this component should be based.
	 * @throws NullPointerException if the given prototype is <code>null</code>.
	 */
	public ImageActionControl(final ActionPrototype actionPrototype) {
		this(actionPrototype, new DefaultImageModel(), actionPrototype, actionPrototype); //use the action prototype as every needed model except the image model TODO see if we need a separate image action prototype
	}

	@Override
	public void addActionListener(final ActionListener actionListener) {
		getEventListenerManager().add(ActionListener.class, actionListener); //add the listener
	}

	@Override
	public void removeActionListener(final ActionListener actionListener) {
		getEventListenerManager().remove(ActionListener.class, actionListener); //remove the listener
	}

	@Override
	public Iterable<ActionListener> getActionListeners() {
		return getEventListenerManager().getListeners(ActionListener.class);
	}

	@Override
	public void performAction() {
		getActionModel().performAction(); //delegate to the installed action model, which will fire an event which we will catch and queue for refiring
	}

	@Override
	public void performAction(final int force, final int option) {
		getActionModel().performAction(force, option); //delegate to the installed action model, which will fire an event which we will catch and refire
	}

	/**
	 * Fires an action event to all registered action listeners. This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button
	 *          click.
	 * @see ActionListener
	 * @see ActionEvent
	 */
	protected void fireActionPerformed(final int force, final int option) {
		if(getEventListenerManager().hasListeners(ActionListener.class)) { //if there are action listeners registered
			fireActionPerformed(new ActionEvent(this, force, option)); //create and fire a new action event
		}
	}

	/**
	 * Fires a given action event to all registered action listeners.
	 * @param actionEvent The action event to fire.
	 */
	protected void fireActionPerformed(final ActionEvent actionEvent) {
		for(final ActionListener actionListener : getEventListenerManager().getListeners(ActionListener.class)) { //for each action listener
			actionListener.actionPerformed(actionEvent); //dispatch the action to the listener
		}
	}

	//Enableable delegations

	@Override
	public boolean isEnabled() {
		return enableable.isEnabled();
	}

	@Override
	public void setEnabled(final boolean newEnabled) {
		enableable.setEnabled(newEnabled);
	}
}
