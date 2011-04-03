// Tabs.java: Class file for WO Component 'Tabs'

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

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.*;

public class Tabs extends WOComponent {
	//protected NSArray source;
	
    /** @TypeInfo net.rujel.ui.LessonTab */
    //public NSArray tablist;
	//public String titleAttribute;
	
	//public Object currTab;
	public Object tabItem;
	public void setTabItem(Object item) {
		tabItem = item;
		_attribs = null;
	}

	
    public Tabs(WOContext context) {
        super(context);
    }
	
	private Object[] _attribs;
    public Number index;
	protected Object[] attribs() {
		if(_attribs == null) {
			NSMutableArray attribs = new NSMutableArray();
			int i = 1;
				String key = "key" + i;
			while(hasBinding(key)) {
				String attr = (String)valueForBinding(key);
				if(attr != null) {
					attribs.addObject(NSKeyValueCodingAdditions.
							Utility.valueForKeyPath(tabItem, attr));
				} else {
					attribs.addObject(NSKeyValueCoding.NullValue);
				}
				i++;
				key = "key" + i;
			}
			_attribs = attribs.objects();
		}
		return _attribs;
	}
	
	public String title() {
		String attr = (String)valueForBinding("formatter");
		if(attr != null) {
			return String.format(attr,attribs());
		}
		
		attr = (String)valueForBinding("titleAttribute");
		if(attr != null && tabItem instanceof NSKeyValueCoding) {
			Object title = NSKeyValueCodingAdditions.Utility.valueForKeyPath(tabItem, attr);
			if(title != null)
				return title.toString();
			else
				return "-null-";
//			if(attr.indexOf('.') > 0 && (tabItem instanceof NSKeyValueCodingAdditions))
//				return ((NSKeyValueCodingAdditions)tabItem).valueForKeyPath(attr).toString();
//			else
//				return ((NSKeyValueCoding)tabItem).valueForKey(attr).toString();
		}
		if (tabItem instanceof GenericTab) {
			GenericTab tab = (GenericTab) tabItem;
			return tab.title();
		}
		if(tabItem != null)
			return tabItem.toString();
		
		return "-=-";
	}
	
	public String hover() {
		String attr = (String)valueForBinding("hformatter");
		if(attr != null && tabItem instanceof NSKeyValueCodingAdditions) {
			return String.format(attr,attribs());
		}
		
		attr = (String)valueForBinding("hoverAttribute");
		if(attr != null) {
			Object value = NSKeyValueCodingAdditions.Utility.valueForKeyPath(tabItem, attr);
			return (value==null)?null:value.toString();
		}
		if (tabItem instanceof GenericTab) {
			GenericTab tab = (GenericTab) tabItem;
			return tab.hover();
		}
		return null;
	}
	
	protected int tabindex(Object tabValue) {
		NSArray tablist = (NSArray)valueForBinding("tablist");
		if(tablist == null) return NSArray.NotFound;
		return tablist.indexOfObject(tabValue);
	}

	public WOActionResults selectTab() {
		Object currTab = valueForBinding("currTab");
		if(currTab instanceof WOComponent) {
			if(tabItem == null)
				return null;
			if(tabItem instanceof WOActionResults)
				return (WOActionResults)tabItem;
			try {
				String pageName = (String)NSKeyValueCoding.Utility.valueForKey(tabItem, "page");
				WOComponent page = (WOComponent)currTab;
				if(page.name().endsWith(pageName))
					return page;
				page = pageWithName(pageName);
				pageName = (String)valueForBinding("pushValues");
				String[] values = pageName.split(";");
				for (int i = 0; i < values.length; i++) {
					Object val = NSKeyValueCoding.Utility.valueForKey(currTab, values[i]);
					page.takeValueForKey(val, values[i]);
				}
				return page;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		if(Various.boolForObject(valueForBinding("numeric"))) {
			if(index == null) {
				setValueForBinding(null,"currTab");
				return null;
			}
//			if(index.equals(currTab))
//				setValueForBinding(null,"currTab");
			setValueForBinding(index,"currTab");
		} else {
			Object selected = tabItem;
			if(Various.boolForObject(valueForBinding("byAttribute"))) {
				String key = (String)valueForBinding("idAttribute");
				selected = NSKeyValueCoding.Utility.valueForKey(selected, key);
			}
			if(selected == currTab)
				setValueForBinding(null,"currTab");
			setValueForBinding(selected,"currTab");
		}
//		tablist=(NSArray)valueForBinding("tablist");
		return (WOActionResults)valueForBinding("selectAction");
	}
	
	public String tabStyle() {
		Object currTab = valueForBinding("currTab");
		if(tabItem == null || index == null || currTab == null) return "grey";
		if(Various.boolForObject(valueForBinding("numeric"))) {
			if(index.equals(currTab)) return "selection";
		} else {
			if(tabItem == currTab)
				return "selection";
			String idAttribute = (String)valueForBinding("idAttribute");
			if(idAttribute == null && currTab instanceof WOComponent) {
				idAttribute = ((WOComponent)currTab).name();
				int idx = idAttribute.lastIndexOf('.');
				if(idx > 0)
					idAttribute = idAttribute.substring(idx +1);
				currTab = idAttribute;
				idAttribute = "page";
			}
			if(idAttribute != null) {
				try {
					Object recent = NSKeyValueCoding.Utility.valueForKey(tabItem, idAttribute);
					if (recent != null) {
						if (recent.equals(currTab))
							return "selection";
						if(!Various.boolForObject(valueForBinding("byAttribute"))) {
							Object sel = NSKeyValueCoding.Utility.valueForKey(currTab,idAttribute);
							if (recent.equals(sel))
								return "selection";
						}
					}
				} catch (Exception e) {
					;
				}
			} else {
				if(tabItem.equals(currTab))
					return "selection";
			}
		}
		return "grey";
	}
	
	public void setOnClick(String onClick){
		;
	}
	
	public String onClick() {
		if(hasBinding("onClick"))
			return (String)valueForBinding("onClick");
		String href = context().componentActionURL();
		String result = "location = '" + href + "';return true;";
		return result;
    }
	
	public boolean isStateless() {
		return true;
	}
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}

	public void reset() {
		_attribs = null;
	}

	public static interface GenericTab {
		public String title();
		public String hover();
		
		public EOQualifier qualifier();
		public boolean defaultCurrent();
		
		public boolean equals(Object aTab);
	}

	public String tabID() {
		String idAttribute = (String)valueForBinding("idAttribute");
		if(idAttribute == null)
			return null;
		Object id = NSKeyValueCoding.Utility.valueForKey(tabItem, idAttribute);
		return (id == null)?null:id.toString();
	}
}
