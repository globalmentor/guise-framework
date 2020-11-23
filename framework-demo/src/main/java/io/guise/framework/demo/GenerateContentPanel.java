/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.demo;

import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.globalmentor.net.URIPath;

import io.guise.framework.component.*;
import io.guise.framework.component.layout.*;
import io.guise.framework.event.ActionEvent;
import io.guise.framework.event.ActionListener;
import io.guise.framework.model.Notification;
import io.guise.framework.validator.RegularExpressionStringValidator;

/**
 * Generate Content Guise demonstration panel. Copyright © 2007 GlobalMentor, Inc. Demonstrates generation of temporary content, navigating with a viewport ID,
 * and notifying the user of errors.
 * @author Garret Wilson
 */
public class GenerateContentPanel extends LayoutPanel {

	/** Default constructor. */
	public GenerateContentPanel() {
		super(new FlowLayout(Flow.PAGE)); //construct the parent class flowing vertically
		setLabel("Guise\u2122 Demonstration: Generate Content"); //set the panel title	

		final TextControl<String> textInput = new TextControl<String>(String.class); //create a text input control to retrieve a string
		textInput.setColumnCount(64); //allow enough room for interesting information
		textInput.setLabel("Text to generate as a text file"); //add a label to the text input control
		textInput.setInfo("Enter text that will be placed in a generated temporary text file."); //add advisory information that may be shown as a tooltip
		textInput.setValidator(new RegularExpressionStringValidator(".+", true)); //require at least a single character
		try {
			textInput.setValue("This text will appear in generated text file."); //provide example text
		} catch(final PropertyVetoException propertyVetoException) { //we should never have a problem setting this value
			throw new AssertionError(propertyVetoException);
		}
		add(textInput); //add the text input control to the panel

		final CheckControl sessionRestrictedCheckControl = new CheckControl(); //create a check control
		sessionRestrictedCheckControl.setLabel("Restrict resource access to current session."); //add a label to the check control
		try {
			sessionRestrictedCheckControl.setValue(Boolean.TRUE); //default to restricting the resource to the current session
		} catch(final PropertyVetoException propertyVetoException) { //we should never have a problem setting this value
			throw new AssertionError(propertyVetoException);
		}
		add(sessionRestrictedCheckControl); //add the session restriction check control to the panel

		final Button generateButton = new Button(); //create a new button for generating content
		generateButton.setLabel("Generate in Separate Viewport"); //add a label to the button
		generateButton.addActionListener(new ActionListener() { //listen for the button being pressed

			@Override
			public void actionPerformed(ActionEvent actionEvent) { //if the button was pressed
				final Notification notification = new Notification("ready to generate?");
				getSession().notify(new Runnable() {

					@Override
					public void run() {
						if(GenerateContentPanel.this.validate()) { //validate the form; if validation succeeds
							try {
								//see if we should restrict access to the current session
								final boolean sessionRestricted = sessionRestrictedCheckControl.getValue().booleanValue();
								//generate a temporary file, restricting access to the file to the current session if requested
								//if we knew ahead of time we would want access restriction to the current session,
								//	we could call the convenience method getSession().createTempPublicResource("generated-text", "txt")
								final URIPath tempPath = getSession().getApplication().createTempAsset("generated-text", "txt", sessionRestricted ? getSession() : null);
								//get a UTF-8 writer to the application-relative path to the temporary public resource
								try (final Writer tempWriter = new BufferedWriter(new OutputStreamWriter(getSession().getApplication().getOutputStream(tempPath), "UTF-8"))) {
									tempWriter.write(textInput.getValue()); //write the provided text to the temporary file; closing will flush the buffered contents
								}
								getSession().navigate(tempPath, "generatedContentViewport"); //navigate to the generated content in a separate viewpoert
							} catch(final IOException ioException) { //if there was an error generating the content
								getSession().notify(ioException); //inform the user of the error
							}
						}

					}
				}, notification);

			}

		});
		add(generateButton); //add the generate button to the panel
	}

}
