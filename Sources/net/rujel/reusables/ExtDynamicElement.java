package net.rujel.reusables;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.*;

public class ExtDynamicElement extends WODynamicElement {
	protected WOElement children;
	protected NSDictionary bindings;

	public ExtDynamicElement(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		bindings = associations;
		children = template;
	}

	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		if(children != null)
			children.appendToResponse(aResponse, aContext);
	}
	
	public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
		if(children != null)
			return children.invokeAction(aRequest, aContext);
		return null;
	}
	
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		if(children != null)
			children.takeValuesFromRequest(aRequest, aContext);
	}
	
	public NSArray bindingKeys() {
		if(bindings != null)
			return bindings.allKeys();
		return null;
	}

	public boolean hasBinding(String aBindingName, WOContext context) {
		if(bindings == null)
			return false;
		WOAssociation association = (WOAssociation)bindings.valueForKey(aBindingName);
		return (association != null);
	}
	
	public Object valueForBinding(String aBindingName, WOContext context) {
		if(bindings == null)
			return null;
		WOAssociation association = (WOAssociation)bindings.valueForKey(aBindingName);
		if(association == null)
			return null;
		return association.valueInComponent(context.component());
	}
	
	public void setValueForBinding(Object aValue, String aBindingName, WOContext context) {
		if(bindings == null)
			throw new IllegalStateException("No bindings a re set");
		WOAssociation association = (WOAssociation)bindings.valueForKey(aBindingName);
		if(association == null)
			throw new IllegalArgumentException("No such binding found: " + aBindingName);
		association.setValue(aValue, context.component());
	}
	
	protected void checkRequired (NSDictionary associations, String attribute) {
		if(associations.objectForKey(attribute) == null) {
			throw new WODynamicElementCreationException('<' + getClass().getName() + 
					"> Missing required attribute '" + attribute + '\'');
		}
	}
}
