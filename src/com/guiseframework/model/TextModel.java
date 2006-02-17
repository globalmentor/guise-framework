package com.guiseframework.model;

import javax.mail.internet.ContentType;

import static com.garretwilson.lang.ClassUtilities.*;

/**A model for text and an associated label.
This model only supports text content types, including:
<ul>
	<li><code>text/*</code></li>
	<li><code>application/xml</code></li>
	<li><code>application/*+xml</code></li>
</ul>
<p>The model defaults to a content type of <code>text/plain</code>.</p>
@author Garret Wilson
*/
public interface TextModel extends Model
{
	/**The text content type bound property.*/
	public final static String TEXT_CONTENT_TYPE_PROPERTY=getPropertyName(TextModel.class, "contentType");
	/**The text bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(TextModel.class, "text");
	/**The text resource key bound property.*/
	public final static String TEXT_RESOURCE_KEY_PROPERTY=getPropertyName(TextModel.class, "textResourceKey");

	/**@return The text, or <code>null</code> if there is no text.*/
	public String getText();

	/**Sets the text.
	This is a bound property.
	@param newText The new text.
	@see #TEXT_PROPERTY
	*/
	public void setText(final String newText);

	/**@return The content type of the text.*/
	public ContentType getTextContentType();

	/**Sets the content type of the text.
	This is a bound property.
	@param newTextContentType The new text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #TEXT_CONTENT_TYPE_PROPERTY
	*/
	public void setTextContentType(final ContentType newTextContentType);

	/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
	public String getTextResourceKey();

	/**Sets the key identifying the text in the resources.
	This is a bound property.
	@param newTextResourceKey The new text resource key.
	@see #TEXT_RESOURCE_KEY_PROPERTY
	*/
	public void setTextResourceKey(final String newTextResourceKey);

}
