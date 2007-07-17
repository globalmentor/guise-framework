package com.guiseframework.component;

import java.util.Collection;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.util.Debug;

import com.guiseframework.Bookmark;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.geometry.Extent;
import com.guiseframework.model.TaskState;
import com.guiseframework.platform.PlatformFile;
import com.guiseframework.prototype.ActionPrototype;
import static com.guiseframework.theme.Theme.*;

/**Panel to collect resources and send them to the specified destination.
@author Garret Wilson
*/
public class UploadPanel extends AbstractPanel
{

	/**The bound property of the destination path.*/
	public final static String DESTINATION_PATH_PROPERTY=getPropertyName(UploadPanel.class, "destinationPath");

	/**The bound property of the destination bookmark.*/
	public final static String DESTINATION_BOOKMARK_PROPERTY=getPropertyName(UploadPanel.class, "destinationBookmark");

	/**The number of platform files to display at the same time.*/
	private final static int PLATFORM_FILE_DISPLAY_COUNT=16;

	/**The panel containing controls such as buttons.*/
	private final Panel controlPanel;

		/**@return The panel containing controls such as buttons.*/
		public Panel getControlPanel() {return controlPanel;}

	/**The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
	private String destinationPath=null;

		/**@return The destination path of the upload relative to the application context path, or <code>null</code> if the destination path has not yet been set.*/
		public String getDestinationPath() {return destinationPath;}

		/**Sets the destination path of the upload.
		This is a bound property.
		@param newDestinationPath The path relative to the application representing the destination of the collected resources.
		@exception NullPointerException if the given path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		@see #DESTINATION_PATH_PROPERTY
		*/
		public void setDestinationPath(final String newDestinationPath)
		{
			if(!ObjectUtilities.equals(destinationPath, checkRelativePath(newDestinationPath)))	//if the value is really changing
			{
				final String oldDestinationPath=destinationPath;	//get the old value
				destinationPath=newDestinationPath;	//actually change the value
				firePropertyChange(DESTINATION_PATH_PROPERTY, oldDestinationPath, newDestinationPath);	//indicate that the value changed
			}
		}

	/**The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
	private Bookmark destinationBookmark=null;

		/**@return The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.*/
		public Bookmark getDestinationBookmark() {return destinationBookmark;}	

		/**Sets the destination bookmark of the upload.
		This is a bound property.
		@param newDestinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
		@see #DESTINATION_BOOKMARK_PROPERTY
		*/
		public void setDestinationBookmark(final Bookmark newDestinationBookmark)
		{
			if(!ObjectUtilities.equals(destinationBookmark, newDestinationBookmark))	//if the value is really changing
			{
				final Bookmark oldDestinationBookmark=destinationBookmark;	//get the old value
				destinationBookmark=newDestinationBookmark;	//actually change the value
				firePropertyChange(DESTINATION_BOOKMARK_PROPERTY, oldDestinationBookmark, newDestinationBookmark);	//indicate that the value changed
			}
		}

	/**The platform file list control.*/
	private final ListControl<PlatformFile> platformFileListControl;

	/**The label containing the current status.*/
	private final Label currentStatusLabel;
	
	/**The resource collect control.*/
	private final ResourceCollectControl resourceCollectControl;

		/**@return The resource collect control.*/
		public ResourceCollectControl getResourceCollectControl() {return resourceCollectControl;}

	/**The action prototype for browsing the platform file system.*/
	private final ActionPrototype browseActionPrototype;

		/**@return The action prototype for browsing the platforom file system.*/
		public ActionPrototype getBrowseActionPrototype() {return browseActionPrototype;}

	/**The action prototype for uploading.*/
	private final ActionPrototype uploadActionPrototype;

		/**@return The action prototype for uploading.*/
		public ActionPrototype getUploadActionPrototype() {return uploadActionPrototype;}

	/**The action prototype for canceling.*/
	private final ActionPrototype cancelActionPrototype;

		/**@return The action prototype for canceling.*/
		public ActionPrototype getCancelActionPrototype() {return cancelActionPrototype;}

	/**The progress listener that updates that status components in response to file transfers.*/
	private final ProgressListener platformFileProgressListener=new ProgressListener()
			{
				public void progressed(final ProgressEvent progressEvent)	//when progress occurs
				{
					updateStatusLabel((PlatformFile)progressEvent.getSource(), progressEvent.getTaskState(), progressEvent.getValue(), progressEvent.getMaximumValue());	//update the status level with the progress
//TODO fix; why do we need this? do something with overall progress if we need to					fireProgressed(new ProgressEvent(UploadPanel.this, progressEvent));	//refire the progress event using this panel as the source
				}
			};

	/**Destination path constructor a default vertical flow layout.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public UploadPanel(final String destinationPath)
	{
		this(destinationPath, null);	//construct the panel with no bookmark
	}

	/**Destination path and destination bookmark constructor a default vertical flow layout.
	@param destinationPath The path relative to the application representing the destination of the collected resources.
	@param destinationBookmark The bookmark to be used in sending resources to the destination path, or <code>null</code> if there is no bookmark specified.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	public UploadPanel(final String destinationPath, final Bookmark destinationBookmark)
	{
		this();	//construct the default panel
		setDestinationPath(destinationPath);	//set the destination path
		setDestinationBookmark(destinationBookmark);	//set the destionation bookmark
	}

	/**Default constructor with a default vertical flow layout.*/
	public UploadPanel()
	{
		super(new FlowLayout(Flow.PAGE));	//construct the parent class
		platformFileListControl=new ListControl<PlatformFile>(PlatformFile.class, PLATFORM_FILE_DISPLAY_COUNT);	//create a list in which to show the platform files
		platformFileListControl.setEditable(false);	//don't allow the list to be edited
		platformFileListControl.setLineExtent(new Extent(30, Extent.Unit.EM));
		add(platformFileListControl);

		currentStatusLabel=new Label();	//current status label
		add(currentStatusLabel);
		
			//the horizontal panel of controls
		controlPanel=new LayoutPanel(new FlowLayout(Flow.LINE));
		resourceCollectControl=new ResourceCollectControl();	//resource collector
		controlPanel.add(resourceCollectControl);

		browseActionPrototype=new ActionPrototype(LABEL_BROWSE+HORIZONTAL_ELLIPSIS_CHAR, GLYPH_BROWSE);	//browse
		browseActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						getSession().getPlatform().selectPlatformFiles(true, new ValueSelectListener<Collection<PlatformFile>>()	//select platform files, listening for the selection to occur
								{
									public void valueSelected(final ValueEvent<Collection<PlatformFile>> valueEvent)	//when files are selected
									{
										final Collection<PlatformFile> platformFiles=valueEvent.getValue();	//get the new platform files
										platformFileListControl.clear();	//remove the currently displayed platform files
										platformFileListControl.addAll(platformFiles);	//add all the new platform files to the list
										for(final PlatformFile platformFile:platformFiles)	//for each platform file
										{
											platformFile.removeProgressListener(platformFileProgressListener);	//make sure we're not already listening for progress on this platform file
											platformFile.addProgressListener(platformFileProgressListener);	//start listening for progress on this platform file
										}
										updateComponents();	//update the components in response
									}							
								});
					}
				});
		controlPanel.add(browseActionPrototype);
		uploadActionPrototype=new ActionPrototype(LABEL_UPLOAD, GLYPH_UPLOAD);	//resource upload
		uploadActionPrototype.setEnabled(false);	//initially disable upload
		uploadActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						final String destinationPath=getDestinationPath();	//get the destination path
						if(destinationPath==null)	//if there is no destination path
						{
							throw new IllegalStateException("Destination path not set.");
						}
						platformFileListControl.get(0).upload(destinationPath, getDestinationBookmark());	//TODO fix; testing
					}
				});
		controlPanel.add(uploadActionPrototype);
		cancelActionPrototype=new ActionPrototype(LABEL_CANCEL, GLYPH_CANCEL);	//upload cancel
		cancelActionPrototype.setEnabled(false);	//initially disable canceling
		cancelActionPrototype.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent actionEvent)
					{
						resourceCollectControl.cancelReceive();	//tell the resource collect control to cancel the transfer
					}
				});
		controlPanel.add(cancelActionPrototype);
	
			//listen for the resource collection control changing its list of collected resource paths
/*TODO del all resource collect control references
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.RESOURCE_PATHS_PROPERTY, new AbstractGenericPropertyChangeListener<List<String>>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<List<String>> genericPropertyChangeEvent)	//if the list of resource path changes
					{
						platformFileListControl.clear();	//remove the currently displayed resource paths
						platformFileListControl.addAll(genericPropertyChangeEvent.getNewValue());	//add all the new resource paths to the list
						updateComponents();	//update the components in response
					}
				});
*/
			//listen for the resource collection control changing its send state, and update the state of the components in response
/*TODO del
		resourceCollectControl.addPropertyChangeListener(ResourceCollectControl.STATE_PROPERTY, new AbstractGenericPropertyChangeListener<TaskState>()
				{
					public void propertyChange(final GenericPropertyChangeEvent<TaskState> propertyChangeEvent)	//if the transfer state changes
					{
						updateComponents();	//update the components in response
						updateStatusLabel(null, -1, propertyChangeEvent.getNewValue());	//update the status label with the new state
					}
				});
*/
			//listen for progress from the resource collect control and update the progress labels in response
/*TODO del
		resourceCollectControl.addProgressListener(new ProgressListener()
				{
					public void progressed(final ProgressEvent progressEvent)	//if progress occurs
					{
						updateStatusLabel(progressEvent.getTask(), progressEvent.getValue(), progressEvent.getTaskState());	//update the status level with the progress
						fireProgressed(new ProgressEvent(UploadPanel.this, progressEvent));	//refire the progress event using this panel as the source
					}
				});
*/
		add(controlPanel);
	}

	/**Updates the state of components.*/
	protected void updateComponents()
	{
		uploadActionPrototype.setEnabled(!platformFileListControl.isEmpty());	//only allow upload if no platform files are being uploaded and there are platform files to upload
//TODO fix		cancelActionPrototype.setEnabled(state==TaskState.INCOMPLETE);	//only allow cancel if the control is sending
	}

	/**Updates the status label.
	@param platformFile The current platform file.
	@param state The new transfer state, or <code>null</code> if there is no state.
	@param transferred The current number of bytes transferred, or -1 if the bytes transferred is not known.
	@param total The total number of bytes to transfer, or -1 if the total is not known.
	*/
	protected void updateStatusLabel(final PlatformFile platformFile, final TaskState state, final long progress, final long total)	//TODO update API after deciding how to determine overall transfer
	{
		final StringBuilder statusStringBuilder=new StringBuilder();	//build the status string
		if(platformFile!=null)	//if there is a platform file
		{
			statusStringBuilder.append(platformFile.getName()).append(':').append(' ');	//platform file:
			if(progress>=0)	//if a valid value is given
			{
				statusStringBuilder.append(progress);	//show the value
			}
			else	//if there is no value
			{
				statusStringBuilder.append(LABEL_UNKNOWN);	//indicate an unknown progress
			}
			if(total>=0)	//if the total is known
			{
				statusStringBuilder.append('/').append(progress);	//show the total
			}
		}
		else if(state!=null)	//if there is no platform file, just show the task state
		{
			statusStringBuilder.append(state.getLabel());	//show the task status label
		}
		currentStatusLabel.setLabel(statusStringBuilder.toString());	//update the status
	}

	/**Adds a progress listener.
	@param progressListener The progress listener to add.
	*/
	public void addProgressListener(final ProgressListener progressListener)
	{
		getEventListenerManager().add(ProgressListener.class, progressListener);	//add the listener
	}

	/**Removes an progress listener.
	@param progressListener The progress listener to remove.
	*/
	public void removeProgressListener(final ProgressListener progressListener)
	{
		getEventListenerManager().remove(ProgressListener.class, progressListener);	//remove the listener
	}

	/**Fires a given progress event to all registered progress listeners.
	@param progressEvent The progress event to fire.
	*/
	protected void fireProgressed(final ProgressEvent progressEvent)
	{
		for(final ProgressListener progressListener:getEventListenerManager().getListeners(ProgressListener.class))	//for each progress listener
		{
			progressListener.progressed(progressEvent);	//dispatch the progress event to the listener
		}
	}

}
