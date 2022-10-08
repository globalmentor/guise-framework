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

import static io.guise.framework.Resources.*;

import java.net.URI;

import com.globalmentor.model.TaskState;

import io.guise.framework.Resources;
import io.guise.framework.model.*;
import io.guise.framework.theme.Theme;

/**
 * Selectable link that stores a task state. The link uses selected and unselected icons from the resources using resource keys
 * <code>select.action.selected.glyph</code> and <code>select.action.unselected.glyph</code>, respectively. The link uses task state icons from the resources
 * using resource keys <code>task.state.<var>taskState</var>.glyph</code>, where <var>taskState</var> represents the task state enum value such as
 * {@link TaskState#INCOMPLETE} in its resource key form such as <code>task.state.incomplete.glyph</code>, and <code>task.state..glyph</code> for the
 * <code>null</code> task state value.
 * @author Garret Wilson
 * @see Resources#getResourceKeyName(Enum)
 */
public class TaskStateSelectLink extends ValueSelectLink<TaskState> {

	/** The resource URI for the selected icon. */
	public static final URI SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI = createURIResourceReference("theme.select.action.selected.glyph"); //TODO eventually remove these and use the defaults (make sure the defualts are set, using theme.glyph.selected)
	/** The resource URI for the unselected icon. */
	public static final URI SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI = createURIResourceReference("theme.select.action.unselected.glyph");

	/** Default constructor. */
	public TaskStateSelectLink() {
		this(new DefaultInfoModel(), new DefaultActionModel(), new DefaultValueModel<TaskState>(TaskState.class), new DefaultEnableable()); //construct the class with default models
	}

	/**
	 * Info model, action model, value model, and enableable object constructor.
	 * @param infoModel The component info model.
	 * @param actionModel The component action model.
	 * @param valueModel The component value model.
	 * @param enableable The enableable object in which to store enabled status.
	 * @throws NullPointerException if the given info model, action model, and/or enableable object is <code>null</code>.
	 */
	public TaskStateSelectLink(final InfoModel infoModel, final ActionModel actionModel, final ValueModel<TaskState> valueModel, final Enableable enableable) {
		super(infoModel, actionModel, valueModel, enableable); //construct the parent class		
		setSelectedGlyphURI(SELECT_ACTION_SELECTED_GLYPH_RESOURCE_URI);
		setUnselectedGlyphURI(SELECT_ACTION_UNSELECTED_GLYPH_RESOURCE_URI);
		setValueGlyphURI(null, Theme.GLYPH_BLANK); //set the icon resource for no task state
		for(final TaskState taskState : TaskState.values()) { //for each task status
			setValueGlyphURI(taskState, Resources.getGlyphResourceReference(taskState)); //set the icon resource for this task state
		}
	}

}
