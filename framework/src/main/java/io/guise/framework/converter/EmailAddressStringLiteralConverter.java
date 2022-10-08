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

import com.globalmentor.net.EmailAddress;
import com.globalmentor.text.ArgumentSyntaxException;

/**
 * A converter that converts an {@link EmailAddress} from and to a string literal.
 * @author Garret Wilson
 */
public class EmailAddressStringLiteralConverter extends AbstractStringLiteralConverter<EmailAddress> {

	@Override
	public EmailAddress convertLiteral(final String literal) throws ConversionException {
		if(literal == null) { //if the literal is null
			return null; //the value is null
		} else { //if the literal is not null
			try {
				return EmailAddress.fromString(literal); //construct the email address
			} catch(final ArgumentSyntaxException argumentSyntaxException) { //if the email address is not in the correct syntax
				throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal); //indicate that the value was invalid				
			}
		}
	}
}
