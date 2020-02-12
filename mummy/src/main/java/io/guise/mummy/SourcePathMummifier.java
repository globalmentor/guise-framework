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

import static com.globalmentor.io.Paths.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.net.ContentType;

/**
 * Mummifier for processing resources with a source path.
 * <p>
 * During mummification, it is important to determine references such as relative links in relation to the <dfn>context artifact</dfn>. An artifact such as
 * <code>/foo/bar/index.html</code> may merely be the implementation for storing the content of the <code>/foo/bar/</code>, and other resources will refer to
 * <code>/foo/bar/</code>, not <code>/foo/bar/index.html</code>. In that case <code>/foo/bar/</code> will be the context artifact, and it should be used when
 * requesting e.g. child or sibling artifacts from the mummy context.
 * </p>
 * @author Garret Wilson
 */
public interface SourcePathMummifier extends Mummifier {

	/**
	 * Retrieves the extensions of files supported by this mummifier.
	 * <p>
	 * An extension may be a simple extension such as <code>foo</code>, or a compound extension such as <code>foo.bar</code>, the latter of which would match a
	 * file ending in <code>.foo.bar</code>, such as <code>example.foo.bar</code>.
	 * </p>
	 * @return The extensions of filenames for file types supported by this mummifier.
	 */
	public @Nonnull Set<String> getSupportedFilenameExtensions();

	/**
	 * Determines the output path for an artifact in the site target directory based upon the source path in the site source directory.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @return The path in the site target directory to which the given source path should be generated.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @see MummyContext#getSiteTargetDirectory()
	 * @see MummyContext#getTargetPath(Path)
	 */
	public Path getArtifactTargetPath(@Nonnull MummyContext context, @Nonnull Path sourcePath);

	/**
	 * Determines the output path for an artifact description in the site description target directory based upon the source path in the site source directory.
	 * @implSpec The default implementation first determines the target path by delegating to {@link #getArtifactTargetPath(MummyContext, Path)}, and then
	 *           produces a filename based upon the target path filename, but in the {@link MummyContext#getSiteDescriptionTargetDirectory()} directory.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @return The path in the site description target directory to which a description may be generated.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @see MummyContext#getSiteDescriptionTargetDirectory()
	 */
	public default Path getArtifactDescriptionPath(@Nonnull MummyContext context, @Nonnull Path sourcePath) {
		final Path targetPath = getArtifactTargetPath(context, sourcePath);
		return addExtension(changeBase(targetPath, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory()), "@.turf"); //TODO use constant
	}

	/**
	 * Determines the media type for an artifact from the given source path
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory; not guaranteed to exist;
	 * @return The target media type for the generated artifact, if known
	 * @throws IOException if there is an I/O error determining the media type.
	 */
	public Optional<ContentType> getArtifactMediaType(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException;

	/**
	 * Plans mummification of a source path supported by this mummifier.
	 * @param context The context of static site generation.
	 * @param sourcePath The source path to be mummified.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error during planning.
	 */
	public Artifact plan(@Nonnull MummyContext context, @Nonnull Path sourcePath) throws IOException;

}
