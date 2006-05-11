package com.guiseframework;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**The interface for a Guise container.
@author Garret Wilson
*/
public interface GuiseContainer
{

	/**Reports the base URI of the container.
	The base URI is an absolute URI that ends with the base path, which ends with a slash ('/').
	@return The base URI representing the Guise container.
	*/
	public URI getBaseURI();

	/**Reports the base path of the container.
	The base path is an absolute path that ends with a slash ('/'), indicating the base path of the application base paths.
	@return The base path representing the Guise container.
	*/
	public String getBasePath();

	/**Resolves a relative or absolute path against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be be considered already resolved.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path".
	@param path The path to be resolved.
	@return The path resolved against the container base path.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #resolveURI(URI)} should be used instead).
	@see #resolveURI(URI)
	*/
	public String resolvePath(final String path);

	/**Resolves URI against the container base path.
	Relative paths will be resolved relative to the container base path. Absolute paths will be considered already resolved, as will absolute URIs.
	For a container base path "/path/to/container/", resolving "relative/path" will yield "/path/to/container/relative/path",
	while resolving "/absolute/path" will yield "/absolute/path". Resolving "http://example.com/path" will yield "http://example.com/path".
	@param uri The URI to be resolved.
	@return The uri resolved against the container base path.
	@exception NullPointerException if the given URI is <code>null</code>.
	@see GuiseContainer#getBasePath()
	*/
	public URI resolveURI(final URI uri);

	/**Retrieves an input stream to the entity at the given URI.
	The URI is first resolved to the container base URI.
	@param uri A URI to the entity; either absolute or relative to the container.
	@return An input stream to the entity at the given resource URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	@exception IOException if there was an error connecting to the entity at the given URI.
	@see #getBaseURI()
	*/
	public InputStream getInputStream(final URI uri) throws IOException;

}
