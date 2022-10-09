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

import java.net.URI;
import java.util.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.net.URIs.*;

import com.globalmentor.event.EventListenerManager;
import com.globalmentor.model.TaskState;
import com.globalmentor.net.URIs;

import io.guise.framework.*;
import io.guise.framework.event.*;
import io.guise.framework.model.*;

/**
 * Control that allows resources to be collected and received at a given path. The destination path should reference a registered
 * {@link ResourceWriteDestination} of the application.
 * @author Garret Wilson
 */
public class ResourceCollectControl extends AbstractControl {

	/** The bound property of the paths of the collected resources. */
	public static final String RESOURCE_PATHS_PROPERTY = getPropertyName(ResourceCollectControl.class, "resourcePaths");
	/** The bound property of the state of receiving collected resources. */
	public static final String STATE_PROPERTY = getPropertyName(ResourceCollectControl.class, "state");

	@Override
	@SuppressWarnings("unchecked")
	public Depictor<? extends ResourceCollectControl> getDepictor() {
		return (Depictor<? extends ResourceCollectControl>)super.getDepictor();
	}

	/** The state of receiving resources, or <code>null</code> if receiving has not been initiated. */
	private TaskState state = null;

	/** @return The state of receiving resources, or <code>null</code> if receiving has not been initiated. */
	public TaskState getState() {
		return state;
	}

	/**
	 * Sets the state of receiving resources. This method is called by the associated depictor and should normally not be called directly by applications. This is
	 * a bound property.
	 * @param newSendState The new state of receiving resources, or <code>null</code> if receiving has not been initiated.
	 * @see #STATE_PROPERTY
	 */
	public void setState(final TaskState newSendState) {
		if(state != newSendState) { //if the value is really changing
			final TaskState oldState = state; //get the old value
			state = newSendState; //actually change the value
			firePropertyChange(STATE_PROPERTY, oldState, newSendState); //indicate that the value changed
		}
	}

	/** The paths of the currently collected resources. */
	private List<String> resourcePaths = new CopyOnWriteArrayList<String>();

	/**
	 * Returns the paths of the currently collected resources. These paths are for identification only, and are not guaranteed to represent any location
	 * accessible from the application.
	 * @return The the paths of the currently collected resources.
	 */
	public List<String> getResourcePaths() {
		return unmodifiableList(resourcePaths);
	}

	/**
	 * Adds a new resource path. This method changes a bound property of type {@link List} holding type {@link String}. This method is called by the framework and
	 * normally this should not be called directly from applications. Manually adding a new resource path, depending on the platform, may not actually result in
	 * another resource being collected absent user intervention.
	 * @param resourcePath The resource path to add.
	 * @throws NullPointerException if the given resource path is <code>null</code>.
	 * @see #RESOURCE_PATHS_PROPERTY
	 */
	public void addResourcePath(final String resourcePath) {
		resourcePaths.add(requireNonNull(resourcePath, "Resource path cannot be null."));
		final List<String> newList = unmodifiableList(new ArrayList<String>(resourcePaths)); //create an unmodifiable copy of the resource paths
		firePropertyChange(RESOURCE_PATHS_PROPERTY, null, newList); //indicate that the value changed			
	}

	/**
	 * Removes a resource path. This method changes a bound property of type {@link List} holding type {@link String}. This method is called by the framework and
	 * normally this should not be called directly from applications. //TODO fix to actually tell the browser control that the path has changed Manually adding a
	 * new resource path, depending on the platform, may not actually result in another resource being collected absent user intervention.
	 * @param resourcePath The resource path to remove.
	 * @throws NullPointerException if the given resource path is <code>null</code>.
	 * @see #RESOURCE_PATHS_PROPERTY
	 */
	public void removeResourcePath(final String resourcePath) {
		resourcePaths.remove(requireNonNull(resourcePath, "Resource path cannot be null."));
		final List<String> newList = unmodifiableList(new ArrayList<String>(resourcePaths)); //create an unmodifiable copy of the resource paths
		firePropertyChange(RESOURCE_PATHS_PROPERTY, null, newList); //indicate that the value changed			
	}

	/**
	 * Clears all resource paths. This method changes a bound property of type {@link List} holding type {@link String}. This method is called by the framework
	 * and normally this should not be called directly from applications. //TODO fix to actually tell the browser control that the path has changed Manually
	 * adding a new resource path, depending on the platform, may not actually result in another resource being collected absent user intervention.
	 * @see #RESOURCE_PATHS_PROPERTY
	 */
	public void clearResourcePaths() {
		resourcePaths.clear(); //clear the resource paths
		final List<String> newList = unmodifiableList(new ArrayList<String>(resourcePaths)); //create an unmodifiable copy of the resource paths
		firePropertyChange(RESOURCE_PATHS_PROPERTY, null, newList); //indicate that the value changed			
	}

	/** The destination path relative to the application context path, of <code>null</code> if no resources are currently being sent. */
	private String destinationPath = null;

	/**
	 * Indicates the destination path relative to the application context path.
	 * @return The path representing the destination of the collected resources, or <code>null</code> if no resources are currently being sent.
	 */
	public String getDestinationPath() {
		return destinationPath;
	}

	/**
	 * The bookmark being used in receiving the resources at the destination path, or <code>null</code> if there is no bookmark specified and/or no resources are
	 * currently being sent.
	 */
	private Bookmark destinationBookmark = null;

	/**
	 * @return The bookmark being used in receiving the resources at the destination path, or <code>null</code> if there is no bookmark specified and/or no
	 *         resources are currently being sent.
	 */
	public Bookmark getDestinationBookmark() {
		return destinationBookmark;
	}

	/** Default constructor with a default models. */
	public ResourceCollectControl() {
		this(new DefaultInfoModel(), new DefaultEnableable()); //construct the class with default models
	}

	/**
	 * Info model and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model and/or enableable object is <code>null</code>.
	 */
	public ResourceCollectControl(final InfoModel infoModel, final Enableable enableable) {
		super(infoModel, enableable); //construct the parent class
	}

	/**
	 * Receives collected resources at the given destination path with no bookmark.
	 * @param destinationPath The path representing the destination of the collected resources, relative to the application.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	 */
	public void receiveResources(final String destinationPath) {
		receiveResources(destinationPath, null); //send the resources with no bookmark
	}

	/**
	 * Receives collected resources at the given destination path using the given bookmark. If the resources are currently started to be received or already being
	 * received, no action occurs.
	 * @param destinationPath The path representing the destination of the collected resources, relative to the application.
	 * @param destinationBookmark The bookmark to be used in receiving the resources at the destination path, or <code>null</code> if no bookmark should be used.
	 * @throws NullPointerException if the given path is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public void receiveResources(final String destinationPath, final Bookmark destinationBookmark) {
		final TaskState sendState = getState(); //get the current send state
		if(sendState != TaskState.INITIALIZE && sendState != TaskState.INCOMPLETE) { //if the transfer has not yet started
			setState(TaskState.INITIALIZE); //show that we're initializing the transfer
			this.destinationPath = checkRelativePath(destinationPath); //save the path
			this.destinationBookmark = destinationBookmark; //save the bookmark
			getDepictor().receive(createPathURI(destinationPath), destinationBookmark); //tell the depictor to start receiving
		}
	}

	/**
	 * Cancels any currently occurring resource transfers. If the resources are not currently being transferred, no action occurs.
	 */
	public void cancelReceive() {
		final TaskState sendState = getState(); //get the current send state
		if(sendState == TaskState.INCOMPLETE) { //if a transfer is occurring
			getDepictor().cancel(); //tell the depictor to cancel
		}
	}

	/**
	 * Adds a progress listener.
	 * @param progressListener The progress listener to add.
	 */
	public void addProgressListener(final ProgressListener<Long> progressListener) {
		getEventListenerManager().add(ProgressListener.class, progressListener); //add the listener
	}

	/**
	 * Removes an progress listener.
	 * @param progressListener The progress listener to remove.
	 */
	public void removeProgressListener(final ProgressListener<Long> progressListener) {
		getEventListenerManager().remove(ProgressListener.class, progressListener); //remove the listener
	}

	/**
	 * Fires a progress event to all registered progress listeners. This method delegates to {@link #fireProgressed(ProgressEvent)}.
	 * @param task The task being performed, or <code>null</code> if not indicated.
	 * @param taskState The state of the task.
	 * @param value The current progress, or <code>-1</code> if not known.
	 * @param maximumValue The goal, or <code>-1</code> if not known.
	 * @throws NullPointerException if the given task state is <code>null</code>.
	 * @see ProgressListener
	 * @see ProgressEvent
	 */
	public void fireProgressed(final String task, final TaskState taskState, final long value, final long maximumValue) {
		//TODO put this stuff in a setProgress() method, and keep track of this information locally
		if(task != null && taskState == TaskState.COMPLETE) { //if we complete a file
			for(final String resourcePath : resourcePaths) { //look at the resource paths
				if(URIs.getName(resourcePath).equals(task)) { //if this resource path just finished TODO create a better way to check; this could result in inconsistencies if multiple paths have the same filename
					removeResourcePath(resourcePath); //remove this resource path
					break; //stop checking for matches
				}
			}
		}
		final EventListenerManager eventListenerManager = getEventListenerManager(); //get event listener support
		if(eventListenerManager.hasListeners(ProgressListener.class)) { //if there are progress listeners registered
			fireProgressed(new ProgressEvent<Long>(this, task, taskState, value, maximumValue)); //create and fire a new progress event
		}
	}

	/**
	 * Fires a given progress event to all registered progress listeners.
	 * @param progressEvent The progress event to fire.
	 */
	@SuppressWarnings("unchecked")
	protected void fireProgressed(final ProgressEvent<Long> progressEvent) {
		for(final ProgressListener<Long> progressListener : getEventListenerManager().getListeners(ProgressListener.class)) { //for each progress listener
			progressListener.progressed(progressEvent); //dispatch the progress event to the listener
		}
	}

	/**
	 * The custom depictor type for web collect controls.
	 * @author Garret Wilson
	 * @param <C> The type of control to be depicted.
	 */
	public interface Depictor<C extends ResourceCollectControl> extends io.guise.framework.platform.Depictor<C> {

		/**
		 * Requests that resource collection start.
		 * @param destinationURI The URI representing the destination of the collected resources, relative to the application.
		 * @param destinationBookmark The bookmark to be used in receiving the resources at the destination path, or <code>null</code> if no bookmark should be
		 *          used.
		 * @throws NullPointerException if the given destination URI is <code>null</code>.
		 */
		public void receive(URI destinationURI, final Bookmark destinationBookmark);

		/** Requests that resource collection be canceled. */
		public void cancel();

	}

}
