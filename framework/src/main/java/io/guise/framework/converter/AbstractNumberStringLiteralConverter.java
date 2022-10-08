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

package io.guise.framework.converter;

import static java.text.MessageFormat.*;
import static java.util.Objects.*;

import java.text.*;
import java.util.*;

import static com.globalmentor.java.Characters.*;

/**
 * An abstract implementation an object that can convert a number from and to a string. If the currency style is chosen, care should be taken to indicate a
 * specific constant currency unless it is desired that the currency type change whenever the locale changes. This implementation caches a number format and
 * only creates a new one if the locale has changed. Child classes should override {@link #createNumberFormat(Locale)} if custom number formats are desired.
 * This implementation synchronizes all conversions on the {@link NumberFormat} object.
 * @param <V> The value type this converter supports.
 * @author Garret Wilson
 */
public abstract class AbstractNumberStringLiteralConverter<V extends Number> extends AbstractConverter<V, String> implements NumberStringLiteralConverter<V> {

	/** The representation style. */
	private final Style style;

	/** @return The representation style. */
	public Style getStyle() {
		return style;
	}

	/**
	 * The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically determined by the
	 * locale.
	 */
	private final Currency currency;

	/**
	 * @return The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically determined
	 *         by the locale.
	 */
	public Currency getCurrency() {
		return currency;
	}

	/** The lazily-created cached object for converting numbers to and from strings. */
	private NumberFormat numberFormat = null;

	/** The lazily-assigned locale for which a number format was generated. */
	private Locale locale = null;

	/** @return A number format object appropriate for the session's current locale. */
	protected synchronized NumberFormat getNumberFormat() {
		final Locale sessionLocale = getSession().getLocale(); //get the current session locale
		if(numberFormat == null || !sessionLocale.equals(locale)) { //if we haven't yet generated a number format or the locale has changed
			numberFormat = createNumberFormat(sessionLocale); //create a new number format
			locale = sessionLocale; //update the locale			
		}
		return numberFormat; //return the number format
	}

	/**
	 * Style and currency constructor.
	 * @param style The representation style.
	 * @param currency The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically
	 *          determined by the locale.
	 * @throws NullPointerException if the given style is <code>null</code>.
	 * @throws IllegalArgumentException if a currency is provided for a style other than {@link Style#CURRENCY}.
	 */
	public AbstractNumberStringLiteralConverter(final Style style, final Currency currency) {
		this.style = requireNonNull(style, "Style cannot be null"); //save the style
		if(currency != null && style != Style.CURRENCY) { //if a currency is provided for a non-currency style
			throw new IllegalArgumentException("A specific currency is only relevant for the currency representation style.");
		}
		this.currency = currency; //save the currency
	}

	/**
	 * Creates a new number format object for the indicated locale. //TODO del This implementation defaults to formatting as many fraction digits as is possible
	 * to keep the value synchronized with the lexical form.
	 * @param locale The locale for which a number format should be created.
	 * @return A number format object appropriate for the given locale.
	 * @see #getStyle()
	 */
	protected NumberFormat createNumberFormat(final Locale locale) {
		final Style style = getStyle(); //get the style
		final NumberFormat numberFormat; //we'll store here the number format
		switch(style) { //see which style to use
			case NUMBER:
				numberFormat = NumberFormat.getInstance(locale); //create a general number format for the given locale
				break;
			case CURRENCY:
				numberFormat = NumberFormat.getCurrencyInstance(locale); //create a general number format for the given locale
				{
					final Currency currency = getCurrency(); //get the designated currency
					if(currency != null) { //if a specific currency is designated
						numberFormat.setCurrency(currency); //update the number format's currency
						//update the fraction digits based upon the new currency (code modified from Sun's NumberFormat.adjustForCurrencyDefaultFractionDigits() version 1.65, 05/10/04)
						final int defaultFractionDigits = currency.getDefaultFractionDigits(); //see if this currency designates default fraction digits
						if(defaultFractionDigits >= 0) { //if default fraction digits are specified
							final int oldMinimumFactionDigits = numberFormat.getMinimumFractionDigits(); //get the old minimum fraction digits
							final int oldMaximumFactionDigits = numberFormat.getMaximumFractionDigits(); //get the old maximum fraction digits
							numberFormat.setMaximumFractionDigits(defaultFractionDigits); //update the maximum number of digits
							numberFormat.setMinimumIntegerDigits(oldMinimumFactionDigits == oldMaximumFactionDigits ? defaultFractionDigits : Math.min(defaultFractionDigits,
									oldMinimumFactionDigits)); //keep the lower minimum fraction digits, if that value was different
						}
					}
				}
				break;
			case PERCENT:
				numberFormat = NumberFormat.getPercentInstance(locale); //create a percent number format for the given locale
				break;
			case INTEGER:
				numberFormat = NumberFormat.getIntegerInstance(locale); //create an integer number format for the given locale
				break;
			default: //if we don't recognize the style
				throw new AssertionError("Unrecognized style: " + style);
		}
		//TODO fix		numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);	//format all the fraction digits so that round-trip conversion will be accurate
		return numberFormat; //return the number format we created
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation converts the value using the number format object. This implementation synchronizes on the {@link NumberFormat} instance.
	 * </p>
	 */
	@Override
	public String convertValue(final V value) throws ConversionException {
		if(value != null) { //if there is a value
			final NumberFormat numberFormat = getNumberFormat(); //get the number format to use
			synchronized(numberFormat) { //don't allow other threads to access the number format object while we're using it
				return numberFormat.format(value); //format the number
			}
		} else { //if there is no value
			return null; //there's nothing to convert
		}
	}

	/**
	 * Converts a literal representation of a value from the lexical space into a number. This implementation converts the empty string to a <code>null</code>
	 * value. This implementation adds any appropriate symbols, such as a percent sign or currency symbol, if needed.
	 * @param literal The literal value in the lexical space to convert.
	 * @return A number representing the converted value in the value space in either <code>Long</code> or <code>Double</code> form, or <code>null</code> if the
	 *         given literal is <code>null</code> or the empty string.
	 * @throws ConversionException if the literal value cannot be converted.
	 */
	protected Number parseNumber(String literal) throws ConversionException {
		if(literal != null && literal.length() > 0) { //if there is a literal value
			final NumberFormat numberFormat = getNumberFormat(); //get the number format to use
			final Style style = getStyle(); //get the style
			switch(style) { //improve the string literal for certain types
				case CURRENCY: //make sure a currency symbol is present
				{
					//TODO fix $ being used instead of USD$ in some locales; improve overall parsing
					final String currencySymbol = numberFormat.getCurrency().getSymbol(getSession().getLocale()); //get the currency symbol for the current locale
					if(!literal.contains(currencySymbol)) { //if the currency symbol is not in the string
						literal = currencySymbol + literal; //prepend the currency symbol to the literal
					}
				}
					break;
				case PERCENT:
					if(literal.indexOf(PERCENT_SIGN_CHAR) < 0) { //if there is no percent sign present
						literal += PERCENT_SIGN_CHAR; //append the percent sign to the literal
					}
					break;
			}
			final ParsePosition parsePosition = new ParsePosition(0); //create a new parse position
			final Number number; //we'll store the number here
			synchronized(numberFormat) { //don't allow other threads to access the number format object while we're using it
				number = numberFormat.parse(literal, parsePosition); //parse the value, retrieving the parse positoin
			}
			if(parsePosition.getIndex() < literal.length()) { //if the whole string wasn't parsed, we'll consider that an error (either there was an error, in which case the index is zero, or part of the string was ignored)
				throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);
			}
			return number; //return the number we parsed
		} else { //if there is no literal value
			return null; //there is nothing to convert
		}
	}

}
