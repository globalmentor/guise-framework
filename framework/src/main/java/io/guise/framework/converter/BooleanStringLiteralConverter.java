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

/**
 * A converter that converts a <code>Boolean</code> from and to a string literal.
 * @author Garret Wilson
 * @see Boolean
 */
public class BooleanStringLiteralConverter extends AbstractStringLiteralConverter<Boolean> {

	@Override
	public Boolean convertLiteral(final String literal) throws ConversionException {
		try {
			return literal != null && literal.length() > 0 ? Boolean.valueOf(literal) : null; //if there is a literal, convert it to a Boolean			
		} catch(final NumberFormatException numberFormatException) { //if the string does not contain a valid Boolean
			throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);
		}
	}
}
