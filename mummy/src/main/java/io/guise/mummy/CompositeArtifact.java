/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * An artifact that is composed of several other artifacts.
 * @apiNote This artifact is appropriate for traversing an entire tree of artifacts without regard for navigability.
 * @author Garret Wilson
 */
public interface CompositeArtifact extends Artifact {

	/**
	 * Returns all the artifacts of which this artifact is composed.
	 * @return The artifacts comprised by this artifact.
	 */
	public Stream<Artifact> comprisedArtifacts();

	/**
	 * Returns any comprised artifacts that have been subsumed into this one and should not be visible as separate references.
	 * @apiNote the subsumed artifacts returned here will be a subset of those returned from {@link #comprisedArtifacts()}.
	 * @return The subsumed artifacts, if any, of this artifact.
	 */
	public Collection<Artifact> getSubsumedArtifacts();

}
