/*
 * Copyright © 2005-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.framework.theme;

import java.net.URI;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.util.Objects.*;

import com.globalmentor.collections.CollectionMap;
import com.globalmentor.collections.HashSetHashMap;
import com.globalmentor.net.MediaType;
import com.globalmentor.util.*;

import io.guise.framework.style.*;
import io.urf.model.UrfResourceDescription;

import static com.globalmentor.java.Classes.*;
import static io.guise.framework.Resources.*;

/**
 * Guise theme specification.
 * @author Garret Wilson
 */
public class Theme extends ArrayList<Rule> /*TODO delete legacy URF extends URFListResource<Rule>*/ {

	private static final long serialVersionUID = 194898499290026009L;

	/** The extension for Guise theme resource names. */
	public static final String FILENAME_EXTENSION = "guisetheme";
	/** The media type for theme resources: <code>application/theme+turf</code>. */
	public static final MediaType MEDIA_TYPE = MediaType.of(MediaType.APPLICATION_PRIMARY_TYPE, "theme" + MediaType.SUBTYPE_SUFFIX_DELIMITER_CHAR + "turf"); //TODO TODO use constant for new TURF: TURF.SUBTYPE_SUFFIX

	/** The recommended prefix to the theme ontology namespace. */
	public static final String THEME_NAMESPACE_PREFIX = "theme";
	/** The URI to the theme ontology namespace. */
	public static final URI THEME_NAMESPACE_URI = URI.create("https://guise.io/framework/namespaces/theme/");

	//classes
	/** The URI of the template class. */
	//TODO fix for new URF: public static final URI TEMPLATE_CLASS_URI = createResourceURI(THEME_NAMESPACE_URI, getLocalName(Template.class));

	//properties
	/** The apply property name. */
	//TODO fix for new URF: public static final URI APPLY_PROPERTY_URI = createResourceURI(THEME_NAMESPACE_URI, "apply");
	/** The property for the URI of the theme's parent. */
	public static final URI PARENT_URI_PROPERTY_URI = THEME_NAMESPACE_URI.resolve("parentURI");
	/** The resources property name. */
	public static final URI RESOURCES_PROPERTY_URI = THEME_NAMESPACE_URI.resolve("resources+");

	/** The theme description. */
	private final UrfResourceDescription description; //TODO improve; temporary hack to get legacy Guise working

	/** @return The theme description. */
	public UrfResourceDescription getDescription() {
		return description;
	}

	/** The theme parent, or <code>null</code> if there is no resolving parent. */
	private Theme parent = null;

	/** @return The theme parent, or <code>null</code> if there is no resolving parent. */
	public Theme getParent() {
		return parent;
	}

	/**
	 * Sets the theme parent.
	 * @param newParent The new theme parent, or <code>null</code> if there should be no resolving parent.
	 */
	public void setParent(final Theme newParent) {
		parent = newParent;
	} //TODO maybe remove and create custom ThemeIO

	/** The map of sets of rules that have selectors selecting classes. */
	private final CollectionMap<Class<?>, Rule, Set<Rule>> classRuleMap = new HashSetHashMap<Class<?>, Rule>(); //TODO make this store a sorted set, and use a comparator based on order

	/**
	 * Retrieves the set of rules that selects the class of the given object, including parent classes. It is not guaranteed that the object will match all or any
	 * of the returned rules; only that the object's class is used as part of the selections of the returned rules.
	 * @param object The object for which class-selected rules should be returned.
	 * @return A set of all rules that reference a class that selects the given object's class.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 */
	public Set<Rule> getClassRules(final Object object) {
		final Class<?> objectClass = requireNonNull(object, "Object cannot be null").getClass(); //get the object's class
		Set<Rule> combinedRuleSet = null; //we'll create the rule set only if needed
		final List<Class<?>> ancestorClasses = getAncestorClasses(objectClass); //get the class ancestor hierarchy of this class TODO cache these
		for(final Class<?> ancestorClass : ancestorClasses) { //for each ancestor class TODO iterate the list in the correct order; send back the rules in the correct order
			final Set<Rule> ruleSet = classRuleMap.get(ancestorClass); //try to get a rule for the object's ancestor class
			if(ruleSet != null) { //if we found a rule set
				if(combinedRuleSet == null) { //if we haven't yet created the combined rule set
					combinedRuleSet = new HashSet<Rule>(); //create a new hash set
				}
				combinedRuleSet.addAll(ruleSet); //add all the rules for the ancestor class to the combined rule set
			}
		}
		return combinedRuleSet != null ? combinedRuleSet : Collections.<Rule>emptySet(); //return the combined set of rules we've found (Java won't allow emptySet() to be used in this context, but a warning here is better than alternate, less-efficient methods)
	}

	//TODO delete legacy URF
	//	/** Default constructor. */
	/*TODO delete legacy URF
	public Theme() {
		this((URI)null); //construct the class with no reference URI
	}
	*/

	//TODO delete legacy URF
	//	/**
	//	 * Reference URI constructor.
	//	 * @param referenceURI The reference URI for the new resource.
	//	 */
	/*TODO delete legacy URF
	public Theme(final URI referenceURI) {
		super(referenceURI, createResourceURI(THEME_NAMESPACE_URI, getLocalName(Theme.class))); //construct the parent class, using a type based upon the name of this class
	}
	 */
	//TODO delete legacy URF
	//	/**
	//	 * Collection constructor with no URI. The elements of the specified collection will be added to this list in the order they are returned by the collection's
	//	 * iterator.
	//	 * @param collection The collection whose elements are to be placed into this list.
	//	 * @throws NullPointerException if the specified collection is <code>null</code>.
	//	 */
	/*TODO delete legacy URF
	public Theme(final Collection<? extends Rule> collection) {
		this(null, collection); //construct the class with no URI
	}
	 */
	//TODO delete legacy URF
	//	/**
	//	 * URI and collection constructor. The elements of the specified collection will be added to this list in the order they are returned by the collection's
	//	 * iterator.
	//	 * @param uri The URI for the resource, or <code>null</code> if the resource should have no URI.
	//	 * @param collection The collection whose elements are to be placed into this list.
	//	 * @throws NullPointerException if the specified collection is <code>null</code>.
	//	 */
	/*TODO delete legacy URF
	public Theme(final URI uri, final Collection<? extends Rule> collection) {
		this(uri); //construct the class with the URI
		addAll(collection); //add all the collection elements to the list
	}
	*/

	/** The URI of the theme. */
	private final URI uri; //TODO improve; this is a short-term hack to allow loading relative paths from legacy Guise themes

	/** @return The URI of the theme. */
	public URI getURI() {
		return uri;
	}

	/**
	 * URI and description constructor.
	 * @param uri The URI of the theme.
	 * @param description The theme description, loaded from a legacy Guise theme file.
	 */
	public Theme(final URI uri, final UrfResourceDescription description) {
		this.uri = requireNonNull(uri);
		this.description = requireNonNull(description);
	}

	/**
	 * Parent theme and description constructor.
	 * @param parent The theme to serve as the parent of this theme, or <code>null</code> if this theme should have no parent.
	 * @param description The theme description, loaded from a legacy Guise theme file.
	 */
	/*TODO determine if needed
	public Theme(final Theme parent, final UrfResourceDescription description) {
		this.parent = parent; //save the parent theme
		this.description = requireNonNull(description);
	}
	*/

	/**
	 * Retrieves the URI indicating the parent theme.
	 * @return The URI indicating the parent theme, or <code>null</code> if no parent theme is indicated or the value is not a URI.
	 */
	public URI getParentURI() {
		//TODO create utility as() method for Optional
		return getDescription().findPropertyValue(PARENT_URI_PROPERTY_URI).filter(URI.class::isInstance).map(URI.class::cast).orElse(null); //return the theme.parent property as a URI
	}

	/**
	 * Retrieves the resources URF resources. Each resource may indicate an external set of resources to load by providing a reference URI, or be a map of
	 * resource definitions.
	 * @param locale The locale of the resource to be retrieved.
	 * @return The list of resources that indicate resources locations and/or contain resource definitions.
	 */
	public Set<Object> getResourceResources(final Locale locale) { //TODO use the locale to narrow down the resources
		return getDescription().getPropertyValues(RESOURCES_PROPERTY_URI); //return all the theme.resource properties
	}

	/**
	 * Retrieves an iterable to the XML styles.
	 * @return The styles.
	 */
	/*TODO fix for new URF
	public Iterable<URFResource> getStyles() {
		return XML.getStyles(this); //return the styles
	}
	*/

	/**
	 * Updates the internal maps of rules. This method should be called after rules are modified so that rules will be applied correctly in the future.
	 * @throws ClassNotFoundException if one of the rules selects a class that cannot be found.
	 */
	//TODO fix for new URF	 * @see PropertySelector#getSelector()
	public void updateRules() throws ClassNotFoundException {
		/*TODO fix for new URF
		classRuleMap.clear(); //clear the map of rules
		for(final Rule rule : this) { //for each rule in this theme
			final Selector selector = rule.getSelector(); //get what this rule selects
			if(selector != null) { //if there is a selector for this rule
				updateRules(rule, selector); //update the rules with this selector
			}
		}
		*/
	}

	/**
	 * Updates the internal maps of rules based upon a selector and its subselectors. Rules with {@link OperatorSelector}s will be updated recursively.
	 * @param rule The rule with which the theme will be updated.
	 * @param selector The selector which may result in the theme being updated with this rule.
	 * @throws NullPointerException if the given rule and/or selector is <code>null</code>.
	 * @throws ClassNotFoundException if one of the selectors selects a class that cannot be found.
	 * @see OperatorSelector#getSelectors()
	 */
	/*TODO fix for new URF
	protected void updateRules(final Rule rule, final Selector selector) throws ClassNotFoundException {
		requireNonNull(rule, "Rule cannot be null.");
		requireNonNull(selector, "Selector cannot be null.");
		if(selector instanceof ObjectClassSelector) { //if this is a class selector
			final Class<?> selectClass = ((ObjectClassSelector)selector).getSelectClass(); //get the class selected by the selector
			if(selectClass != null) { //if we have a selected class
				classRuleMap.addItem(selectClass, rule); //add this rule to our map
			} else {
				throw new IllegalStateException("Object class selector missing class selection property.");
			}
		} else if(selector instanceof OperatorSelector) { //if this is an operator selector
			for(final Selector subselector : ((OperatorSelector)selector).getSelectors()) { //for each subselector
				updateRules(rule, subselector); //update the rules for each subselector
			}
		}
	}
	*/

	/**
	 * Applies this theme to the given object. Any parent theme is first applied to the object before this theme is applied.
	 * @param object The object to which this theme should be applied.
	 * @throws NullPointerException if the given object is <code>null</code>.
	 * @throws IllegalStateException if a resource is a Java-typed resource the class of which cannot be found.
	 * @throws IllegalStateException if a particular value is not an appropriate argument for the corresponding property.
	 * @throws IllegalStateException If a particular property could not be accessed.
	 * @throws IllegalStateException if a resource indicates a Java class the constructor of which throws an exception.
	 */
	public void apply(final Object object) {
		/*TODO fix for new URF
		try {
			final Theme parent = getParent(); //get the parent theme
			if(parent != null) { //if there is a parent theme
				parent.apply(object); //first apply the ancestor hierarchy to this object
			}
			final PLOOPURFProcessor ploopProcessor = new PLOOPURFProcessor(); //use the same PLOOP processor for all the rules of this theme
			final Set<Rule> classRules = getClassRules(object); //get all the rules applying to the object class
			for(final Rule rule : classRules) { //for each rule
				rule.apply(object, ploopProcessor); //apply the rule to the component, if the rule is applicable
			}
		} catch(final DataException dataException) {
			throw new IllegalStateException(dataException);
		} catch(final InvocationTargetException invocationTargetException) {
			throw new IllegalStateException(invocationTargetException);
		}
		*/
	}

	//standard colors
	/** Standard theme color. */
	public static final Color COLOR_SELECTED_BACKGROUND = new ResourceColor("theme.color.selected.background");

	//standard theme labels
	/** Standard theme label. */
	public static final String LABEL_ABOUT = createStringResourceReference("theme.label.about");
	/** Standard theme label. */
	public static final String LABEL_ABOUT_X = createStringResourceReference("theme.label.about.x");
	/** Standard theme label. */
	public static final String LABEL_ACCEPT = createStringResourceReference("theme.label.accept");
	/** Standard theme label. */
	public static final String LABEL_ACCESS = createStringResourceReference("theme.label.access");
	/** Standard theme label. */
	public static final String LABEL_ACCESS_X = createStringResourceReference("theme.label.access.x");
	/** Standard theme label. */
	public static final String LABEL_ADD = createStringResourceReference("theme.label.add");
	/** Standard theme label. */
	public static final String LABEL_ADD_X = createStringResourceReference("theme.label.add.x");
	/** Standard theme label. */
	public static final String LABEL_AUDIO = createStringResourceReference("theme.label.audio");
	/** Standard theme label. */
	public static final String LABEL_BROWSE = createStringResourceReference("theme.label.browse");
	/** Standard theme label. */
	public static final String LABEL_CALENDAR = createStringResourceReference("theme.label.calendar");
	/** Standard theme label. */
	public static final String LABEL_CANCEL = createStringResourceReference("theme.label.cancel");
	/** Standard theme label. */
	public static final String LABEL_CLOSE = createStringResourceReference("theme.label.close");
	/** Standard theme label. */
	public static final String LABEL_CONFIRM = createStringResourceReference("theme.label.confirm");
	/** Standard theme label. */
	public static final String LABEL_DATE = createStringResourceReference("theme.label.date");
	/** Standard theme label. */
	public static final String LABEL_DEFAULT = createStringResourceReference("theme.label.default");
	/** Standard theme label. */
	public static final String LABEL_DELETE = createStringResourceReference("theme.label.delete");
	/** Standard theme label. */
	public static final String LABEL_DELETE_X = createStringResourceReference("theme.label.delete.x");
	/** Standard theme label. */
	public static final String LABEL_DOWNLOAD = createStringResourceReference("theme.label.download");
	/** Standard theme label. */
	public static final String LABEL_EDIT = createStringResourceReference("theme.label.edit");
	/** Standard theme label. */
	public static final String LABEL_EMAIL = createStringResourceReference("theme.label.email");
	/** Standard theme label. */
	public static final String LABEL_ERROR = createStringResourceReference("theme.label.error");
	/** Standard theme label. */
	public static final String LABEL_FINISH = createStringResourceReference("theme.label.finish");
	/** Standard theme label. */
	public static final String LABEL_FIRST = createStringResourceReference("theme.label.first");
	/** Standard theme label. */
	public static final String LABEL_FOLDER = createStringResourceReference("theme.label.folder");
	/** Standard theme label. */
	public static final String LABEL_GENERAL = createStringResourceReference("theme.label.general");
	/** Standard theme label. */
	public static final String LABEL_HELP = createStringResourceReference("theme.label.help");
	/** Standard theme label. */
	public static final String LABEL_HOME = createStringResourceReference("theme.label.home");
	/** Standard theme label. */
	public static final String LABEL_IMAGE = createStringResourceReference("theme.label.image");
	/** Standard theme label. */
	public static final String LABEL_INSERT = createStringResourceReference("theme.label.insert");
	/** Standard theme label. */
	public static final String LABEL_JOIN = createStringResourceReference("theme.label.join");
	/** Standard theme label. */
	public static final String LABEL_JOIN_X = createStringResourceReference("theme.label.join.x");
	/** Standard theme label. */
	public static final String LABEL_LAST = createStringResourceReference("theme.label.last");
	/** Standard theme label. */
	public static final String LABEL_LOGIN = createStringResourceReference("theme.label.login");
	/** Standard theme label. */
	public static final String LABEL_LOGOUT = createStringResourceReference("theme.label.logout");
	/** Standard theme label. */
	public static final String LABEL_LOWER = createStringResourceReference("theme.label.lower");
	/** Standard theme label. */
	public static final String LABEL_MISSING = createStringResourceReference("theme.label.missing");
	/** Standard theme label. */
	public static final String LABEL_NEXT = createStringResourceReference("theme.label.next");
	/** Standard theme label. */
	public static final String LABEL_NEW = createStringResourceReference("theme.label.new");
	/** Standard theme label. */
	public static final String LABEL_NEW_X = createStringResourceReference("theme.label.new.x");
	/** Standard theme label. */
	public static final String LABEL_ORDER = createStringResourceReference("theme.label.order");
	/** Standard theme label. */
	public static final String LABEL_PASSWORD = createStringResourceReference("theme.label.password");
	/** Standard theme label. */
	public static final String LABEL_PASSWORD_VERIFICATION = createStringResourceReference("theme.label.password_verification");
	/** Standard theme label. */
	public static final String LABEL_PREVIOUS = createStringResourceReference("theme.label.previous");
	/** Standard theme label. */
	public static final String LABEL_PRODUCT_JAVA = createStringResourceReference("theme.label.product.java");
	/** Standard theme label. */
	public static final String LABEL_PROPERTIES = createStringResourceReference("theme.label.properties");
	/** Standard theme label. */
	public static final String LABEL_X_PROPERTIES = createStringResourceReference("theme.label.x.properties");
	/** Standard theme label. */
	public static final String LABEL_RAISE = createStringResourceReference("theme.label.raise");
	/** Standard theme label. */
	public static final String LABEL_READ = createStringResourceReference("theme.label.read");
	/** Standard theme label. */
	public static final String LABEL_REFRESH = createStringResourceReference("theme.label.refresh");
	/** Standard theme label. */
	public static final String LABEL_REJECT = createStringResourceReference("theme.label.reject");
	/** Standard theme label. */
	public static final String LABEL_REMOVE = createStringResourceReference("theme.label.remove");
	/** Standard theme label. */
	public static final String LABEL_RENAME = createStringResourceReference("theme.label.rename");
	/** Standard theme label. */
	public static final String LABEL_RENAME_X = createStringResourceReference("theme.label.rename.x");
	/** Standard theme label. */
	public static final String LABEL_RESOURCE = createStringResourceReference("theme.label.resource");
	/** Standard theme label. */
	public static final String LABEL_RETRY = createStringResourceReference("theme.label.retry");
	/** Standard theme label. */
	public static final String LABEL_SAVE = createStringResourceReference("theme.label.save");
	/** Standard theme label. */
	public static final String LABEL_SORT = createStringResourceReference("theme.label.sort");
	/** Standard theme label. */
	public static final String LABEL_START = createStringResourceReference("theme.label.start");
	/** Standard theme label. */
	public static final String LABEL_STOP = createStringResourceReference("theme.label.stop");
	/** Standard theme label. */
	public static final String LABEL_SUBMIT = createStringResourceReference("theme.label.submit");
	/** Standard theme label. */
	public static final String LABEL_SUBTRACT = createStringResourceReference("theme.label.subtract");
	/** Standard theme label. */
	public static final String LABEL_TIME = createStringResourceReference("theme.label.time");
	/** Standard theme label. */
	public static final String LABEL_TOTAL = createStringResourceReference("theme.label.total");
	/** Standard theme label. */
	public static final String LABEL_TYPE = createStringResourceReference("theme.label.type");
	/** Standard theme label. */
	public static final String LABEL_UNKNOWN = createStringResourceReference("theme.label.unknown");
	/** Standard theme label. */
	public static final String LABEL_UPLOAD = createStringResourceReference("theme.label.upload");
	/** Standard theme label. */
	public static final String LABEL_UPLOAD_TO_X = createStringResourceReference("theme.label.upload.to.x");
	/** Standard theme label. */
	public static final String LABEL_URI = createStringResourceReference("theme.label.uri");
	/** Standard theme label. */
	public static final String LABEL_USERNAME = createStringResourceReference("theme.label.username");
	/** Standard theme label. */
	public static final String LABEL_VERIFTY = createStringResourceReference("theme.label.verify");
	/** Standard theme label. */
	public static final String LABEL_VERSION = createStringResourceReference("theme.label.version");
	/** Standard theme label. */
	public static final String LABEL_VIEW = createStringResourceReference("theme.label.view");
	//standard theme icons
	/** Standard theme icon. */
	public static final URI ICON_ABOUT = createURIResourceReference("theme.icon.about");
	/** Standard theme icon. */
	public static final URI ICON_ERROR = createURIResourceReference("theme.icon.error");
	/** Standard theme icon. */
	public static final URI ICON_HELP = createURIResourceReference("theme.icon.help");
	/** Standard theme icon. */
	public static final URI ICON_INFO = createURIResourceReference("theme.icon.info");
	/** Standard theme icon. */
	public static final URI ICON_QUESTION = createURIResourceReference("theme.icon.question");
	/** Standard theme icon. */
	public static final URI ICON_STOP = createURIResourceReference("theme.icon.stop");
	/** Standard theme icon. */
	public static final URI ICON_WARN = createURIResourceReference("theme.icon.warn");
	//standard theme glyph icons
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ABOUT = createURIResourceReference("theme.glyph.about");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ACCEPT = createURIResourceReference("theme.glyph.accept");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ACCEPT_MULTIPLE = createURIResourceReference("theme.glyph.accept.multiple");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ACCESS = createURIResourceReference("theme.glyph.access");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ADD = createURIResourceReference("theme.glyph.add");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ANIMATION = createURIResourceReference("theme.glyph.animation");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ARROW_DOWN = createURIResourceReference("theme.glyph.arrow.down");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ARROW_LEFT = createURIResourceReference("theme.glyph.arrow.left");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ARROW_RIGHT = createURIResourceReference("theme.glyph.arrow.right");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ARROW_UP = createURIResourceReference("theme.glyph.arrow.up");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_AUDIO = createURIResourceReference("theme.glyph.audio");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_BLANK = createURIResourceReference("theme.glyph.blank");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_BROWSE = createURIResourceReference("theme.glyph.browse");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_BUSY = createURIResourceReference("theme.glyph.busy");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_CALENDAR = createURIResourceReference("theme.glyph.calendar");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_CANCEL = createURIResourceReference("theme.glyph.cancel");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_CLOSE = createURIResourceReference("theme.glyph.close");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_CONFIRM = createURIResourceReference("theme.glyph.confirm");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DELETE = createURIResourceReference("theme.glyph.delete");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT = createURIResourceReference("theme.glyph.document");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_BROKEN = createURIResourceReference("theme.glyph.document.broken");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_CONTENT = createURIResourceReference("theme.glyph.document.content");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_NEW = createURIResourceReference("theme.glyph.document.new");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_PREVIEW = createURIResourceReference("theme.glyph.document.preview");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_RICH_CONTENT = createURIResourceReference("theme.glyph.document.rich.content");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOCUMENT_STACK = createURIResourceReference("theme.glyph.document.stack");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_DOWNLOAD = createURIResourceReference("theme.glyph.download");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_EDIT = createURIResourceReference("theme.glyph.edit");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_EMAIL = createURIResourceReference("theme.glyph.email");
	/** Standard theme glyph icon. */
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ENTER = createURIResourceReference("theme.glyph.enter");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ERROR = createURIResourceReference("theme.glyph.error");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_EXIT = createURIResourceReference("theme.glyph.exit");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_EXCLAMATION = createURIResourceReference("theme.glyph.exclamation");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_EYEGLASSES = createURIResourceReference("theme.glyph.eyeglasses");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FINISH = createURIResourceReference("theme.glyph.finish");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FIRST = createURIResourceReference("theme.glyph.first");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FOLDER = createURIResourceReference("theme.glyph.folder");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FOLDER_CLOSED = createURIResourceReference("theme.glyph.folder.closed");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FOLDER_OPEN = createURIResourceReference("theme.glyph.folder.open");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FOLDER_RICH_CONTENT = createURIResourceReference("theme.glyph.folder.rich_content");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_FOLDER_TREE = createURIResourceReference("theme.glyph.folder.tree");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_HELP = createURIResourceReference("theme.glyph.help");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_HIDE = createURIResourceReference("theme.glyph.hide");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_HIERARCHY = createURIResourceReference("theme.glyph.hierarchy");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_HOME = createURIResourceReference("theme.glyph.home");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_IMAGE = createURIResourceReference("theme.glyph.image");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_INFO = createURIResourceReference("theme.glyph.info");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_INSERT = createURIResourceReference("theme.glyph.insert");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_JOIN = createURIResourceReference("theme.glyph.join");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_KEY = createURIResourceReference("theme.glyph.key");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LAST = createURIResourceReference("theme.glyph.last");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LIST = createURIResourceReference("theme.glyph.list");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LOCK_CLOSED = createURIResourceReference("theme.glyph.lock.closed");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LOCK_OPEN = createURIResourceReference("theme.glyph.lock.open");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LOGIN = createURIResourceReference("theme.glyph.login");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_LOGOUT = createURIResourceReference("theme.glyph.logout");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_ADVANCE = createURIResourceReference("theme.glyph.media.advance");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_NEXT = createURIResourceReference("theme.glyph.media.next");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_PAUSE = createURIResourceReference("theme.glyph.media.pause");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_PLAY = createURIResourceReference("theme.glyph.media.play");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_PREVIOUS = createURIResourceReference("theme.glyph.media.previous");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_RECEDE = createURIResourceReference("theme.glyph.media.recede");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_RECORD = createURIResourceReference("theme.glyph.media.record");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MEDIA_STOP = createURIResourceReference("theme.glyph.media.stop");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MISSING = createURIResourceReference("theme.glyph.missing");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_MUSIC = createURIResourceReference("theme.glyph.music");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_NEXT = createURIResourceReference("theme.glyph.next");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_ORDER = createURIResourceReference("theme.glyph.order");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PASSWORD = createURIResourceReference("theme.glyph.password");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PICTURE = createURIResourceReference("theme.glyph.picture");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_POLYGON_CURVED = createURIResourceReference("theme.glyph.polygon.curved");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_POLYGON_POINTS = createURIResourceReference("theme.glyph.polygon.points");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PREVIEW = createURIResourceReference("theme.glyph.preview");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PREVIOUS = createURIResourceReference("theme.glyph.previous");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PRODUCT_JAVA = createURIResourceReference("theme.glyph.product.java");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_PROPERTIES = createURIResourceReference("theme.glyph.properties");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_QUESTION = createURIResourceReference("theme.glyph.question");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_REFRESH = createURIResourceReference("theme.glyph.refresh");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_REMOVE = createURIResourceReference("theme.glyph.remove");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_REJECT = createURIResourceReference("theme.glyph.reject");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_REJECT_MULTIPLE = createURIResourceReference("theme.glyph.reject.multiple");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_RENAME = createURIResourceReference("theme.glyph.rename");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_RESOURCE = createURIResourceReference("theme.glyph.resource");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_RETRY = createURIResourceReference("theme.glyph.retry");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SAVE = createURIResourceReference("theme.glyph.save");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SELECTED = createURIResourceReference("theme.glyph.selected");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SORT = createURIResourceReference("theme.glyph.sort");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SPEECH_BUBBLE = createURIResourceReference("theme.glyph.speech.bubble");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SPEECH_BUBBLE_TEXT = createURIResourceReference("theme.glyph.speech.bubble.text");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SPEECH_BUBBLE_TEXT_ADD = createURIResourceReference("theme.glyph.speech.bubble.text.add");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_START = createURIResourceReference("theme.glyph.start");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_STOP = createURIResourceReference("theme.glyph.stop");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_STRING_EDIT = createURIResourceReference("theme.glyph.string.edit");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SUBMIT = createURIResourceReference("theme.glyph.submit");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_SUBTRACT = createURIResourceReference("theme.glyph.subtract");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_THUMBNAILS = createURIResourceReference("theme.glyph.thumbnails");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_TREE = createURIResourceReference("theme.glyph.tree");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_UNSELECTED = createURIResourceReference("theme.glyph.unselected");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_UPLOAD = createURIResourceReference("theme.glyph.upload");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_USER = createURIResourceReference("theme.glyph.user");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_VERIFY = createURIResourceReference("theme.glyph.verify");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_VIEW = createURIResourceReference("theme.glyph.view");
	/** Standard theme glyph icon. */
	public static final URI GLYPH_WARN = createURIResourceReference("theme.glyph.warn");
	//standard theme messages
	/** Standard theme message. */
	public static final String MESSAGE_BUSY = createStringResourceReference("theme.message.busy");
	/** Standard theme message. */
	public static final String MESSAGE_PASSWORD_INVALID = createStringResourceReference("theme.message.password.invalid");
	/** Standard theme message. */
	public static final String MESSAGE_PASSWORD_UNVERIFIED = createStringResourceReference("theme.message.password.unverified");
	/** Standard theme message. */
	public static final String MESSAGE_TASK_SUCCESS = createStringResourceReference("theme.message.task.success");
	/** Standard theme message. */
	public static final String MESSAGE_USER_INVALID = createStringResourceReference("theme.message.user.invalid");
	/** Standard theme message. */
	public static final String MESSAGE_USER_EXISTS = createStringResourceReference("theme.message.user.exists");
	//standard theme cursors
	/** Standard theme cursor. */
	public static final URI CURSOR_CROSSHAIR = createURIResourceReference("theme.cursor.crosshair");
	/** Standard theme cursor. */
	public static final URI CURSOR_DEFAULT = createURIResourceReference("theme.cursor.default");
	/** Standard theme cursor. */
	public static final URI CURSOR_HELP = createURIResourceReference("theme.cursor.help");
	/** Standard theme cursor. */
	public static final URI CURSOR_MOVE = createURIResourceReference("theme.cursor.move");
	/** Standard theme cursor. */
	public static final URI CURSOR_POINTER = createURIResourceReference("theme.cursor.pointer");
	/** Standard theme cursor. */
	public static final URI CURSOR_PROGRESS = createURIResourceReference("theme.cursor.progress");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_FAR = createURIResourceReference("theme.cursor.resize.line.far");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_FAR_PAGE_FAR = createURIResourceReference("theme.cursor.resize.line.far.page.far");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_FAR_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.line.far.page.near");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_NEAR = createURIResourceReference("theme.cursor.resize.line.near");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_NEAR_PAGE_FAR = createURIResourceReference("theme.cursor.resize.line.near.page.far");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_LINE_NEAR_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.line.near.page.near");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_PAGE_FAR = createURIResourceReference("theme.cursor.resize.page.far");
	/** Standard theme cursor. */
	public static final URI CURSOR_RESIZE_PAGE_NEAR = createURIResourceReference("theme.cursor.resize.page.near");
	/** Standard theme cursor. */
	public static final URI CURSOR_TEXT = createURIResourceReference("theme.cursor.text");
	/** Standard theme cursor. */
	public static final URI CURSOR_WAIT = createURIResourceReference("theme.cursor.wait");
	//components
	/** Theme component. */
	public static final URI SLIDER_THUMB_X_IMAGE = createURIResourceReference("theme.slider.thumb.x.image");
	/** Theme component. */
	public static final URI SLIDER_THUMB_Y_IMAGE = createURIResourceReference("theme.slider.thumb.y.image");
	/** Theme component. */
	public static final URI SLIDER_TRACK_X_IMAGE = createURIResourceReference("theme.slider.track.x.image");
	/** Theme component. */
	public static final URI SLIDER_TRACK_Y_IMAGE = createURIResourceReference("theme.slider.track.y.image");

}
