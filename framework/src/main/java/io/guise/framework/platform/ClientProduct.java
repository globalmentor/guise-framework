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

package io.guise.framework.platform;

import java.util.Locale;

import com.globalmentor.net.MediaType;

/**
 * The identification of the client software accessing Guise on the platform.
 * @author Garret Wilson
 */
public interface ClientProduct extends Product {

	/** @return The content types accepted by the client. */
	public Iterable<MediaType> getAcceptedContentTypes();

	/**
	 * Determines if the client accepts the given content type. Wildcard content types are correctly matched.
	 * @param contentType The content type to check.
	 * @return <code>true</code> if the client accepts the given content type.
	 */
	public boolean isAcceptedContentType(final MediaType contentType);

	/**
	 * Determines if the client accepts the given content type.
	 * @param contentType The content type to check.
	 * @param matchWildcards <code>true</code> if the content type should be matched against wildcard sequences, as is normal.
	 * @return <code>true</code> if the client accepts the given content type.
	 */
	public boolean isAcceptedContentType(final MediaType contentType, final boolean matchWildcards);

	/** @return The languages accepted by the client. */
	public Iterable<Locale> getClientAcceptedLanguages();

}
