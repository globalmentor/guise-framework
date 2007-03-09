package com.guiseframework.platform.web;

import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.*;

import javax.mail.internet.ContentType;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.*;

import org.apache.commons.fileupload.*;
import org.w3c.dom.*;
import org.w3c.dom.css.CSSStyleSheet;
import org.xml.sax.SAXException;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.io.*;
import com.garretwilson.lang.ClassUtilities;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.model.DefaultResource;
import com.garretwilson.model.Resource;

import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.lang.StringBuilderUtilities.*;
import static com.garretwilson.lang.ThreadUtilities.*;

import com.garretwilson.net.URIConstants;
import com.garretwilson.net.URIUtilities;
import com.garretwilson.net.http.*;

import static com.garretwilson.net.http.HTTPConstants.*;

import static com.garretwilson.servlet.ServletConstants.*;
import static com.garretwilson.servlet.http.HttpServletConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.xml.XMLConstants.*;
import static com.garretwilson.text.xml.XMLUtilities.createDocumentBuilder;

import com.garretwilson.text.FormatUtilities;
import com.garretwilson.text.W3CDateFormat;
import com.garretwilson.text.elff.*;

import static com.garretwilson.text.elff.WebTrendsConstants.*;
import com.garretwilson.text.xml.XMLUtilities;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.rdf.RDF;
import com.garretwilson.rdf.RDFResource;
import com.garretwilson.rdf.RDFResourceIO;
import com.garretwilson.rdf.RDFUtilities;
import com.garretwilson.rdf.RDFXMLProcessor;
import com.garretwilson.rdf.maqro.Activity;
import com.garretwilson.rdf.maqro.MAQROUtilities;
import com.garretwilson.rdf.ploop.PLOOPProcessor;
import com.garretwilson.security.Nonce;
import com.garretwilson.servlet.ServletUtilities;
import com.garretwilson.servlet.http.HttpServletUtilities;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;

import com.garretwilson.text.xml.xhtml.XHTMLConstants;
import com.garretwilson.text.xml.xpath.*;
import com.garretwilson.util.*;
import com.guiseframework.*;
import com.guiseframework.Bookmark.Parameter;
import com.guiseframework.component.*;
import com.guiseframework.context.GuiseContext;
import com.guiseframework.controller.*;
import com.guiseframework.controller.text.xml.xhtml.*;
import com.guiseframework.geometry.*;
import com.guiseframework.model.FileItemResourceImport;
import com.guiseframework.platform.web.css.*;
import com.guiseframework.theme.Theme;
import com.guiseframework.view.text.xml.xhtml.XHTMLApplicationFrameView;

import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.util.LocaleUtilities.*;
import static com.garretwilson.util.UUIDUtilities.*;
import static com.guiseframework.Guise.*;
import static com.guiseframework.platform.web.WebPlatformConstants.*;

/**The servlet that controls a Guise web applications. 
Only one Guise context will be active at one one time for a single session, so any Guise contexts that are not inactive can be assured that they will be the only context accessing the data.
Navigation frame bindings for paths can be set in initialization parameters named <code>navigation.<var>frameID</var></code>, with values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public class GuiseHTTPServlet extends DefaultHTTPServlet
{
	/**The init parameter, "application", used to specify the relative path to the application description file.*/
	public final static String APPLICATION_INIT_PARAMETER="application";

	/**The content type of a Guise AJAX request, <code>application/x-guise-ajax-request</code>.*/
	public final static ContentType GUISE_AJAX_REQUEST_CONTENT_TYPE=new ContentType(ContentTypeConstants.APPLICATION, ContentTypeConstants.EXTENSION_PREFIX+"guise-ajax-request"+ContentTypeConstants.SUBTYPE_SUFFIX_DELIMITER_CHAR+ContentTypeConstants.XML_SUBTYPE_SUFFIX, null);

	/**The content type of a Guise AJAX response, <code>application/x-guise-ajax-response</code>.*/
	public final static ContentType GUISE_AJAX_RESPONSE_CONTENT_TYPE=new ContentType(ContentTypeConstants.APPLICATION, ContentTypeConstants.EXTENSION_PREFIX+"guise-ajax-response"+ContentTypeConstants.SUBTYPE_SUFFIX_DELIMITER_CHAR+ContentTypeConstants.XML_SUBTYPE_SUFFIX, null);

	/**The Guise container that owns the applications.*/
	private HTTPServletGuiseContainer guiseContainer=null;

		/**Returns the Guise container.
		This method must not be called before a request is processed.
		@return The Guise container that owns the applications.
		@exception IllegalStateException if this method is called before any requests have been processed.
		*/
		protected HTTPServletGuiseContainer getGuiseContainer()
		{
			if(guiseContainer==null)	//if no container exists
			{
				throw new IllegalStateException("Cannot access Guise container before first HTTP request, due to the Java Servlet architecture.");
			}
			return guiseContainer;	//return the Guise container
		}

	/**The Guise application controlled by this servlet.*/
	private AbstractGuiseApplication guiseApplication;

		/**@return The Guise application controlled by this servlet.*/
		protected AbstractGuiseApplication getGuiseApplication() {return guiseApplication;}

	/**The ELFF logger for this Guise application.*/
	private final ELFF elff;

		/**@return The ELFF logger for this application.*/
		public ELFF getELFF() {return elff;}

	/**The initializer for initializing ELFF writers.
	This implementation writes the default directives along with the <code>Start-Date</code> directive.
	*/
	protected final IOOperation<Writer> elffWriterInitializer=new IOOperation<Writer>()
	{

		/**Performs an operation on the indicated object.
		This implementation writes ELFF directives to the ELFF writer along with the <code>Start-Date</code> directive.
		@param writer The ELFF writer to be initialized.
		@throws IOException if there is an error during the operation.
		@see ELFF#START_DATE_DIRECTIVE
		*/
		@SuppressWarnings("unchecked")	//we use a generic NameValuePair as a vararg
		public void perform(final Writer writer) throws IOException
		{
			writer.write(getELFF().serializeDirectives());	//write the directives to the ELFF writer
			writer.write(getELFF().serializeDirective(ELFF.START_DATE_DIRECTIVE, ELFF.createDateTimeFormat().format(new Date())));	//add the Start-Date directive with the current time
			writer.flush();	//flush the directives to the writer
		}
	};

	/**The uninitializer for uninitializing ELFF writers.
	This implementation writes the <code>End-Date</code> directive.
	*/
	protected final IOOperation<Writer> elffWriterUninitializer=new IOOperation<Writer>()
	{

		/**Performs an operation on the indicated object.
		This implementation writes the <code>End-Date</code> directive to the ELFF writer.
		@param writer The ELFF writer to be uninitialized.
		@throws IOException if there is an error during the operation.
		@see ELFF#END_DATE_DIRECTIVE
		*/
		public void perform(final Writer writer) throws IOException
		{
			writer.write(getELFF().serializeDirective(ELFF.END_DATE_DIRECTIVE, ELFF.createDateTimeFormat().format(new Date())));	//add the End-Date directive with the current time
			writer.flush();	//flush the directive to the writer
		}
	};

	/**Default constructor.
	Creates a single Guise application.
	*/
	public GuiseHTTPServlet()
	{
		elff=new ELFF(	//create an ELFF log
				Field.DATE_FIELD, Field.TIME_FIELD, Field.CLIENT_IP_FIELD, Field.CLIENT_SERVER_USERNAME_FIELD, Field.CLIENT_SERVER_HOST_FIELD,
				Field.CLIENT_SERVER_METHOD_FIELD, Field.CLIENT_SERVER_URI_STEM_FIELD, Field.CLIENT_SERVER_URI_QUERY_FIELD,
				Field.SERVER_CLIENT_STATUS_FIELD, Field.CLIENT_SERVER_BYTES_FIELD, Field.CLIENT_SERVER_VERSION_FIELD,
				Field.CLIENT_SERVER_USER_AGENT_HEADER_FIELD, Field.CLIENT_SERVER_COOKIE_HEADER_FIELD,
				Field.CLIENT_SERVER_REFERER_HEADER_FIELD, Field.DCS_ID_FIELD);
		elff.setDirective(ELFF.SOFTWARE_DIRECTIVE, Guise.GUISE_NAME+' '+Guise.BUILD_ID);	//set the software directive of the ELFF log
	}
		
	/**Initializes the servlet.
	@param servletConfig The servlet configuration.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		Debug.log("initializing servlet", servletConfig.getServletName(), Guise.GUISE_NAME, Guise.BUILD_ID);
		setReadOnly(true);	//make this servlet read-only
		//TODO turn off directory listings, and/or fix them
		try
		{
			guiseApplication=initGuiseApplication(servletConfig);	//initialize the application and frame bindings
		}
		catch(final Exception exception)	//if there is any problem initializing the Guise application
		{
			throw new ServletException("Error initializing Guise application: "+exception.getMessage(), exception);
		}
	}

	/**Initializes bindings between paths and associated navigation frame classes.
	This implementation reads the initialization parameters named <code>navigation.<var>frameID</var></code>, expecting values in the form <code><var>contextRelativePath</var>?class=<var>com.example.Frame</var></code>.
	@param servletConfig The servlet configuration.
	@return The new initialized application. 
	@exception IllegalArgumentException if the one of the frame bindings is not expressed in correct format.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception IllegalArgumentException if the one of the classes specified as a frame binding could not be found.
	@exception ClassCastException if the one of the classes specified as a frame binding does not represent a subclass of a frame component.
	@exception ServletException if there is a problem initializing the application or frame bindings.
	@see Frame
	*/
	protected AbstractGuiseApplication initGuiseApplication(final ServletConfig servletConfig) throws ServletException
	{
		final AbstractGuiseApplication guiseApplication;	//create the application and store it here
		final String guiseApplicationDescriptionPath=servletConfig.getInitParameter(APPLICATION_INIT_PARAMETER);	//get name of the guise application description file
		if(guiseApplicationDescriptionPath!=null)	//if there is a Guise application description file specified
		{
//TODO del Debug.trace("found path to application description:", guiseApplicationDescriptionPath);
			final String normalizedGuiseApplicationDescriptionPath=normalizePath(guiseApplicationDescriptionPath);	//normalize the path
			if(isAbsolutePath(normalizedGuiseApplicationDescriptionPath))	//if the given path is absolute
			{
				throw new ServletException("Guise application path "+normalizedGuiseApplicationDescriptionPath+" is not a relative path.");
			}
			final String absoluteGuiseApplicationDescriptionPath=WEB_INF_DIRECTORY_PATH+normalizedGuiseApplicationDescriptionPath;	//determine the context-relative absolute path of the description file
//		TODO del Debug.trace("determined absolute path to application description:", absoluteGuiseApplicationDescriptionPath);
			try
			{
				final ServletContext servletContext=servletConfig.getServletContext();	//get the servlet context
				final URL guiseApplicationDescriptionURL=servletContext.getResource(absoluteGuiseApplicationDescriptionPath);	//get the URL to the application description
//			TODO del Debug.trace("found URL to application description", guiseApplicationDescriptionURL);
				if(guiseApplicationDescriptionURL==null)	//if we can't find the resource
				{
					throw new ServletException("Missing Guise application resource description at "+absoluteGuiseApplicationDescriptionPath);
				}
				final InputStream guiseApplicationDescriptionInputStream=servletContext.getResourceAsStream(absoluteGuiseApplicationDescriptionPath);	////get an input stream to the application description
				assert guiseApplicationDescriptionInputStream!=null : "Could not get an input stream to Guise application description path "+absoluteGuiseApplicationDescriptionPath+" even though earlier retrieval of URL succeeded.";
				final InputStream guiseApplicationDescriptionBufferedInputStream=new BufferedInputStream(guiseApplicationDescriptionInputStream);	//get a buffered input stream to the application description 
				try
				{
					
						//TODO change to use new PLOOPResourceIO
					final DocumentBuilder documentBuilder=createDocumentBuilder(true);	//create a new namespace-aware document builder
					final Document document=documentBuilder.parse(guiseApplicationDescriptionBufferedInputStream);	//parse the description document
//				TODO del Debug.trace("application description:", XMLUtilities.toString(document));
					final RDFXMLProcessor rdfProcessor=new RDFXMLProcessor();	//create a new RDF processor
					final RDF rdf=rdfProcessor.processRDF(document, guiseApplicationDescriptionURL.toURI());	//process the RDF from the XML, using the URI o the application description as the base URI
					final PLOOPProcessor ploopProcessor=new PLOOPProcessor();	//create a new PLOOP processor
					guiseApplication=ploopProcessor.getObject(rdf, AbstractGuiseApplication.class);	//create and retrieve the Guise application from the RDF instance
					if(guiseApplication==null)	//if there is no Guise application described
					{
						throw new ServletException("Guise application description document did not describe a Guise application.");
					}
/*TODO del					
Debug.trace("checking for categories");
					for(final Destination destination:guiseApplication.getDestinations())	//for each destination
					{
						Debug.trace("looking at destination", destination.getPath());
						for(final Category category:destination.getCategories())
						{
							Debug.trace("destination has category", category.getID());
							for(final Category subcategory:category.getCategories())
							{
								Debug.trace("category has subcategory", subcategory.getID());
							}
						}
					}
*/
				}
/*TODO del
				catch(Exception exception)
				{
					Debug.error(exception);
					throw new ServletException(exception);
				}
*/
				catch(final ParserConfigurationException parserConfigurationException)	//if we can't find an XML parser
				{
					throw new ServletException(parserConfigurationException);
				}	
				catch(final SAXException saxException)
				{
					throw new ServletException(saxException);
				}
				catch(final URISyntaxException uriSyntaxException)
				{
					throw new ServletException(uriSyntaxException);
				}
				catch(final InvocationTargetException invocationTargetException)
				{
					throw new ServletException(invocationTargetException);
				}
				finally
				{
					guiseApplicationDescriptionBufferedInputStream.close();	//always close the input stream
				}
			}
			catch(final IOException ioException)	//if there is an I/O error
			{
				throw new ServletException(ioException);
			}		
		}
		else	//if no application description is specified, indicate an error TODO allow Guise to support overlays in the future with default Guise applications
		{
			throw new ServletException("web.xml missing Guise application init parameter \""+APPLICATION_INIT_PARAMETER+"\".");
		}
		guiseApplication.installComponentKit(new XHTMLComponentKit());	//create and install an XHTML controller kit
		return guiseApplication;	//return the created Guise application
	}

	/**The mutex that prevents two threads from trying to initialize the Guise container simultaneously.*/
	private Object guiseContainerMutex=new Object();

	/**Initializes the servlet upon receipt of the first request.
	This version initializes the reference to the Guise container.
	This version installs the application into the container.
	@param request The servlet request.
	@exception IllegalStateException if this servlet has already been initialized from a request.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final HttpServletRequest request) throws ServletException
	{
//TODO del Debug.trace("initializing servlet from request");
		super.init(request);	//do the default initialization
		synchronized(guiseContainerMutex)	//if more than one request are coming in simultaneously, only look up the container for the first one (although multiple lookups should still retrieve the same container)
		{
//TODO del	Debug.trace("checking container");
			if(guiseContainer==null)	//if no container exists
			{
//TODO del				Debug.trace("context path", getContextPath());
				final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request
//			TODO del	Debug.trace("requestURI", requestURI);
				final String containerBasePath=getContextPath()+PATH_SEPARATOR;	//determine the base path of the container
				final URI containerBaseURI=changePath(requestURI, containerBasePath);	//determine the container base URI
//			TODO del	Debug.trace("containerURI", containerBaseURI);
	
				final ServletContext servletContext=getServletContext();	//get the servlet context
				guiseContainer=HTTPServletGuiseContainer.getGuiseContainer(servletContext, containerBaseURI);	//get a reference to the Guise container, creating it if needed
//			TODO del	Debug.trace("guise container: ", guiseContainer, "for servlet context", getServletContext());
//TODO del Debug.trace("installing application into guise container: ", guiseContainer, "for servlet context", getServletContext());
					//install the application into the container
				final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
						//"/contextPath" or "", "/servletPath" or ""
				final String guiseApplicationBasePath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application base path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
				final String guiseApplicationRelativePath=relativizePath(containerBasePath, guiseApplicationBasePath);	//get the application path relative to the container path

//TODO del Debug.trace("context path", request.getContextPath(), "servlet path", request.getServletPath(), "container base path", containerBasePath, "application base path", guiseApplicationBasePath, "application relative path", guiseApplicationRelativePath);
				
				final File guiseApplicationHomeDirectory=getDataDirectory(servletContext, DATA_DIRECTORY_INIT_PARAMETER, "guise/home/"+guiseApplicationRelativePath);	//get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
				final File guiseApplicationLogDirectory=getDataDirectory(servletContext, LOG_DIRECTORY_INIT_PARAMETER, "guise/logs/"+guiseApplicationRelativePath);	//get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
				final File guiseApplicationTempDirectory=getDataDirectory(servletContext, TEMP_DIRECTORY_INIT_PARAMETER, "guise/temp/"+guiseApplicationRelativePath);	//get the explicitly defined data directory; if there is no data directory defined, use the default data directory with a subpath of "guise/home" plus the application relative path TODO use a constant
				//			TODO delDebug.trace("ready to install application into container with context path", guiseApplicationContextPath);
				try
				{
					guiseContainer.installApplication(guiseApplication, guiseApplicationBasePath, guiseApplicationHomeDirectory, guiseApplicationLogDirectory, guiseApplicationTempDirectory);	//install the application
				}
				catch(final IOException ioException)	//if there is an I/O exception installing the application
				{
					throw new ServletException(ioException);
				}
			}
		}
//	TODO del		Debug.trace("initializing; container base URI:", guiseContainer.getBaseURI(), "container base path:", guiseContainer.getBasePath());
/*TODO del when works; now application is installed when container is retrieved
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container (which we just created if needed)
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()==null)	//if this application has not yet been installed (note that there is a race condition here if multiple HTTP requests attempt to access the application simultaneously, but the losing thread will simply throw an exception and not otherwise disturb the application functionality)
		{
			final String guiseApplicationContextPath=request.getContextPath()+request.getServletPath()+PATH_SEPARATOR;	//construct the Guise application context path from the servlet request, which is the concatenation of the web application path and the servlet's path with an ending slash
//		TODO delDebug.trace("ready to install application into container with context path", guiseApplicationContextPath);
			guiseContainer.installApplication(guiseApplication, guiseApplicationContextPath);	//install the application			
		}
*/
	}

//TODO fix HEAD method servicing, probably by overriding serveResource()

	/**Services the GET method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
//	TODO del Debug.info("method:", request.getMethod(), "raw path info:", rawPathInfo);
//TODO del Debug.info("user agent:", getUserAgent(request));
//	TODO del final Runtime runtime=Runtime.getRuntime();	//get the runtime instance
//	TODO del Debug.info("before service request: memory max", runtime.maxMemory(), "total", runtime.totalMemory(), "free", runtime.freeMemory(), "used", runtime.totalMemory()-runtime.freeMemory());
		
		assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
		final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
/*TODO del
final Enumeration headerNames=request.getHeaderNames();	//TODO del
while(headerNames.hasMoreElements())
{
	final String headerName=(String)headerNames.nextElement();
	Debug.info("request header:", headerName, request.getHeader(headerName));
}
*/
		if(navigationPath.startsWith(GuiseApplication.GUISE_RESERVED_BASE_PATH))	//if this is a request for a Guise reserved path (e.g. a public resource or a temporary resource)
		{
			super.doGet(request, response);	//go ahead and retrieve the resource immediately
			return;	//don't try to see if there is a navigation path for this path
		}
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.hasDestination(navigationPath))	//if we have a destination associated with the requested path
		{
			final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request
			
/*TODO del
	Debug.info("session ID", guiseSession.getHTTPSession().getId());	//TODO del
	Debug.info("content length:", request.getContentLength());
	Debug.info("content type:", request.getContentType());
	*/
			
				//make sure the environment has the WebTrends ID
			final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the session's environment
			if(!environment.hasProperty(WEBTRENDS_ID_COOKIE_NAME))	//if the environment doesn't have a WebTrends ID
			{
				final StringBuilder webtrendsIDStringBuilder=new StringBuilder();	//create a string builder for creating a WebTrends ID
				webtrendsIDStringBuilder.append(request.getRemoteAddr());	//IP address
				webtrendsIDStringBuilder.append('-');	//-
				webtrendsIDStringBuilder.append(System.currentTimeMillis());	//current time in milliseconds
				//TODO fix nanonseconds if needed, but Java doesn't even offer this information
/*TODO del; this is some sort of checksum, not a UUID
				webtrendsIDStringBuilder.append("::");	//::
				final UUID uuid=UUID.randomUUID();	//create a new UUID
				webtrendsIDStringBuilder.append(toHexString(uuid).toUpperCase());	//append the UUID in hex
*/
				environment.setProperty(WEBTRENDS_ID_COOKIE_NAME, webtrendsIDStringBuilder.toString());	//store the WebTrends ID in the environment, which will be stored in the cookies eventually
			}
	//TODO del Debug.info("supports Flash: ", guiseSession.getEnvironment().getProperty(GuiseEnvironment.CONTENT_APPLICATION_SHOCKWAVE_FLASH_ACCEPTED_PROPERTY));
	//TODO del Debug.trace("creating thread group");
			final GuiseSessionThreadGroup guiseSessionThreadGroup=Guise.getInstance().getThreadGroup(guiseSession);	//get the thread group for this session
	//	TODO del Debug.trace("creating runnable");
			final GuiseSessionRunnable guiseSessionRunnable=new GuiseSessionRunnable(request, response, guiseContainer, guiseApplication, guiseSession);	//create a runnable instance to service the Guise request
	//TODO del Debug.trace("calling runnable");
			call(guiseSessionThreadGroup, guiseSessionRunnable);	//call the runnable in a new thread inside the thread group
	//TODO del Debug.trace("done with the call");
			if(guiseSessionRunnable.servletException!=null)
			{
	//TODO del			Debug.trace("callling runnable");
				throw guiseSessionRunnable.servletException;
			}
			if(guiseSessionRunnable.ioException!=null)
			{
				throw guiseSessionRunnable.ioException;
			}
			if(!guiseSessionRunnable.isGuiseRequest)	//TODO do we even need this case now that we check before-hand?
			{
				super.doGet(request, response);	//let the default functionality take over
			}
		}
		else	//if there is no Guise destination for the requested path
		{
			super.doGet(request, response);	//let the default functionality take over			
		}
	}

	/**The runnable class that services an HTTP request.
	@author Garret Wilson
	*/
	protected class GuiseSessionRunnable implements Runnable
	{
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private final HTTPServletGuiseContainer guiseContainer;
		private final GuiseApplication guiseApplication;
		private final GuiseSession guiseSession;

		public ServletException servletException=null;
		public IOException ioException=null;
		public boolean isGuiseRequest=false;
		
		/**Constructor.
	  @param request The HTTP request.
	  @param response The HTTP response.
	  @param guiseContainer The Guise container.
	  @param guiseApplication The Guise application.
	  @param guiseSession The Guise session.
		*/
		public GuiseSessionRunnable(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession)
		{
			this.request=request;
			this.response=response;
			this.guiseContainer=guiseContainer;
			this.guiseApplication=guiseApplication;
			this.guiseSession=guiseSession;
		}

		/**Actually services the Guise request.
		@see GuiseHTTPServlet#serviceGuiseRequest(HttpServletRequest, HttpServletResponse, HTTPServletGuiseContainer, GuiseApplication, GuiseSession)
		*/
		public void run()
		{
			try
			{
/*TODO del				
				
Debug.trace("ready to service Guise request with session", guiseSession);
Debug.trace("Guise thinks we're in session", Guise.getInstance().getGuiseSession());
Debug.trace("are the sessions equal?", guiseSession.equals(Guise.getInstance().getGuiseSession()));

*/
				isGuiseRequest=serviceGuiseRequest(request, response, guiseContainer, guiseApplication, guiseSession);
			}
			catch(final ServletException servletException)
			{
				this.servletException=servletException;
			}
			catch(final IOException ioException)
			{
				this.ioException=ioException;
			}
		}
	}
	
	/**Services a Guise request.
  If this is a request for a Guise navigation path, a Guise context will be assigned to the Guise session while the request is processed.
  @param request The HTTP request.
  @param response The HTTP response.
  @param guiseContainer The Guise container.
  @param guiseApplication The Guise application.
  @param guiseSession The Guise session.
  @return <code>true</code> if this request was for Guise components, else <code>false</code> if no navigation panel was found at the given location.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	private boolean serviceGuiseRequest(final HttpServletRequest request, final HttpServletResponse response, final HTTPServletGuiseContainer guiseContainer, final GuiseApplication guiseApplication, final GuiseSession guiseSession) throws ServletException, IOException
	{
		final URI requestURI=URI.create(request.getRequestURL().toString());	//get the URI of the current request		
//TODO del Debug.trace("servicing Guise request with request URI:", requestURI);
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		final boolean isAJAX=contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType);	//see if this is a Guise AJAX request
			//TODO verify; does this work with file uploads?
			//this is a non-AJAX Guise POST if there is an XHTML action input ID field TODO add a better field; stop using a view
		final boolean isGuisePOST=POST_METHOD.equals(request.getMethod()) && request.getParameter(XHTMLApplicationFrameView.getActionInputID(guiseSession.getApplicationFrame()))!=null;

		final String rawPathInfo=getRawPathInfo(request);	//get the raw path info
		assert isAbsolutePath(rawPathInfo) : "Expected absolute path info, received "+rawPathInfo;	//the Java servlet specification says that the path info will start with a '/'
		final String navigationPath=rawPathInfo.substring(1);	//remove the beginning slash to get the navigation path from the path info
		final Destination destination=guiseApplication.getDestination(navigationPath);	//get the destination, if any, associated with the requested path
		if(destination instanceof ComponentDestination)	//if we have a component destination associated with the requested path
		{
			final ComponentDestination componentDestination=(ComponentDestination)destination;	//get the destination as a component destination
//		TODO del Debug.trace("have destination; creating context");
			final HTTPServletGuiseContext guiseContext=new HTTPServletGuiseContext(guiseSession, destination, request, response);	//create a new Guise context
//		TODO del Debug.trace("got context");
			synchronized(guiseSession)	//don't allow other session contexts to be active at the same time
			{

//TODO del Debug.trace("setting context");
				guiseSession.setContext(guiseContext);	//set the context for this session
				try
				{
					if(!isAJAX)	//if this is not an AJAX request, see if we need to enforce modal navigation (only do this after we find a navigation panel, as this request might be for a stylesheet or some other non-panel resource, which shouldn't be redirected)
					{
						final ModalNavigation modalNavigation=guiseSession.getModalNavigation();	//see if we are currently doing modal navigation
						if(modalNavigation!=null)	//if we are currently in the middle of modal navigation, make sure the correct panel was requested
						{
							final URI modalNavigationURI=modalNavigation.getNewNavigationURI();	//get the modal navigation URI
							if(!requestURI.getRawPath().equals(modalNavigationURI.getRawPath()))		//if this request was for a different path than our current modal navigation path (we wouldn't be here if the domain, application, etc. weren't equivalent)
							{
								throw new HTTPMovedTemporarilyException(modalNavigationURI);	//redirect to the modal navigation location				
							}
						}
					}
					final Bookmark navigationBookmark=getBookmark(request);	//get the bookmark from this request
		//TODO fix to recognize navigation, bookmark, and principal changes when the navigation panel is created		final Bookmark bookmark=getBookmark(request);	//get the bookmark from this request
					final Bookmark oldBookmark=isAJAX ? guiseSession.getBookmark() : navigationBookmark;	//get the original bookmark, which will be the one requested in navigation (which we'll soon set) if this is a normal HTTP GET/POST
//TODO del Debug.trace("navigation bookmark:", navigationBookmark, "old bookmark:", oldBookmark, "session bookmark:", guiseSession.getBookmark(), "is AJAX:", isAJAX);
					final Principal oldPrincipal=guiseSession.getPrincipal();	//get the old principal
					final Component<?> destinationComponent=guiseSession.getDestinationComponent(componentDestination);	//get the component bound to the requested destination
					assert destinationComponent!=null : "No component found, even though we found a valid destination.";
					final ApplicationFrame<?> applicationFrame=guiseSession.getApplicationFrame();	//get the application frame
//TODO del Debug.trace("ready to get control events");
					final List<ControlEvent> controlEvents=getControlEvents(request, guiseSession);	//get all control events from the request
Debug.trace("got control events");
					if(isAJAX)	//if this is an AJAX request
					{
/*TODO tidy when stringbuilder context works
						guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type
						guiseContext.writeElementBegin(null, "response");	//<response>	//TODO use a constant, decide on a namespace
*/
					}
					else	//if this is not an AJAX request
					{
//TODO del Debug.trace("this is not AJAX, with method:", request.getMethod(), "content type", contentType, "guise POST?", isGuisePOST);
						applicationFrame.setContent(destinationComponent);	//place the component in the application frame
						setNoCache(request, response);	//make sure the response is not cached TODO should we do this for AJAX responses as well?				
						final String referrer=getReferer(request);	//get the request referrer, if any
						final URI referrerURI=referrer!=null ? getPlainURI(URI.create(referrer)) : null;	//get a plain URI version of the referrer, if there is a referrer
							//see if there is non-Guise HTTP POST data, and if so, set that bookmark navigation temporarily
							//a non-Guise form HTTP POST, get the servlet parameters (which will include the URL query information)
						if(POST_METHOD.equals(request.getMethod()) && contentType!=null && APPLICATION_X_WWW_FORM_URLENCODED_CONTENT_TYPE.match(contentType) && !isGuisePOST)
						{
//TODO del Debug.trace("using servlet parameter methods");
							final List<Bookmark.Parameter> bookmarkParameterList=new ArrayList<Bookmark.Parameter>();	//create a new list of bookmark parameters
							final Iterator parameterEntryIterator=request.getParameterMap().entrySet().iterator();	//get an iterator to the parameter entries
							while(parameterEntryIterator.hasNext())	//while there are more parameter entries
							{
								final Map.Entry parameterEntry=(Map.Entry)parameterEntryIterator.next();	//get the next parameter entry
								final String parameterKey=(String)parameterEntry.getKey();	//get the parameter key
								final String[] parameterValues=(String[])parameterEntry.getValue();	//get the parameter values
								for(final String parameterValue:parameterValues)	//for each parameter value
								{
//TODO del Debug.trace("adding parameter bookmark:", parameterKey, parameterValue);
									bookmarkParameterList.add(new Bookmark.Parameter(parameterKey, parameterValue));	//create a corresponding bookmark parameter
								}
							}
							if(!bookmarkParameterList.isEmpty())	//if there are bookmark parameters
							{
								final Bookmark.Parameter[] bookmarkParameters=bookmarkParameterList.toArray(new Bookmark.Parameter[bookmarkParameterList.size()]);	//get an array of bookmark parameters
								final Bookmark postBookmark=new Bookmark(bookmarkParameters);	//create a new bookmark to represent the POST information
								guiseSession.setNavigation(navigationPath, postBookmark, referrerURI);	//set the session navigation to the POST bookmark information
							}
						}
//TODO del Debug.trace("ready to set navigation with new navigation path:", navigationPath, "navigation bookmark:", navigationBookmark, "referrerURI:", referrerURI);
						guiseSession.setNavigation(navigationPath, navigationBookmark, referrerURI);	//set the session navigation with the navigation bookmark, firing any navigation events if appropriate
					}
					final Set<Frame<?>> removedFrames=new HashSet<Frame<?>>();	//create a set of frames so that we can know which ones were removed TODO testing
					CollectionUtilities.addAll(removedFrames, guiseSession.getFrameIterator());	//get all the current frames; we'll determine which ones were removed, later TODO improve all this
					boolean isNavigating=false;	//we'll check this later to see if we're navigating so we won't have to update all the components
					for(final ControlEvent controlEvent:controlEvents)	//for each control event
					{
						final Set<Component<?>> requestedComponents=new HashSet<Component<?>>();	//create a set of component that were identified in the request
						try
						{
							if(controlEvent instanceof FormControlEvent)	//if this is a form submission
							{
								final FormControlEvent formControlEvent=(FormControlEvent)controlEvent;	//get the form control event
								if(formControlEvent.isExhaustive())	//if this is an exhaustive form submission (such as a POST submission)
								{
									if(formControlEvent.getParameterListMap().size()>0)	//only process the event if there were submitted values---especially important for radio buttons and checkboxes, which must assume a value of false if nothing is submitted for them, thereby updating the model
									{
										requestedComponents.add(destinationComponent);	//we'll give the event to the entire destination component
									}
								}
								else	//if this is only a partial form submission
								{
									final CollectionMap<String, Object, List<Object>> parameterListMap=formControlEvent.getParameterListMap();	//get the request parameter map
									for(final Map.Entry<String, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
									{
										final String parameterName=parameterListMapEntry.getKey();	//get the parameter name
				
										if(parameterName.equals(XHTMLApplicationFrameView.getActionInputID(applicationFrame)) && parameterListMapEntry.getValue().size()>0)	//if this parameter is for an action
										{
											final Component<?> actionComponent=getComponentByID(guiseSession, parameterListMapEntry.getValue().get(0).toString());	//get an action component
											if(actionComponent!=null)	//if we found an action component
											{
												requestedComponents.add(actionComponent);	//add it to the list of requested components
											}
										}
										else	//if this parameter is not a special action parameter
										{
											//TODO don't re-update nested components (less important for controls, which don't have nested components) 
							//TODO del Debug.trace("looking for component with name", parameterName);
											getControlsByName(guiseSession, parameterName, requestedComponents);	//TODO comment; tidy
				//TODO del; test new method; tidy; comment							getControlsByName(guiseContext, navigationPanel, parameterName, requestedComponents);	//get all components identified by this name
										}
									}
								}
							}
							else if(controlEvent instanceof ComponentControlEvent)	//if this event is bound for a specific component
							{
			//TODO del Debug.trace("this is a control event; looking for component with ID", ((ComponentControlEvent)controlEvent).getComponentID());
								final Component<?> component=getComponentByID(guiseSession, ((ComponentControlEvent)controlEvent).getComponentID());	//get the target component from its ID
								if(component!=null)	//if there is a target component
								{
			//TODO del Debug.trace("got component", component);
									requestedComponents.add(component);	//add the component to the set of requested components
								}
							}
							else if(controlEvent instanceof InitControlEvent)	//if this is an initialization event
							{
								final InitControlEvent initControlEvent=(InitControlEvent)controlEvent;	//get the init control event
								final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the session's environment
								{	//set up the environment; put this in another scope so the variable names won't clash; we may move logging in the future
									final String javascriptVersion=initControlEvent.getJavaScriptVersion();	//get the JavaScript version reported
									if(javascriptVersion!=null)	//if JavaScript is supported
									{
										environment.setProperty(GuiseEnvironment.CONTENT_TEXT_JAVASCRIPT_SUPPORTED_PROPERTY, Boolean.TRUE);	//indicate that JavaScript is supported
										environment.setProperty(GuiseEnvironment.CONTENT_TEXT_JAVASCRIPT_VERSION, javascriptVersion);	//indicate which JavaScript version supported
									}
									else	//if JavaScript isn't supported
									{
										environment.setProperty(GuiseEnvironment.CONTENT_TEXT_JAVASCRIPT_SUPPORTED_PROPERTY, Boolean.FALSE);	//indicate that JavaScript isn't supported
									}
								}
								final Date now=new Date();
								final Entry entry=new Entry();
								entry.setFieldValue(Field.DATE_FIELD, now);
								entry.setFieldValue(Field.TIME_FIELD, now);
								entry.setFieldValue(Field.CLIENT_IP_FIELD, request.getRemoteAddr());
								entry.setFieldValue(Field.CLIENT_SERVER_USERNAME_FIELD, request.getRemoteUser());
//							TODO fix				entry.setFieldValue(Field.CLIENT_SERVER_HOST_FIELD, request.get
								entry.setFieldValue(Field.CLIENT_SERVER_METHOD_FIELD, GET_METHOD);	//log the GET method always for WebTrends
//TODO del								entry.setFieldValue(Field.CLIENT_SERVER_METHOD_FIELD, request.getMethod());
								entry.setFieldValue(Field.CLIENT_SERVER_URI_STEM_FIELD, rawPathInfo);
//TODO del								final List<NameValuePair<String, String>> queryParameters=new ArrayList<NameValuePair<String, String>>();	//create an array of parameters
								final StringBuilder queryParametersStringBuilder=new StringBuilder(request.getQueryString());	//create a new string builder for adding the query parameters, starting with the current query string
								if(queryParametersStringBuilder.length()>0)	//if we have an existing query string
								{
									queryParametersStringBuilder.append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//append '&' to separate the old parameters from the new ones
								}
									//WT.bh
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, BROWSING_HOUR_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getHour())).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.bh as a query parameter
								//TODO add WT.co to indicate whether cookies are enabled
								//TODO add WT.co_d to provide cookie data (only on the first time---is this a WebTrends hack that isn't needed here, or is it used for something?)
									//WT.sr
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, BROWSER_SIZE_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getBrowserWidth())+"x"+Integer.toString(initControlEvent.getBrowserHeight())).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.bs as a query parameter
									//WT.cd
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, COLOR_DEPTH_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getColorDepth())).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.cd as a query parameter
								//TODO add WT.fi (ActiveX)
								//TODO add WT.fv (ActiveX version?)
									//WT.jo
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVA_ENABLED_QUERY_ATTRIBUTE_NAME, WebTrendsYesNo.asYesNo(initControlEvent.isJavaEnabled()).toString()).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.jo as a query parameter
									//WT.js
								final Boolean isJavaScriptSupported=asInstance(environment.getProperty(GuiseEnvironment.CONTENT_TEXT_JAVASCRIPT_SUPPORTED_PROPERTY), Boolean.class);	//see if the environment knows about Java
								if(isJavaScriptSupported!=null)	//if JavaScript is supported
								{
										//WT.jv
									ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVASCRIPT_QUERY_ATTRIBUTE_NAME, WebTrendsYesNo.asYesNo(isJavaScriptSupported.booleanValue()).toString()).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.js as a query parameter
									final String javascriptVersion=asInstance(environment.getProperty(GuiseEnvironment.CONTENT_TEXT_JAVASCRIPT_VERSION), String.class);	//get the JavaScript version
									if(javascriptVersion!=null)	//if we know the JavaScript version
									{
										ELFF.appendURIQueryParameter(queryParametersStringBuilder, JAVASCRIPT_VERSION_QUERY_ATTRIBUTE_NAME, javascriptVersion).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.jv as a query parameter
									}
								}
								//TODO add WT.sp, if needed
									//WT.sr
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, SCREEN_RESOLUTION_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getScreenWidth())+"x"+Integer.toString(initControlEvent.getScreenHeight())).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.sr as a query parameter
									//WT.ti
								final String title=destinationComponent.getLabel();	//get the title of the page, if there is a title
								if(title!=null)	//if there is a title
								{
									ELFF.appendURIQueryParameter(queryParametersStringBuilder, TITLE_QUERY_ATTRIBUTE_NAME, guiseSession.resolveString(title)).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.ti as a query parameter
								}
									//WT.tz
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, TIMEZONE_QUERY_ATTRIBUTE_NAME, Integer.toString(initControlEvent.getTimeZone())).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.tz as a query parameter
									//WT.ul
								ELFF.appendURIQueryParameter(queryParametersStringBuilder, USER_LANGUAGE_QUERY_ATTRIBUTE_NAME, initControlEvent.getLanguage()).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.ul as a query parameter
									//content groups and subgroups
								final List<String> destinationCategoryIDs=new ArrayList<String>();	//we'll look for all the categories available
								final List<String> destinationSubcategoryIDs=new ArrayList<String>();	//we'll look for all the subcategories available, in whatever category (because WebTrends doesn't distinguish among categories for subcategories)								
								for(final Category category:destination.getCategories())	//look at each category
								{
//TODO del									Debug.trace("destination has category", category.getID());
									final String categoryID=category.getID();	//get this category's ID
									if(!destinationCategoryIDs.contains(categoryID))	//if this category hasn't yet been added TODO use an array set
									{
										destinationCategoryIDs.add(categoryID);	//note this category's ID
									}
									for(final Category subcategory:category.getCategories())	//look at each subcategory
									{
//TODO del										Debug.trace("category has subcategory", subcategory.getID());
										final String subcategoryID=subcategory.getID();	//get this subcategory's ID
										if(!destinationSubcategoryIDs.contains(subcategoryID))	//if this subcategory hasn't yet been added TODO use an array set
										{
											destinationSubcategoryIDs.add(subcategoryID);	//note this subcategory's ID (ignore all sub-subcategories, as WebTrends doesn't support them)
										}
									}
								}
									//WT.cg_n
								if(!destinationCategoryIDs.isEmpty())	//if there are destination categories
								{
/*TODO fix									
TODO: find out why sometimes ELFF can't be loaded because the application isn't installed into the container
*/
									ELFF.appendURIQueryParameter(queryParametersStringBuilder, CONTENT_GROUP_NAME_QUERY_ATTRIBUTE_NAME, destinationCategoryIDs.toArray(new String[destinationCategoryIDs.size()])).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.cg_n as a query parameter
										//WT.cg_s
									if(!destinationSubcategoryIDs.isEmpty())	//if there are destination subcategories (there cannot be subcategories without categories, and moreover WebTrends documentation does not indicate that subcategories are allowed without categories)
									{
										ELFF.appendURIQueryParameter(queryParametersStringBuilder, CONTENT_SUBGROUP_NAME_QUERY_ATTRIBUTE_NAME, destinationSubcategoryIDs.toArray(new String[destinationSubcategoryIDs.size()])).append(QUERY_NAME_VALUE_PAIR_DELIMITER);	//add WT.cg_s as a query parameter
									}
								}
								queryParametersStringBuilder.delete(queryParametersStringBuilder.length()-1, queryParametersStringBuilder.length());	//remove the last parameter delimiter
//TODO del Debug.trace("ready to log query:", queryParametersStringBuilder);
//TODO del when works								final NameValuePair<String, String>[] queryParameterArray=(NameValuePair<String, String>[])queryParameters.toArray(new NameValuePair[queryParameters.size()]);	//put the query parameters into an array
//TODO del when works								entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, appendQueryParameters(request.getQueryString(), queryParameterArray));	//append the new parameters and set the log field
								entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, queryParametersStringBuilder.toString());	//set the log field to be the parameters we determined
//TODO del entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, request.getQueryString());
								
//							TODO fix				entry.setFieldValue(Field.CLIENT_SERVER_URI_QUERY_FIELD, request.getQueryString());
								entry.setFieldValue(Field.SERVER_CLIENT_STATUS_FIELD, new Integer(200));	//TODO fix with real HTTP status
//							TODO fix cs-status
//							TODO fix cs-bytes
//							TODO fix cs-version
								entry.setFieldValue(Field.CLIENT_SERVER_USER_AGENT_HEADER_FIELD, getUserAgent(request));
								final String webTrendsID=asInstance(environment.getProperty(WEBTRENDS_ID_COOKIE_NAME), String.class);	//get the WebTrends ID
								entry.setFieldValue(Field.CLIENT_SERVER_COOKIE_HEADER_FIELD, webTrendsID!=null ? WEBTRENDS_ID_COOKIE_NAME+"="+webTrendsID : null);	//store the WebTrends ID cookie as the cookie TODO decide if we want to get general cookies instead of just the WebTrends cookie
								final URI referrerURI=initControlEvent.getReferrerURI();	//get the initialization referrer URI
								entry.setFieldValue(Field.CLIENT_SERVER_REFERER_HEADER_FIELD, referrerURI!=null ? referrerURI.toString() : null);	//store the referrer URI, if any
								entry.setFieldValue(Field.DCS_ID_FIELD, guiseApplication.getDCSID());	//get the DCS ID from the application, if there is a DCS ID
									//log this page
								final Writer elffWriter=guiseApplication.getLogWriter("elff.log", elffWriterInitializer, elffWriterUninitializer);	//get the ELFF log writer for this application TODO use a constant
								elffWriter.write(getELFF().serializeEntry(entry));	//serialize the ELFF entry to the ELFF writer
								elffWriter.flush();	//flush the ELFF writer
							}
							if(!requestedComponents.isEmpty())	//if components were requested
							{
								guiseContext.setState(GuiseContext.State.PROCESS_EVENT);	//update the context state for processing an event
								for(final Component<?> component:requestedComponents)	//for each requested component
								{
			Debug.trace("ready to process event", controlEvent, "for component", component);
									component.processEvent(controlEvent);		//tell the component to process the event
								}
								guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated
							}					
			/*TODO del
			Debug.trace("we now have affected components:", affectedComponents.size());
			for(final Component<?> affectedComponent:affectedComponents)
			{
				Debug.trace("affected component:", affectedComponent);
			}
			*/
							final Bookmark newBookmark=guiseSession.getBookmark();	//see if the bookmark has changed
//TODO del Debug.trace("navigation bookmark:", navigationBookmark, "new bookmark", newBookmark);
							final Navigation requestedNavigation=guiseSession.getRequestedNavigation();	//get the requested navigation
							if(requestedNavigation!=null || !ObjectUtilities.equals(navigationBookmark, newBookmark))	//if navigation is requested or the bookmark has changed, redirect the browser
							{
								final String redirectURIString;	//we'll determine where to direct to
								if(requestedNavigation!=null)	//if navigation is requested
								{
									final URI requestedNavigationURI=requestedNavigation.getNewNavigationURI();
				//TODO del Debug.trace("navigation requested to", requestedNavigationURI);
									guiseSession.clearRequestedNavigation();	//remove any navigation requests
									if(requestedNavigation instanceof ModalNavigation)	//if modal navigation was requested
									{
										beginModalNavigation(guiseApplication, guiseSession, (ModalNavigation)requestedNavigation);	//begin the modal navigation
									}
									redirectURIString=requestedNavigationURI.toString();	//we already have the destination URI
								}
								else	//if navigation is not requested, request a navigation to the new bookmark location
								{
									redirectURIString=request.getRequestURL().append(newBookmark).toString();	//save the string form of the constructed bookmark URI
								}
								if(isAJAX)	//if this is an AJAX request
								{
									guiseContext.clearText();	//clear all the response data (which at this point should only be navigation information, anyway)
									guiseContext.writeElementBegin(null, "navigate");	//<navigate>	//TODO use a constant
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									if(requestedNavigation!=null)	//if navigation was requested (i.e. this isn't just a bookmark registration)
									{
										final String viewportID=requestedNavigation.getViewportID();	//get the requested viewport ID
										if(viewportID!=null)	//if a viewport was requested
										{
											guiseContext.writeAttribute(null, "viewportID", viewportID);	//specify the viewport ID TODO use a constant
										}
									}
									guiseContext.write(redirectURIString);	//write the navigation URI
									guiseContext.writeElementEnd(null, "navigate");	//</navigate>
									isNavigating=true;	//show that we're going to navigate; process the other events to make sure the data model is up-to-date (and in case the navigation gets overridden)
								}
								else	//if this is not an AJAX request
								{
									throw new HTTPMovedTemporarilyException(URI.create(redirectURIString));	//redirect to the new navigation location TODO fix to work with other viewports						
								}
			//TODO if !AJAX						throw new HTTPMovedTemporarilyException(requestedNavigationURI);	//redirect to the new navigation location
								//TODO store a flag or something---if we're navigating, we probably should flush the other queued events
							}
			
							
							
							
							if(!isNavigating && !ObjectUtilities.equals(oldPrincipal, guiseSession.getPrincipal()))	//if the principal has changed after updating the model (if we're navigating there's no need to reload)
							{
								if(isAJAX)	//if this is an AJAX request
								{
									guiseContext.clearText();	//clear all the response data (which at this point should only be navigation information, anyway)
									guiseContext.writeElementBegin(null, "reload", true);	//<reload>	//TODO use a constant
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									guiseContext.writeElementEnd(null, "reload");	//</reload>
									isNavigating=true;	//show that we're navigating, so there's no need to update views
								}
								else	//if this is not an AJAX request
								{
									throw new HTTPMovedTemporarilyException(guiseContext.getNavigationURI());	//redirect to the same page, which will generate a new request with no POST parameters, which would likely change the principal again)
								}
							}
						}
						catch(final RuntimeException runtimeException)	//if we run into any errors processing events
						{
							if(isAJAX)	//if this is an AJAX request
							{
								Debug.error(runtimeException);	//log the error
								//TODO send back the error
							}
							else	//if this is ano an AJAX request
							{
								throw runtimeException;	//pass the error back to the servlet TODO improve; pass to Guise
							}
							
						}

						if(isAJAX && !isNavigating && controlEvent instanceof InitControlEvent)	//if this is an AJAX initialization event (if we're navigating, there's no need to initialize this page) TODO maybe just dirty all the frames so this happens automatically
						{
							guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
								//close all the flyover frames to get rid of stuck flyover frames, such as those left from refreshing the page during flyover TODO fix; this is a workaround to keep refreshing the page from leaving stuck flyover frames; maybe do something better
							final Iterator<Frame<?>> flyoverFrameIterator=guiseSession.getFrameIterator();	//get an iterator to all the frames
							while(flyoverFrameIterator.hasNext())	//while there are more frames
							{
								final Frame<?> frame=flyoverFrameIterator.next();	//get the next frame
								if(frame instanceof FlyoverFrame)	//if this is a flyover frame
								{
									frame.close();	//close all flyover frames
								}
							}
								//send back any open frames
							final Iterator<Frame<?>> frameIterator=guiseSession.getFrameIterator();	//get an iterator to all the frames
							while(frameIterator.hasNext())	//while there are more frames
							{
								final Frame<?> frame=frameIterator.next();	//get the next frame
								if(frame!=guiseSession.getApplicationFrame())	//don't send back the application frame
								{
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
		//							TODO fix							else	//if the component is not visible, remove the component's elements
									guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									frame.updateView(guiseContext);		//tell the component to update its view
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
								}
							}
						}
						else	//if we don't need to update any views for these control events
						{
							guiseContext.setState(GuiseContext.State.INACTIVE);	//deactivate the context so that any model update events will be generated								
						}
					}

					
					
						//TODO move this to the bottom of the processing, as cookies only need to be updated before they go back
					synchronizeCookies(request, response, guiseSession);	//synchronize the cookies going out in the response; do this before anything is written back to the client
					
					if(!isNavigating)	//we'll only update the views if we're not navigating (if we're navigating, we're changing pages, anyway)
					{
						guiseContext.setState(GuiseContext.State.UPDATE_VIEW);	//update the context state for updating the view
						if(isAJAX)	//if this is an AJAX request
						{
							final Collection<Component<?>> dirtyComponents=getDirtyComponents(guiseSession);	//get all dirty components in all the session frames
			
							CollectionUtilities.removeAll(removedFrames, guiseSession.getFrameIterator());	//remove all the ending frames, leaving us the frames that were removed TODO improve all this
			//TODO fix					dirtyComponents.addAll(frames);	//add all the frames that were removed
							
							Debug.trace("we now have dirty components:", dirtyComponents.size());
							for(final Component<?> affectedComponent:dirtyComponents)
							{
								Debug.trace("affected component:", affectedComponent);
							}
							if(dirtyComponents.contains(applicationFrame))	//if the application frame itself was affected, we might as well reload the page
							{
								guiseContext.writeElementBegin(null, "reload", true);	//<reload>	//TODO use a constant
								guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
								guiseContext.writeElementEnd(null, "reload");	//</reload>
							}
							else	//if the application frame wasn't affected
							{
								for(final Component<?> dirtyComponent:dirtyComponents)	//for each component affected by this update cycle
								{
			//TODO fix							if(dirtyComponent.isVisible())	//if the component is visible
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "patch");	//<xhtml:patch>	//TODO use a constant TODO don't use the XHTML namespace if we can help it
			//TODO fix							else	//if the component is not visible, remove the component's elements
									guiseContext.writeAttribute(null, ATTRIBUTE_XMLNS, XHTML_NAMESPACE_URI.toString());	//xmlns="http://www.w3.org/1999/xhtml"
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									dirtyComponent.updateView(guiseContext);		//tell the component to update its view
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "patch");	//</xhtml:patch>
								}
								for(final Frame<?> frame:removedFrames)	//for each removed frame
								{
									guiseContext.writeElementBegin(XHTML_NAMESPACE_URI, "remove");	//<xhtml:remove>	//TODO use a constant TODO don't use the XHTML namespace if we can help it								
									guiseContext.writeAttribute(XMLNS_NAMESPACE_URI, GUISE_ML_NAMESPACE_PREFIX, GUISE_ML_NAMESPACE_URI.toString());	//xmlns:guise="http://guiseframework.com/id/ml#"
									guiseContext.writeAttribute(null, "id", frame.getID());	//TODO fix
			//TODO del Debug.trace("Sending message to remove frame:", frame.getID());
									guiseContext.writeElementEnd(XHTML_NAMESPACE_URI, "remove");	//</xhtml:remove>							
								}
							}
						}
						else	//if this is not an AJAX request
						{
							applicationFrame.updateView(guiseContext);		//tell the application frame to update its view						
						}
					}
					
					String text=guiseContext.getText();	//get the text to output
					if(isAJAX)	//if this is an AJAX request
					{
						guiseContext.setOutputContentType(XML_CONTENT_TYPE);	//switch to the "text/xml" content type TODO verify UTF-8 in a consistent, elegant way
						text="<response>"+text+"</response>";	//wrap the text in a response element
					}
//TODO del Debug.trace("response length:", text.length());
//TODO del Debug.trace("response:", text);
					final byte[] bytes=text.getBytes(UTF_8);	//write the content we collected in the context as series of bytes encoded in UTF-8
					final OutputStream outputStream=getCompressedOutputStream(request, response);	//get a compressed output stream, if possible
					outputStream.write(bytes);	//write the bytes
					outputStream.close();	//close the output stream, finishing writing the compressed contents (don't put this in a finally block, as it will attempt to write more data and raise another exception)						
				}
				finally
				{
					guiseContext.setState(GuiseContext.State.INACTIVE);	//always deactivate the context			
					guiseSession.setContext(null);	//remove this context from the session
				}
			}
			return true;	//show that we processed the Guise request
		}
		else if(destination instanceof RedirectDestination)	//if we have a component destination associated with the requested path
		{
			redirect(requestURI, guiseApplication, (RedirectDestination)destination);	//perform the redirect; this should never return
			throw new AssertionError("Redirect not expected to allow processing to continue.");
		}
		else	//if there was no navigation panel at the given path
		{
			return false;	//indicate that this was not a Guise component-related request
		}
	}

	/**Processes a redirect from a redirect destination.
	This method will unconditionally throw an exception.
	Under normal circumstances, an {@link HTTPRedirectException} will be thrown.
	@param requestURI The requested URI.
	@param guiseApplication The Guise application.
	@param redirectDestination The destination indicating how and to where redirection should occur.
	@throws IllegalArgumentException if the referenced destination does not specify a path (instead specifying a path pattern, for example).
	@throws HTTPRedirectException unconditionally to indicate how and to where redirection should occur.
	*/
	protected void redirect(final URI requestURI, final GuiseApplication guiseApplication, final RedirectDestination redirectDestination) throws HTTPRedirectException
	{
		final String redirectPath;	//the path to which direction should occur
		if(redirectDestination instanceof ReferenceDestination)	//if the destination references another destination
		{
			redirectPath=((ReferenceDestination)redirectDestination).getDestination().getPath();	//get the path of the referenced destination TODO what if the referenced destination is itself a redirect? should we support that, too? probably
			if(redirectPath==null)	//if there is no redirect path
			{
				throw new IllegalArgumentException("Redirect destination "+redirectDestination+" does not have a valid path.");
			}
		}
		else	//we don't yet support non-reference redirects
		{
			throw new AssertionError("Unsupported redirect destination type "+redirectDestination.getClass().getName());
		}
		final URI redirectURI=requestURI.resolve(guiseApplication.resolvePath(redirectPath));	//resolve the path to the application and resolve that against the request URI
		if(redirectDestination instanceof TemporaryRedirectDestination)	//if this is a temporary redirect
		{
			throw new HTTPMovedTemporarilyException(redirectURI);	//redirect temporarily
		}
		else if(redirectDestination instanceof PermanentRedirectDestination)	//if this is a permanent redirect
		{
			throw new HTTPMovedPermanentlyException(redirectURI);	//redirect permanently
		}
		else	//if we don't recognize the type of redirect
		{
			throw new AssertionError("Unsupported redirect destination type "+redirectDestination.getClass().getName());			
		}
/*TODO del when works		
		
		if(redirectDestination instanceof TemporaryRedirectDestination)	//if this is a temporary redirect
		{
			if(redirectDestination instanceof ReferenceDestination)	//if the destination references another destination
			{
				redirectPath=((ReferenceDestination)redirectDestination).getDestination().getPath();	//get the path of the referenced destination			
			}
			else	//we don't yet support non-reference redirects
			{
				throw new AssertionError("Unsupported redirect destination type "+redirectDestination.getClass().getName());
			}
			throw new HTTPMovedTemporarilyException(requestURI.resolve(guiseApplication.resolvePath(redirectPath)));	//resolve the path to the application and resolve that against the request URI; then redirect
		}
*/
//TODO del		Debug.trace("Just got redirect to ", destination);
	}
	
	/**Synchronizes the cookies in a request with the environment properties in a Guise session.
  Any cookies missing from the request will be added from the environment to the response.
  @param request The HTTP request.
  @param response The HTTP response.
  @param guiseSession The Guise session.
  */
	protected void synchronizeCookies(final HttpServletRequest request, final HttpServletResponse response, final GuiseSession guiseSession)
	{
			//remove unneeded cookies from the request						
		final String applicationBasePath=guiseSession.getApplication().getBasePath();	//get the application's base path
		assert applicationBasePath!=null : "Application not yet installed during cookie synchronization.";
		final GuiseEnvironment environment=guiseSession.getEnvironment();	//get the session's environment
		final Cookie[] cookies=request.getCookies();	//get the cookies in the request
		final Map<String, Cookie> cookieMap=new HashMap<String, Cookie>(cookies!=null ? cookies.length : 0);	//create a map to hold the cookies for quick lookup
		if(cookies!=null)	//if a cookie array was returned
		{
			for(final Cookie cookie:cookies)	//for each cookie in the request
			{
				final String cookieName=cookie.getName();	//get the name of this cookie
//TODO del Debug.trace("Looking at cookie", cookieName, "with value", cookie.getValue());
				if(!SESSION_ID_COOKIE_NAME.equals(cookieName))	//ignore the session ID
				{
//TODO del Debug.trace("Removing cookie", cookieName);
					final String environmentPropertyValue=asInstance(environment.getProperty(cookieName), String.class);	//see if there is a string environment property value for this cookie's name
					if(environmentPropertyValue!=null)	//if a value in the environment matches the cookie's name
					{
						if(!ObjectUtilities.equals(cookie.getValue(), encode(environmentPropertyValue)))	//if the cookie's value doesn't match the encoded environment property value
						{
							cookie.setValue(encode(environmentPropertyValue));	//update the cookie's value, making sure the value is encoded
						}
						cookieMap.put(cookieName, cookie);	//store the cookie in the map
					}
					else	//if there is no such environment property, remove the cookie
					{
						cookie.setValue(null);	//remove the value now
						cookie.setPath(applicationBasePath);	//set the cookie path to the application base path, because we'll need the same base path as the one that was set
						cookie.setMaxAge(0);	//tell the cookie to expire immediately
						response.addCookie(cookie);	//add the cookie to the response to delete it
					}
				}
			}
		}
			//add new cookies from the environment to the response
		for(final Map.Entry<String, Object> environmentPropertyEntry:environment.getProperties().entrySet())	//iterate the environment properties so that new cookies can be added as needed
		{
			final String environmentPropertyName=environmentPropertyEntry.getKey();	//get the name of the environment property value
			if(!GuiseEnvironment.SYSTEM_PROPERTIES.contains(environmentPropertyName))	//ignore system environment properties
			{
				if(!cookieMap.containsKey(environmentPropertyName))	//if no cookie contains this environment variable
				{
					final String environmentPropertyValue=asInstance(environmentPropertyEntry.getValue(), String.class);	//get the environment property value as a string
					if(environmentPropertyValue!=null)	//if there is a non-null environment property value
					{									
						final Cookie cookie=new Cookie(environmentPropertyName, encode(environmentPropertyValue));	//create a new cookie with the encoded property value
						cookie.setPath(applicationBasePath);	//set the cookie path to the application base path
						cookie.setMaxAge(Integer.MAX_VALUE);	//don't allow the cookie to expire for a very long time
						response.addCookie(cookie);	//add the cookie to the response
					}
				}
			}
		}		
	}

	/*TODO comment
	@param context Guise context information.
	*/
/*TODO del when works
	protected <T extends Component<?>> void getControlsByName(final GuiseContext context, final T component, final String name, final Set<Component<?>> componentSet)
	{
			//TODO check first that the component is a control; that should be much faster
		final Controller<? extends GuiseContext, ?> controller=component.getController();
		if(controller instanceof AbstractXHTMLControlController)
		{
			final AbstractXHTMLControlController xhtmlControlController=(AbstractXHTMLControlController)controller;
//TODO del Debug.trace("checking control with ID", xhtmlControlController.getAbsoluteUniqueID(component), "and name", xhtmlControlController.getComponentName((Control)component));
			if(name.equals(xhtmlControlController.getComponentName((Control)component)))	//TODO comment: the returned name can be null
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
		}
		else if(controller instanceof XHTMLTabbedPanelController)	//TODO fix hack; make XHTMLTabbedPanelController descend from XHTMLControlController
		{
			final XHTMLTabbedPanelController tabbedPanelController=(XHTMLTabbedPanelController)controller;
			if(name.equals(component.getID()))	//TODO fix hack
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
			
		}
		if(component instanceof CompositeComponent)
		{
			for(final Component<?> childComponent:(CompositeComponent<?>)component)
			{
				getControlsByName(context, childComponent, name, componentSet);
			}
		}
	}
*/

	/**Retrieves all components that have views needing updated within a session.
	This method checks all frames in the session.
	If a given component is dirty, its child views will not be checked.
	@param session The Guise session to check for dirty views.
	*/
	protected void getControlsByName(final GuiseSession session, final String name, final Set<Component<?>> componentSet)	//TODO comment; tidy
	{
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			getControlsByName(frame, name, componentSet);			
//TODO del			AbstractComponent.getDirtyComponents(frame, dirtyComponents);	//gather more dirty components
		}
	}

	/*TODO comment
	@param context Guise context information.
	*/
	protected <T extends Component<?>> void getControlsByName(/*TODO fixfinal GuiseContext context, */final T component, final String name, final Set<Component<?>> componentSet)
	{
			//TODO check first that the component is a control; that should be much faster
		final Controller<? extends GuiseContext, ?> controller=component.getController();
		if(controller instanceof AbstractXHTMLControlController)
		{
			final AbstractXHTMLControlController xhtmlControlController=(AbstractXHTMLControlController)controller;
//TODO del Debug.trace("checking control with ID", xhtmlControlController.getAbsoluteUniqueID(component), "and name", xhtmlControlController.getComponentName((Control)component));
			if(name.equals(xhtmlControlController.getComponentName((Control)component)))	//TODO comment: the returned name can be null
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
		}
		else if(controller instanceof XHTMLTabbedPanelController)	//TODO fix hack; make XHTMLTabbedPanelController descend from XHTMLControlController
		{
			final XHTMLTabbedPanelController tabbedPanelController=(XHTMLTabbedPanelController)controller;
			if(name.equals(component.getID()))	//TODO fix hack
			{
//TODO del Debug.trace("using this component");
				componentSet.add(component);
			}
			
		}
		if(component instanceof CompositeComponent)
		{
			for(final Component<?> childComponent:((CompositeComponent<?>)component).getChildren())
			{
				getControlsByName(childComponent, name, componentSet);
//TODO fix				getControlsByName(context, childComponent, name, componentSet);
			}
		}
	}

	/**Retrieves all components that have views needing updated within a session.
	This method checks all frames in the session.
	If a given component is dirty, its child views will not be checked.
	@param session The Guise session to check for dirty views.
	@return The components with views needing to be updated. 
	*/
	public static Collection<Component<?>> getDirtyComponents(final GuiseSession session)
	{
		final ArrayList<Component<?>> dirtyComponents=new ArrayList<Component<?>>();	//create a new list to hold dirty components
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			AbstractComponent.getDirtyComponents(frame, dirtyComponents);	//gather more dirty components
		}
		return dirtyComponents;	//return the dirty components we collected
	}

	/**Retrieves a component with the given ID.
	This method searches the hierarchies of all frames in the session.
	@param session The Guise session to check for the component.
	@return The component with the given ID, or <code>null</code> if no component with the given ID could be found on any of the given frames. 
	*/
	public static Component<?> getComponentByID(final GuiseSession session, final String id)
	{
		final Iterator<Frame<?>> frameIterator=session.getFrameIterator();	//get an iterator to session frames
		while(frameIterator.hasNext())	//while there are more frames
		{
			final Frame<?> frame=frameIterator.next();	//get the next frame
			final Component<?> component=AbstractComponent.getComponentByID(frame, id);	//try to find the component within this frame
			if(component!=null)	//if a component with the given ID was found
			{
				return component;	//return the component
			}
		}
		return null;	//indicate that no such component could be found
	}

	private final PathExpression AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION=new PathExpression("request", "events", "*");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_CONTROL_XPATH_EXPRESSION=new PathExpression("control");	//TODO use constants; comment 
//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_NAME_XPATH_EXPRESSION=new PathExpression("control", "name");	//TODO use constants; comment 
//TODO del	private final PathExpression AJAX_REQUEST_CONTROL_VALUE_XPATH_EXPRESSION=new PathExpression("control", "value");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_SOURCE_XPATH_EXPRESSION=new PathExpression("source");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_TARGET_XPATH_EXPRESSION=new PathExpression("target");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION=new PathExpression("component");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_MOUSE_XPATH_EXPRESSION=new PathExpression("mouse");	//TODO use constants; comment 
	private final PathExpression AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION=new PathExpression("viewport");	//TODO use constants; comment 
	
	/**Retrieves control events from the HTTP request.
  @param request The HTTP request.
	@param guiseSession The Guise session object.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected List<ControlEvent> getControlEvents(final HttpServletRequest request, final GuiseSession guiseSession) throws ServletException, IOException
	{
Debug.trace("getting control events");
		final List<ControlEvent> controlEventList=new ArrayList<ControlEvent>();	//create a new list for storing control events
		final String contentTypeString=request.getContentType();	//get the request content type
		final ContentType contentType=contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object from the request content type, if there is one
		if(contentType!=null && GUISE_AJAX_REQUEST_CONTENT_TYPE.match(contentType))	//if this is a Guise AJAX request
		{
//		TODO del Debug.trace("Guise AJAX request");
			try
			{

				
				
				
				final DocumentBuilderFactory documentBuilderFactory=DocumentBuilderFactory.newInstance();	//create a document builder factory TODO create a shared document builder factory, maybe---but make sure it is used by only one thread			
				final DocumentBuilder documentBuilder=documentBuilderFactory.newDocumentBuilder();	//create a new document builder
				final Document document=documentBuilder.parse(request.getInputStream());	//read the document from the request

/*TODO del if not needed; IE6 SSL bug now worked around
final Document document=getXML(request);
//TODO del Debug.trace("got document:", XMLUtilities.toString(document));
if(document==null)	//TODO fix; del
{
	
	Debug.traceStack("error: unable to get document");
	throw new AssertionError("unable to get document");
}
*/			
				
				final List<Node> eventNodes=(List<Node>)XPath.evaluatePathExpression(document, AJAX_REQUEST_EVENTS_WILDCARD_XPATH_EXPRESSION);	//get all the events
				for(final Node eventNode:eventNodes)	//for each event node
				{
					if(eventNode.getNodeType()==Node.ELEMENT_NODE)//if this is an event element
					{
						final Element eventElement=(Element)eventNode;	//cast the node to an element
						final String eventName=eventNode.getNodeName();	//get the event name
Debug.info("AJAX event:", eventName);
						if("form".equals(eventName))	//if this is a form event TODO use a constant
						{
							final boolean exhaustive=Boolean.valueOf(eventElement.getAttribute("exhaustive")).booleanValue();	//get the exhaustive indication TODO use a constant
							final boolean provisional=Boolean.valueOf(eventElement.getAttribute("provisional")).booleanValue();	//get the provisional indication TODO use a constant
							final FormControlEvent formSubmitEvent=new FormControlEvent(exhaustive, provisional);	//create a new form submission event
							final CollectionMap<String, Object, List<Object>> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
							final List<Node> controlNodes=(List<Node>)XPath.evaluatePathExpression(eventNode, AJAX_REQUEST_CONTROL_XPATH_EXPRESSION);	//get all the control settings
							for(final Node controlNode:controlNodes)	//for each control node
							{
								final Element controlElement=(Element)controlNode;	//get this control element
								final String controlName=controlElement.getAttribute("name");	//get the control name TODO use a constant
								if(controlName!=null && controlName.length()>0)	//if this control has a name
								{
									final String controlValue=controlElement.getTextContent();	//get the control value
									parameterListMap.addItem(controlName, controlValue);	//store the value in the parameters
								}
							}
							controlEventList.add(formSubmitEvent);	//add the event to the list
						}
						else if("action".equals(eventName))	//if this is an action event TODO use a constant
						{
							final String componentID=eventElement.getAttribute("componentID");	//get the ID of the component TODO use a constant
							if(componentID!=null)	//if there is a component ID TODO add better event handling, to throw an error and send back that error
							{
								final String targetID=eventElement.getAttribute("targetID");	//get the ID of the target element TODO use a constant
								final String actionID=eventElement.getAttribute("actionID");	//get the action identifier TODO use a constant								
								final int option=Integer.parseInt(eventElement.getAttribute("option"));	//TODO tidy; improve; check for errors; comment
								final ActionControlEvent actionControlEvent=new ActionControlEvent(componentID, targetID, actionID, option);	//create a new action control event
								controlEventList.add(actionControlEvent);	//add the event to the list
							}
						}
						else if("drop".equals(eventNode.getNodeName()))	//if this is a drop event TODO use a constant
						{
							final Node sourceNode=XPath.getNode(eventNode, AJAX_REQUEST_SOURCE_XPATH_EXPRESSION);	//get the source node
							final String dragSourceID=((Element)sourceNode).getAttribute("id");	//TODO tidy; improve; comment
							final Node targetNode=XPath.getNode(eventNode, AJAX_REQUEST_TARGET_XPATH_EXPRESSION);	//get the target node
							final String dropTargetID=((Element)targetNode).getAttribute("id");	//TODO tidy; improve; comment
							final DropControlEvent dropEvent=new DropControlEvent(dragSourceID, dropTargetID);	//create a new drop event
							controlEventList.add(dropEvent);	//add the event to the list
						}
						else if("mouseEnter".equals(eventName) || "mouseExit".equals(eventName))	//if this is a mouse event TODO use a constant
						{
							try
							{
								final MouseControlEvent.EventType eventType=MouseControlEvent.EventType.valueOf(eventNode.getNodeName().toUpperCase());	//get the event type from the type string

								final Node componentNode=XPath.getNode(eventNode, AJAX_REQUEST_COMPONENT_XPATH_EXPRESSION);	//get the component node
								final String componentID=((Element)componentNode).getAttribute("id");	//TODO tidy; improve; comment
								final int componentX=Integer.parseInt(((Element)componentNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int componentY=Integer.parseInt(((Element)componentNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int componentWidth=Integer.parseInt(((Element)componentNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int componentHeight=Integer.parseInt(((Element)componentNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment

								final Node targetNode=XPath.getNode(eventNode, AJAX_REQUEST_TARGET_XPATH_EXPRESSION);	//get the target node
								final String targetID=((Element)targetNode).getAttribute("id");	//TODO tidy; improve; comment
								final int targetX=Integer.parseInt(((Element)targetNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int targetY=Integer.parseInt(((Element)targetNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int targetWidth=Integer.parseInt(((Element)targetNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int targetHeight=Integer.parseInt(((Element)targetNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment
								
								final Node viewportNode=XPath.getNode(eventNode, AJAX_REQUEST_VIEWPORT_XPATH_EXPRESSION);	//get the viewport node
								final int viewportX=Integer.parseInt(((Element)viewportNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int viewportY=Integer.parseInt(((Element)viewportNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment
								final int viewportWidth=Integer.parseInt(((Element)viewportNode).getAttribute("width"));	//TODO tidy; improve; check for errors; comment
								final int viewportHeight=Integer.parseInt(((Element)viewportNode).getAttribute("height"));	//TODO tidy; improve; check for errors; comment

								final Node mouseNode=XPath.getNode(eventNode, AJAX_REQUEST_MOUSE_XPATH_EXPRESSION);	//get the mouse node
								final int mouseX=Integer.parseInt(((Element)mouseNode).getAttribute("x"));	//TODO tidy; improve; check for errors; comment
								final int mouseY=Integer.parseInt(((Element)mouseNode).getAttribute("y"));	//TODO tidy; improve; check for errors; comment

								if(componentID!=null)	//if there is a component ID TODO add better event handling, to throw an error and send back that error
								{
									final MouseControlEvent mouseEvent=new MouseControlEvent(eventType, componentID, new Rectangle(componentX, componentY, componentWidth, componentHeight),
											targetID, new Rectangle(targetX, targetY, targetWidth, targetHeight), new Rectangle(viewportX, viewportY, viewportWidth, viewportHeight),
											new Point(mouseX, mouseY));	//create a new mouse event
									controlEventList.add(mouseEvent);	//add the event to the list
//TODO del Debug.trace("mouse event; targetXY:", targetX, targetY, "viewportXY:", viewportX, viewportY, "mouseXY:", mouseX, mouseY);
								}
							}
							catch(final IllegalArgumentException illegalArgumentException)	//if we don't ignore the event type, don't create an event
							{								
							}
						}
						else if("init".equals(eventNode.getNodeName()))	//if this is an initialization event TODO use a constant
						{
							final String hour=eventElement.getAttribute("hour");
							final String timezone=eventElement.getAttribute("timezone");
							final String language=eventElement.getAttribute("language");
							final String colorDepth=eventElement.getAttribute("colorDepth");
							final String screenWidth=eventElement.getAttribute("screenWidth");
							final String screenHeight=eventElement.getAttribute("screenHeight");
							final String browserWidth=eventElement.getAttribute("browserWidth");
							final String browserHeight=eventElement.getAttribute("browserHeight");
							final String javascriptVersion=eventElement.getAttribute("javascriptVersion");	//get the JavaScript version TODO use a constant
							final String javaEnabled=eventElement.getAttribute("javaEnabled");
							final String referrer=eventElement.getAttribute("referrer");
							URI referrerURI=null;	//assume we can't get a referrer URI
							if(referrer.length()>0)	//if there is a referrer
							{
								try
								{
									referrerURI=new URI(referrer);	//create a URI object from the referrer string
								}
								catch(final URISyntaxException uriSyntaxException)	//if there is a problem with the URI syntax
								{
									Debug.warn("Invalid referrer URI syntax: "+referrer);
								}
							}
							final InitControlEvent initEvent=new InitControlEvent(
									hour.length()>0 ? Integer.parseInt(hour) : 0, timezone.length()>0 ? Integer.parseInt(timezone) : 0, language.length()>0 ? language : "en-US",
									colorDepth.length()>0 ? Integer.parseInt(colorDepth) : 24, screenWidth.length()>0 ? Integer.parseInt(screenWidth) : 1024, screenHeight.length()>0 ? Integer.parseInt(screenHeight) : 768,
									browserWidth.length()>0 ? Integer.parseInt(browserWidth) : 1024, browserHeight.length()>0 ? Integer.parseInt(browserHeight) : 768,
									javascriptVersion.length()>0 ? javascriptVersion : null, javaEnabled.length()>0 ? Boolean.valueOf(javaEnabled) : false,
											referrerURI);	//create a new initialization event TODO check for NumberFormatException
							controlEventList.add(initEvent);	//add the event to the list
						}
					}
				}
			}
			catch(final ParserConfigurationException parserConfigurationException)	//we don't expect parser configuration errors
			{
				throw new AssertionError(parserConfigurationException);
			}
			catch(final SAXException saxException)	//we don't expect parsing errors
			{
				throw new AssertionError(saxException);	//TODO maybe change to throwing an IOException
			}
			catch(final IOException ioException)	//if there is an I/O exception
			{
				throw new AssertionError(ioException);	//TODO fix better
			}
		}
		else	//if this is not a Guise AJAX request
		{
				//populate our parameter map
			if(FileUpload.isMultipartContent(request))	//if this is multipart/form-data content
			{
				final FormControlEvent formSubmitEvent=new FormControlEvent(true);	//create a new form submission event, indicating that the event is exhaustive
				final CollectionMap<String, Object, List<Object>> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
				final DiskFileUpload diskFileUpload=new DiskFileUpload();	//create a file upload handler
				diskFileUpload.setSizeMax(-1);	//don't reject anything
				try	//try to parse the file items submitted in the request
				{
					final List fileItems=diskFileUpload.parseRequest(request);	//parse the request
					for(final Object object:fileItems)	//look at each file item
					{
						final FileItem fileItem=(FileItem)object;	//cast the object to a file item
						final String parameterKey=fileItem.getFieldName();	//the parameter key will always be the field name
						final Object parameterValue=fileItem.isFormField() ? fileItem.getString() : new FileItemResourceImport(fileItem);	//if this is a form field, store it normally; otherwise, create a file item resource import object
						parameterListMap.addItem(parameterKey, parameterValue);	//store the value in the parameters
					}
				}
				catch(final FileUploadException fileUploadException)	//if there was an error parsing the files
				{
					throw new IllegalArgumentException("Couldn't parse multipart/form-data request.");
				}
				controlEventList.add(formSubmitEvent);	//add the event to the list
			}
			else	//if this is normal application/x-www-form-urlencoded data
			{
				final boolean exhaustive=POST_METHOD.equals(request.getMethod());	//if this is an HTTP post, the form event is exhaustive of all controls on the form
				if(!exhaustive || request.getParameter(XHTMLApplicationFrameView.getActionInputID(guiseSession.getApplicationFrame()))!=null)	//if this is a POST, only use the data if it is a Guise POST
				{				
					final FormControlEvent formSubmitEvent=new FormControlEvent(exhaustive);	//create a new form submission event
					final CollectionMap<String, Object, List<Object>> parameterListMap=formSubmitEvent.getParameterListMap();	//get the map of parameter lists
					final Iterator parameterEntryIterator=request.getParameterMap().entrySet().iterator();	//get an iterator to the parameter entries
					while(parameterEntryIterator.hasNext())	//while there are more parameter entries
					{
						final Map.Entry parameterEntry=(Map.Entry)parameterEntryIterator.next();	//get the next parameter entry
						final String parameterKey=(String)parameterEntry.getKey();	//get the parameter key
						final String[] parameterValues=(String[])parameterEntry.getValue();	//get the parameter values
						final List<Object> parameterValueList=new ArrayList<Object>(parameterValues.length);	//create a list to hold the parameters
						addAll(parameterValueList, parameterValues);	//add all the parameter values to our list
						parameterListMap.put(parameterKey, parameterValueList);	//store the the array of values as a list, keyed to the value
					}
					controlEventList.add(formSubmitEvent);	//add the event to the list
				}
			}
		}
		if(controlEventList.size()>0 && Debug.isDebug() && Debug.getReportLevels().contains(Debug.ReportLevel.INFO))	//indicate the parameters if information tracing is turned on
		{
			Debug.info("Received Control Events:");
			for(final ControlEvent controlEvent:controlEventList)	//for each control event
			{
				Debug.info("  Event:", controlEvent.getClass(), controlEvent);				
			}
		}

/*TODO del
Debug.trace("parameter names:", request.getParameterNames());	//TODO del when finished with dual mulipart+encoded content
Debug.trace("number of parameter names:", request.getParameterNames());
Debug.trace("***********number of distinct parameter keys", parameterListMap.size());
*/
		return controlEventList;	//return the list of control events
	}

	/**Begins modal navigation based upon modal navigation information.
	@param guiseApplication The Guise application.
	@param guiseSession The Guise session.
	@param modalNavigation The modal navigation information
	*/
	protected void beginModalNavigation(final GuiseApplication guiseApplication, final GuiseSession guiseSession, final ModalNavigation modalNavigation)
	{
		final String navigationPath=guiseApplication.relativizeURI(modalNavigation.getNewNavigationURI());	//get the navigation path for the modal navigation
		final Destination destination=guiseApplication.getDestination(navigationPath);	//get the destination for this path TODO maybe add a GuiseSession.getDestination()
		if(destination instanceof ComponentDestination)	//if the destination is a component destination
		{
			final ComponentDestination componentDestination=(ComponentDestination)destination;	//get the destination as a component destination
			final Component<?> destinationComponent=guiseSession.getDestinationComponent(componentDestination);	//get the component for this destination
			if(destinationComponent instanceof ModalNavigationPanel)	//if the component is a modal navigatoin panel, as we expect
			{
				final ModalNavigationPanel<?, ?> modalPanel=(ModalNavigationPanel<?, ?>)destinationComponent;	//get the destination component as a modal panel
				guiseSession.beginModalNavigation(modalPanel, modalNavigation);	//begin modal navigation with the panel
			}
		}
	}

	/**Called by the servlet container to indicate to a servlet that the servlet is being taken out of service.
	This version uninstalls the Guise application from the Guise container.
	*/
	public void destroy()
	{
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		if(guiseApplication.getContainer()!=null)	//if the Guise application is installed
		{
			getGuiseContainer().uninstallApplication(guiseApplication);	//uninstall the application
		}
		super.destroy();	//do the default destroying
	}

	/**Looks up a principal from the given ID.
	This version delegates to the Guise container.
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	@exception HTTPInternalServerErrorException if there is an error getting the principal.
	*/
	protected Principal getPrincipal(final String id) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getPrincipal(getGuiseApplication(), id);	//delegate to the container
	}

	/**Looks up the corresponding password for the given principal.
	This version delegates to the Guise container.
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	@exception HTTPInternalServerErrorException if there is an error getting the principal's password.
	*/
	protected char[] getPassword(final Principal principal) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getPassword(getGuiseApplication(), principal);	//delegate to the container
	}

	/**Determines the realm applicable for the resource indicated by the given URI.
	This version delegates to the container.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@exception HTTPInternalServerErrorException if there is an error getting the realm.
	*/
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException
	{
		return getGuiseContainer().getRealm(getGuiseApplication(), resourceURI);	//delegate to the container
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version delegates to the Guise container, using the principal of the current Guise session instead of the given principal.
	This technique allows browser-based authentication to function normally (as successful authentication will have already updated the session's principal),
	and also allows browser-based authentication to work with session-based authentication in the even that the session has already authenticated a principal unknown to the browser.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	*/
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm) throws HTTPInternalServerErrorException
	{
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
//TODO del; we don't want to force a session here, in case this is a non-Guise resource		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request
		return getGuiseContainer().isAuthorized(guiseApplication, resourceURI, principal, realm);	//delegate to the container, using the current Guise session
	}

	/**Determines if the given nonce is valid.
	This version counts a nonce as invalid if it was associated with a different principal than the current Guise session principal (i.e. the Guise principal was logged out).
  @param request The HTTP request.
	@param nonce The nonce to check for validity.
	@return <code>true</code> if the nonce is not valid.
	*/
	protected boolean isValid(final HttpServletRequest request, final Nonce nonce)	//TODO check to see if we want to force a session
	{
//	TODO del Debug.trace("ready to check validity of nonce; default validity", nonce);
		if(!super.isValid(request, nonce))	//if the nonce doesn't pass the normal validity checks
		{
//		TODO del Debug.trace("doesn't pass the basic checks");
			return false;	//the nonce isn't valid
		}
		final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
		final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
		final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request
		final Principal guiseSessionPrincipal=guiseSession.getPrincipal();	//get the current principal of the Guise session
		final String guiseSessionPrincipalID=guiseSessionPrincipal!=null ? guiseSessionPrincipal.getName() : null;	//get the current guise session principal ID
//	TODO del Debug.trace("checking to see if nonce principal ID", getNoncePrincipalID(nonce), "matches Guise session principal ID", guiseSessionPrincipalID); 
		if(!ObjectUtilities.equals(getNoncePrincipalID(nonce), guiseSessionPrincipalID))	//if this nonce was for a different principal
		{
			return false;	//the user must have logged out or have been changed
		}
//	TODO del Debug.trace("nonce is valid");
		return true;	//indicate that the nonce passed all the tests
	}

	/**Called when a principal has went through authentication and indicates the result of authentication.
	This version stores the authenticated principal in the current Guise session if authentication was successful for valid credentials.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm for which the principal was authenticated.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@param authenticated <code>true</code> if the principal succeeded in authentication, else <code>false</code>.
	@see GuiseSession#setPrincipal(Principal)
	*/
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm, final AuthenticateCredentials credentials, final boolean authenticated)
	{
		if(authenticated && credentials!=null)	//if authentication was successful with credentials (don't change the session principal for no credentials, because this might remove a principal set by the session itself with no knowledge of the browser)
		{
			final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
			final AbstractGuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
			final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request
			guiseSession.setPrincipal(principal);	//set the new principal in the Guise session
		}
	}

	/**Extracts the bookmark contained in the given request URL.
	@param request The HTTP request object.
	@return The bookmark represented by the request, or <code>null</code> if no bookmark is contained in the request.
	*/
	protected static Bookmark getBookmark(final HttpServletRequest request)
	{
		final String queryString=request.getQueryString();	//get the query string from the request
		if(queryString!=null && queryString.length()>0)	//if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null)
		{
//TODO del Debug.trace("just got query string from request, length", queryString.length(), "content", queryString);
			return new Bookmark(String.valueOf(QUERY_SEPARATOR)+queryString);	//construct a new bookmark, preceding the string with a query indicator
		}
		else	//if there is no query string, there is no bookmark
		{
//TODO del Debug.trace("just got null query string from request");
			return null;	//indicate that there is no bookmark information
		}
	}	

  /**Determines if the resource at a given URI exists.
  This version adds checks to see if the URI represents a valid application navigation path.
  This version adds support for Guise public resources.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see GuiseApplication#hasDestination(String)
	@see #isGuisePublicResourceURI(URI)
  */
  protected boolean exists(final URI resourceURI) throws IOException
  {
//TODO del Debug.trace("checking exists", resourceURI);
  		//see if this is a Guise public resource
  	if(isGuisePublicResourceURI(resourceURI))	//if this URI represents a Guise public resource
  	{
  		final String publicResourceKey=getGuisePublicResourceKey(resourceURI);	//get the Guise public resource key
  		return Guise.getInstance().hasGuisePublicResourceURL(publicResourceKey);	//see if there is a public resource for this key
  	}
  	final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
  	final String path=guiseApplication.relativizeURI(resourceURI);	//get the application-relative path
  		//check for a temporary public resource
  	if(guiseApplication.hasTempPublicResource(path))	//if the URI represents a valid temporary public resource
  	{
  		return true;	//the resource exists
  	}
  	if(guiseApplication.hasDestination(path))	//if the URI represents a valid navigation path
  	{
  		return true;	//the navigation path exists
  	}
 		return super.exists(resourceURI);	//see if a physical resource exists at the location, if we can't find a virtual resource (a Guise public resource or a navigation path component)
  }

  /**The thread-safe map of references to cached stylesheets fixed for IE6.*/ 
  private final Map<URI, Reference<HTTPServletResource>> cachedIE6FixedStylesheetResources=new ConcurrentHashMap<URI, Reference<HTTPServletResource>>();

	/**Determines the requested resource.
  This version adds support for Guise public resources.
	@param request The HTTP request in response to which the resource is being retrieved.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource, but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
	@see #isGuisePublicResourceURI(URI)
	@see #getGuisePublicResourceKey(URI)
	@see Guise#getGuisePublicResourceURL(String)
	@see GuiseApplication#hasTempPublicResource(String)
	@see GuiseApplication#getInputStream(String)
  */
	protected HTTPServletResource getResource(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException
	{
//TODO del Debug.trace("getting resource for URI: ", resourceURI);
		final HTTPServletResource resource;	//we'll create a resource for this resource URI
  	if(isGuisePublicResourceURI(resourceURI))	//if this URI represents a Guise public resource
  	{
  		final String publicResourceKey=getGuisePublicResourceKey(resourceURI);	//get the Guise public resource key
//  	TODO del Debug.trace("this is a public resource with key: ", publicResourceKey);
			final URL publicResourceURL=Guise.getInstance().getGuisePublicResourceURL(publicResourceKey);	//get a URL to the resource
//		TODO del Debug.trace("found URL to resource: ", publicResourceURL);
			if(publicResourceURL==null)	//if there is no such resource
			{
				throw new HTTPNotFoundException("No such Guise public resource: "+resourceURI);
			}
			resource=new DefaultHTTPServletResource(resourceURI, publicResourceURL);	//create a new default resource with a URL to the public resource
//		TODO del Debug.trace("constructed a resource with length:", resource.getContentLength(), "and last modified", resource.getLastModified());
  	}
  	else	//if this is not a Guise public resource, see if it is a temporary public resource of the application
  	{
  		final GuiseApplication guiseApplication=getGuiseApplication();	//get the Guise application
			final String applicationBasePath=guiseApplication.getBasePath();	//get the application base path
  		final String rawPath=resourceURI.getRawPath();	//get the raw path of the requested resource
			final String relativePath=rawPath!=null ? relativizePath(applicationBasePath, rawPath) : null;	//relativize the raw path to the base path
			final String tempRelativePath=relativePath!=null && relativePath.startsWith(GuiseApplication.GUISE_PUBLIC_TEMP_BASE_PATH) ? relativePath : null;	//see if this is a path to a Guise temporary public resource
			if(tempRelativePath!=null)	//if this is a path to a temporary public file
			{
				final HTTPServletGuiseContainer guiseContainer=getGuiseContainer();	//get the Guise container
				final GuiseSession guiseSession=HTTPGuiseSessionManager.getGuiseSession(guiseContainer, guiseApplication, request);	//retrieve the Guise session for this container and request TODO see if we can request a session without forcing one to be created, if that's useful
				final URL tempResourceURL;	//we'll store here the URL to the temporary resource
				try
				{
					tempResourceURL=guiseApplication.getTempPublicResourceURL(relativePath, guiseSession);	//get a URL to the temporary resource, if there is one
					if(tempResourceURL==null)	//if there is no such resource
					{
						throw new HTTPNotFoundException("No such Guise temporary resource: "+resourceURI);
					}
				}
				catch(final IllegalStateException illegalStateException)	//if we cannot access the resource from the current session  
				{
					throw new HTTPForbiddenException(illegalStateException.getMessage(), illegalStateException);	//forbid the user from accessing the resource TODO should the Throwable constructor only use Throwable.getMessage() instead of the Throwable.toString()
				}
				resource=new DefaultHTTPServletResource(resourceURI, tempResourceURL);	//create a new default resource with a URL to the temporary resource
			}
	  	else	//if this is not a Guise public resource or a temporary resource, access the resource normally
	  	{
	  		resource=super.getResource(request, resourceURI);	//return a default resource
	  	}
  	}
		final ContentType contentType=getContentType(resource);	//get the content type of the resource
//TODO del Debug.trace("got content type", contentType, "for resource", resource);
		if(contentType!=null && TEXT_CSS_CONTENT_TYPE.match(contentType))	//if this is a CSS stylesheet
		{
			final Map<String, Object> userAgentProperties=getUserAgentProperties(request);	//get the user agent properties for this request
			if(USER_AGENT_NAME_MSIE.equals(userAgentProperties.get(USER_AGENT_NAME_PROPERTY)))	//if this is IE
			{
				final Object version=userAgentProperties.get(USER_AGENT_VERSION_NUMBER_PROPERTY);	//get the version number
				if(version instanceof Float && ((Float)version).floatValue()<7.0f)	//if this is IE 6 (lower than IE 7)
				{
//TODO del Debug.trace("need a stylesheet for IE6");
					final Reference<HTTPServletResource> ie6CSSResourceReference=cachedIE6FixedStylesheetResources.get(resourceURI);	//get a reference to the IE6 fixed stylesheet, if there is one cached
					HTTPServletResource ie6CSSResource=ie6CSSResourceReference!=null ? ie6CSSResourceReference.get() : null;	//dereference the reference if there is one
					if(ie6CSSResource==null)	//we don't have the resource in the cache (the race condition here is benign, and could only result in an initual multiple-loading of a stylesheet)
					{
//TODO del Debug.trace("IE6 stylesheet wasn't cached");
						ie6CSSResource=new IE6CSSResource(resource);	//create a resource that does extra CSS processing for IE6
						cachedIE6FixedStylesheetResources.put(resourceURI, new SoftReference<HTTPServletResource>(ie6CSSResource));	//cache the processed stylesheet for later
					}
					else
					{
//TODO del Debug.trace("IE6 stylesheet was cached");						
					}
					return ie6CSSResource;	//use the cached IE6 CSS resource so we won't have to process it all over again
//TODO del								Debug.trace("fixed stylesheet for IE6", cssStylesheet);
				}
			}
		}
		return resource;	//return the resource without extra processing
	}

	/**A resource that represents a CSS file, decorating an existing resource.
	This version compresses resources of type <code>text/css</code>.
	@author Garret Wilson
	*/
	protected static class CSSResource extends AbstractByteCacheDecoratorResource
	{

		/**Loads a CSS stylesheet from the requested resource.
		@param request The HTTP request in response to which the bytes are being retrieved.
		@param cssProcessor The processor to use in processing the stylesheet.
		@return A stylesheet object representing the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected CSSStylesheet loadStylesheet(final HttpServletRequest request, final GuiseCSSProcessor cssProcessor) throws IOException
		{
			final InputStream inputStream=getResource().getInputStream(request);	//get an input stream to the resource
			try
			{
				final ParseReader cssReader=new ParseReader(new InputStreamReader(inputStream, UTF_8));
				final CSSStylesheet cssStylesheet=cssProcessor.process(cssReader);	//parse the stylesheet
				return cssStylesheet;	//return the stylesheet
			}
			finally
			{
				inputStream.close();	//always close the original input stream
			}
		}

		/**Loads bytes from the requested resource.
		@param request The HTTP request in response to which the bytes are being retrieved.
		@return The bytes that constitute the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected byte[] loadBytes(final HttpServletRequest request) throws IOException
		{
			return loadStylesheet(request, new GuiseCSSProcessor()).toString().getBytes(UTF_8);	//load the stylesheet and return its bytes
		}		

		/**HTTP servlet resource constructor.
		@param resource The decorated HTTP servlet resource.
		@exception IllegalArgumentException if the given resource is <code>null</code>.
		*/
		public CSSResource(final HTTPServletResource resource)
		{
			super(resource);	//construct the parent class
		}
	}

	/**A resource that represents an IE6 CSS file, decorating an existing resource.
	This version processes resources of type <code>text/css</code> to work around IE6 bugs, if IE6 is the user agent.	
	@author Garret Wilson
	*/
	protected static class IE6CSSResource extends CSSResource
	{

		/**Loads a CSS stylesheet from the requested resource.
		@param request The HTTP request in response to which the bytes are being retrieved.
		@param cssProcessor The processor to use in processing the stylesheet.
		@return A stylesheet object representing the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected CSSStylesheet loadStylesheet(final HttpServletRequest request, final GuiseCSSProcessor cssProcessor) throws IOException
		{
			final CSSStylesheet cssStylesheet=super.loadStylesheet(request, cssProcessor);	//load and process the stylesheet normally
			cssProcessor.fixIE6Stylesheet(cssStylesheet);	//fix this stylesheet for IE6
			return cssStylesheet;	//return the fixed stylesheet
		}

		/**HTTP servlet resource constructor.
		@param resource The decorated HTTP servlet resource.
		@exception IllegalArgumentException if the given resource is <code>null</code>.
		*/
		public IE6CSSResource(final HTTPServletResource resource)
		{
			super(resource);	//construct the parent class
		}
	}
	
	/**Determines whether the given URI references a Guise public resource.
	The URI references a public resource if the path, relative to the application base path, begins with {@value GuiseApplication#GUISE_PUBLIC_RESOURCE_BASE_PATH}.
	@param uri The reference URI, which is assumed to have this servlet's domain.
	@return <code>true</code> if the given URI references a Guise public resource.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public boolean isGuisePublicResourceURI(final URI uri)
	{
		final String rawPath=uri.getRawPath();	//get the raw path of the URI
		if(rawPath!=null)	//if there is a raw path
		{
//		TODO del 	Debug.trace("is public resource URI?", uri);
			final String applicationBasePath=getGuiseApplication().getBasePath();	//get the application base path
//		TODO del Debug.trace("application base path", applicationBasePath);
			final String relativePath=relativizePath(applicationBasePath, rawPath);	//relativize the raw path to the base path
//		TODO del Debug.trace("relativePath", relativePath);
			return relativePath.startsWith(GuiseApplication.GUISE_PUBLIC_RESOURCE_BASE_PATH);	//see if the relative path starts with the Guise public resource base path
		}
		else	//if there is no raw path
		{
			return false;	//this is not a public resource URI
		}
	}

	/**Determines the Guise public resource key for the given URI.
	The path of the given URI, relative to the application base path, must begin with {@value GuiseApplication#GUISE_PUBLIC_RESOURCE_BASE_PATH}.
	This path prefix will be replaced with {@value Guise#GUISE_PUBLIC_RESOURCE_BASE_KEY}.
	@param uri The URI of the public resource, which is assumed to have this servlet's domain.
	@return The key to a Guise public resource.
	@exception IllegalArgumentException if the raw path of the URI is <code>null</code> or does not start with {@value GuiseApplication#GUISE_PUBLIC_RESOURCE_BASE_PATH}.
	@see Guise#hasGuisePublicResourceURL(String)
	@see Guise#getGuisePublicResource(String)
	@see Guise#getGuisePublicResourceInputStream(String)
	@see Guise#getGuisePublicResourceURL(String)
	*/
	public String getGuisePublicResourceKey(final URI uri)
	{
		final String rawPath=uri.getRawPath();	//get the raw path of the URI
		if(rawPath==null)	//if the raw path is null
		{
			throw new IllegalArgumentException("Guise public resource URI "+uri+" has no path.");
		}
		final String applicationBasePath=getGuiseApplication().getBasePath();	//get the application base path
		final String relativePath=relativizePath(applicationBasePath, rawPath);	//relativize the raw path to the base path
		if(!relativePath.startsWith(GuiseApplication.GUISE_PUBLIC_RESOURCE_BASE_PATH))	//if this isn't a public resource URI
		{
			throw new IllegalArgumentException("URI "+uri+ " does not identify a Guise public resource.");
		}
		return GUISE_PUBLIC_RESOURCE_BASE_KEY+relativePath.substring(GuiseApplication.GUISE_PUBLIC_RESOURCE_BASE_PATH.length());	//replace the beginning of the relative path with the resource base path
	}
}
