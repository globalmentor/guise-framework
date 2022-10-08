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

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.List;

import com.globalmentor.beans.*;
import com.globalmentor.model.TaskState;

import io.guise.framework.Bookmark;
import io.guise.framework.component.layout.*;
import io.guise.framework.model.*;
import io.guise.framework.prototype.AbstractActionPrototype;
import io.guise.framework.prototype.ActionPrototype;

import static com.globalmentor.java.Classes.*;
import static io.guise.framework.Resources.*;
import static io.guise.framework.theme.Theme.*;

/**
 * A card panel representing a sequence of cards. If any card has constraints of {@link TaskCardConstraints}, this class will update the task state based upon
 * visited and validated status.
 * @author Garret Wilson
 * @see CardLayout
 */
public class SequenceCardPanel extends AbstractCardPanel implements ArrangeContainer, Commitable {

	/** The bound property of the sequence state. */
	public static final String STATE_PROPERTY = getPropertyName(SequenceCardPanel.class, "state");
	/** The bound property of whether the transitions are enabled. */
	public static final String TRANSITION_ENABLED_PROPERTY = getPropertyName(SequenceCardPanel.class, "transitionEnabled");

	/** The current state of the sequence, or <code>null</code> if the sequence is not occurring. */
	private TaskState state = null;

	/** @return The current state of the sequence, or <code>null</code> if the sequence is not occurring. */
	public TaskState getState() {
		return state;
	}

	/**
	 * Sets the current state of the sequence. This is a bound property.
	 * @param newState The current state of the sequence, or <code>null</code> if the sequence is not occurring.
	 * @see #STATE_PROPERTY
	 */
	protected void setState(final TaskState newState) {
		if(state != newState) { //if the value is really changing
			final TaskState oldState = state; //get the current value
			state = newState; //update the value
			firePropertyChange(STATE_PROPERTY, oldState, newState);
		}
	}

	/** The current transition in the sequence, or <code>null</code> if no transition is occurring. */
	private SequenceTransition transition = null;

	/** @return The current transition in the sequence, or <code>null</code> if no transition is occurring. */
	public SequenceTransition getTransition() {
		return transition;
	}

	/** Whether transitions are enabled, so that changing selected cards will cause the appropriate validate/commit functionality. */
	private boolean transitionEnabled = true;

	/** @return Whether transitions are enabled, so that changing selected cards will cause the appropriate validate/commit functionality. */
	public boolean isTransitionEnabled() {
		return transitionEnabled;
	}

	/**
	 * Sets whether transitions are enabled, so that changing selected cards will cause the appropriate validate/commit functionality. This is a bound property of
	 * type <code>Boolean</code>.
	 * @param newTransitionEnabled <code>true</code> if transitions are enabled, so that changing selected cards will cause the appropriate validate/commit
	 *          functionality.
	 * @see #TRANSITION_ENABLED_PROPERTY
	 */
	public void setTransitionEnabled(final boolean newTransitionEnabled) {
		if(transitionEnabled != newTransitionEnabled) { //if the value is really changing
			final boolean oldTransitionEnabled = transitionEnabled; //get the current value
			transitionEnabled = newTransitionEnabled; //update the value
			firePropertyChange(TRANSITION_ENABLED_PROPERTY, Boolean.valueOf(oldTransitionEnabled), Boolean.valueOf(newTransitionEnabled));
		}
	}

	/** The prototype for the previous action. */
	private final ActionPrototype previousActionPrototype;

	/** @return The prototype for the previous action. */
	public ActionPrototype getPreviousActionPrototype() {
		return previousActionPrototype;
	}

	/** The prototype for the next action. */
	private final ActionPrototype nextActionPrototype;

	/** @return The prototype for the next action. */
	public ActionPrototype getNextActionPrototype() {
		return nextActionPrototype;
	}

	/** The prototype for the action to finish the sequence. */
	private final ActionPrototype finishActionPrototype;

	/** @return The prototype for the action to finish the sequence. */
	public ActionPrototype getFinishActionPrototype() {
		return finishActionPrototype;
	}

	/** The prototype for the continue action, which delegates to {@link #goNext()} or {@link #goFinish()}, depending on the state of {@link #hasNext()}. */
	private final ActionPrototype continueActionPrototype;

	/** @return The prototype for the next action, which delegates to {@link #goNext()} or {@link #goFinish()}, depending on the state of {@link #hasNext()}.. */
	public ActionPrototype getContinueActionPrototype() {
		return continueActionPrototype;
	}

	/** The prototype for the action to cancel the sequence. */
	private final ActionPrototype cancelActionPrototype;

	/** @return The prototype for the action to cancel the sequence. */
	public ActionPrototype getCancelActionPrototype() {
		return cancelActionPrototype;
	}

	/** The listener that ensures making a card displayed doesn't create an invalid gap in a sequence of valid cards. */
	private PropertyChangeListener cardDisplayedChangeListener = new AbstractGenericPropertyChangeListener<Boolean>() { //create a new display change listener

		@Override
		public void propertyChange(final GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //if the child component's display status changes (we're really listening for the constraint's displayable status, which we'll check later)
			if(isTransitionEnabled() && Boolean.TRUE.equals(propertyChangeEvent.getNewValue())) { //if transitions are turned on and the card is being displayed
				final Object target = propertyChangeEvent.getTarget(); //get the target of the event
				if(target instanceof ControlConstraints) { //if this is control constraints changing
					//TODO del if not needed					final ControlConstraints controlConstraints=(ControlConstraints)target;	//get the control constraints target
					final int currentIndex = getSelectedIndex(); //get the currently selected index
					if(currentIndex >= 0) { //if a card is selected
						final Component card = (Component)propertyChangeEvent.getSource(); //get the card source of the event
						final int cardIndex = indexOf(card); //get the index of the card
						if(cardIndex > currentIndex) { //if this card is in front of the current card
							for(int i = size() - 1; i >= cardIndex; --i) { //for each card (including the one that changed) after the current card
								setEnabled(get(i), false); //disable this component
							}
						}
					}
				}
			}
		}

	};

	/** Default constructor. */
	public SequenceCardPanel() {
		this(new CardLayout()); //default to flowing vertically
	}

	/**
	 * Layout constructor.
	 * @param layout The layout definition for the container.
	 * @throws NullPointerException if the given layout is <code>null</code>.
	 */
	protected SequenceCardPanel(final CardLayout layout) {
		super(layout); //construct the parent class
		//previous action prototype
		previousActionPrototype = new AbstractActionPrototype(LABEL_PREVIOUS, GLYPH_PREVIOUS) {

			@Override
			protected void action(final int force, final int option) {
				goPrevious(); //go to the previous card
			};
		};
		//next action prototype
		nextActionPrototype = new AbstractActionPrototype(LABEL_NEXT, GLYPH_NEXT) {

			@Override
			protected void action(final int force, final int option) {
				goNext(); //go to the next card
			};
		};
		//finish action prototype
		finishActionPrototype = new AbstractActionPrototype(LABEL_FINISH, GLYPH_FINISH) {

			@Override
			protected void action(final int force, final int option) {
				goFinish(); //finish the sequence
			};
		};
		//continue action prototype
		continueActionPrototype = new AbstractActionPrototype(LABEL_NEXT, GLYPH_NEXT) {

			@Override
			protected void action(final int force, final int option) {
				if(hasNext()) { //if there is a next card
					goNext(); //go to the next card
				} else { //if there is no next card
					goFinish(); //finish the sequence
				}
			};
		};
		//cancel action prototype
		cancelActionPrototype = new AbstractActionPrototype(LABEL_CANCEL, GLYPH_CANCEL) {

			@Override
			protected void action(final int force, final int option) {
				goCancel(); //cancel the sequence
			};
		};
		addVetoableChangeListener(VALUE_PROPERTY, new SequenceCardVetoableChangeListener()); //do validation as needed on card changes
		addPropertyChangeListener(VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<Component>() {

			@Override
			public void propertyChange(final GenericPropertyChangeEvent<Component> propertyChangeEvent) { //if the selected card changes
				if(isTransitionEnabled()) { //if transitions are enabled
					final Component oldCard = propertyChangeEvent.getOldValue();
					final Component newCard = propertyChangeEvent.getNewValue(); //TODO comment all this
					if(oldCard != null) { //if there was an old card
						final Constraints constraints = oldCard.getConstraints();
						if(constraints instanceof TaskCardConstraints) {
							final TaskCardConstraints taskCardConstraints = (TaskCardConstraints)constraints;
							if(taskCardConstraints.getTaskState() == TaskState.ERROR && oldCard.isValid()) { //if there was an error but the old card is now valid
								taskCardConstraints.setTaskState(TaskState.INCOMPLETE);
							}
							final int oldIndex = indexOf(oldCard); //get the index of the old card
							assert oldIndex >= 0 : "Expected old card to be present in the container.";
							final int newIndex = indexOf(newCard); //see what index the new value has
							if(newIndex > oldIndex) { //if we advanced to a new card
								taskCardConstraints.setTaskState(TaskState.COMPLETE); //show that the old task is complete
							}
						}
					}
					if(newCard != null) { //if there is a new card
						final Constraints constraints = newCard.getConstraints();
						if(constraints instanceof TaskCardConstraints) {
							final TaskCardConstraints taskCardConstraints = (TaskCardConstraints)constraints;
							if(taskCardConstraints.getTaskState() == null) {
								taskCardConstraints.setTaskState(TaskState.INCOMPLETE);
							}
						}
					}
				}
				previousActionPrototype.setEnabled(hasPrevious()); //enable or disable the previous action prototype
				final boolean hasNext = hasNext(); //see if we can go forward
				nextActionPrototype.setEnabled(hasNext); //enable or disable the previous action prototype
				finishActionPrototype.setEnabled(!hasNext); //enable or disable the finish action prototype
				if(hasNext) { //if there is a next TODO replace this with a proxy action prototype
					continueActionPrototype.setLabel(nextActionPrototype.getLabel()); //copy the next action prototype properties
					continueActionPrototype.setGlyphURI(nextActionPrototype.getGlyphURI());
					continueActionPrototype.setEnabled(nextActionPrototype.isEnabled());
				} else { //if there is no next
					continueActionPrototype.setLabel(finishActionPrototype.getLabel()); //copy the finish action prototype properties
					continueActionPrototype.setGlyphURI(finishActionPrototype.getGlyphURI());
					continueActionPrototype.setEnabled(finishActionPrototype.isEnabled());
				}
			}

		});
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version installs a listener for the component's displayed status.
	 * </p>
	 */
	@Override
	protected void addComponent(final int index, final Component childComponent) {
		super.addComponent(index, childComponent); //initialize the child component as needed
		childComponent.addPropertyChangeListener(DISPLAYED_PROPERTY, cardDisplayedChangeListener); //listen for changes in the component's displayed status and make sure the sequence is disabled as needed
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version uninstalls a listener for the component's displayed status.
	 * </p>
	 */
	@Override
	protected void removeComponent(final Component childComponent) {
		childComponent.removePropertyChangeListener(DISPLAYED_PROPERTY, cardDisplayedChangeListener); //stop listening for changes in the component's displayed status
		super.removeComponent(childComponent); //uninitialize the child component as needed
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version updates the error status of the child component's constraints if those constraints implement {@link TaskCardConstraints}.
	 * </p>
	 */
	@Override
	protected void childComponentValidPropertyChanged(final Component childComponent, final boolean oldValid, final boolean newValid) {
		super.childComponentValidPropertyChanged(childComponent, oldValid, newValid); //call the parent version
		final Constraints constraints = childComponent.getConstraints(); //get the child component constraints
		if(constraints instanceof TaskCardConstraints) { //if these are task card constraints
			final TaskCardConstraints taskCardConstraints = ((TaskCardConstraints)constraints); //get the constraints as task card constraints
			if(taskCardConstraints.getTaskState() != null) { //if the task is started
				taskCardConstraints.setTaskState(newValid ? TaskState.INCOMPLETE : TaskState.ERROR); //update the task status based upon the new valid state
			}
		}
	}

	/**
	 * Sets the new selected card. This version validates the input on the currently selected card as needed.
	 * @param newValue The new selected card.
	 * @throws ValidationException if the provided value is not valid.
	 * @see #getValidator()
	 * @see #VALUE_PROPERTY
	 */
	/*TODO del
		public void setValue(final Component newValue) throws ValidationException
		{
			final Component currentCard=getValue();	//get the currently selected card
			if(currentCard!=null) {	//if there is a selected card, do validation if we need to
				final int selectedIndex=indexOf(currentCard);	//get the index of the selected card
				assert selectedIndex>=0 : "Expected selected card to be present in the container.";
				final int newIndex=indexOf(newValue);	//see what index the new value has
				if(newIndex<0) {	//if the new value isn't in the container TODO maybe put this in a default card panel validator
					return;	//TODO decide what to do here
				}
				if(newIndex>selectedIndex) {	//if we're advancing forward in the sequence
					try
					{
						currentCard.validate();	//validate the currently selected card
					}
					catch(final ComponentExceptions componentException) {	//if the current card doesn't validate
						return;	//don't go forward TODO decide what to do here
					}									
				}
			}
			super.setValue(newValue);	//set the value normally
		}
	*/

	/**
	 * Determines if there is a previous step in the sequence. This version returns <code>true</code> if there is a selected card and there exists a card before
	 * the selected card.
	 * @return <code>true</code> if there is a previous step in the sequence.
	 */
	public boolean hasPrevious() {
		return getPrevious() != null; //see if there is a previous component
	}

	/**
	 * Determines the previous component in the sequence. Components that are not displayed or not enabled based upon their associated constraints are skipped.
	 * @return The previous component in the sequence, or <code>null</code> if there is no previous component in the sequence.
	 */
	public Component getPrevious() {
		final Component selectedComponent = getSelectedValue(); //get the selected component
		return selectedComponent != null ? getPrevious(selectedComponent) : null; //return the previous component of the selected component, or null if no component is selected
	}

	/**
	 * Determines the previous component in the sequence relative to the given component. Components that are not displayed or not enabled based upon their
	 * associated constraints are skipped.
	 * @param component The component the previous component to which should be found.
	 * @return The previous component in the sequence, or <code>null</code> if there is no previous component in the sequence.
	 * @throws NullPointerException if the given component is <code>null</code>.
	 */
	protected Component getPrevious(final Component component) {
		//TODO check for null and throw an exception instead of this lenient check
		final int selectedIndex = indexOf(component); //get the index of the given component
		if(selectedIndex >= 0) { //if a card is selected
			for(int i = selectedIndex - 1; i >= 0; --i) { //for each previous card
				final Component card = get(i); //get this card
				if(isDisplayed(card) && isEnabled(card)) { //if the card is displayed and enabled
					return card; //return this card
				}
			}
		}
		return null; //indicate that there is no previous card
	}

	/**
	 * Determines if there is a next step in the sequence. This version returns <code>true</code> if there is a selected card and there exists a card after the
	 * selected card.
	 * @return <code>true</code> if there is a next step in the sequence.
	 */
	public boolean hasNext() {
		return getNext() != null; //see if there is a next component
	}

	/**
	 * Determines the next component in the sequence. Components that are not displayed or not enabled based upon their associated constraints are skipped.
	 * @return The next component in the sequence, or <code>null</code> if there is no next component in the sequence.
	 */
	public Component getNext() {
		final int selectedIndex = getSelectedIndex(); //get the selected index
		if(selectedIndex >= 0) { //if a card is selected
			final int cardCount = size(); //find out how many cards there are
			for(int i = selectedIndex + 1; i < cardCount; ++i) { //for each next card
				final Component card = get(i); //get this card
				if(isDisplayed(card)) //if the card is displayed
				//TODO decide; currently we need to get disabled cards so that we can enable them				if(isDisplayed(card) && isEnabled(card))	//if the card is displayed and enabled
				{
					return card; //return this card
				}
			}
		}
		return null; //indicate that there is no next card
	}

	/**
	 * Goes to the previous step in the sequence. If there is no previous step, no action occurs. This method calls {@link #hasPrevious()}.
	 */
	public void goPrevious() {
		final SequenceTransition oldTransition = transition; //save the current transition
		transition = SequenceTransition.PREVIOUS; //indicate the current transition
		try {
			final Component previousCard = getPrevious(); //get the previous card
			if(previousCard != null) { //if there is a previous card
				try {
					final Component selectedCard = getSelectedValue(); //get the selected card
					assert selectedCard != null : "No card selected, even though hasPrevious() should have returned false if no card is selected.";
					//				try
					{
						//					selectedCard.validate();	//validate the selected card
						setValue(previousCard); //select the previous card
					}
					//				catch(final ComponentExceptions componentException)
					{
						//TODO improve; inform user
					}
				} catch(final PropertyVetoException propertyVetoException) {
					//TODO fix				throw new AssertionError(validationException);	//TODO improve
				}
			}
		} finally {
			transition = oldTransition; //restore the old transition (usually null)
		}
	}

	/**
	 * Flag to indicate that the card is changing locally and has already been validated. Prevents double validation by the vetoable change listener.
	 */
	private boolean alreadyValidated = false;

	/**
	 * Advances to the next step in the sequence. If the current card passes validation, the next card is enabled before advancing. If there is no next step, no
	 * action occurs. This method calls {@link #getNext()}.
	 */
	public void goNext() {
		//TODO del Log.trace("ready to go next");
		final SequenceTransition oldTransition = transition; //save the current transition
		transition = SequenceTransition.NEXT; //indicate the current transition
		try {
			final Component nextCard = getNext(); //get the next card
			if(nextCard != null) { //if there is a next card
				if(isTransitionEnabled()) { //if transitions are enabled
					//				TODO del Log.trace("got next card; ready to get selected card");
					final Component selectedCard = getSelectedValue(); //get the selected card
					//TODO del Log.trace("got next card", nextCard, "selected card", selectedCard);
					assert selectedCard != null : "No card selected, even though getNext() should have returned null if no card is selected.";
					//				TODO del Log.trace("ready to validate selected card");
					if(validate()) { //validate this panel; if everything, including the selected card, is valid
						//				TODO del Log.trace("card validated");
						//show any notifications, anyway
						final List<Notification> notifications = getNotifications(selectedCard); //get the notifications from the card
						final Runnable valueSetter = new Runnable() { //create code for notifying, committing the card and advancing to the next card

							@Override
							public void run() {
								try {
									setValue(nextCard); //select the next card
								} catch(final PropertyVetoException propertyVetoException) { //if the change is vetoed, don't do anything special
									//										TODO del Log.warn(propertyVetoException);
								} finally {
									transition = oldTransition; //restore the old transition (usually null)
								}
							}

						};
						//						TODO del Log.trace("ready to do notify/commit/advance");
						final int notificationCount = notifications.size(); //see how many notifications there are
						if(notificationCount > 0) { //if there is at least one notification
							getSession().notify(valueSetter, notifications.toArray(new Notification[notifications.size()])); //do the notification, followed by the value setting
						} else { //if there are no notifications
							valueSetter.run(); //change the value right away with no notifications
						}
					} else { //if the card didn't validate
						transition = oldTransition; //restore the old transition (usually null)					
					}
				} else { //if transitions aren't enabled
					try {
						setValue(nextCard); //select the next card
					} catch(final PropertyVetoException propertyVetoException) { //if the change is vetoed, don't do anything special
					} finally {
						transition = oldTransition; //restore the old transition (usually null)
					}
				}
			} else { //if there is no next card
				transition = oldTransition; //restore the old transition (usually null)					
			}
		} finally {
			//TODO del			transition=oldTransition;	//restore the old transition (usually null)
		}
	}

	/**
	 * Finishes the sequence. This method validates and commits the current card, and then calls {@link #finish()}. If no card is selected, no action occurs. The
	 * state is set to {@link TaskState#COMPLETE}.
	 */
	public void goFinish() {
		//TODO fix transition; right now it won't be set properly if we have a notification
		final SequenceTransition oldTransition = transition; //save the current transition
		transition = SequenceTransition.NEXT; //indicate the current transition
		try {
			final Component selectedCard = getSelectedValue(); //get the selected card
			if(selectedCard != null) { //if a card is selected
				//TODO decide if we want to disable validation if transitions are disabled
				if(validate()) { //validate this panel; if everything, including the selected card, is valid
					//show any notifications, anyway
					final List<Notification> notifications = getNotifications(selectedCard); //get the notifications from the card
					final Runnable finisher = new Runnable() { //create code for committing and finishing

						@Override
						public void run() {
							try {
								commit(); //commit this panel
								finish(); //finish the sequence
								setState(TaskState.COMPLETE); //indicate that the sequence is finished
							} catch(final IOException ioException) { //if there is a problem commiting the result
								getSession().notify(new Notification(ioException)); //notify the user
							}
						}

					};
					final int notificationCount = notifications.size(); //see how many notifications there are
					if(notificationCount > 0) { //if there is at least one notification
						getSession().notify(finisher, notifications.toArray(new Notification[notifications.size()])); //do the notification, followed by finishing
					} else { //if there are no notifications
						finisher.run(); //finish right away with no notifications
					}
				}
			}
		} finally {
			transition = oldTransition; //restore the old transition (usually null)
		}
	}

	/**
	 * Cancels the sequence. This method calls {@link #cancel()}. The state is set to {@link TaskState#CANCELED}.
	 */
	public void goCancel() {
		cancel(); //cancel the sequence
		setState(TaskState.CANCELED); //indicate that the sequence has been canceled
	}

	/**
	 * Resets the sequence by navigating to the first card and disabling all subsequent cards. The state is set to {@link TaskState#INCOMPLETE}.
	 */
	public void resetSequence() {
		resetValue(); //reset the value so that changing the index won't trigger validation
		if(size() > 0) { //if there are cards in the sequence
			try {
				setSelectedIndexes(0); //browse to the first index
			} catch(final PropertyVetoException propertyVetoException) {
				//TODO fix				throw new AssertionError(validationException);	//TODO improve
			}
			final Component selectedCard = getValue(); //get the selected card
			for(final Component card : this) { //for each card
				final Constraints constraints = card.getConstraints(); //get the card constraints
				//TODO ask the layout to create constraints, because the card may not yet have constraints
				if(constraints instanceof Enableable) { //if these constraints can be enabled or disabled
					((Enableable)constraints).setEnabled(card == selectedCard); //only the selected card should be enabled
				}
			}
		}
		setState(TaskState.INCOMPLETE); //indicate that the sequence is started
	}

	@Override
	public boolean validate() {
		if(!super.validate()) { //validate the component normally; if the component does not validate
			Notification notification = getNotification(); //see if this panel has any notification
			final Component selectedCard = getValue(); //get the selected card
			if(selectedCard != null) { //if there is a selected card
				final Constraints constraints = selectedCard.getConstraints(); //get the current card constraints
				if(constraints instanceof TaskCardConstraints) { //if these are task card constraints
					((TaskCardConstraints)constraints).setTaskState(TaskState.ERROR); //set the task status to error
				}
				if(notification == null) { //if we don't have a notification
					final List<Notification> notifications = getNotifications(selectedCard); //get the notifications from the card
					if(!notifications.isEmpty()) { //if there are notifications
						notification = notifications.get(0); //use the first notification
					}
				}
			}
			if(notification == null) { //if we didn't find a custom notification
				notification = new Notification(VALIDATION_FALSE_MESSAGE_RESOURCE_REFERENCE, Notification.Severity.ERROR); //use a general validation notification
			}
			getSession().notify(notification); //indicate that there was a validation error
		}
		return isValid(); //return the current valid state
	}

	/**
	 * Commits the data. This version commits the selected card if there is a selected card and it implements {@link Commitable}. Subclass versions should call
	 * this version.
	 * @throws IOException if there is an error committing data.
	 */
	public void commit() throws IOException {
		final Component selectedCard = getSelectedValue(); //get the selected card
		if(selectedCard instanceof Commitable) { //if the selected card is committable
			((Commitable)selectedCard).commit(); //tell the card to commit itself
		}
	}

	/**
	 * Finishes the sequence. This version does nothing.
	 */
	public void finish() {
	}

	/**
	 * Cancels the sequence. This version does nothing.
	 */
	public void cancel() {
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version finds the first previous enabled and displayed card, searching backwards from the requested card, if the requested card is not enabled and
	 * displayed. This version chooses the first card if no card is requested.
	 * </p>
	 */
	@Override
	protected Component getComponent(final Bookmark bookmark) {
		/*TODO del if not wanted; why did we put this in in the first place? will improving AbstractCardPanel to update the URL help, so that we will always have the correct bookmark?
					//choose the first card if no card was specified
				final String parameterValue;	//see if there is a parameter value
				if(bookmark!=null) {	//if there is a bookmark
					final String parameterName=getName();	//use this component's name as the bookmark parameter name
					parameterValue=parameterName!=null ? bookmark.getParameterValue(parameterName) : null;	//get the parameter value if there is a parameter name
				}
				else {	//if there is no bookmark
					parameterValue=null;	//there is no parameter value
				}
				if(parameterValue==null) {	//if no parameter for this component was specified
					if(size()>0) {	//if this panel has components
						return get(0);	//automatically choose the first card
					}
				}
		*/
		Component component = super.getComponent(bookmark); //get the requested component normally
		if(component != null) { //if a component was requested
			if(!isDisplayed(component) || !isEnabled(component)) { //if the component is not displayed or not enabled
				component = getPrevious(component); //get the previous component
			}
		}
		return component; //return the determined component 
	}

	/**
	 * A vetoable property change listener validates cards before changing to new cards. When {@link SequenceTransition#NEXT} occurs, validation is assumed to
	 * have already occurred and it not performed again.
	 * @author Garret Wilson
	 */
	protected class SequenceCardVetoableChangeListener extends AbstractGenericVetoableChangeListener<Component> {

		@Override
		public void vetoableChange(final GenericPropertyChangeEvent<Component> genericPropertyChangeEvent) throws PropertyVetoException {
			if(isTransitionEnabled() && genericPropertyChangeEvent.getNewValue() != getValue()) { //if transitions are enabled and the index is really changing (the VetoableChangeListener contract says that if a change is vetoed this method will be called again with a reverse change, and we don't want to validate in those circumstances)
				//		TODO del Log.trace("validating vetoable change");
				final Component currentCard = genericPropertyChangeEvent.getOldValue(); //get the currently selected card
				if(currentCard != null) { //if there is a selected card, do validation if we need to
					final int selectedIndex = indexOf(currentCard); //get the index of the selected card
					//			TODO del Log.trace("selected index:", selectedIndex);
					assert selectedIndex >= 0 : "Expected selected card to be present in the container.";
					final Component newCard = genericPropertyChangeEvent.getNewValue(); //get the new card
					final int newIndex = indexOf(newCard); //see what index the proposed new value has
					//TODO del					Log.trace("selected index", selectedIndex, "new index", newIndex, "current selected index", getSelectedIndex());
					/*TODO del
									if(newIndex<0) {	//if the new value isn't in the container TODO maybe put this in a default card panel validator
										return false;	//we can't select a card not in the container
									}
					*/
					final int indexDelta = newIndex - selectedIndex; //get the relative index
					//			TODO del Log.trace("index delta:", indexDelta);
					final SequenceTransition transition = getTransition(); //get thet current transition
					//			TODO del Log.trace("current transition:", transition);
					if(transition != SequenceTransition.NEXT && transition != SequenceTransition.PREVIOUS && !isEnabled(newCard)) { //if the new card is not enabled (with exceptions for the previous and next buttons)
						throw new PropertyVetoException("Card not enabled.", genericPropertyChangeEvent); //indicate that the new card isn't enabled and the value shouldn't be changed TODO i18n
					}
					final boolean needsValidationCommit; //we'll find out whether we should validate the card; going forward always gets validation; going backwards only gets validation if there is any subsequent card that is enabled
					if(newIndex > selectedIndex) { //if we're advancing forward in the sequence
						needsValidationCommit = true; //we always validate going forwards
					} else { //if we're going backwards in the sequence (or staying put)
						boolean hasNextCard = false; //we'll see if there is a next card TODO see if we can combine this code with getNext() eventually
						final int cardCount = size(); //find out how many cards there are
						for(int i = selectedIndex + 1; i < cardCount; ++i) { //for each next card
							final Component card = get(i); //get this card
							if(isDisplayed(card) && isEnabled(card)) { //if the card is displayed and enabled
								hasNextCard = true; //show that we have a next card
								break; //stop looking for a next card
							}
						}
						needsValidationCommit = hasNextCard; //we need validation if we have a next card
					}
					if(needsValidationCommit) { //if we need to validate and commit
						//				TODO del Log.trace("need to validate and commit; ready to validate");
						if(transition == SequenceTransition.NEXT || validate()) { //validate this panel (unless this is a "next" transition, meaning validation already occurred); if everything, including the selected card, is valid
							//					TODO del Log.trace("validated; ready to check canTransition if needed");
							if(!(currentCard instanceof SequenceTransitionable) || ((SequenceTransitionable)currentCard).canTransition(indexDelta)) { //if the current card is sequence transitionable, see if it OKs the transition
								try {
									//							TODO del Log.trace("can transition; ready to commit");
									commit(); //commit this panel
									//							TODO del Log.trace("committed; ready to set enabled if supported");
									final Constraints newCardConstraints = newCard.getConstraints(); //get the new card's constraints
									if(newCardConstraints instanceof Enableable) { //if the new card constraints is enableable
										((Enableable)newCardConstraints).setEnabled(true); //enable the new card constraints
									}
									//							TODO del Log.trace("enabled");
								} catch(final IOException ioException) { //if there is a problem commiting the result
									getSession().notify(new Notification(ioException)); //notify the user
									throw new PropertyVetoException(ioException.getMessage(), genericPropertyChangeEvent); //indicate that the card value shouldn't be changed
								}
							} else { //if the card denies transition
								throw new PropertyVetoException("Card denied transition.", genericPropertyChangeEvent); //indicate that the card didn't allow transition TODO i18n
							}
						} else { //if the panel doesn't validate
							throw new PropertyVetoException(VALIDATION_FALSE_MESSAGE_RESOURCE_REFERENCE, genericPropertyChangeEvent); //indicate that the old card didn't validate and the value shouldn't be changed
						}
					} else { //if we don't need to validate and commit
						if(currentCard instanceof SequenceTransitionable && !((SequenceTransitionable)currentCard).canTransition(indexDelta)) { //if the current card is sequence transitionable and denies transition
							throw new PropertyVetoException("Card denied transition.", genericPropertyChangeEvent); //indicate that the card didn't allow transition TODO i18n						
						}
					}
					//TODO del Log.trace("vetoable change seemed to go OK");
				}
			}
		}
	}
}
