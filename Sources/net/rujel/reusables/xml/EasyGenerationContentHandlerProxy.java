/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Modified By Gennady Kushnir for RUJEL project, Oct 2011 */

package net.rujel.reusables.xml;

//SAX
import java.util.Iterator;
import java.util.Stack;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is an implementation of ContentHandler which acts as a proxy to
 * another ContentHandler and has the purpose to provide a few handy methods
 * that make life easier when generating SAX events.
 * <br>
 * Note: This class is only useful for simple cases with no namespaces.
 */

public class EasyGenerationContentHandlerProxy implements ContentHandler {

    /** An empty Attributes object used when no attributes are needed. */
    public static final Attributes EMPTY_ATTS = new AttributesImpl();

    private ContentHandler target;
    private Stack<String> stack = new Stack<String>();
    private AttributesImpl attributes;

    /**
     * Main constructor.
     * @param forwardTo ContentHandler to forward the SAX event to.
     */
    public EasyGenerationContentHandlerProxy(ContentHandler forwardTo) {
        this.target = forwardTo;
    }

    /**
     * Recent SAX element local name.
     * While diving into XML structure element names are written into stack.
     * This method peeks last element in that stack. 
     * @return recent SAX element local name or null if in root
     */
    public String recentElement() {
    	if(stack.isEmpty())
    		return null;
    	return stack.peek();
    }
    
    /**
     * Full path to recent element.
     * While diving into XML structure element names are written into stack.
     * This method prints out that stack. 
     * @return full path to recent element or "/" if in root.
     */
    public String recentPath() {
    	if(stack.isEmpty())
    		return "/";
    	StringBuilder buf = new StringBuilder();
    	Iterator<String> iterator = stack.iterator();
    	while (iterator.hasNext()) {
			buf.append('/').append(iterator.next());
		}
    	return buf.toString();
    }
    
    /**
     * Prepares attribute of default type "CDATA" for the next coming element.
     * @param name Name for the attribute.
     * @param value Value for the attribute.
     */
    public void prepareAttribute(String name, String value) {
    	prepareAttribute(name, "CDATA", value);
    }
    
    /**
     * Prepares attribute for hthe next coming element.
     * The attribute type is one of the strings "CDATA", "ID", "IDREF", "IDREFS",
     * "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES", or "NOTATION"
     * @param name Name for the attribute.
     * @param type Attribute type.
     * @param value Value for the attribute.
     */
    public void prepareAttribute(String name, String type, String value) {
    	if(attributes == null)
    		attributes = new AttributesImpl();
    	attributes.addAttribute(null, name, name, type, value);
    }
    
    /**
     * Remove all prepared attributes
     */
    public void dropAttributes() {
    	attributes = null;
    }
    
    /**
     * Sends the notification of the beginning of an element.
     * @param name Name for the element.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void startElement(String name) throws SAXException {
    	if(attributes == null) {
    		startElement(name, EMPTY_ATTS);
    	} else {
    		startElement(name,attributes);
    		attributes = null;
    	}
    }


    /**
     * Sends the notification of the beginning of an element.
     * @param name Name for the element.
     * @param atts The attributes attached to the element. If there are no
     * attributes, it shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void startElement(String name, Attributes atts) throws SAXException {
        startElement(null, name, name, atts);
    }


    /**
     * Send a String of character data.
     * @param s The content String
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void characters(String s) throws SAXException {
        characters(s.toCharArray(), 0, s.length());
    }


    /**
     * Send the notification of the end of an element.
     * @param name Name for the element.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void endElement(String name) throws SAXException {
        endElement(null, name, name);
    }


    /**
     * Sends notifications for a whole element with some String content.
     * @param name Name for the element.
     * @param value Content of the element.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void element(String name, String value) throws SAXException {
    	if(attributes == null) {
    		element(name, value, EMPTY_ATTS);
    	} else {
    		element(name, value, attributes);
    		attributes = null;
    	}
    }


    /**
     * Sends notifications for a whole element with some String content.
     * @param name Name for the element.
     * @param value Content of the element.
     * @param atts The attributes attached to the element. If there are no
     * attributes, it shall be an empty Attributes object.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public void element(String name, String value, Attributes atts) throws SAXException {
        startElement(name, atts);
        if (value != null) {
            characters(value);
        }
        endElement(name);
    }

    /* =========== ContentHandler interface =========== */

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        target.setDocumentLocator(locator);
    }


    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        target.startDocument();
    }


    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        target.endDocument();
    }


    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        target.startPrefixMapping(prefix, uri);
    }


    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        target.endPrefixMapping(prefix);
    }


    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName,
                        String qName, Attributes atts) throws SAXException {
        target.startElement(namespaceURI, localName, qName, atts);
        stack.push(localName);
    }


    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
                                                        throws SAXException {
        target.endElement(namespaceURI, localName, qName);
        String pop = stack.pop();
        if(!pop.equals(localName)) {
        	stack.push(pop);
        	throw new IllegalStateException("Should not end element'" + localName +
        			"' until internal elements ended");
        }
    }


    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        target.characters(ch, start, length);
    }


    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        target.ignorableWhitespace(ch, start, length);
    }


    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String aTarget, String data) throws SAXException {
        this.target.processingInstruction(aTarget, data);
    }


    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        target.skippedEntity(name);
    }

}