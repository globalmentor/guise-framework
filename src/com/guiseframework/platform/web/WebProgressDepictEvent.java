package com.guiseframework.platform.web;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.model.TaskState;
import com.guiseframework.platform.DepictedObject;

/**Indicates that some sort of progress has been made on the web platform.
@author Garret Wilson
*/
public class WebProgressDepictEvent extends AbstractWebDepictEvent
{
	/**The task being performed, or <code>null</code> if not indicated.*/
	private String task;

		/**@return The task being performed, or <code>null</code> if not indicated.*/
		public String getTask() {return task;}

	/**The state of the task.*/
	private TaskState taskState;

		/**@return The state of the task.*/
		public TaskState getTaskState() {return taskState;}

	/**The amount of progress that has been made, or -1 if the progress is unknown.*/
	private final long progress;

		/**@return The amount of progress that has been made, or -1 if the progress is unknown.*/
		public long getProgress() {return progress;}

	/**The total amount of progress that will have been made upon completion, or -1 if the goal is unknown.*/
	private final long goal;

		/**@return The total amount of progress that will have been made upon completion, or -1 if the goal is unknown.*/
		public long getGoal() {return goal;}

	/**Depicted object constructor with an unknown goal.
	@param depictedObject The depicted object on which the event initially occurred.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@param progress The amount of progress that has been made, or -1 if the progress is unknown.
	@exception NullPointerException if the given depicted object and/or task state is <code>null</code>.
	*/
	public WebProgressDepictEvent(final DepictedObject depictedObject, final String task, final TaskState taskState, final long progress)
	{
		this(depictedObject, task, taskState, progress, -1);	//construct the class with an unknown goal
	}

	/**Depicted object and goal constructor.
	@param depictedObject The depicted object on which the event initially occurred.
	@param task The task being performed, or <code>null</code> if not indicated.
	@param taskState The state of the task.
	@param progress The amount of progress that has been made, or -1 if the progress is unknown.
	@param goal The total amount of progress that will have been made upon completion, or -1 if the goal is unknown.
	@exception NullPointerException if the given depicted object and/or task state is <code>null</code>.
	*/
	public WebProgressDepictEvent(final DepictedObject depictedObject, final String task, final TaskState taskState, final long progress, final long goal)
	{
		super(depictedObject);	//construct the parent class
		this.task=task;
		this.taskState=checkInstance(taskState, "Task state cannot be null.");
		this.progress=progress;
		this.goal=goal;
	}
}