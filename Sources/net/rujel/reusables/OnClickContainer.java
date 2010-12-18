//OnClickContainer.java  Class file for WO Component 'OnClickContainer'

/*
 * Copyright (c) 2008, Gennady & Michael Kushnir
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * 	•	Redistributions of source code must retain the above copyright notice, this
 * 		list of conditions and the following disclaimer.
 * 	•	Redistributions in binary form must reproduce the above copyright notice,
 * 		this list of conditions and the following disclaimer in the documentation
 * 		and/or other materials provided with the distribution.
 * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
 * 		to endorse or promote products derived from this software without specific 
 * 		prior written permission.
 * 		
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.rujel.reusables;

import java.util.Enumeration;


import com.webobjects.appserver.*;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

// Generated by the WOLips Templateengine Plug-in at Aug 6, 2008 5:58:38 PM
public class OnClickContainer extends ExtDynamicElement {
     
    public OnClickContainer(String name, NSDictionary associations,WOElement template) {
		super(name, associations, template);

		checkRequired(associations, "elementName");
	}

    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(Various.boolForObject(valueForBinding("hide", aContext)))
    		return;
    	NSMutableArray bindingsList = bindingKeys().mutableClone();
    	String tagName = (String)valueForBinding("elementName",aContext);
    	bindingsList.removeObject("elementName");
    	//String action = (String)valueForBinding("action");
    	bindingsList.removeObject("invokeAction");
    	String onclick = null;
    	boolean disabled = Various.boolForObject(valueForBinding("disabled",aContext));
    	bindingsList.removeObject("disabled");
    	if(!disabled) {
    		onclick = (String)valueForBinding("onclick",aContext);
    		if(onclick == null && hasBinding("invokeAction",aContext))
    			onclick = "location = '" + aContext.componentActionURL() + "';return true;";
    	}
    	if(onclick == null)
    		disabled = true;
    	bindingsList.removeObject("onclick");

    	aResponse.appendContentCharacter('<');
    	aResponse.appendContentString(tagName);
    	if(!disabled) {
    		String point = (String)valueForBinding("parent", aContext);
    		if(point == null) {
    			point = "this";
    		} else {
    			point = "get(this,'" + point + "')";
    		}
    		aResponse.appendContentString(" onmouseover=\"dim(");
    		aResponse.appendContentString(point);
    		aResponse.appendContentString(");\" onmouseout=\"unDim(");
    		aResponse.appendContentString(point);
    		aResponse.appendContentString(");\" onclick=\"");
    		aResponse.appendContentString(onclick);
    		aResponse.appendContentCharacter('"');
    	}
    	Enumeration enu = bindingsList.objectEnumerator();
    	while (enu.hasMoreElements()) {
    		String cur = (String) enu.nextElement();
    		Object value = valueForBinding(cur,aContext);
    		if(value != null) {
    			aResponse.appendContentCharacter(' ');
    			aResponse.appendContentString(cur);
    			aResponse.appendContentCharacter('=');
    			aResponse.appendContentCharacter('"');
    			aResponse.appendContentString(value.toString());
    			aResponse.appendContentCharacter('"');
    		}
    	}
    	aResponse.appendContentCharacter('>');
    	super.appendToResponse(aResponse, aContext);
    	aResponse.appendContentString("</");
    	aResponse.appendContentString(tagName);
    	aResponse.appendContentCharacter('>');

    }

    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if(Various.boolForObject(valueForBinding("hide", aContext)))
    		return null;
    	return super.invokeAction(aRequest, aContext);
    }
	
/*    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if(aContext.elementID().equals(aContext.senderID())) {
    		return (WOActionResults)valueForBinding("invokeAction",aContext);
    	}
    	return super.invokeAction(aRequest, aContext);
    }*/
	
	protected WOActionResults action(WOContext aContext) {
		return (WOActionResults)valueForBinding("invokeAction",aContext);
	}
}