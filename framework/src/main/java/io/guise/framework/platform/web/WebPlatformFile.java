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

package io.guise.framework.platform.web;

import java.net.URI;

import static java.util.Objects.*;

import com.globalmentor.model.TaskState;

import io.guise.framework.event.*;
import io.guise.framework.platform.*;

/**
 * A local file represented by a Flash <code>flash.net.FileReference</code> on the web platform. Because Flash registers progress listeners on a per-file basis,
 * this file keeps track of a single listener, available only to web classes (as other upload implementations may not register listeners for individual files).
 * @author Garret Wilson
 */
public class WebPlatformFile extends AbstractPlatformFile {

	/** The platform file collector that owns this platform file. */
	private final PlatformFileCollector platformFileCollector;

	/** @return The platform file collector that owns this platform file. */
	protected PlatformFileCollector getPlatformFileCollector() {
		return platformFileCollector;
	}

	/** The ID given to the file by Flash. */
	private final String id;

	/** @return The ID given to the file by Flash. */
	public String getID() {
		return id;
	}

	/**
	 * File reference list, name and size constructor.
	 * @param fileReferenceList The Flash file reference list that owns this platform file.
	 * @param id The ID given to the file by Flash.
	 * @param name The name of the file.
	 * @param size The size of the file, or -1 if the size is unknown.
	 * @throws NullPointerException if the given ID, file reference list, and/or name is <code>null</code>.
	 */
	public WebPlatformFile(final PlatformFileCollector fileReferenceList, final String id, final String name, final long size) {
		super(name, size); //construct the parent class
		this.id = requireNonNull(id, "ID cannot be null.");
		this.platformFileCollector = requireNonNull(fileReferenceList, "File reference list cannot be null.");
	}

	@Override
	public void upload(final URI destinationURI) {
		getPlatformFileCollector().upload(this, destinationURI); //tell the owner file reference list to upload this file
	}

	@Override
	public void cancel() {
		getPlatformFileCollector().cancel(this); //tell the owner file reference list to cancel this file transfer
	}

	@Override
	protected void fireProgressed(final TaskState state, final long transferred, final long total) {
		super.fireProgressed(state, transferred, total); //delegate to the super version
	}

}
