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

import java.io.IOException;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.event.TargetedEvent;

import io.guise.framework.GuiseApplication;
import io.guise.framework.GuiseSession;
import io.guise.framework.component.effect.Effect;
import io.guise.framework.component.layout.*;
import io.guise.framework.component.transfer.*;
import io.guise.framework.event.*;
import io.guise.framework.geometry.*;
import io.guise.framework.input.Input;
import io.guise.framework.input.InputStrategy;
import io.guise.framework.model.*;
import io.guise.framework.model.ui.PresentationModel;
import io.guise.framework.platform.*;
import io.guise.framework.theme.Theme;

/**
 * Base interface for all Guise components. Each component must provide either a Guise session constructor; or a Guise session and string ID constructor. Any
 * component may contain other components, but only a {@link Container} allows for custom addition and removal of child components.
 * <p>
 * A component takes up space regardless of whether it is set to be visible using {@link Component#setVisible(boolean)}. {@link Component#setDisplayed(boolean)}
 * determines whether the component is displayed at all. If a component is not displayed, it takes up no space. If a component is not displayed, it is not
 * visible regardless of whether it is set to be visible. If a developer must hide sensitive data, the developer should remove the component from its parent
 * container altogether.
 * </p>
 * <p>
 * For widest platform support the general {@link PresentationModel#ROUNDED_CORNER_ARC_SIZE} constant should be used whenever possible when requesting rounded
 * corners.
 * </p>
 * @author Garret Wilson
 */
public interface Component extends DepictedObject, PresentationModel, InfoModel {

	/** The bound property of whether the component has bookmarks enabled. */
	public static final String BOOKMARK_ENABLED_PROPERTY = getPropertyName(Component.class, "bookmarkEnabled");
	/** The bound property of the layout constraints. */
	public static final String CONSTRAINTS_PROPERTY = getPropertyName(Component.class, "constraints");
	/** The bound property of whether the component has dragging enabled. */
	public static final String DRAG_ENABLED_PROPERTY = getPropertyName(Component.class, "dragEnabled");
	/** The bound property of whether the component has dropping enabled. */
	public static final String DROP_ENABLED_PROPERTY = getPropertyName(Component.class, "dropEnabled");
	/** The bound property of whether the component has flyovers enabled. */
	public static final String FLYOVER_ENABLED_PROPERTY = getPropertyName(Component.class, "flyoverEnabled");
	/** The bound property of the strategy controlling flyovers. */
	public static final String FLYOVER_STRATEGY_PROPERTY = getPropertyName(Component.class, "flyoverStrategy");
	/** The input strategy bound property. */
	public static final String INPUT_STRATEGY_PROPERTY = getPropertyName(Component.class, "inputStrategy");
	/** The bound property of the component name. */
	public static final String NAME_PROPERTY = getPropertyName(Component.class, "name");
	/** The bound property of the notification. */
	public static final String NOTIFICATION_PROPERTY = getPropertyName(Component.class, "notification");
	/** The orientation bound property. */
	public static final String ORIENTATION_PROPERTY = getPropertyName(Component.class, "orientation");
	/** The bound property of whether a theme has been applied to this object. */
	public static final String THEME_APPLIED_PROPERTY = getPropertyName(Component.class, "themeApplied");
	/** The valid bound property. */
	public static final String VALID_PROPERTY = getPropertyName(Component.class, "valid");

	/** @return The depictor for this component. */
	public Depictor<? extends Component> getDepictor();

	/**
	 * Adds a property to be saved and loaded as a preference.
	 * @param propertyName The property to store as a preference.
	 * @see #loadPreferences(boolean)
	 * @see #savePreferences(boolean)
	 */
	public void addPreferenceProperty(final String propertyName);

	/**
	 * Determines whether the given property is saved and loaded as a preference.
	 * @param propertyName The property to determine if it is stored as a preference.
	 * @return <code>true</code> if the given property is saved and loaded as a preference.
	 * @see #loadPreferences(boolean)
	 * @see #savePreferences(boolean)
	 */
	public boolean isPreferenceProperty(final String propertyName);

	/**
	 * Returns all properties stored as preferences.
	 * @return An iterable of all properties saved and loaded as preferences.
	 * @see #loadPreferences(boolean)
	 * @see #savePreferences(boolean)
	 */
	public Iterable<String> getPreferenceProperties();

	/**
	 * Removes a property from being saved and loaded as preferences.
	 * @param propertyName The property that should no longer be stored as a preference.
	 * @see #loadPreferences(boolean)
	 * @see #savePreferences(boolean)
	 */
	public void removePreferenceProperty(final String propertyName);

	/**
	 * @return The name of the component, not guaranteed to be unique (but guaranteed not to be the empty string) and useful only for searching for components
	 *         within a component sub-hierarchy, or <code>null</code> if the component has no name.
	 */
	public String getName();

	/**
	 * Sets the name of the component. This is a bound property.
	 * @param newName The new name of the component, or <code>null</code> if the component should have no name.
	 * @throws IllegalArgumentException if the given name is the empty string.
	 * @see #NAME_PROPERTY
	 */
	public void setName(final String newName);

	/**
	 * @return The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified for this
	 *         component.
	 */
	public Constraints getConstraints();

	/**
	 * Sets the layout constraints of this component. This is a bound property.
	 * @param newConstraints The layout constraints describing individual component layout information, or <code>null</code> if no constraints have been specified
	 *          for this component.
	 * @see #CONSTRAINTS_PROPERTY
	 */
	public void setConstraints(final Constraints newConstraints);

	/** @return The strategy for processing input, or <code>null</code> if this component has no input strategy. */
	public InputStrategy getInputStrategy();

	/**
	 * Sets the strategy for processing input. This is a bound property.
	 * @param newInputStrategy The new strategy for processing input, or <code>null</code> if this component is to have no input strategy.
	 * @see #INPUT_STRATEGY_PROPERTY
	 */
	public void setInputStrategy(final InputStrategy newInputStrategy);

	/** @return The notification associated with the component, or <code>null</code> if no notification is associated with this component. */
	public Notification getNotification();

	/**
	 * Sets the component notification. This is a bound property. The notification is also fired as a {@link NotificationEvent} on this component if a new
	 * notification is given. Parents are expected to refire the notification event up the hierarchy.
	 * @param newNotification The notification for the component, or <code>null</code> if no notification is associated with this component.
	 * @see #NOTIFICATION_PROPERTY
	 */
	public void setNotification(final Notification newNotification);

	/**
	 * Returns this component's requested orientation. To resolve the orientation up the hierarchy, {@link #getComponentOrientation()} should be used.
	 * @return The internationalization orientation of the component's contents, or <code>null</code> if the default orientation should be used.
	 * @see #getComponentOrientation()
	 */
	public Orientation getOrientation();

	/**
	 * Determines the internationalization orientation of the component's contents. This method returns the local orientation value, if there is one. If there is
	 * no orientation specified for this component, the request is deferred to this component's parent. If there is no parent component, a default orientation is
	 * retrieved from the current session.
	 * @return The internationalization orientation of the component's contents.
	 * @see #getOrientation()
	 * @see GuiseSession#getOrientation()
	 */
	public Orientation getComponentOrientation();

	/**
	 * Sets the orientation. This is a bound property
	 * @param newOrientation The new internationalization orientation of the component's contents, or <code>null</code> if default orientation should be
	 *          determined based upon the session's locale.
	 * @see #ORIENTATION_PROPERTY
	 */
	public void setOrientation(final Orientation newOrientation);

	/** @return The parent of this component, or <code>null</code> if this component does not have a parent. */
	public CompositeComponent getParent();

	/**
	 * Retrieves the first ancestor of the given type.
	 * @param <A> The type of ancestor component requested.
	 * @param ancestorClass The class of ancestor component requested.
	 * @return The first ancestor component of the given type, or <code>null</code> if this component has no such ancestor.
	 */
	public <A extends CompositeComponent> A getAncestor(final Class<A> ancestorClass);

	/**
	 * Sets the parent of this component. This method is managed by containers, and normally should not be called by applications. A component cannot be given a
	 * parent if it already has a parent. A component's parent cannot be removed if that parent is a container and this component is still a child of that
	 * container. A container's parent cannot be set to a container unless that container already recognizes this component as one of its children. If a component
	 * is given the same parent it already has, no action occurs.
	 * @param newParent The new parent for this component, or <code>null</code> if this component is being removed from a parent.
	 * @throws IllegalStateException if a parent is provided and this component already has a parent.
	 * @throws IllegalStateException if no parent is provided and this component's old parent is a container that still recognizes this component as its child.
	 * @throws IllegalArgumentException if a parent container is provided and the given parent container does not already recognize this component as its child.
	 * @see Container#add(Component)
	 * @see Container#remove(Object)
	 */
	public void setParent(final CompositeComponent newParent);

	/** @return Whether the component has dragging enabled. */
	public boolean isDragEnabled();

	/**
	 * Sets whether the component has dragging enabled. This is a bound property of type {@link Boolean}.
	 * @param newDragEnabled <code>true</code> if the component should allow dragging, else <code>false</code>.
	 * @see #DRAG_ENABLED_PROPERTY
	 */
	public void setDragEnabled(final boolean newDragEnabled);

	/** @return Whether the component has dropping enabled. */
	public boolean isDropEnabled();

	/**
	 * Sets whether the component has dropping enabled. This is a bound property of type {@link Boolean}.
	 * @param newDropEnabled <code>true</code> if the component should allow dropping, else <code>false</code>.
	 * @see #DROP_ENABLED_PROPERTY
	 */
	public void setDropEnabled(final boolean newDropEnabled);

	/** @return Whether flyovers are enabled for this component. */
	public boolean isFlyoverEnabled();

	/**
	 * Sets whether flyovers are enabled for this component. Flyovers contain information from the component model's "description" property. This is a bound
	 * property of type {@link Boolean}.
	 * @param newFlyoverEnabled <code>true</code> if the component should display flyovers, else <code>false</code>.
	 * @see InfoModel#getDescription()
	 * @see #FLYOVER_ENABLED_PROPERTY
	 */
	public void setFlyoverEnabled(final boolean newFlyoverEnabled);

	/** @return The installed flyover strategy, or <code>null</code> if there is no flyover strategy installed. */
	public FlyoverStrategy<?> getFlyoverStrategy();

	/**
	 * Sets the strategy for controlling flyovers. The flyover strategy will be registered as a mouse listener for this component. This is a bound property.
	 * @param newFlyoverStrategy The new flyover strategy, or <code>null</code> if there is no flyover strategy installed.
	 * @see #FLYOVER_STRATEGY_PROPERTY
	 */
	public void setFlyoverStrategy(final FlyoverStrategy<?> newFlyoverStrategy);

	/** @return Whether a theme has been applied to this component. */
	public boolean isThemeApplied();

	/**
	 * Sets whether a theme has been applied to this component. This is a bound property of type {@link Boolean}.
	 * @param newThemeApplied <code>true</code> if a theme has been applied to this component, else <code>false</code>.
	 * @see #THEME_APPLIED_PROPERTY
	 */
	public void setThemeApplied(final boolean newThemeApplied);

	/**
	 * Adds an export strategy to the component. The export strategy will take precedence over any compatible export strategy previously added.
	 * @param exportStrategy The export strategy to add.
	 */
	public void addExportStrategy(final ExportStrategy<?> exportStrategy);

	/**
	 * Removes an export strategy from the component.
	 * @param exportStrategy The export strategy to remove.
	 */
	public void removeExportStrategy(final ExportStrategy<?> exportStrategy);

	/**
	 * Exports data from the component. Each export strategy, from last to first added, will be asked to export data, until one is successful.
	 * @return The object to be transferred, or <code>null</code> if no data can be transferred.
	 */
	public Transferable<?> exportTransfer(); //TODO del when move to DepictedObject is complete

	/**
	 * Adds an import strategy to the component. The import strategy will take precedence over any compatible import strategy previously added.
	 * @param importStrategy The import strategy to add.
	 */
	public void addImportStrategy(final ImportStrategy<?> importStrategy);

	/**
	 * Removes an import strategy from the component.
	 * @param importStrategy The import strategy to remove.
	 */
	public void removeImportStrategy(final ImportStrategy<?> importStrategy);

	/**
	 * Imports data to the component. Each import strategy, from last to first added, will be asked to import data, until one is successful.
	 * @param transferable The object to be transferred.
	 * @return <code>true</code> if the given object was be imported.
	 */
	public boolean importTransfer(final Transferable<?> transferable);

	/**
	 * Initializes the component after construction. This method can only be called once during the life of a component. Subclasses should call this version.
	 * @throws IllegalStateException if this method has already been called.
	 */
	public void initialize();

	/** @return Whether the state of the component and all child components represents valid user input. */
	public boolean isValid();

	/**
	 * Validates the user input of this component and all child components. The component will be updated with error information.
	 * @return The current state of {@link #isValid()} as a convenience.
	 */
	public boolean validate();

	/**
	 * Returns the theme to apply to this component. If there is no theme to this component, the parent theme will be returned.
	 * @return The theme to apply to this component.
	 * @throws IOException if there is an error loading the theme.
	 */
	public Theme getTheme() throws IOException;

	/**
	 * Resets this object's theme. This method sets to <code>false</code> the state of whether a theme has been applied to this object. This method is called for
	 * any child components resetting its own theme. No new theme is actually loaded. There is normally no need to override this method or to call this method
	 * directly by applications.
	 * @see #setThemeApplied(boolean)
	 */
	public void resetTheme();

	/**
	 * Updates this object's theme. This method checks whether a theme has been applied to this object. If a theme has not been applied to this object this method
	 * calls {@link #applyTheme()}. This method is called for any child components before applying the theme to the component itself, to assure that child theme
	 * updates have already occurred before theme updates occur for this component. There is normally no need to override this method or to call this method
	 * directly by applications.
	 * @throws IOException if there was an error loading or applying a theme.
	 * @see #isThemeApplied()
	 * @see #applyTheme()
	 */
	public void updateTheme() throws IOException;

	/**
	 * Applies the theme to this object. Themes are only applied of the application is themed. This method may be overridden to effectively override theme
	 * settings by ensuring the state of important properties after the theme has been set. If the theme is successfully applied, this method updates the theme
	 * applied status.
	 * @throws IOException if there was an error loading or applying a theme.
	 * @see GuiseApplication#isThemed()
	 * @see #getTheme()
	 * @see #applyTheme(Theme)
	 * @see #setThemeApplied(boolean)
	 */
	public void applyTheme() throws IOException;

	/**
	 * Applies a theme and its parents to this object. The theme's rules will be applied to this object and any related objects. Theme application occurs
	 * unconditionally, regardless of whether themes have been applied to this component before. There is normally no need to call this method directly by
	 * applications.
	 * @param theme The theme to apply to the object.
	 */
	public void applyTheme(final Theme theme);

	/**
	 * Loads the preferences for this component and optionally any descendant components. Any preferences returned from {@link #getPreferenceProperties()} will be
	 * loaded automatically.
	 * @param includeDescendants <code>true</code> if preferences of any descendant components should also be loaded, else <code>false</code>.
	 * @throws IOException if there is an error loading preferences.
	 */
	public void loadPreferences(final boolean includeDescendants) throws IOException;

	/**
	 * Saves the preferences for this component and optionally any descendant components. Any preferences returned from {@link #getPreferenceProperties()} will be
	 * saved automatically.
	 * @param includeDescendants <code>true</code> if preferences of any descendant components should also be saved, else <code>false</code>.
	 * @throws IOException if there is an error saving preferences.
	 */
	public void savePreferences(final boolean includeDescendants) throws IOException;

	/**
	 * Dispatches an input event to this component and all child components, if any. If this is a {@link FocusedInputEvent}, the event will be directed towards
	 * the branch in which lies the focused component of any {@link InputFocusGroupComponent} ancestor of this component (or this component, if it is a focus
	 * group). If this is instead a {@link TargetedEvent}, the event will be directed towards the branch in which lies the target component of the event.
	 * Otherwise, the event will be dispatched to all child components. Only after the event has been dispatched to any children will the event be fired to any
	 * event listeners and then passed to the installed input strategy, if any. Once the event is consumed, no further processing takes place.
	 * @param inputEvent The input event to dispatch.
	 * @throws NullPointerException if the given event is <code>null</code>.
	 * @see TargetedEvent
	 * @see FocusedInputEvent
	 * @see InputEvent#isConsumed()
	 * @see #fireInputEvent(InputEvent)
	 * @see #getInputStrategy()
	 * @see InputStrategy#input(Input)
	 */
	public void dispatchInputEvent(final InputEvent inputEvent);

	/**
	 * Fire the given even to all registered listeners, if any. If the event is consumed further processing should cease.
	 * @param inputEvent The input event to fire.
	 * @throws NullPointerException if the given event is <code>null</code>.
	 * @see InputEvent#isConsumed()
	 */
	public void fireInputEvent(final InputEvent inputEvent);

	/**
	 * Adds a command listener.
	 * @param commandListener The command listener to add.
	 */
	public void addCommandListener(final CommandListener commandListener);

	/**
	 * Removes a command listener.
	 * @param commandListener The command listener to remove.
	 */
	public void removeCommandListener(final CommandListener commandListener);

	/** @return <code>true</code> if there is one or more command listeners registered. */
	public boolean hasCommandListeners();

	/**
	 * Adds a key listener.
	 * @param keyListener The key listener to add.
	 */
	public void addKeyListener(final KeyboardListener keyListener);

	/**
	 * Removes a key listener.
	 * @param keyListener The key listener to remove.
	 */
	public void removeKeyListener(final KeyboardListener keyListener);

	/** @return <code>true</code> if there is one or more key listeners registered. */
	public boolean hasKeyListeners();

	/**
	 * Adds a mouse listener.
	 * @param mouseListener The mouse listener to add.
	 */
	public void addMouseListener(final MouseListener mouseListener);

	/**
	 * Removes a mouse listener.
	 * @param mouseListener The mouse listener to remove.
	 */
	public void removeMouseListener(final MouseListener mouseListener);

	/** @return all registered mouse listeners. */
	//TODO del if not needed	public Iterable<MouseListener> getMouseListeners();

	/** @return <code>true</code> if there is one or more mouse listeners registered. */
	public boolean hasMouseListeners();

	/**
	 * Fires a mouse entered event to all registered mouse listeners. This method is used by the framework and should not be called directly by application code.
	 * @param componentBounds The absolute bounds of the component.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @throws NullPointerException if one or more of the arguments are <code>null</code>.
	 * @see MouseListener
	 * @see MouseEvent
	 */
	//TODO del if not needed	public void fireMouseEntered(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

	/**
	 * Fires a mouse exited event to all registered mouse listeners. This method is used by the framework and should not be called directly by application code.
	 * @param componentBounds The absolute bounds of the component.
	 * @param viewportBounds The absolute bounds of the viewport.
	 * @param mousePosition The position of the mouse relative to the viewport.
	 * @throws NullPointerException if one or more of the arguments are <code>null</code>.
	 * @see MouseListener
	 * @see MouseEvent
	 */
	//TODO del if not needed	public void fireMouseExited(final Rectangle componentBounds, final Rectangle viewportBounds, final Point mousePosition);

	/**
	 * Adds a notification listener.
	 * @param notificationListener The notification listener to add.
	 */
	public void addNotificationListener(final NotificationListener notificationListener);

	/**
	 * Removes a notification listener.
	 * @param notificationListener The notification listener to remove.
	 */
	public void removeNotificationListener(final NotificationListener notificationListener);

	/**
	 * Notifies the user of the given notification information. The notification is stored in this component using {@link #setNotification(Notification)}, which
	 * fires appropriate notification events. This method calls {@link GuiseSession#notify(Notification...)}.
	 * @param notification The notification information to relay.
	 */
	public void notify(final Notification notification);

	/**
	 * A strategy for showing and hiding flyovers in response to mouse events.
	 * @param <S> The type of component for which this object is to control flyovers.
	 * @author Garret Wilson
	 */
	public interface FlyoverStrategy<S extends Component> extends MouseListener {

		/**
		 * @return The requested line extent (width in left-to-right top-to-bottom orientation) of the flyover component, or <code>null</code> if no preferred line
		 *         extent has been specified.
		 */
		public Extent getLineExtent();

		/**
		 * Sets the requested line extent (width in left-to-right top-to-bottom orientation) of the flyover component.
		 * @param newLineExtent The new requested line extent of the flyover component, or <code>null</code> there is no line extent preference.
		 */
		public void setLineExtent(final Extent newLineExtent);

		/**
		 * @return The requested page extent (height in left-to-right top-to-bottom orientation) of the flyover component, or <code>null</code> if no preferred page
		 *         extent has been specified.
		 */
		public Extent getPageExtent();

		/**
		 * Sets the requested page extent (height in left-to-right top-to-bottom orientation) of the flyover component.
		 * @param newPageExtent The new requested page extent of the flyover component, or <code>null</code> there is no page extent preference.
		 */
		public void setPageExtent(final Extent newPageExtent);

		/** @return The style identifier of the flyover, or <code>null</code> if there is no style ID. */
		public String getStyleID();

		/**
		 * Identifies the style for the flyover component.
		 * @param newStyleID The style identifier of the flyover, or <code>null</code> if there is no style ID.
		 */
		public void setStyleID(final String newStyleID);

		/** @return The effect used for opening the flyover, or <code>null</code> if there is no open effect. */
		public Effect getOpenEffect();

		/**
		 * Sets the effect used for opening the flyover.
		 * @param newOpenEffect The new effect used for opening the flyover, or <code>null</code> if there should be no open effect.
		 * @see Frame#OPEN_EFFECT_PROPERTY
		 */
		public void setOpenEffect(final Effect newOpenEffect);

		/** Shows a flyover for the component. */
		public void openFlyover();

		/** Closes the flyover for the component. */
		public void closeFlyover();
	}

}
