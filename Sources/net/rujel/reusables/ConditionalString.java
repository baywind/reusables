package net.rujel.reusables;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class ConditionalString extends ExtDynamicElement {

    public ConditionalString(String name, NSDictionary associations,WOElement template) {
		super(name, associations, template);
		checkRequired(associations, "condition");
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	Object cond = valueForBinding("condition",aContext);
    	Object value = null;
    	if(cond == null) {
    		if(hasBinding("ifNull", aContext)) {
				value = valueForBinding("ifNull",aContext);
    		} else if (hasBinding("ifFalse", aContext)) {
    			value = valueForBinding("ifFalse",aContext);
    		}
    	} else if(Various.boolForObject(cond)) {
    		if (hasBinding("ifTrue", aContext)) {
    			value = valueForBinding("ifTrue",aContext);
    		} else {
    			value = valueForBinding(cond.toString(),aContext);
    		}
    	} else if (hasBinding("ifFalse", aContext)) {
    		value = valueForBinding("ifFalse",aContext);
    	} else {
    		value = valueForBinding(cond.toString(),aContext);
    	}
    	if(value != null) {
    		if(Various.boolForObject(valueForBinding("escapeHTML", aContext)))
    			value = WOMessage.stringByEscapingHTMLString(value.toString());
    		aResponse.appendContentString(value.toString());
    	}
	}
}
