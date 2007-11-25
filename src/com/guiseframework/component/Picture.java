package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.model.*;

/**An image component that displays an associated label and description, if present.
@author Garret Wilson
*/
public class Picture extends AbstractImageComponent
{

	/**The bound property of whether the description is displayed.*/
	public final static String DESCRIPTION_DISPLAYED_PROPERTY=getPropertyName(Picture.class, "descriptionDisplayed");
	/**The image opacity bound property.*/
	public final static String IMAGE_OPACITY_PROPERTY=getPropertyName(Picture.class, "imageOpacity");

	/**The bound property of whether the component has image dragging enabled.*/
//TODO del if not needed	public final static String IMAGE_DRAG_ENABLED_PROPERTY=getPropertyName(Picture.class, "imageDragEnabled");

	/**Whether the description is displayed.*/
	private boolean descriptionDisplayed=true;

		/**@return Whether the description is displayed.
		@see #isDisplayed()
		*/
		public boolean isDescriptionDisplayed() {return descriptionDisplayed;}

		/**Sets whether the description is displayed.
		This is a bound property of type {@link Boolean}.
		@param newDescriptionDisplayed <code>true</code> if the description should be displayed, else <code>false</code>.
		@see #DESCRIPTION_DISPLAYED_PROPERTY
		*/
		public void setDescriptionDisplayed(final boolean newDescriptionDisplayed)
		{
			if(descriptionDisplayed!=newDescriptionDisplayed)	//if the value is really changing
			{
				final boolean oldDescriptionDisplayed=descriptionDisplayed;	//get the current value
				descriptionDisplayed=newDescriptionDisplayed;	//update the value
				firePropertyChange(DESCRIPTION_DISPLAYED_PROPERTY, Boolean.valueOf(oldDescriptionDisplayed), Boolean.valueOf(newDescriptionDisplayed));
			}
		}

	/**The opacity of the image in the range (0.0-1.0), with a default of 1.0.*/
	private float imageOpacity=1.0f;

		/**@return The opacity of the image in the range (0.0-1.0), with a default of 1.0.*/
		public float getImageOpacity() {return imageOpacity;}

		/**Sets the opacity of the image.
		This is a bound property of type <code>Float</code>.
		@param newImageOpacity The new opacity of the image in the range (0.0-1.0).
		@exception IllegalArgumentException if the given opacity is not within the range (0.0-1.0).
		@see #IMAGE_OPACITY_PROPERTY 
		*/
		public void setImageOpacity(final float newImageOpacity)
		{
			if(newImageOpacity<0.0f || newImageOpacity>1.0f)	//if the new opacity is out of range
			{
				throw new IllegalArgumentException("Opacity "+newImageOpacity+" is not within the allowed range.");
			}
			if(imageOpacity!=newImageOpacity)	//if the value is really changing
			{
				final float oldImageOpacity=imageOpacity;	//get the old value
				imageOpacity=newImageOpacity;	//actually change the value
				firePropertyChange(IMAGE_OPACITY_PROPERTY, new Float(oldImageOpacity), new Float(newImageOpacity));	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public Picture()
	{
		this(new DefaultLabelModel(), new DefaultImageModel());	//construct the parent class with default models
	}

	/**Label model and image model constructor.
	@param labelModel The component label model.
	@param imageModel The component image model.
	@exception NullPointerException if the given label model and/or image model is <code>null</code>.
	*/
	public Picture(final LabelModel labelModel, final ImageModel imageModel)
	{
		super(labelModel, imageModel);	//construct the parent class
		}

	/**Whether the component has image dragging enabled.*/
//TODO del if not needed	private boolean imageDragEnabled=false;

		/**@return Whether the component has image dragging enabled.*/
//TODO del if not needed		public boolean isImageDragEnabled() {return imageDragEnabled;}

		/**Sets whether the component has image dragging enabled.
		This is a bound property of type <code>Boolean</code>.
		@param newImageDragEnabled <code>true</code> if the component should allow image dragging, else false, else <code>false</code>.
		@see #IMAGE_DRAG_ENABLED_PROPERTY
		*/
/*TODO del if not needed
		public void setImageDragEnabled(final boolean newImageDragEnabled)
		{
			if(imageDragEnabled!=newImageDragEnabled)	//if the value is really changing
			{
				final boolean oldImageDragEnabled=imageDragEnabled;	//get the current value
				imageDragEnabled=newImageDragEnabled;	//update the value
				firePropertyChange(IMAGE_DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldImageDragEnabled), Boolean.valueOf(newImageDragEnabled));
			}
		}
*/

}
