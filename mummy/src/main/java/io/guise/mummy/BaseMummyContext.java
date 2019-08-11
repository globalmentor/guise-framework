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
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;

/**
 * Abstract base implementation of a mummification context with common default functionality.
 * @author Garret Wilson
 */
public abstract class BaseMummyContext implements MummyContext {

	/** The segment prefix that indicates a veiled resource or resource parent. */
	public static final String VEILED_PATH_SEGMENT_PREFIX = "_";

	private final GuiseProject project;

	@Override
	public GuiseProject getProject() {
		return project;
	}

	/** The shared page document builder factory. Use must be synchronized on the factory itself. */
	private final DocumentBuilderFactory pageDocumentBuilderFactory;

	/**
	 * Constructor.
	 * @param project The Guise project.
	 */
	public BaseMummyContext(@Nonnull final GuiseProject project) {
		this.project = requireNonNull(project);
		pageDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
		pageDocumentBuilderFactory.setNamespaceAware(true);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This specification currently ignores dotfiles, for example <code>.git</code> and <code>.gitignore</code>; as well as non-regular files.
	 */
	public boolean isIgnore(final Path sourcePath) {
		if(isDotfile(sourcePath)) { //ignore dotfiles
			return true;
		}
		if(!isRegularFile(sourcePath) && !isDirectory(sourcePath)) { //TODO add option to traverse symbolic links
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation considers veiled any source path the source filename of which, or the filename of any parent source directory of which
	 *           (within the site), starts with {@value #VEILED_PATH_SEGMENT_PREFIX}. For example both <code>…/_foo/bar.txt</code> and <code>…/foo/_bar.txt</code>
	 *           would be considered veiled.
	 */
	@Override
	public boolean isVeiled(Path sourcePath) {
		final Path siteSourceDirectory = getSiteSourceDirectory();
		while(!sourcePath.equals(siteSourceDirectory)) {
			if(sourcePath.getFileName().toString().startsWith(VEILED_PATH_SEGMENT_PREFIX)) {
				return true;
			}
			sourcePath = sourcePath.getParent();
			assert sourcePath != null : "Source path is expected to be inside site source directory.";
		}
		return false;
	}

	private final Map<Artifact, Artifact> parentArtifactsByArtifact = new HashMap<>();

	@Override
	public Optional<Artifact> findParentArtifact(final Artifact artifact) {
		return Optional.ofNullable(parentArtifactsByArtifact.get(requireNonNull(artifact)));
	}

	private final Map<Path, Artifact> artifactsByReferenceSourcePath = new HashMap<>();

	@Override
	public Optional<Artifact> findArtifactBySourceReference(final Path referenceSourcePath) {
		return Optional.ofNullable(artifactsByReferenceSourcePath.get(checkArgumentAbsolute(referenceSourcePath)));
	}

	/**
	 * Recursively updates the mummification plan for the given artifact. Parent artifacts are updated in the map, for example.
	 * @param artifact The artifact the plan of which to update.
	 */
	protected void updatePlan(@Nonnull final Artifact artifact) {
		requireNonNull(artifact);
		artifact.getReferentSourcePaths().forEach(referenceSourcePath -> artifactsByReferenceSourcePath.put(referenceSourcePath, artifact));
		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				parentArtifactsByArtifact.put(childArtifact, artifact); //map the parent to the child
				updatePlan(childArtifact); //recursively update the plan for the children
			}
		}
	}

	//factory methods

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation synchronizes on the internal document builder factory instance.
	 */
	@Override
	public DocumentBuilder newPageDocumentBuilder() {
		synchronized(pageDocumentBuilderFactory) {
			try {
				return pageDocumentBuilderFactory.newDocumentBuilder(); //TODO install appropriate entity resolvers as needed
			} catch(final ParserConfigurationException parserConfigurationException) {
				throw new RuntimeException(parserConfigurationException); //TODO switch to Confound configuration exception
			}
		}
	}

}
