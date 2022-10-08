/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.framework.platform.web;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.globalmentor.collections.DecoratorReadWriteLockCollectionMap;
import com.globalmentor.collections.DecoratorReadWriteLockMap;
import com.globalmentor.collections.HashSetHashMap;
import com.globalmentor.collections.ReadWriteLockCollectionMap;
import com.globalmentor.collections.ReadWriteLockMap;
import com.globalmentor.model.AbstractProxyHashObject;
import com.globalmentor.net.URIPath;

import io.clogr.Clogged;
import io.guise.framework.*;

import static com.globalmentor.net.URIs.*;
import static com.globalmentor.servlet.Servlets.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;

/**
 * A Guise container for Guise HTTP servlets. There will be one servlet Guise container per {@link ServletContext}, which usually corresponds to a single web
 * application on a JVM.
 * @author Garret Wilson
 */
public class HTTPServletGuiseContainer extends AbstractGuiseContainer implements Clogged {

	/** The absolute path, relative to the servlet context, of the resources directory. */
	public static final String RESOURCES_DIRECTORY_PATH = WEB_INF_DIRECTORY_PATH + "guise" + PATH_SEPARATOR + "resources" + PATH_SEPARATOR; //TODO use constants; combine with other similar designations for the same path

	/** The static, synchronized map of Guise containers keyed to servlet contexts. */
	private static final Map<ServletContext, HTTPServletGuiseContainer> servletContextGuiseContainerMap = synchronizedMap(
			new HashMap<ServletContext, HTTPServletGuiseContainer>());

	/**
	 * Retrieves the Guise container associated with the given servlet context. Because the Java Servlet architecture does not provide the context path to the
	 * servlet context, this method can only be called after the first request, which will provide the context path. If no Guise container is associated with the
	 * servlet context, one is created.
	 * @param servletContext The servlet context with which this container is associated.
	 * @param baseURI The base URI of the container, an absolute URI that ends with the base path, which ends with a slash ('/'), indicating the base path of the
	 *          application base paths.
	 * @return The Guise container associated with the given servlet context.
	 * @throws NullPointerException if the servlet context and/or base URI is <code>null</code>.
	 * @throws IllegalArgumentException if the base URI is not absolute or does not end with a slash ('/') character.
	 */
	public static HTTPServletGuiseContainer getGuiseContainer(final ServletContext servletContext, final URI baseURI) {
		synchronized(servletContextGuiseContainerMap) { //don't allow the map to be used while we do the lookup
			HTTPServletGuiseContainer guiseContainer = servletContextGuiseContainerMap.get(servletContext); //get the Guise container for this servlet context
			if(guiseContainer == null) { //if there is no Guise container
				guiseContainer = new HTTPServletGuiseContainer(baseURI, servletContext); //create a new Guise container for this servlet context, specifying the base URI
				servletContextGuiseContainerMap.put(servletContext, guiseContainer); //associate the Guise container with the servlet context
			}
			return guiseContainer; //return the Guise container
		}
	}

	/** The servlet context with which this container is associated. */
	private final ServletContext servletContext;

	/** @return The servlet context with which this container is associated. */
	protected final ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * Servlet contains and container base URI constructor.
	 * @param baseURI The base URI of the container, an absolute URI that ends with the base path, which ends with a slash ('/'), indicating the base path of the
	 *          application base paths.
	 * @param servletContext The servlet context with which this container is associated.
	 * @throws NullPointerException if the base URI and/or servlet context is <code>null</code>.
	 * @throws IllegalArgumentException if the base URI is not absolute or does not end with a slash ('/') character.
	 */
	public HTTPServletGuiseContainer(final URI baseURI, final ServletContext servletContext) {
		super(baseURI); //construct the parent class
		this.servletContext = requireNonNull(servletContext, "Servlet context cannot be null.");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to expose the method to the servlet.
	 * </p>
	 */
	@Override
	protected void installApplication(final AbstractGuiseApplication application, final URI baseURI, final File homeDirectory, final File logDirectory,
			final File tempDirectory) throws IOException {
		super.installApplication(application, baseURI, homeDirectory, logDirectory, tempDirectory); //delegate to the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to expose the method to the servlet.
	 * </p>
	 */
	@Override
	protected void uninstallApplication(final AbstractGuiseApplication application) {
		super.uninstallApplication(application); //delegate to the parent class
	}

	/**
	 * The read/write lock map of Guise sessions keyed to Guise applications and HTTP sessions (as a single HTTP session may be used across different Guise
	 * applications within one container).
	 */
	private final ReadWriteLockMap<GuiseApplicationHTTPSessionKey, GuiseSession> httpSessionGuiseApplicationGuiseSessionMap = new DecoratorReadWriteLockMap<GuiseApplicationHTTPSessionKey, GuiseSession>(
			new HashMap<GuiseApplicationHTTPSessionKey, GuiseSession>());

	/**
	 * The read/write lock set map of Guise sessions associated with a single HTTP session (as there may be several Guise sessions in this container using the
	 * same HTTP session. This set map is only accessed when a Guise session is being added or removed, so its locks should not slow down normal HTTP requests.
	 * The sets within the map are not thread-safe, so all access to iterables of values should be performed after retrieving a read lock.
	 */
	private final ReadWriteLockCollectionMap<HttpSession, GuiseSession, Set<GuiseSession>> httpSessionGuiseSessionSetMap = new DecoratorReadWriteLockCollectionMap<HttpSession, GuiseSession, Set<GuiseSession>>(
			new HashSetHashMap<HttpSession, GuiseSession>());

	/**
	 * Retrieves a Guise session for the given HTTP session. A Guise session will be created if none is currently associated with the given HTTP session. When a
	 * Guise session is first created, its locale will be updated to match the language, if any, accepted by the HTTP request. This method should only be called
	 * by HTTP Guise session manager.
	 * @param guiseApplication The Guise application that will own the Guise session.
	 * @param httpRequest The HTTP request with which the Guise session is associated.
	 * @param httpSession The HTTP session for which a Guise session should be retrieved.
	 * @return The Guise session associated with the provided HTTP session.
	 * @see HTTPServletGuiseSessionManager
	 */
	protected GuiseSession getGuiseSession(final GuiseApplication guiseApplication, final HttpServletRequest httpRequest, final HttpSession httpSession) {
		final GuiseApplicationHTTPSessionKey sessionKey = new GuiseApplicationHTTPSessionKey(guiseApplication, httpSession); //create a key for looking up a Guise session based upon the Guise application and the HTTP session
		final boolean isNewGuiseSession; //we'll determine whether we're creating a new Guise session
		GuiseSession guiseSession = httpSessionGuiseApplicationGuiseSessionMap.get(sessionKey); //get the Guise session associated with the Guise application and HTTP session, if there is one
		if(guiseSession == null) { //if there is no such Guise session
			httpSessionGuiseApplicationGuiseSessionMap.writeLock().lock(); //get a write lock to the map
			try {
				guiseSession = httpSessionGuiseApplicationGuiseSessionMap.get(sessionKey); //try to get the Guise session again, just in case one has just been created for this application and HTTP session
				isNewGuiseSession = guiseSession == null; //if there is still no Guise session associated with the given HTTP session and Guise application, we'll create a Guise session
				if(isNewGuiseSession) { //if we should create a new Guise session
					guiseSession = guiseApplication.createSession(new HTTPServletWebPlatform(guiseApplication, httpSession, httpRequest)); //ask the application to create a new Guise session for the given platform
					getLogger().info("Adding Guise session {} associated with HTTP sesssion {}", guiseSession, httpSession.getId());
					addGuiseSession(guiseSession); //add and initialize the Guise session
					final Locale[] clientAcceptedLanguages = getAcceptedLanguages(httpRequest); //get all languages accepted by the client
					guiseSession.requestLocale(asList(clientAcceptedLanguages)); //ask the Guise session to change to one of the accepted locales, if the application supports one
					httpSessionGuiseApplicationGuiseSessionMap.put(sessionKey, guiseSession); //associate the Guise session with the Guise application and HTTP session
				}
			} finally {
				httpSessionGuiseApplicationGuiseSessionMap.writeLock().unlock(); //always release the write lock
			}
		} else { //if we already have a Guise session
			isNewGuiseSession = false; //indicate that we didn't create a new Guise session
		}
		if(isNewGuiseSession) { //if we created a new Guise session, associate the Guise application with the new Guise session so that when the HTTP session expires we'll know which Guise sessions went with it (it is important to do this outside the synchronized block, because there are nested synchronizations when the HTTP session expires, but that won't happen until later so there's no need to synchronize now)		
			httpSessionGuiseSessionSetMap.addItem(httpSession, guiseSession); //indicate that this Guise session is for this HTTP session
		}
		final URI requestDepictionURI = URI.create(httpRequest.getRequestURL().toString()); //get the depiction URI of the current request
		guiseSession.setDepictionRootURI(getPlainURI(resolve(requestDepictionURI, ROOT_PATH))); //update the depiction plain root URI to the root of the URL specified by the request, in case the session is created from a different URL
		return guiseSession; //return the Guise session
	}

	/*TODO bring back logging after testing log out-of-memory error
					try
					{
						final File baseLogDirectory=GuiseHTTPServlet.getLogDirectory(getServletContext());	//get the base log directory
						final File logDirectory=new File(baseLogDirectory, relativeApplicationPath);	//get a subdirectory if needed; the File class allows a relative application path of ""
						ensureDirectoryExists(logDirectory);	//make sure the log directory exists
						final DateFormat logFilenameDateFormat=new W3CDateFormat(W3CDateFormat.Style.DATE);	//create a formatter for the log filename
						final String logFilename=logFilenameDateFormat.format(new Date())+" session "+httpSession.getId()+".csv";	//create a filename in the form "date session sessionID.log" TODO use a constant
						final File logFile=new File(logDirectory, logFilename);	//create a log file object
						final boolean isNewLogFile=!logFile.exists();	//see if we're creating a new log file
						final Writer logWriter=new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(logFile, true)), UTF_8);	//create a buffered UTF-8 log writer, appending if the file already exists
						if(isNewLogFile) {	//if this is a new log file
							Log.logHeaders(logWriter);	//log the headers to the log writer
						}
						guiseSession.setLogWriter(logWriter);	//tell the session to use the new log writer
						final Map<String, Object> logParameters=new HashMap<String, Object>();	//create a map for our log parameters
						logParameters.put("id", httpSession.getId());	//session ID
						logParameters.put("creationTime", new Date(httpSession.getCreationTime()));	//session creation time
						logParameters.put("isNew", Boolean.valueOf(httpSession.isNew()));	//session is new
						logParameters.put("lastAccessedTime", new Date(httpSession.getLastAccessedTime()));	//session last accessed time
						logParameters.put("maxInactiveInterval", httpSession.getMaxInactiveInterval());	//session max inactive interval
						Log.log(logWriter, InformationLevel.LOG, null, null, "guise-session-create", null, logParameters, null);	//TODO improve; use a constant
						logWriter.flush();	//flush the log information
					}
					catch(final UnsupportedEncodingException unsupportedEncodingException) {	//we should always support UTF-8
						throw new AssertionError(unsupportedEncodingException);					
					}
					catch(final IOException ioException) {	//we have a problem if we can't create a file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
						throw new AssertionError(ioException);
					}
	*/
	/*TODO del if can't be salvaged for Firefox
						//The "Accept: application/x-shockwave-flash" header is only sent in the first request from IE.
						//Unfortunately Firefox 1.5.0.3 doesn't send it at all.
						//see http://www.sitepoint.com/article/techniques-unearthed
						//see http://www.adobe.com/support/flash/releasenotes/player/rn_6.html
					environment.setProperty(CONTENT_APPLICATION_SHOCKWAVE_FLASH_ACCEPTED_PROPERTY,	//content.application.shockwave.flash.accepted
							Boolean.valueOf(isAcceptedContentType(httpRequest, APPLICATION_SHOCKWAVE_FLASH_CONTENT_TYPE, false)));	//see if Flash is installed
	*/
	/*TODO bring back logging after testing log out-of-memory error
						//log the environment variables
					try
					{
						Log.log(guiseSession.getLogWriter(), InformationLevel.LOG, null, "guise-session-environment", null, null, environment.getProperties(), null);	//TODO improve; use a constant
						guiseSession.getLogWriter().flush();	//flush the log information
					}
					catch(final IOException ioException) {	//we have a problem if we can't write to the file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
						throw new AssertionError(ioException);
					}
	*/

	/**
	 * Removes the Guise sessions for the given HTTP session. This method can only be accessed by classes in the same package. This method should only be called
	 * by HTTP Guise session manager.
	 * @param httpSession The HTTP session which should be removed along with its corresponding Guise session.
	 * @return The set of Guise sessions previously associated with the HTTP session.
	 * @see HTTPServletGuiseSessionManager
	 */
	protected Set<GuiseSession> removeGuiseSessions(final HttpSession httpSession) {
		final Set<GuiseSession> guiseSessions; //we'll find all Guise sessions associated with the HTTP session
		synchronized(httpSessionGuiseApplicationGuiseSessionMap) { //because it might be possible for a new HTTP request to revive the HTTP session and repopulate the HTTP session/Guise application->Guise session map, lock access to the map until we remove all the relevant entries
			guiseSessions = httpSessionGuiseSessionSetMap.remove(httpSession); //in one synchronized motion, remove and retrieve any Guise sessions associated with this HTTP session
			if(guiseSessions == null) { //if there is no set of Guise sessions, there's nothing for us to do here
				return emptySet(); //indicate that there were no Guise sessions
			}
			for(final GuiseSession guiseSession : guiseSessions) { //look at all Guise sessions associated with this HTTP session
				final GuiseApplicationHTTPSessionKey sessionKey = new GuiseApplicationHTTPSessionKey(guiseSession.getApplication(), httpSession); //create a key for looking up a Guise session based upon the Guise application and the HTTP session
				httpSessionGuiseApplicationGuiseSessionMap.remove(sessionKey); //remove the HTTP session and Guise session association
			}
		}
		for(final GuiseSession guiseSession : guiseSessions) { //now that we've updated the relevant maps related to the HTTP session, we can uninitialize the Guise sessions at our leisure without blocking new HTTP requests
			getLogger().info("Removing Guise session {} associated with HTTP sesssion {}", guiseSession, httpSession.getId());
			removeGuiseSession(guiseSession); //remove the Guise session
		}
		return guiseSessions; //return the Guise sessions
	}

	/*TODO bring back logging after testing log out-of-memory error
	try
	{
			//TODO refactor and consolidate; prevent code duplication
		final Map<String, Object> logParameters=new HashMap<String, Object>();	//create a map for our log parameters
		logParameters.put("id", httpSession.getId());	//session ID
		logParameters.put("creationTime", new Date(httpSession.getCreationTime()));	//session creation time
		logParameters.put("isNew", Boolean.valueOf(httpSession.isNew()));	//session is new
		logParameters.put("lastAccessedTime", new Date(httpSession.getLastAccessedTime()));	//session last accessed time
		logParameters.put("maxInactiveInterval", httpSession.getMaxInactiveInterval());	//session max inactive interval
		try
		{
			Log.log(guiseSession.getLogWriter(), InformationLevel.LOG, null, null, "guise-session-destroy", null, logParameters, null);	//TODO improve; use a constant
			guiseSession.getLogWriter().flush();	//flush the log information
		}
		catch(final IOException ioException) {	//we have a problem if we can't write to the file TODO improve; we would go on now, but we would have a problem when we try to close the standard error stream; what needs to be done is to create a better default stream, and then recover from this error
			throw new AssertionError(ioException);
		}
	}
	finally
	{
		try
		{
			guiseSession.getLogWriter().close();	//always close the log writer
		}
		catch(final IOException ioException) {	//if there is an I/O error
			Log.error(ioException);	//log the error
		}
	}
	*/

	@Override
	protected boolean hasResource(final String resourcePath) {
		try {
			return getServletContext().getResource(getContextAbsoluteResourcePath(resourcePath)) != null; //determine whether we can get a URL to that resource
		} catch(final MalformedURLException malformedURLException) { //if the path is malformed
			throw new IllegalArgumentException(malformedURLException);
		}
	}

	@Override
	protected InputStream getResourceInputStream(final String resourcePath) {
		return getServletContext().getResourceAsStream(getContextAbsoluteResourcePath(resourcePath)); //try to get an input stream to the resource
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version loads local resources directly through the servlet context.
	 * </p>
	 */
	@Override
	public InputStream getInputStream(final URI uri) throws IOException {
		//TODO del Log.trace("getting container input stream to URI", uri);
		final URI baseURI = getBaseURI(); //get the base URI
		//	TODO del Log.trace("base URI:", baseURI);
		final URI absoluteResolvedURI = resolve(baseURI, uri); //resolve the URI against the container base URI
		//	TODO del Log.trace("resolved URI:", absoluteResolvedURI);
		final URI relativeURI = baseURI.relativize(absoluteResolvedURI); //see if the absolute URI is in the application public path
		//	TODO del Log.trace("relative URI:", relativeURI);		

		//TODO allow multiple base URIs; as a short-term fix, check HTTP and HTTPS variations 

		if(!relativeURI.isAbsolute()) { //if the URI is relative to the container base URI, we can load it directly
			//TODO del Log.trace("Loading directly from servlet: "+ROOT_PATH+relativeURI);
			return getServletContext().getResourceAsStream(ROOT_PATH + relativeURI.toString()); //get an input stream through the servlet context (which may be null if there is no such resource)
		}
		return super.getInputStream(uri); //if we can't get the resource locally, delegate to the super class
	}

	/**
	 * Determines the servlet context-relative absolute path of the given container-relative path. The provided path is first normalized.
	 * @param containerRelativeResourcePath A container-relative path to a resource in the resource storage area.
	 * @return The absolute path to the resource relative to the servlet context.
	 * @throws IllegalArgumentException if the given resource path is absolute.
	 */
	protected String getContextAbsoluteResourcePath(final String containerRelativeResourcePath) {
		final String normalizedPath = normalizePath(containerRelativeResourcePath); //normalize the path
		if(isPathAbsolute(normalizedPath)) { //if the given path is absolute
			throw new IllegalArgumentException("Resource path " + normalizedPath + " is not a relative path.");
		}
		return RESOURCES_DIRECTORY_PATH + normalizedPath; //construct the absolute context-relative path to the resource
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow package access.
	 * </p>
	 */
	@Override
	protected Principal getPrincipal(final AbstractGuiseApplication application, final String id) {
		return super.getPrincipal(application, id); //delegate to the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow package access.
	 * </p>
	 */
	@Override
	protected char[] getPassword(final AbstractGuiseApplication application, final Principal principal) {
		return super.getPassword(application, principal); //delegate to the parent class			
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow package access.
	 * </p>
	 */
	@Override
	protected String getRealm(final AbstractGuiseApplication application, final URI resourceURI) {
		return super.getRealm(application, resourceURI); //delegate to the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow package access.
	 * </p>
	 */
	@Override
	protected boolean isAuthorized(final AbstractGuiseApplication application, final URI resourceURI, final Principal principal, final String realm) {
		return super.isAuthorized(application, resourceURI, principal, realm); //delegate to the parent class
	}

	/**
	 * A key suitable for a hash map made up of a Guise application and an HTTP session.
	 * @author Garret Wilson
	 */
	protected static class GuiseApplicationHTTPSessionKey extends AbstractProxyHashObject {

		/**
		 * Guise application and HTTP session constructor.
		 * @param guiseApplication The Guise application providing part of the key.
		 * @param httpSession The HTTP session providing part of the key.
		 */
		public GuiseApplicationHTTPSessionKey(final GuiseApplication guiseApplication, final HttpSession httpSession) {
			super(guiseApplication, httpSession); //construct the parent class
		}
	}
}
