package com.javaguise.component;

import com.javaguise.model.LabelModel;

/**A component that accepts user interaction to manipulate a data model.
@param <M> The type of model contained in the component.
@author Garret Wilson
*/
public interface Control<M extends LabelModel, C extends Control<M, C>> extends ModelComponent<M, C>
{
}