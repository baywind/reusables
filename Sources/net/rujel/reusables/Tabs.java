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
	/*
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
//		source = (NSArray)valueForBinding("source");
		sync();
		if (currTab == null && tablist != null)
			currTab = (BaseTab)tablist.lastObject();
		if(currTab != null) {
			EduLesson lesson = (EduLesson)valueForBinding("currLesson");
			if(lesson != null && !currTab.containsLesson(lesson)) {
				int lesNum = lesson.number().intValue();
				int currNum = tablist.indexOfObject(currTab);
				if(lesNum < currTab.startIndex()) {
					while (currNum >= 0 && lesNum < currTab.startIndex()) {
						currNum--;
						currTab = (BaseTab)tablist.objectAtIndex(currNum);
					}
				} else {
					while (currNum < (tablist.count() - 1) && !currTab.lessonsInTab().containsObject(lesson)) {
						currNum++;
						currTab = (BaseTab)tablist.objectAtIndex(currNum);
					}
				}
				setValueForBinding(currTab,"currTab");
			}
		}
			
		//
		if(currNum < 0) {
			//currTab = tabList.lastObject();
			currNum = tabList.count() - 1;
			currTab = (LessonTab)tabList.objectAtIndex(currNum);
		}
		EduLesson lesson = (EduLesson)valueForBinding("currLesson");
		if (lesson != null) {
			int lesNum = source.indexOfObject(lesson);
			if (lesNum >=0 && !currTab.lessonsInTab().containsObject(lesson)) {
				if(lesNum < currTab.startIndex()) {
					while (currNum >= 0 && lesNum < currTab.startIndex()) {
						currNum--;
						currTab = (LessonTab)tabList.objectAtIndex(currNum);
					}
				} else {
					while (currNum < tabList.count() && !currTab.lessonsInTab().containsObject(lesson)) {
						currNum++;
						currTab = (LessonTab)tabList.objectAtIndex(currNum);
					}
				}
			}
		}
		
		super.appendToResponse(aResponse,aContext);
	}
	*/
	
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
					attribs.addObject(((NSKeyValueCodingAdditions)tabItem).valueForKeyPath(attr));
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
		if(attr != null && tabItem instanceof NSKeyValueCodingAdditions) {
			return String.format(attr,attribs());
		}
		
		attr = (String)valueForBinding("titleAttribute");
		if(attr != null && tabItem instanceof NSKeyValueCoding) {
			if(attr.indexOf('.') > 0 && (tabItem instanceof NSKeyValueCodingAdditions))
				return ((NSKeyValueCodingAdditions)tabItem).valueForKeyPath(attr).toString();
			else
				return ((NSKeyValueCoding)tabItem).valueForKey(attr).toString();
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

	public void selectTab() {
		if(Various.boolForObject(valueForBinding("numeric"))) {
			if(index == null) {
				setValueForBinding(null,"currTab");
				return;
			}
			if(index.equals(valueForBinding("currTab")))
				setValueForBinding(null,"currTab");
			setValueForBinding(index,"currTab");
		} else {

			if(tabItem.equals(valueForBinding("currTab")))
				setValueForBinding(null,"currTab");
			//currTab = tabItem;
			setValueForBinding(tabItem,"currTab");
		}
//		tablist=(NSArray)valueForBinding("tablist");
	}
	
	public String tabStyle() {
		Object currTab = valueForBinding("currTab");
		if(tabItem == null || index == null || currTab == null) return "grey";
		if(Various.boolForObject(valueForBinding("numeric"))) {
			if(index.equals(currTab)) return "selection";
		} else {
			String idAttribute = (String)valueForBinding("idAttribute");
			if(idAttribute != null) {
				try {
					Object recent = NSKeyValueCoding.Utility.valueForKey(
							tabItem, idAttribute);
					if (recent != null) {
						if (recent.equals(currTab))
							return "selection";
						Object sel = NSKeyValueCoding.Utility.valueForKey(
								currTab, idAttribute);
						if (recent.equals(sel))
							return "selection";
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
	/*
	public WOElement template() {
		sync();
		return super.template();
	}
	
	public void sync() {
		tablist = (NSArray)valueForBinding("tablist");
		currTab = (BaseTab)valueForBinding("currTab");
	}
	*/
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
