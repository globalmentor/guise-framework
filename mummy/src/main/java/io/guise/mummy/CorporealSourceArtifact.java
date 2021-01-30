/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy;

import java.io.*;

import javax.annotation.*;

/**
 * Artifact that conceptually originates from some source that provides content (e.g. a source file as opposed to a directory in the site source tree).
 * @author Garret Wilson
 */
public interface CorporealSourceArtifact extends Artifact {

	/**
	 * Returns the size of of the source contents in bytes.
	 * @apiNote The source size may not be the same as the size of the file indicated by the source path, which may not even exist.
	 * @param context The context of static site generation.
	 * @return The size of the artifact source, in byte.
	 * @throws IOException if there is an error retrieving the source size.
	 */
	public long getSourceSize(@Nonnull MummyContext context) throws IOException;

	/**
	 * Opens an input stream to the source content of this artifact.
	 * @apiNote The input stream may not necessarily return a stream to file indicated by the source path.
	 * @param context The context of static site generation.
	 * @return An input stream to the source contents.
	 * @throws IOException if there is an error opening the source contents.
	 */
	public InputStream openSource(@Nonnull MummyContext context) throws IOException;

}
