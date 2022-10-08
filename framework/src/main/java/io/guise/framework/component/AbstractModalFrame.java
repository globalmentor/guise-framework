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

package io.guise.framework.component;

import com.globalmentor.java.Objects;

import io.guise.framework.GuiseSession;

/**
 * Abstract implementation of a modal frame.
 * @param <R> The type of modal result this modal frame produces.
 * @author Garret Wilson
 */
public abstract class AbstractModalFrame<R> extends AbstractFrame implements ModalFrame<R> {

	/** The result of this frame's modal interaction, or <code>null</code> if no result is given. */
	private R result = null;

	@Override
	public R getResult() {
		return result;
	}

	@Override
	public void setResult(final R newResult) {
		if(!Objects.equals(result, newResult)) { //if the value is really changing (compare their values, rather than identity)
			final R oldResult = result; //get the old value
			result = newResult; //actually change the value
			firePropertyChange(RESULT_PROPERTY, oldResult, newResult); //indicate that the value changed
		}
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 */
	public AbstractModalFrame(final Component component) {
		super(component); //construct the parent class
	}

	/**
	 * Ends this frame's modal interaction and navigates either to the previous modal navigation or to this frame's referring URI, if any.
	 * @param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	 * @see #setResult(Object)
	 * @see GuiseSession#endModalNavigation(ModalNavigationPanel)
	 */
	public void endModal(final R result) {
		/*TODO fix
				setResult(result);	//update the result
				getSession().endModalNavigation(this);	//end modal navigation for this modal frame
		*/
	}
}
