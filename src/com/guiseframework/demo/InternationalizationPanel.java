package com.guiseframework.demo;

import java.util.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.converter.Converter;
import com.guiseframework.converter.DateStringLiteralConverter;
import com.guiseframework.converter.DateStringLiteralStyle;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;

/**Internationalization Guise demonstration panel.
Copyright � 2005 GlobalMentor, Inc.
Demonstrates locale label models, date label models,
	application default locale, application supported locales, menus,
	component IDs, localized resource bundle resources, and localized resource files.
@author Garret Wilson
*/
public class InternationalizationPanel extends DefaultNavigationPanel
{

	/**The key to the UN Charter Preamble resource.*/
	protected final static String UN_CHARTER_PREAMBLE_RESOURCE_KEY="uncharterpreamble.html";

	/**Guise session constructor.
	@param session The Guise session that owns this panel.
	*/
	public InternationalizationPanel(final GuiseSession session)
	{
		super(session, new RegionLayout(session));	//construct the parent class, using a region layout
		setLabel("Guise\u2122 Demonstration: Internationalization");	//set the panel title

		final Locale defaultLocale=session.getApplication().getDefaultLocale();	//get the default application locale supported by the application

		final Set<Locale> supportedLocales=session.getApplication().getSupportedLocales();	//get the locales supported by the application
			//create a mutual exclusion policy group to only allow one language to be selected at one time
		final ModelGroup<ValueModel<Boolean>> localeMutualExclusionPolicyModelGroup=new MutualExclusionPolicyModelGroup();
		final DropMenu menu=new DropMenu(session, Flow.LINE);	//create a horizontal menu

			//Language
		final DropMenu languageMenu=new DropMenu(session, "languageMenu", Flow.PAGE);	//create a menu with a custom ID
		languageMenu.setLabelResourceKey("menu.language.label");	//show which resource to use for the label
			//create check controls for each locale supported by the application (defined in the web.xml file, for example)
		for(final Locale supportedLocale:supportedLocales)	//for each supported locale
		{
			final LabelModel localeLabelModel=new LocaleLabelModel(session, supportedLocale);	//create a label model to represent the locale
				//create a check control, using the locale label model
			final CheckControl checkControl=new CheckControl(session, localeLabelModel);
			checkControl.setCheckType(CheckControl.CheckType.ELLIPSE);	//show the check as an ellipse
			if(supportedLocale.equals(defaultLocale))	//if this is the default locale
			{
				try
				{
					checkControl.setValue(Boolean.TRUE);	//select this check control
				}
				catch(final ValidationException validationException)	//there should be no problem selecting the model 
				{
					throw new AssertionError(validationException);
				}		
			}
				//install a value change listener to listen for language selection
			checkControl.addPropertyChangeListener(CheckControl.VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Boolean>()
					{
						public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//when the language check changes
						{
							if(Boolean.TRUE.equals(propertyChangeEvent.getNewValue()))	//if this language is being set
							{
								session.setLocale(supportedLocale);	//change the session locale
							}
						}
					});
			localeMutualExclusionPolicyModelGroup.add(checkControl);	//add this check control to the mutual exclusion policy group
			languageMenu.add(checkControl);	//add the check control to the language menu			
		}
		
		menu.add(languageMenu);	//add the language menu to the horizontal menu

			//Date
		final DropMenu dateMenu=new DropMenu(session, "dateMenu", Flow.PAGE);	//create a menu with a custom ID
		dateMenu.setLabelResourceKey("menu.date.label");	//show which resource to use for the label
			//Date|date
				//create a converter to convert the date to a string in long format using the current locale
		final Converter<Date, String> dateConverter=new DateStringLiteralConverter(session, DateStringLiteralStyle.LONG);
			//create a label with the current date using the converter we created to show the date in the label
		final Label dateLabel=new Label(session, "dateLabel", new ValueConverterLabelModel<Date>(session, new Date(), dateConverter));
		dateMenu.add(dateLabel);	//add the date label to the date menu
		
		menu.add(dateMenu);	//add the date menu to the horizontal menu

		add(menu, new RegionConstraints(session, Region.PAGE_START));	//add the menu at the top

			//localized text
		final Text text=new Text(session);	//create a text component
		text.setTextContentType(XHTML_CONTENT_TYPE);	//use application/xhtml+xml content
		text.setTextResourceKey(UN_CHARTER_PREAMBLE_RESOURCE_KEY);	//use the UN Charter Preamble resource, appropriately localized

		add(text, new RegionConstraints(session, Region.CENTER));	//add the text in the center of the panel
	}

}