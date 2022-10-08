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

package io.guise.framework.demo;

import java.security.Principal;
import java.text.Collator;

/**
 * A user class for the Guise demo.
 * @author Garret Wilson
 */
public class DemoUser implements Principal, Comparable<DemoUser> {

	/** The collator for comparing user names. */
	protected static final Collator COLLATOR = Collator.getInstance();

	/** The user ID. */
	private final String id;

	/** @return The user ID. */
	public String getID() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the user ID.
	 * </p>
	 */
	@Override
	public String getName() {
		return getID();
	}

	/** The first name of the user. */
	private final String firstName;

	/** @return The first name of the user. */
	public String getFirstName() {
		return firstName;
	}

	/** The middle name of the user, or <code>null</code> if there is no middle name. */
	private final String middleName;

	/** @return The middle name of the user, or <code>null</code> if there is no middle name. */
	public String getMiddleName() {
		return middleName;
	}

	/** The last name of the user. */
	private final String lastName;

	/** @return The last name of the user. */
	public String getLastName() {
		return lastName;
	}

	/** The password of the user. */
	private final char[] password;

	/** @return The password of the user. */
	public char[] getPassword() {
		return password;
	}

	/** The email address of the user. */
	private String email;

	/** @return The email address of the user. */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email address of the user.
	 * @param email The new email address of the user.
	 * @throws NullPointerException if the email is <code>null</code>.
	 */
	public void setEmail(final String email) {
		if(email == null) { //if the email is null
			throw new NullPointerException("Only the user middle name is optional");
		}
		this.email = email; //save the email
	}

	/** Whether the user is authorized. */
	private boolean authorized = true;

	/** @return Whether the user is authorized. */
	public boolean isAuthorized() {
		return authorized;
	}

	/**
	 * Sets whether the user is authorized.
	 * @param authorized Whether the user is authorized.
	 */
	public void setAuthorized(final boolean authorized) {
		this.authorized = authorized;
	}

	/**
	 * Constructor.
	 * @param id The user ID.
	 * @param firstName The first name of the user.
	 * @param middleName The middle name of the user, or <code>null</code> if there is no middle name.
	 * @param lastName The last name of the user.
	 * @param password The password of the user.
	 * @param email The email address of the user.
	 * @throws NullPointerException if the ID, first name, last name, and/or email address is <code>null</code>.
	 */
	public DemoUser(final String id, final String firstName, final String middleName, final String lastName, final char[] password, final String email) {
		if(id == null || firstName == null || lastName == null || password == null || email == null) { //if anything besides the middle name is null
			throw new NullPointerException("Only the user middle name is optional");
		}
		this.id = id;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
	}

	@Override
	public int compareTo(final DemoUser user) {
		return COLLATOR.compare(getLastName() + getFirstName() + getMiddleName() + getID(),
				user.getLastName() + user.getFirstName() + user.getMiddleName() + user.getID()); //compare names
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return <code>true</code> if the given object is another user with the same ID.
	 * @see #getID()
	 */
	@Override
	public boolean equals(final Object object) {
		return object instanceof DemoUser && getID().equals(((DemoUser)object).getID()); //see if the other object is a user with the same ID
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the hash code of the ID.
	 * </p>
	 * 
	 * @return The hash code for this user.
	 * @see #getID()
	 */
	@Override
	public int hashCode() {
		return getID().hashCode(); //return the hash code of the ID, as the ID should be unique
	}

	/** @return A string representation of this user in the form "(<var>id</var>) <var>lastName</var>, <var>firstName</var> <var>middleName</var>" */
	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder(); //create a new string builder
		stringBuilder.append('(').append(getID()).append(')').append(' '); //(id) 
		stringBuilder.append(getLastName()).append(',').append(' ').append(getFirstName()); //lastName, firstName
		final String middleName = getMiddleName(); //get the user's middle name
		if(middleName != null) { //if there is a middle name
			stringBuilder.append(' ').append(middleName); // middle name
		}
		return stringBuilder.toString(); //return the string representation we created of the user
	}
}
