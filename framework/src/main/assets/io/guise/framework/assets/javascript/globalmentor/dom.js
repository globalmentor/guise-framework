/* GlobalMentor Object Model JavaScript Library
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

/*
 * GlobalMentor general DOM library.
 * Author: Garret Wilson
 * 
 * Dependencies:
 * 	javascript.js
 */

var com = com || {}; //create the com.globalmentor.dom package
com.globalmentor = com.globalmentor || {};
com.globalmentor.dom = com.globalmentor.dom || {};

/**
 * Key codes.
 * 
 * @see <a href="http://www.quirksmode.org/js/keys.html">Detecting keystrokes</a>
 * @see <a href="http://www.quirksmode.org/dom/w3c_events.html">W3C DOM Compatibility - Events</a>
 */
var KEY_CODE =
{
	ALT : 18,
	BACKSPACE : 8,
	CONTROL : 17,
	DELETE : 46,
	DOWN : 40,
	END : 35,
	ENTER : 13,
	ESCAPE : 27,
	F1 : 112,
	F2 : 113,
	F3 : 114,
	F4 : 115,
	F5 : 116,
	F6 : 117,
	F7 : 118,
	F8 : 119,
	F9 : 120,
	F10 : 121,
	F11 : 122,
	F12 : 123,
	HOME : 36,
	LEFT : 37,
	PAGE_UP : 33,
	PAGE_DOWN : 34,
	RIGHT : 39,
	SHIFT : 16,
	TAB : 9,
	UP : 38
};

/**
 * Mouse buttons as defined by the W3C and as normalized here.
 * 
 * @see <a href="http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-MouseEvent">Interface MouseEvent</a>
 * @see <a href="http://www.quirksmode.org/dom/w3c_events.html">W3C DOM Compatibility - Events</a>
 */
var MOUSE_BUTTON =
{
	LEFT : 0,
	MIDDLE : 1,
	RIGHT : 2
};

//Document

if(typeof document.createElementNS == "undefined") //if the document does not support createElementNS(), create a substitute
{
	/**
	 * Create an element for the given namespace URI and qualified name using the document.createElement() method.
	 * 
	 * @param namespaceURI The URI of the namespace.
	 * @param qname The qualified name of the element.
	 * @returns The new created element.
	 */
	document.createElementNS = function(namespaceURI, qname) //create an adapter function to call document.createElement()
	{
		return document.createElement(qname); //create the element, ignoring the namespace TODO does this use closure unnecessarily?
	};
}

if(typeof document.createAttributeNS == "undefined") //if the document does not support createAttributeNS(), create a substitute
{
	/**
	 * Create an attribute for the given namespace URI and qualified name using the document.createAttribute() method.
	 * 
	 * @param namespaceURI The URI of the namespace.
	 * @param qname The qualified name of the attribute.
	 * @returns The new created attribute.
	 */
	document.createAttributeNS = function(namespaceURI, qname) //create an adapter function to call document.createAttribute()
	{
		return document.createAttribute(qname); //create the attribute, ignoring the namespace TODO does this use closure unnecessarily?
	};
}

/**
 * Manually imports a new node in the the document.
 * 
 * @param node The node to import.
 * @param deep Whether the entire hierarchy should be imported.
 * @returns A new clone of the node with the document as its owner.
 */
document.importNodeCustom = function(node, deep)
{
	var importedNode = null; //we'll create a new node and store it here
	switch(node.nodeType)
	//see which type of child node this is
	{
		case Node.COMMENT_NODE: //comment
			importedNode = document.createCommentNode(node.nodeValue); //create a new comment node with appropriate text
			break;
		case Node.ELEMENT_NODE: //element
			if(typeof node.namespaceURI != "undefined") //if the node supports namespaces
			{
				importedNode = document.createElementNS(node.namespaceURI, node.nodeName); //create a namespace-aware element
			}
			else
			//if the node does not support namespaces
			{
				importedNode = document.createElement(node.nodeName); //create a non-namespace-aware element
			}
			var attributes = node.attributes; //get the element's attributes
			var attributeCount = attributes.length; //find out how many attributes there are
			for( var i = 0; i < attributeCount; ++i) //for each attribute
			{
				var attribute = attributes[i]; //get this attribute
				var attributeName = attribute.nodeName; //get the attribute name
				var attributeValue = attribute.nodeValue; //get the attribute value
				//some attributes must be copied differently to the HTML DOM
				switch(attributeName)
				{
					case "style": //see http://www.quirksmode.org/dom/w3c_css.html#t30
						importedNode.style.cssText = attributeValue;
						break;
					case "type": //IE9 doesn't copy <input type="..."/> correctly unless the property is used; see http://msdn.microsoft.com/en-us/library/ms534700.aspx
						importedNode.type = attributeValue;
						break;
					default:
						if(importedNode.setAttributeNodeNS instanceof Function && typeof attribute.namespaceURI != "undefined") //if the attribute supports namespaces
						{
							var importedAttribute = document.createAttributeNS(attribute.namespaceURI, attributeName); //create a namespace-aware attribute
							importedAttribute.nodeValue = attributeValue; //set the attribute value
							importedNode.setAttributeNodeNS(importedAttribute); //set the attribute for the element						
						}
						else
						//if the attribute does not support namespaces
						{
							var importedAttribute = document.createAttribute(attributeName); //create a non-namespace-aware element
							importedAttribute.nodeValue = attributeValue; //set the attribute value TODO verify this works on Safari
							importedNode.setAttributeNode(importedAttribute); //set the attribute for the element
						}
						break;
				}
			}
			if(deep) //if we should import deep
			{
				var childNodes = node.childNodes; //get a list of child nodes
				var childNodeCount = childNodes.length; //find out how many child nodes there are
				for( var i = 0; i < childNodeCount; ++i) //for each child node
				{
					var childNode = childNodes[i]; //get this child node
					var importedChildNode = document.importNode(childNode, deep);
					importedNode.appendChild(importedChildNode);
				}
			}
			break;
		case Node.TEXT_NODE: //text
			importedNode = document.createTextNode(node.nodeValue); //create a new text node with appropriate text
			break;
		default:
			throw "Unknown node type: " + node.nodeType;
			break;
		//TODO add checks for other elements, such as CDATA
	}
	return importedNode; //return the imported node
};

if(typeof document.importNode == "undefined") //if the document does not support document.importNode(), create use a substitute
{
	document.importNode = document.importNodeCustom;
}

/** Global utilities for working with the screen. */
var GUIUtilities =
{

	/**
	 * Centers a node on the screen. It is assumed that the node is already specified as absolutely positioned.
	 * 
	 * @param node The node to center.
	 */
	centerNode : function(node)
	{
		var viewportBounds = this.getViewportBounds(); //get the bounds of the viewport so that we can center the node
		var x = viewportBounds.x + ((viewportBounds.width - node.offsetWidth) / 2); //center the node horizontally
		var y = viewportBounds.y + ((viewportBounds.height - node.offsetHeight) / 2); //center the node vertically
		//TODO del alert("with viewport width: "+viewportBounds.width+" and node width: "+node.offsetWidth+" setting x: "+x);
		node.style.left = x + "px"; //set the node's horizontal position
		node.style.top = y + "px"; //set the node's vertical position
	},

	/**
	 * Retrieves the absolute bounds of the given element.
	 * 
	 * @param The element the bounds of which to find.
	 * @returns A Rectangle containing the coordinates and size of the element.
	 */
	getElementBounds : function(element)
	{
		return new Rectangle(this.getElementCoordinates(element), new Size(element.offsetWidth, element.offsetHeight)); //create a rectangle containing the coordinates and size of the element
	},

	/**
	 * Retrieves the absolute X and Y coordinates of the given element.
	 * 
	 * @param The element the coordinates of which to find.
	 * @returns A Point containing the coordinates of the element.
	 * @see <a href="http://www.oreillynet.com/pub/a/javascript/excerpt/JSDHTMLCkbk_chap13/index6.html">Cooking with
	 *      JavaScript & DHTML, Part 6</a>
	 * @see <a href="http://www.quirksmode.org/js/findpos.html">Find position</a>
	 * @see <a href="http://blog.firetree.net/2005/07/04/javascript-find-position/">JavaScript Find Position</a>
	 */
	getElementCoordinates : function(element) //TODO make sure this method correctly calculates margins and padding, as Mozilla and IE both show slight variations for text, but not for images
	{
		var originalElement = element; //keep track of which element was originally requested
		var x = 0, y = 0;
		if(element.currentStyle) //compensate for negative margins on IE6, which apparently is only effective on the immediate element (primarily frames); if IE7 fixes this bug we'll have to check for quirks mode (which we use with IE6 but not on IE7), or as a last resort check specifically for IE6
		{
			if(element.currentStyle.marginLeft) //if there is a left margin
			{
				var marginLeft = parseInt(element.currentStyle.marginLeft); //parse the margin left value, which may be a string in the form XXpx
				if(marginLeft && marginLeft < 0) //if there is a negative margin (and not some keyword)
				{
					x -= marginLeft; //compensate for negative left margin
				}
			}
			if(element.currentStyle.marginTop) //if there is a top margin
			{
				var marginTop = parseInt(element.currentStyle.marginTop); //parse the margin top value, which may be a string in the form XXpx
				if(marginTop && marginTop < 0) //if there is a negative margin (and not some keyword)
				{
					y -= marginTop; //compensate for negative top margin
				}
			}
		}
		if(element.offsetParent) //if element.offsetParent is supported
		{
			//TODO del alert("using calculated element position");
			while(element) //while we have an element
			{
				x += element.offsetLeft; //add this element's offsets
				y += element.offsetTop;
				/*TODO fix or del; apparently this code is not as good as the version below, although it doesn't seem to compensate for scrollLeft
							if(element.scrollLeft)
							{
								x-=element.scrollLeft;
							}
							if(element.scrollTop)
							{
				//TODO del alert("element "+element.nodeName+" scroll top "+element.scrollTop);
								y-=element.scrollTop;
							}
				*/
				element = element.offsetParent; //go to the element's parent offset
			}
			/*TODO fix for Mac
			    if (navigator.userAgent.indexOf("Mac") != -1 && 
			        typeof document.body.leftMargin != "undefined") {
			        offsetLeft += document.body.leftMargin;
			        offsetTop += document.body.topMargin;
			    }
			*/
			/*TODO fix; this inappropriately adds in the viewport scroll position on IE but not on Mozilla---but this is needed for internal scrolled divs on IE; try to distinguish the two when internal scrolled divs are used
					var parent=originalElement.parentNode;
					while(parent!=document.documentElement)
					{
						if(parent.scrollTop)
						{
			//TODO fix alert("element "+parent.nodeName+" scroll top "+parent.scrollTop);
							y-=parent.scrollTop;
						}
						parent=parent.parentNode;		
					}
			*/
			var documentElement = document.documentElement;
			element = originalElement.parentNode;
			while(element != documentElement) //TODO fix for scroll left
			{
				var parentNode = element.parentNode;
				if(element.scrollLeft)
				{
					//TODO fix alert("element "+parent.nodeName+" scroll top "+parent.scrollTop);
					x -= element.scrollLeft;
				}
				if(element.scrollTop)
				{
					//TODO fix alert("element "+parent.nodeName+" scroll top "+parent.scrollTop);
					y -= element.scrollTop;
				}
				element = parentNode;
			}
		}
		else if(element.x && element.y) //if element.offsetParent is not supported by but element.x and element.y are supported (e.g. Navigator 4)
		{
			x = element.x; //get the element's coordinates directly
			y = element.y;
		}
		return new Point(x, y); //return the point we calculated
	},

	/**
	 * Retrieves the absolute bounds of the given element, including any negative margins. This method is not currently
	 * guaranteed to work on non-IE browsers.
	 * 
	 * @param The element the bounds of which to find.
	 * @returns A Rectangle containing the coordinates and external size of the element.
	 */
	getElementExternalBounds : function(element)
	{
		var point = this.getElementCoordinates(element); //get the coordinates
		var size = new Size(element.offsetWidth, element.offsetHeight);
		if(element.currentStyle) //compensate for negative margins on IE6, which apparently is only effective on the immediate element (primarily frames); if IE7 fixes this bug we'll have to check for quirks mode (which we use with IE6 but not on IE7), or as a last resort check specifically for IE6
		{
			if(element.currentStyle.marginLeft) //if there is a left margin
			{
				var marginLeft = parseInt(element.currentStyle.marginLeft); //parse the margin left value, which may be a string in the form XXpx
				if(marginLeft && marginLeft < 0) //if there is a negative margin (and not some keyword)
				{
					point.x += marginLeft; //compensate for negative left margin
				}
			}
			if(element.currentStyle.marginTop) //if there is a top margin
			{
				var marginTop = parseInt(element.currentStyle.marginTop); //parse the margin top value, which may be a string in the form XXpx
				if(marginTop && marginTop < 0) //if there is a negative margin (and not some keyword)
				{
					point.y += marginTop; //compensate for negative top margin
				}
			}
		}
		//TODO check to see if the size compensates for negative margins or not
		return new Rectangle(point, size); //create a rectangle containing the coordinates and size of the element
	},

	/**
	 * Retrieves the X and Y coordinates of the given element relative to the viewport.
	 * 
	 * @param The element the coordinates of which to find.
	 * @returns A Point containing the coordinates of the element relative to the viewport.
	 */
	getElementFixedCoordinates : function(element)
	{
		var absoluteCoordinates = this.getElementCoordinates(element); //get the element's absolute coordinates
		var scrollCoordinates = this.getScrollCoordinates(); //get the viewport's scroll coordinates
		return new Point(absoluteCoordinates.x - scrollCoordinates.x, absoluteCoordinates.y - scrollCoordinates.y); //compensate for viewport scrolling
	},

	/**
	 * @returns The size of the document, even if it is outside the viewport.
	 * @see <a href="http://www.quirksmode.org/viewport/compatibility.html">Viewport Compatibility</a>
	 */
	getPageSize : function()
	{
		var width = Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);
		var height = Math.max(document.documentElement.scrollHeight, document.body.scrollHeight);
		//	alert("width: "+width+" height: "+height);
		/*TODO this is presented at http://www.quirksmode.org/viewport/compatibility.html, but doesn't correctly give the scroll width for Firefox
			var width=0, height=0;	//we'll determine the width and height
			var scrollHeight=document.body.scrollHeight;	//get the scroll height
			var offsetHeight=document.body.offsetHeight;	//get the offset height
			if(scrollHeight>offsetHeight)	//if the scroll height is larger
			{
				width=document.body.scrollWidth;	//use the scroll dimensions
				height=document.body.scrollHeight;
			}
			else	//if the body offsets are larger (e.g. Explorer Mac and IE 6 strict)
			{
				width=document.body.offsetWidth;	//use the body offsets
				height=document.body.offsetHeight;
			}
			return new Size(width, height);	//return the page size
			
		alert("document.body.scrollWidth: "+document.body.scrollWidth+"\n"+
					"document.body.offsetWidth: "+document.body.offsetWidth+"\n"+
					"document.body.clientWidth: "+document.body.clientWidth+"\n"+
					"document.documentElement.scrollWidth: "+document.documentElement.scrollWidth+"\n"+
					"document.documentElement.offsetWidth: "+document.documentElement.offsetWidth+"\n"+
					"document.documentElement.clientWidth: "+document.documentElement.clientWidth+"\n")
		*/
		return new Size(width, height); //return the page size
	},

	/**
	 * @returns The coordinates that the page has scrolled.
	 * @see <a href="http://www.quirksmode.org/viewport/compatibility.html">Viewport Compatibility</a>
	 */
	getScrollCoordinates : function()
	{
		var x, y;
		if(typeof window.pageYOffset != "undefined") //if we know the page vertical offset (all browsers except IE)
		{
			x = window.pageXOffset;
			y = window.pageYOffset;
		}
		else if(document.documentElement && document.documentElement.scrollTop) //if we know the document's scroll position (IE 6 strict mode)
		{
			x = document.documentElement.scrollLeft;
			y = document.documentElement.scrollTop;
		}
		else if(document.body) //for all other IE modes
		{
			x = document.body.scrollLeft;
			y = document.body.scrollTop;
		}
		else
		{
			throw "Unable to find scroll coordinates.";
		}
		return new Point(x, y); //return the scrolling coordinates
	},

	/** @returns A Rectangle containing the coordinates and size of the viewport. */
	getViewportBounds : function()
	{
		return new Rectangle(this.getScrollCoordinates(), this.getViewportSize()); //create a rectangle containing the coordinates and size of the viewport
	},

	/**
	 * @returns The size of the viewport.
	 * @see <a href="http://www.quirksmode.org/viewport/compatibility.html">Viewport Compatibility</a>
	 */
	getViewportSize : function()
	{
		var width = 0, height = 0; //we'll determine the width and height
		if(window.innerWidth && window.innerHeight) //if the window knows its inner width and height
		{
			width = window.innerWidth; //use the window's inner dimensions
			height = window.innerHeight;
		}
		else if(document.documentElement && document.documentElement.clientWidth && document.documentElement.clientHeight) //if the document element knows its dimensions (e.g. IE 6 in strict mode)
		{
			width = document.documentElement.clientWidth; //use the document element's client width and height
			height = document.documentElement.clientHeight;
		}
		else if(document.body) //if there is a document body
		{
			width = document.body.clientWidth; //use the document body's client width and height
			height = document.body.clientHeight;
		}
		return new Size(width, height); //return the size
	}

};

/** Utilities for working with the DOM. */
com.globalmentor.dom.DOM =
{

	/** The URI of the XHTML namespace. */
	XHTML_NAMESPACE_URI : "http://www.w3.org/1999/xhtml",

	/**
	 * Ensures that an IMG is loaded before calling a given function.
	 * 
	 * @param img The img element to load.
	 * @param fn The function to call after the image is loaded.
	 */
	waitIMGLoaded : function(img, fn)
	{
		if(img.complete) //if the image is loaded
		{
			fn(); //call the function directly
		}
		else
		//if the image is not loaded
		{
			var onLoad = function() //create a function to call the function after the image is loaded TODO fix this creates a race condition, because the image could finish loading before we can install our listener
			{
				com.globalmentor.dom.EventManager.removeEvent(img, "load", onLoad, false); //stop waiting for the img to load
				fn(); //call the function
			};
			com.globalmentor.dom.EventManager.addEvent(img, "load", onLoad, false); //register an event on the img to wait for it to load
		}
	},

	/**
	 * Cleans a cloned node and all it chidren, removing its element IDs, for example, so that it can inserted into a
	 * document.
	 */
	cleanNode : function(node)
	{
		if(node.nodeType == Node.ELEMENT_NODE) //if this is an element
		{
			if(node.id) //if this element has an ID
			{
				node.id = null; //remove the ID
			}
		}
		var childNodeList = node.childNodes; //get all the child nodes
		for( var i = childNodeList.length - 1; i >= 0; --i) //for each child node
		{
			this.cleanNode(childNodeList[i]); //clean this child node
		}
	},

	/**
	 * Retrieves the computed style of a given node.
	 * 
	 * @param node The node to check.
	 * @param property The name of the style to return.
	 * @returns The style value for the given property, or null if there is no such style defined.
	 * @see <a href="http://www.quirksmode.org/dom/getstyles.html">Get Styles</a>
	 * @see <a href="http://ajaxian.com/archives/javascript-tip-watch-out-for">JavaScript Tip: Watch out for “+”</a>
	 * @see <a href="http://squidfingers.com/code/snippets/?id=getcssprop">getcssprop</a>
	 * @see <a
	 *      href="http://developer.mozilla.org/en/docs/Gecko_DOM_Reference:Examples#Example_6:_getComputedStyle">getComputedStyle</a>
	 */
	getComputedStyle : function(node, property) //TODO later convert to checking the browser once before-hand when the function is first defined to speed things up 
	{
		var explicitStyle = node.style[property]; //get the explicit property, if any
		if(explicitStyle) //if there is a style explicitly set
		{
			return explicitStyle; //return the explicit style
		}
		var currentStyle = node.currentStyle; //see if there is a current style defined (IE)
		if(currentStyle) //if there is a current style
		{
			currentStyle[property]; //return the current style
		}
		var defaultView = document.defaultView; //see if there is a document defaultView (Mozilla, Safari 1.3+)
		if(defaultView) //if there is a defaultView
		{
			var computedStyleFunction = defaultView.getComputedStyle; //get the computed style, if any
			if(computedStyleFunction) //if there is a computed style
			{
				/*TODO del or use from http://ajaxian.com/archives/javascript-tip-watch-out-for
								prop = prop.replace(/([A-Z])/g, \"-$1\");
								prop = prop.toLowerCase();
						return document.defaultView.getComputedStyle(element,\"\").getPropertyValue(prop);
				*/
				return computedStyleFunction(node, null).getPropertyValue(property); //get the computed style's property value
			}
		}
		return null; //indicate that we were unable to find a computed style
	},

	/**
	 * Retrieves the immediate text nodes of the given node as a string.
	 * 
	 * @param node The node from which to retrieve text.
	 * @returns The text of the given node.
	 */
	getNodeText : function(node)
	{
		var childNodeList = node.childNodes; //get all the child nodes of the node
		var childNodeCount = childNodeList.length; //find out how many children there are
		if(childNodeCount > 0) //if there are child nodes
		{
			var stringBuilder = new StringBuilder(); //create a new string builder to construct the string
			for( var i = 0; i < childNodeCount; ++i) //for each child node
			{
				var childNode = childNodeList[i]; //get this child node
				if(childNode.nodeType == Node.TEXT_NODE) //if this is a text node
				{
					stringBuilder.append(childNode.nodeValue); //append this value			
				}
			}
			return stringBuilder.toString(); //return the string we constructed
		}
		else
		//if there are no child nodes
		{
			return ""; //return an empty string
		}
	},

	/**
	 * Creates a string representation of the given node.
	 * 
	 * @param node The node to serialize.
	 * @returns A string representation of the given node.
	 */
	getNodeString : function(node)
	{
		return this.appendNodeString(new StringBuilder(), node).toString(); //serialize the node and return its string representation
	},

	/**
	 * Appends a string representation of the given node.
	 * 
	 * @param stringBuilder The string builder to which a text representation of the node should be appended.
	 * @param node The node to serialize.
	 * @returns A reference to the string builder.
	 */
	appendNodeString : function(stringBuilder, node)
	{
		switch(node.nodeType)
		//see which type of node this is
		{
			case Node.ELEMENT_NODE: //element
				var nodeName = node.nodeName.toLowerCase(); //get the name of the node
				stringBuilder.append("<").append(nodeName); //<nodeName
				var attributes = node.attributes; //get the element's attributes
				var attributeCount = attributes.length; //find out how many attributes there are
				for( var i = 0; i < attributeCount; ++i) //for each attribute
				{
					var attribute = attributes[i]; //get this attribute
					var attributeValue = attribute.nodeValue; //get the attribute value
					if(attributeValue != null) //if this attribute value is not null (IE6 can return null attribute values for HTML DOM attributes)
					{
						this.appendXMLAttribute(stringBuilder, attribute.nodeName, attributeValue); //append this attribute
					}
				}
				if(node.childNodes.length > 0 || this.NON_EMPTY_ELEMENT_SET[nodeName]) //if there are children, or the element cannot be serialized as an empty element (IE6, for instance, which will drop "div" and "span" from the DOM if they are empty)
				{
					stringBuilder.append(">"); //>
					this.appendNodeContentString(stringBuilder, node); //append this node's content
					this.appendXMLEndTag(stringBuilder, nodeName); //append the end tag
				}
				else
				//if there are no children, create an empty element (otherwise, for elements like <input></input>, IE6 will see two elements)
				{
					stringBuilder.append("/>"); ///>
				}
				break;
			case Node.COMMENT_NODE: //comment
				stringBuilder.append("<!--").append(node.nodeValue).append("-->"); //append the node's value with no changes TODO encode the sequence "--"
				break;
			case Node.TEXT_NODE: //text
				this.appendXMLText(stringBuilder, node.nodeValue); //append the node's text value
				break;
			//TODO add checks for other elements, such as CDATA
		}
		return stringBuilder; //return the string builder
	},

	/**
	 * Appends a string representation of the given node's content.
	 * 
	 * @param stringBuilder The string builder to which a text representation of the node content should be appended.
	 * @param node The node the content of which to serialize.
	 * @returns A reference to the string builder.
	 */
	appendNodeContentString : function(stringBuilder, node)
	{
		var childNodes = node.childNodes; //get a list of child nodes
		var childNodeCount = childNodes.length; //find out how many child nodes there are
		for( var i = 0; i < childNodeCount; ++i) //for all of the child nodes
		{
			var childNode = childNodes[i]; //get a reference to this child node
			this.appendNodeString(stringBuilder, childNode); //append this child node
		}
		return stringBuilder; //return the string builder
	},

	/**
	 * Encodes a string so that it may be used in XML by escaping XML-specific characters.
	 * 
	 * @param string The string to encode.
	 * @returns The XML-encoded string.
	 */
	encodeXML : function(string)
	{
		string = string.replace(/&/g, "&amp;"); //encode '&' first
		string = string.replace(/</g, "&lt;"); //encode '<'
		string = string.replace(/>/g, "&gt;"); //encode '>'
		string = string.replace(/\"/g, "&quot;"); //encode '\"'
		return string; //return the encoded string
	},

	/**
	 * Encodes and appends XML text to the given string builder.
	 * 
	 * @param stringBuilder The string builder to hold the data.
	 * @param text The text to encode and append.
	 * @returns A reference to the string builder.
	 */
	appendXMLText : function(stringBuilder, text)
	{
		return stringBuilder.append(this.encodeXML(text)); //encode and append the text, and return the string builder
	},

	/**
	 * Appends an XML start tag with the given name to the given string builder. If the value of a parameter is null, that
	 * parameter will not be used. If the value of a parameter is not a string, it will be converted to one.
	 * 
	 * @param stringBuilder The string builder to hold the data.
	 * @param tagName The name of the XML tag.
	 * @param attributes An associative array of name/value pairs representing attribute names and values, or null if no
	 *          attributes are provided.
	 * @param empty An option boolean indication of whether this start tag should be an empty element.
	 * @returns A reference to the string builder.
	 */
	appendXMLStartTag : function(stringBuilder, tagName, attributes, empty)
	{
		stringBuilder.append("<").append(tagName); //<tagName
		if(attributes != null) //if attributes are provided
		{
			for( var attributeName in attributes) //for each attribute
			{
				var attributeValue = attributes[attributeName]; //get this attribute value
				if(attributeValue != null) //if an attribute value was given
				{
					this.appendXMLAttribute(stringBuilder, attributeName, attributeValue); //append the attribute
				}
			}
		}
		return stringBuilder.append(empty ? "/>" : ">"); ///> or >, depending on whether the element is empty		
	},

	/**
	 * Appends an XML attribute with the given name and value to the given string builder. If the value of an attribute is
	 * not a string, it will be converted to one. The attribute-value combination will be preceded by a space.
	 * 
	 * @param stringBuilder The string builder to hold the data.
	 * @param attributeName The name of the XML attribute.
	 * @param attributeValue The value of the XML attribute, which cannot be null.
	 * @returns A reference to the string builder.
	 */
	appendXMLAttribute : function(stringBuilder, attributeName, attributeValue)
	{
		stringBuilder.append(" ").append(attributeName).append("=\"").append(this.encodeXML(attributeValue.toString())).append("\""); //name="value"
		return stringBuilder; //return the string builder
	},

	/**
	 * Appends an XML end tag with the given name to the given string builder.
	 * 
	 * @param stringBuilder The string builder to hold the data.
	 * @param tagName The name of the XML tag.
	 * @returns A reference to the string builder.
	 */
	appendXMLEndTag : function(stringBuilder, tagName)
	{
		return stringBuilder.append("</").append(tagName).append(">"); //append the start tag
	},

	/**
	 * Appends an XML element containing text.
	 * 
	 * @param stringBuilder The string builder to hold the data.
	 * @param tagName The name of the XML tag.
	 * @param text The text to store in the element.
	 * @returns A reference to the string builder.
	 */
	appendXMLTextElement : function(stringBuilder, tagName, text)
	{
		this.appendXMLStartTag(stringBuilder, tagName); //append the start tag
		this.appendXMLText(stringBuilder, text); //append the text
		return this.appendXMLEndTag(stringBuilder, tagName); //append the end tag
	},

	/**
	 * Determines whether the given element has the given class, using DOM methods. Multiple class names are supported.
	 * 
	 * @param element The element that should be checked for class.
	 * @param className The name of the class for which to check, or a regular expression if a match should be found.
	 * @returns true if one of the element's class names equals the given class name.
	 */
	hasClass : function(element, className)
	{
		var classNamesString = element.getAttribute("class"); //get the element's class names
		var classNames = classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY; //split out the class names
		return className instanceof RegExp ? classNames.containsMatch(className) : classNames.contains(className); //return whether this class name is one of the class names
	},

	/**
	 * Map of property names keyed to DOM attribute names for properties that must be accessed via object properties. This
	 * distinction is important primarily because attributes maintain live values for some properties that are not
	 * synchronized with their original attribute values.
	 * @see <a href="http://reference.sitepoint.com/javascript/Element/getAttribute">sitepoint getAttribute</a>
	 * @see <a href="see http://msdn.microsoft.com/en-us/library/ms536429.aspx">IE getAttribute</a>
	 * @see <a href="http://msdn.microsoft.com/en-us/library/dd347148.aspx">Attribute Differences in Internet Explorer 8</a>
	 */
	ATTRIBUTE_PROPERTY_MAP :
	{
		"value" : "value"
	},

	/** The set of names of elements that cannot be serialized as empty elements. */
	NON_EMPTY_ELEMENT_SET :
	{
		"div" : true,
		"label" : true,
		"span" : true,
		"textarea" : true
	}

};

//Element

if(typeof Element == "undefined") //if no Element type is defined
{
	var Element = {}; //create a new Element class
}

/**
 * Adds the given class name to the element's class attribute.
 * 
 * @param element The element that should be given a class.
 * @param className The name of the class to add.
 */
Element.addClassName = function(element, className)
{
	var classNamesString = element.className; //get the element's class names
	element.className = classNamesString ? classNamesString + " " + className : className; //append the class name if there is a class name already
};

/**
 * Adds or removed a given class name to the element's class attribute. When adding a class name to the element's class
 * attribute, it is only added if the given class name does not already exist.
 * 
 * @param element The element that should be given a class.
 * @param className The name of the class to add.
 * @param state <code>true</code> if the class name should be set, or <code>false</code> if it should be removed.
 */
Element.setClassName = function(element, className, state)
{
	if(state) //if the class name should be set
	{
		var classNameString = element.className; //get the element's class names
		if(classNameString) //if there is a class name already
		{
			if(this.hasClassName(element, className)) //if the element already has this class name
			{
				return;
			}
			classNamesString = classNamesString + " " + className; //append the class name
		}
		else
		//if there is as of yet no class name string
		{
			classNameString = className; //just assign the class name by itself
		}
		element.className = classNameString; //update the element class name
	}
	else
	//if we should unset the class name
	{
		this.removeClassName(element, className);
	}
};

/**
 * Retrieves a namespaced attribute value from an element using the DOM element.getAttributeNS() method. This method
 * correctly returns the empty string ("") as specified by the DOM, regardless of whether the underlying implementation
 * returns "" or null.
 * 
 * @param element The element the attribute value of which to retrieve.
 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
 * @param localName The local name of the attribute.
 * @returns The value of the namespaced attribute, or "" if the attribute is not defined.
 */
Element.getAttributeNSNative = function(element, namespaceURI, localName)
{
	var value = element.getAttributeNS(namespaceURI, localName); //get the attribute value
	return value != null ? value : ""; //return "" instead of null
};

/**
 * Retrieves a namespaced attribute value from an element using the DOM element.getAttribute() method. This method
 * correctly returns the empty string ("") as specified by the DOM, regardless of whether the underlying implementation
 * returns "" or null. This implementation looks up the prefix from the given namespace; therefore only namespaces
 * recognized by Guise are valid. This version assumes that any non-null namespace is that designated by
 * GUISE_ML_NAMESPACE_URI, for which the prefix "guise" will be used.
 * 
 * @param element The element the attribute value of which to retrieve.
 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
 * @param localName The local name of the attribute.
 * @returns The value of the namespaced attribute, or "" if the attribute is not defined.
 */
Element.getAttributeNSCustom = function(element, namespaceURI, localName) //TODO transfer to Guise
{
	var value = element.getAttribute(namespaceURI != null ? "guise:" + localName : localName); //get the attribute value using the correct prefix TODO use a constant
	return value != null ? value : ""; //return "" instead of null
};

/**
 * Removes a namespaced attribute from an element using the DOM element.removeAttributeNS() method.
 * 
 * @param element The element the attribute of which to remove.
 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
 * @param localName The local name of the attribute.
 */
Element.removeAttributeNSNative = function(element, namespaceURI, localName)
{
	element.removeAttributeNS(namespaceURI, localName); //remove the attribute
};

/**
 * Removes a namespaced attribute from an element using the DOM element.removeAttribute() method. This implementation
 * looks up the prefix from the given namespace; therefore only namespaces recognized by Guise are valid. This version
 * assumes that any non-null namespace is that designated by GUISE_ML_NAMESPACE_URI, for which the prefix "guise" will
 * be used.
 * 
 * @param element The element the attribute of which to remove.
 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
 * @param localName The local name of the attribute.
 */
Element.removeAttributeNSCustom = function(element, namespaceURI, localName) //TODO transfer to Guise
{
	element.removeAttribute(namespaceURI != null ? "guise:" + localName : localName); //remove the attribute using the correct prefix TODO use a constant
};

//add correct support for namespace-aware DOM methods
//Safari 1.3.2 requires namespaced attribute access in XMLHTTPRequest XML responses but requires non-namespaced attribute access in the HTML DOM.
if(document.documentElement.getAttributeNS) //if this DOM supports element.getAttributeNS()
{
	Element.getAttributeNS = Element.getAttributeNSNative;
}
else
//if this DOM doesn't support element.getAttributeNS()
{
	Element.getAttributeNS = Element.getAttributeNSCustom;
}

if(document.documentElement.removeAttributeNS) //if this DOM supports element.removeAttributeNS (such as Safari)
{
	Element.removeAttributeNS = Element.removeAttributeNSNative;
}
else
//if this DOM doesn't support element.removeAttributeNS()
{
	Element.removeAttributeNS = Element.removeAttributeNSCustom;
}

/**
 * Returns the given element has the given class. Multiple class names are supported.
 * 
 * @param element The element that should be checked for class.
 * @param className The name of the class for which to check, or a regular expression if a match should be found.
 * @returns The given class name, which will be the regular expression match if a regular expression is used, or null if
 *          there is no matching class name.
 */
Element.getClassName = function(element, className)
{
	var classNamesString = element.className; //get the element's class names
	var classNames = classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY; //split out the class names
	var index = className instanceof RegExp ? classNames.indexOfMatch(className) : classNames.indexOf(className); //get the index of the matching class name
	return index >= 0 ? classNames[index] : null; //return the matching class name, if there is one
};

/**
 * Determines whether the given element has the given class. Multiple class names are supported.
 * 
 * @param element The element that should be checked for class.
 * @param className The name of the class for which to check, or a regular expression if a match should be found.
 * @returns true if one of the element's class names equals the given class name.
 */
Element.hasClassName = function(element, className)
{
	return this.getClassName(element, className) != null; //see if we can find a matching class name
};

/**
 * Removes the given class name from the element's class attribute.
 * 
 * @param element The element that should have a class removed.
 * @param className The name of the class to remove.
 */
Element.removeClassName = function(element, className)
{
	var classNamesString = element.className; //get the element's class names
	var classNames = classNamesString ? classNamesString.split(/\s/) : EMPTY_ARRAY; //split out the class names
	for( var i = classNames.length - 1; i >= 0; --i) //for each index (starting from the end so that we can remove indices at will)
	{
		if(classNames[i] == className) //if this is a class name to remove
		{
			classNames.remove(i); //remove this index
		}
	}
	element.className = classNames.join(" "); //join the remaining class names back together and assign them back to the element's class name
};

if(document.documentElement.setAttributeNS) //if this DOM supports element.setAttributeNS()
{

	/**
	 * Sets a namespaced attribute of an element using the DOM element.setAttributeNS() method.
	 * 
	 * @param element The element the attribute of which to set.
	 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
	 * @param qualifiedName The qualifiedName of the attribute.
	 * @param value The new value of the attribute.
	 */
	Element.setAttributeNS = function(element, namespaceURI, qualifiedName, value)
	{
		element.setAttributeNS(namespaceURI, qualifiedName, value); //set the attribute value
	};
}
else
//if this DOM doesn't support element.setAttributeNS()
{

	/**
	 * Sets a namespaced attribute of an element using the DOM element.setAttribute() method.
	 * 
	 * @param element The element the attribute of which to set.
	 * @param namespaceURI The string designating the namespace of the attribute, or null for no namespace.
	 * @param qualifiedName The qualifiedName of the attribute.
	 */
	Element.setAttributeNS = function(element, namespaceURI, qualifiedName, value)
	{
		element.setAttribute(qualifiedName, value); //set the attribute value without using a namespace
	};

}

//Node

if(typeof Node == "undefined") //if no Node type is defined (e.g. IE6), create one to give us constant node types
{
	var Node =
	{
		ELEMENT_NODE : 1,
		ATTRIBUTE_NODE : 2,
		TEXT_NODE : 3,
		CDATA_SECTION_NODE : 4,
		ENTITY_REFERENCE_NODE : 5,
		ENTITY_NODE : 6,
		PROCESSING_INSTRUCTION_NODE : 7,
		COMMENT_NODE : 8,
		DOCUMENT_NODE : 9,
		DOCUMENT_TYPE_NODE : 10,
		DOCUMENT_FRAGMENT_NODE : 11,
		NOTATION_NODE : 12
	};
}

/**
 * Retrieves the ancestor element with the given class of the given node, starting at the node itself. Multiple class
 * names are supported.
 * 
 * @param node The node the ancestor of which to find, or null if the search should not take place.
 * @param className The name of the class for which to check, or a regular expression if a match should be found.
 * @returns The element with the given class in which the node lies, or null if the node is not within such an element.
 */
Node.getAncestorElementByClassName = function(node, className)
{
	while(node) //while we haven't reached the top of the hierarchy
	{
		if(node.nodeType == Node.ELEMENT_NODE && Element.hasClassName(node, className)) //if this is an element and this class name is one of the class names
		{
			return node; //this node has a matching class name; we'll use it
		}
		node = node.parentNode; //try the parent node
	}
	return node; //return whatever node we found
};

/**
 * Retrieves all the ancestor elements, including the given element, if any, with the given class of the given node,
 * starting at the node itself. Multiple class names are supported.
 * 
 * @param node The node the ancestor of which to find, or null if the search should not take place.
 * @param className The name of the class for which to check, or a regular expression if a match should be found.
 * @returns A non-null array of elements with the given class in which the node lies.
 */
Node.getAncestorElementsByClassName = function(node, className)
{
	var ancestorElements = new Array(); //create a new array
	var ancestorElement = this.getAncestorElementByClassName(node, className); //get the first ancestor element
	while(ancestorElement) //while we're not out of ancestor elements
	{
		ancestorElements.add(ancestorElement); //add this ancestor element
		ancestorElement = this.getAncestorElementByClassName(ancestorElement.parentNode, className); //get the next ancestor element
	}
	return ancestorElements; //return the ancestor elements we found
};

/**
 * Retrieves the named ancestor element of the given node, starting at the node itself.
 * 
 * @param node The node the ancestor of which to find, or null if the search should not take place.
 * @param elementName The name of the element to find.
 * @returns The named element in which the node lies, or null if the node is not within such a named element.
 */
Node.getAncestorElementByName = function(node, elementName)
{
	while(node && (node.nodeType != Node.ELEMENT_NODE || node.nodeName.toLowerCase() != elementName)) //while we haven't found the named element
	{
		node = node.parentNode; //get the parent node
	}
	return node; //return the element we found
};

/**
 * Retrieves the ancestor element with the given style of the given node, starting at the node itself.
 * 
 * @param node The node the ancestor of which to find, or null if the search should not take place.
 * @param styleName The name of the style for which to check.
 * @param styleValue The name of the value for which to check.
 * @returns The element with the given style value in which the node lies, or null if the node is not within such an
 *          element.
 */
Node.getAncestorElementByStyle = function(node, styleName, styleValue)
{
	while(node) //while we haven't reached the top of the hierarchy
	{
		if(node.nodeType == Node.ELEMENT_NODE && node.style[styleName] == styleValue) //if this is an element and it has the requested style
		{
			return node; //this node has a matching style; we'll use it
		}
		node = node.parentNode; //try the parent node
	}
	return node; //return whatever node we found
};

/**
 * Determines the document tree depth of the given node, returning a zero-level depth for the document node.
 * 
 * @param node The node for which a depth should be found.
 * @returns The zero-based depth of the given node in the document, with a zero-level depth for the document node.
 */
Node.getDepth = function(node)
{
	var depth = -1; //this node will be at least depth zero
	do
	{
		node = node.parentNode; //get the parent node
		++depth; //increase the depth
	}
	while(node); //keep getting the parent node while there are ancestors left
	return depth; //return the depth we calculated
};

/**
 * Retrieves the descendant element with the given class of the given node, starting at the node itself. Multiple class
 * names are supported.
 * 
 * @param node The node the descendant of which to find, or null if the search should not take place.
 * @param className The name of the class for which to check, or a regular expression if a match should be found.
 * @returns The element with the given class for which the given node is a parent or itself, or null if there is no such
 *          element descendant.
 */
Node.getDescendantElementByClassName = function(node, className)
{
	if(node) //if we have a node
	{
		if(node.nodeType == Node.ELEMENT_NODE && Element.hasClassName(node, className)) //if this is an element with the given class name
		{
			return node; //show that we found a matching element class name
		}
		var childNodeList = node.childNodes; //get all the child nodes
		var childNodeCount = childNodeList.length; //find out how many children there are
		for( var i = 0; i < childNodeCount; ++i) //for each child node
		{
			var childNode = childNodeList[i]; //get this child node
			var match = this.getDescendantElementByClassName(childNode, className); //see if we can find the node in this branch
			if(match) //if we found a match
			{
				return match; //return it
			}
		}
	}
	return null; //show that we didn't find a matching element
};

/**
 * Retrieves the descendant element with the given name and attributes, starting at the node itself.
 * 
 * @param node The node the descendant of which to find, or null if the search should not take place.
 * @param elementName The name of the element to find.
 * @param parameters An associative array of name/value pairs, each representing an attribute name and value that should
 *          be present (or, if the parameter value is null, an attribute that must not be present), or null if no
 *          parameter matches are requested.
 * @returns The element with the given name, or null if there is no such element descendant.
 */
Node.getDescendantElementByName = function(node, elementName, parameters)
{
	if(node) //if we have a node
	{
		if(node.nodeType == Node.ELEMENT_NODE && node.nodeName.toLowerCase() == elementName) //if this is an element with the given name
		{
			var parametersMatch = true; //start out assuming that the parameters match		
			if(parameters != null) //if parameters were provided
			{
				for( var parameterName in parameters) //for each parameter
				{
					if(node.getAttribute(parameterName) != parameters[parameterName]) //if this attribute doesn't exist or doesn't have the correct value
					{
						parametersMatch = false; //indicate that the parameters do not match
						break; //stop searching for a non-match
					}
				}
			}
			if(parametersMatch) //if all the parameters match
			{
				return node; //show that we found a matching element name and attribute(s)
			}
		}
		var childNodeList = node.childNodes; //get all the child nodes
		var childNodeCount = childNodeList.length; //find out how many children there are
		for( var i = 0; i < childNodeCount; ++i) //for each child node
		{
			var childNode = childNodeList[i]; //get this child node
			var match = this.getDescendantElementByName(childNode, elementName, parameters); //see if we can find the node in this branch
			if(match) //if we found a match
			{
				return match; //return it
			}
		}
	}
	return null; //show that we didn't find a matching element
};

/**
 * Determines whether the given node has the indicated ancestor, including the node itself in the search.
 * 
 * @param node The node the ancestor of which to find, or null if the search should not take place.
 * @param ancestor The ancestor to find.
 * @returns true if the node or one of its ancestors is the given ancestor.
 */
Node.hasAncestor = function(node, ancestor)
{
	while(node) //while we haven't ran out of nodes
	{
		if(node == ancestor) //if this node is the ancestor
		{
			return true; //show that there was such an ancestor
		}
		node = node.parentNode; //go up one level
	}
	return false; //indicate that we didn't find the given ancestor
};

/**
 * Removes a child node from its parent, if any, and then replaces it at the exact spot in the tree.
 * 
 * @param node The node to refresh.
 */
Node.refresh = function(node)
{
	var parentNode = node.parentNode; //get the node's parent
	if(parentNode != null) //if the node has a parent
	{
		var nextSibling = node.nextSibling; //get the node's next sibling, if any
		parentNode.insertBefore(parentNode.removeChild(node), nextSibling); //remove the node and then insert it before its old next sibling (or at the end of the elements if there was no next sibling)
	}
};

/**
 * A class maintaining event function information and optionally adapting an event to a W3C compliant version.
 * <ul>
 * <li>charCode</li>
 * </ul>
 * A decorator will be created for the event function.
 * 
 * @param currentTarget The object for which a listener should be added.
 * @param eventType The type of event.
 * @param fn The function to listen for the event.
 * @param useCapture Whether event capture should be used.
 * @param createDecorator Whether a decorator should be created to wrap the event function and ensure W3DC compliancy.
 *          var decorator The decorator created to wrap the event function and ensure W3C compliancy.
 */
com.globalmentor.dom.EventListener = function(currentTarget, eventType, fn, useCapture, createDecorator)
{
	this.currentTarget = currentTarget;
	this.eventType = eventType;
	this.fn = fn;
	this.useCapture = useCapture;
	var proto = com.globalmentor.dom.EventListener.prototype;
	if(!proto._initialized)
	{
		proto._initialized = true;

		/**
		 * Creates an event function decorator to appropriately set up the event to be W3C compliant, including
		 * event.currentTarget support.
		 * 
		 * @param eventFunction The event function to be decorated.
		 * @param currentTarget The node on which the event listener is to be registered.
		 */
		proto._createDecorator = function(eventFunction, currentTarget)
		{
			var eventListener = this; //store the event listener so that it can be referenced later via closure
			return function(event) //create the decorator function
			{
				event = eventListener._getW3CEvent(event, currentTarget); //make sure the event is a W3C-compliant event
				eventFunction(event); //call the event function with our new event information
			};
		};

		/**
		 * Retrieves W3C event information in a cross-browser manner.
		 * 
		 * @param event The event information, or null if no event information is available (e.g. on IE).
		 * @param currentTarget The current target (the node to which the event listener is bound), or null if the current
		 *          target is not known.
		 * @returns A W3C-compliant event object.
		 */
		proto._getW3CEvent = function(event, currentTarget)
		{
			if(!event) //if no event was passed
			{
				if(window.event) //if IE has provided an event object
				{
					event = window.event; //switch to using the IE event
				}
				else
				//if there is no IE event object
				{
					//TODO throw an assertion
				}
			}
			//fix event.target
			if(!event.target) //if there is no target information
			{
				//TODO assert event.srcElement
				event.target = event.srcElement; //assign a W3C target property
			}
			//fix event.currentTarget
			if(!event.currentTarget && currentTarget) //if there is no current target information, but one was passed to us
			{
				event.currentTarget = currentTarget; //assign a W3C current target property
			}
			//fix event.charCode
			if(!event.charCode) //if this event has no character code
			{
				event.charCode = event.which ? event.which : event.keyCode; //use the NN4  key indication if available; otherwise, use the key code TODO make sure this is a key event, because NN4 uses event.which for mouse events, too			
			}
			//fix event.stopPropagation
			if(!event.stopPropagation) //if there is no method for stopping propagation
			{
				//TODO assert window.event && window.event.cancelBubble
				if(window.event && typeof window.event.cancelBubble == "boolean") //if there is a global event with a cancel bubble property (e.g. IE)
				{
					event.stopPropagation = function() //create a new function to stop propagation
					{
						window.event.cancelBubble = true; //stop bubbling the IE way
					};
				}
			}
			//fix event.preventDefault
			if(!event.preventDefault) //if there is no method for preventing the default functionality
			{
				//TODO assert window.event && window.event.returnValue
				//TODO find out why IE returns "undefined" for window.event.returnValue, yet enumerates it in for:in		if(window.event && typeof window.event.returnValue=="boolean")	//if there is a global event with a return value property (e.g. IE)
				if(window.event) //if there is a global event with a return value property (e.g. IE)
				{
					event.preventDefault = function() //create a new function to stop propagation
					{
						window.event.returnValue = false; //prevent default functionality the IE way
					};
				}
			}
			/*TODO this doesn't seem to be possible; IE doesn't let use change event.button
						if(isUserAgentIE)	//if this is an IE browser, change the event button to match the W3C's definition; see http://www.quirksmode.org/dom/w3c_events.html
						{
							var button=event.button;	//get the button pressed
							if(typeof button!="undefined")	//if there is a button variable
							{
								if(button&1)	//if this was the left button
								{
									event.button=MOUSE_BUTTON.LEFT;
								}
								else if(button&2)	//if this was the right button
								{
									event.button=MOUSE_BUTTON.RIGHT;
								}
								else if(button&4)	//if this was the middle button
								{
									event.button=MOUSE_BUTTON.MIDDLE;
								}
							}
						}
			*/
			//TODO update clientX and clientY as per _DHTML Utopia_ page 90.
			switch(event.type)
			//for special types of events
			{
				case "mouseover":
					if(!event.relatedTarget && event.fromElement) //if there is no related target information, but there is an IE fromElement property
					{
						event.relatedTarget = event.fromElement; //use the fromElement property value
					}
					break;
				case "mouseout":
					if(!event.relatedTarget && event.toElement) //if there is no related target information, but there is an IE toElement property
					{
						event.relatedTarget = event.toElement; //use the toElement property value
					}
					break;
			}
			return event; //return our event
		};
	}
	this.decorator = createDecorator ? this._createDecorator(fn, currentTarget) : null; //create the decorator function if we were asked to do so
};

/** A class that manages events. */
com.globalmentor.dom.EventManager =
{
	/** The array of event listeners. */
	_eventListeners : [],

	/**
	 * Adds an event listener to an object.
	 * 
	 * @param object The object for which a listener should be added.
	 * @param eventType The type of event.
	 * @param fn The function to listen for the event.
	 * @param useCapture Whether event capture should be used.
	 * @see <a href="http://www.scottandrew.com/weblog/articles/cbs-events">Crossbrowser DOM Scripting: Event Handlers</a>
	 */
	addEvent : function(object, eventType, fn, useCapture)
	{
		var eventListener = null; //we'll create an event listener and hold it here
		var result = true; //we'll store the result here
		if(object.addEventListener) //if the W3C DOM method is supported
		{
			object.addEventListener(eventType, fn, useCapture); //add the event normally
			eventListener = new com.globalmentor.dom.EventListener(object, eventType, fn, useCapture, false); //create an event listener to keep track of the information
		}
		else
		//if the W3C version isn't available
		{
			var eventName = "on" + eventType; //create the event name
			if(object.attachEvent) //if we can use the IE version
			{
				eventListener = new com.globalmentor.dom.EventListener(object, eventType, fn, useCapture, true); //create an event listener with a decorator
				result = object.attachEvent(eventName, eventListener.decorator); //attach the function decorator
			}
			else
			//if we can't use the IE version
			{
				eventListener = new com.globalmentor.dom.EventListener(object, eventType, fn, useCapture, true); //create an event listener with a decorator
				object[eventName] = eventListener.decorator; //use the object.onEvent property and our decorator
			}
		}
		this._eventListeners.add(eventListener); //add this listener to the list
		return result; //return the result
	},

	/**
	 * Removes an event listener from an object.
	 * 
	 * @param object The object for which a listener should be removed.
	 * @param eventType The type of event.
	 * @param fn The function listening for the event.
	 * @param useCapture Whether event capture should be used.
	 * @param eventListener The optional event listener object containing the event decorator; if not provided it will be
	 *          retrieved and removed from the list of event listeners.
	 * @see <a href="http://www.scottandrew.com/weblog/articles/cbs-events">Crossbrowser DOM Scripting: Event Handlers</a>
	 */
	removeEvent : function(object, eventType, fn, useCapture, eventListener)
	{
		if(!eventListener) //if no event listener was provided
		{
			eventListener = this._removeEventListener(object, eventType, fn); //remove and retrieve the event listener keeping information about this event			
		}
		if(object.removeEventListener) //if the W3C DOM method is supported
		{
			object.removeEventListener(eventType, fn, useCapture); //remove the event normally
		}
		else
		//if the W3C version isn't available
		{
			var eventName = "on" + eventType; //create the event name
			if(object.detachEvent) //if we can use the IE version
			{
				result = eventListener != null ? object.detachEvent(eventName, eventListener.decorator) : null; //detach the function decorator, if there is one
			}
			else
			//if we can't use the IE version
			{
				object[eventName] = null; //use the object.onEvent property
			}
		}
	},

	/**
	 * Clears all registered events, optionally for a specific object.
	 * 
	 * @param object The object for which events should be cleared, or null if events should be cleared on all objects.
	 */
	clearEvents : function(object)
	{
		var eventListeners = this._eventListeners; //get a reference to our event listeners
		if(object) //if an object is specified
		{
			for( var i = eventListeners.length - 1; i >= 0; --i) //for each event listener, going backwards so that removing an event listener will not disturb iteration
			{
				var eventListener = eventListeners[i]; //get the last event listener
				if(!object || eventListener.currentTarget == object) //if this event listener was registered on this object, or if all event listeners should be removed)
				{
					eventListeners.remove(i); //remove this event listener
					this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture, eventListener); //remove this event, specifying the event listener so that it doesn't have to be looked up again
				}
			}
		}
		else
		//if all event listeners should be cleared (though these loops could be combined, this is more efficient, and as this is usually called before a page unloads, we want to do this as fast as possible
		{
			for( var i = eventListeners.length - 1; i >= 0; --i) //for each event listener
			{
				var eventListener = eventListeners[i]; //get the last event listener
				this.removeEvent(eventListener.currentTarget, eventListener.eventType, eventListener.fn, eventListener.useCapture, eventListener); //remove this event, specifying the event listener so that it doesn't have to be looked up again
			}
			eventListeners.clear(); //clear all event listeners in one fell swoop
		}
	},

	/**
	 * Removes and returns an event listener object encapsulating information on the object, event type, and function.
	 * 
	 * @param object The object for which a listener is listening.
	 * @param eventType The type of event.
	 * @param fn The function listening for the event.
	 * @returns The event listener or <code>null</code> if no matching event listener could be found.
	 */
	_removeEventListener : function(object, eventType, fn)
	{
		var eventListeners = this._eventListeners; //get a reference to our event listeners
		for( var i = eventListeners.length - 1; i >= 0; --i) //for each event listener
		{
			var eventListener = eventListeners[i]; //get this event listener
			if(eventListener.currentTarget == object && eventListener.eventType == eventType && eventListener.fn == fn) //if this is the event listener
			{
				eventListeners.remove(i); //remove this event listener
				return eventListener; //return this event listener
			}
		}
		return null; //indicate that we couldn't find a matching event listener
	}
};

/**
 * Adds a function to be called when a window loads.
 * 
 * @param func The function to listen for window loading.
 * @see <a href="http://simon.incutio.com/archive/2004/05/26/addLoadEvent">addLoadEvent</a>
 */
/*TODO transfer to EventManager and modify as fix for Safari
function addLoadListener(func)
{
	var oldonload=window.onload;	//get the old function
	if(typeof window.onload!="function")	//if there is no onload function
	{
		window.onload=func;	//use the given function
	}
	else	//if there is a window onload function
	{
		window.onload=function()	//create a new function
				{
					oldonload();	//call the old function using closure
					func();	//call the new function
				};
	}
}
*/

com.globalmentor.dom.EventManager.addEvent(window, "unload", com.globalmentor.dom.EventManager.clearEvents.bind(com.globalmentor.dom.EventManager), false); //automatically do the appropriate uninitialization when the window unloads
