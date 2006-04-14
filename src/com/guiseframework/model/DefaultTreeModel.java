package com.guiseframework.model;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.event.*;

/**A default implementation of a tree model.
If no root node is specified, the root node will be a dummy root node that will not be displayed.
If a dummy root node is used, it will automatically be set to an expanded state.
@author Garret Wilson
@see DummyTreeNodeModel
*/
public class DefaultTreeModel extends AbstractModel implements TreeModel
{

	/**A listener to listen for changes in properties of tree nodes that have bubbled up the hierarchy and refire them as {@link TreeNodePropertyChangeEvent}s.*/
	private final PropertyChangeListener treeNodePropertyChangeListener=new PropertyChangeListener()	//TODO update this to match the repeat action listener
		{
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if a tree node property changes
			{
				fireTreeNodePropertyChange(propertyChangeEvent);	//fire an event indicating that a tree node property changed
			}		
		};

	/**An action listener to repeat copies of events received, using this component as the source.*/ 
	private ActionListener repeatActionListener=new ActionListener()
			{
				public void actionPerformed(final ActionEvent actionEvent)	//if an action was performed
				{
					final ActionEvent repeatActionEvent=new ActionEvent(DefaultTreeModel.this, actionEvent);	//copy the action event with this class as its source
					fireActionPerformed(repeatActionEvent);	//fire the repeated action
				}
			};

		/**@return An action listener to repeat copies of events received, using this component as the source.*/ 
//TODO del if not needed		protected ActionListener getRepeatActionListener() {return repeatActionListener;}

	/**The rot node of the tree model.*/
	private TreeNodeModel<?> rootNode;

		/**@return The root node of the tree model.*/
		public TreeNodeModel<?> getRootNode() {return rootNode;}

		/**Sets the root node of the tree model.
		This is a bound property.
		@param newRootNode The new root node of the tree model.
		@exception NullPointerException if the given root node is <code>null</code>.
		@see #ROOT_NODE_PROPERTY
		*/
		public void setRootNode(final TreeNodeModel<?> newRootNode)
		{
			if(!ObjectUtilities.equals(rootNode, checkInstance(newRootNode, "Root node cannot be null.")))	//if the value is really changing
			{
				final TreeNodeModel<?> oldRootNode=rootNode;	//get the old value
				oldRootNode.removePropertyChangeListener(treeNodePropertyChangeListener);	//stop listening for bubbled property change events from tree nodes in the old hierarchy
				oldRootNode.removeActionListener(repeatActionListener);	//stop listening for bubbled action events from tree nodes in the old hierarchy
				rootNode=newRootNode;	//actually change the value
				newRootNode.addPropertyChangeListener(treeNodePropertyChangeListener);	//start listening for bubbled property change events from tree nodes in the new hierarchy
				newRootNode.addActionListener(repeatActionListener);	//start listening for bubbled action events from tree nodes in the new hierarchy
				firePropertyChange(ROOT_NODE_PROPERTY, oldRootNode, newRootNode);	//indicate that the value changed
			}			
		}
		
	/**Whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.*/
//TODO del if not needed	private boolean editable=true;

		/**@return Whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.*/
//TODO del if not needed		public boolean isEditable() {return editable;}

		/**Sets whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the nodes should allow the user to change their values if they are also designated as editable.
		@see TableModel#EDITABLE_PROPERTY
		*/
/*TODO del if not needed
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}
*/

	/**Default constructor with a dummy root tree node.*/
	public DefaultTreeModel()
	{
		this(new DummyTreeNodeModel());	//create a dummy root node
	}

	/**Root node constructor.
	@param session The Guise session that owns this model.
	@param rootNode The root node of the tree model.
	@exception NullPointerException if the given root node is <code>null</code>.
	*/
	public DefaultTreeModel(final TreeNodeModel<?> rootNode)
	{
		this.rootNode=checkInstance(rootNode, "Root node cannot be null.");	//save the root node
		this.rootNode.addPropertyChangeListener(treeNodePropertyChangeListener);	//start listening for bubbled property change events from tree nodes
		this.rootNode.addActionListener(repeatActionListener);	//start listening for bubbled action events from tree nodes
		if(rootNode instanceof DummyTreeNodeModel)	//if a dummy tree node model is being used
		{
			rootNode.setExpanded(true);	//expand the dummy root, as the root node itself will not normally be available to the user
		}
	}

	/**Sets whether all tree nodes are expanded.
	This method delegates to the root node {@link TreeNodeModel#setAllExpanded(boolean)}.
	@param newAllExpanded <code>true</code> if all the nodes should be expanded, or <code>false</code> if they should be collapsed.
	*/
	public void setAllExpanded(final boolean newAllExpanded)
	{
		getRootNode().setAllExpanded(newAllExpanded);	//tell the root node to expand or collapse all its children
	}

		//TODO replace tree node property change stuff with special targeted property change information 

	/**Adds a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to add.
	*/
	public void addTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener)
	{
		getEventListenerManager().add(TreeNodePropertyChangeListener.class, treeNodePropertyChangeListener);	//add the listener
	}

	/**Removes a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to remove.
	*/
	public void removeTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener)
	{
		getEventListenerManager().remove(TreeNodePropertyChangeListener.class, treeNodePropertyChangeListener);	//remove the listener
	}

	/**Fires a tree node property change event to all registered tree node property change listeners.
	@param propertyChangeEvent The property change event representing the property change of the tree node.
	@see TreeNodePropertyChangeListener
	@see TreeNodePropertyChangeEvent
	*/
	protected void fireTreeNodePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(TreeNodePropertyChangeListener.class))	//if there are tree node property change listeners registered
		{
			final TreeNodePropertyChangeEvent<Object> treeNodePropertyChangeEvent=new TreeNodePropertyChangeEvent<Object>(this, propertyChangeEvent);	//create a new tree node property change event
			for(final TreeNodePropertyChangeListener<Object> treeNodePropertyChangeListener:eventListenerManager.getListeners(TreeNodePropertyChangeListener.class))	//for each registered tree node property change listener
			{
				treeNodePropertyChangeListener.propertyChange(treeNodePropertyChangeEvent);
			}
		}
	}

		//ActionModel support

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**@return all registered action listeners.*/
	public Iterable<ActionListener> getActionListeners()
	{
		return getEventListenerManager().getListeners(ActionListener.class);	//remove the listener
	}

	/**Performs the action with default force and default option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This method delegates to {@link #performAction(int, int)}.
	*/
	public void performAction()
	{
		performAction(1, 0);	//fire an event saying that the action has been performed with the default force and option
	}

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		fireActionPerformed(force, option);	//fire an event saying that the action has been performed with the given force and option
	}

	/**Fires an action event to all registered action listeners.
	This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed(final int force, final int option)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			fireActionPerformed(new ActionEvent(this, force, option));	//create and fire a new action event
		}
	}

	/**Fires a given action event to all registered action listeners.
	@param actionEvent The action event to fire.
	*/
	protected void fireActionPerformed(final ActionEvent actionEvent)
	{
		for(final ActionListener actionListener:getEventListenerManager().getListeners(ActionListener.class))	//for each action listener
		{
			actionListener.actionPerformed(actionEvent);	//dispatch the action to the listener
		}
	}

}
