// DelegateManager.java

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

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

public class DelegateManager {
	protected NSMutableArray delegates = new NSMutableArray();
	protected NSMutableArray orderedDelegates = new NSMutableArray();
	
	public int count() {
		return orderedDelegates.count() + delegates.count();
	}
	
	public void setDelegate(Object delegate) {
		delegates.removeAllObjects();
		orderedDelegates.removeAllObjects();
		addDelegate(delegate);
	}
	
	public void addDelegate(Object delegate) {
		if(delegate != null)
			delegates.addObject(delegate);
	}
	
	public void addDelegate(Object delegate, int order) {
		NSDictionary dict = new NSDictionary(
				new Object[] {delegate, new Integer(order)},
				new String[] {"delegate","sort"});
		orderedDelegates.addObject(dict);
		if(orderedDelegates.count() > 1) {
			EOSortOrdering.sortArrayUsingKeyOrderArray(
					orderedDelegates, ModulesInitialiser.sorter);
		}
	}
	
	protected Class delegateClass(Object delegate) {
		Class delClass = null;
		if(delegate instanceof Class) {
			delClass = (Class)delegate;
		} else if(delegate instanceof Integer) {
			return null;
		} else if(delegate instanceof String) {
			try {
				delClass = Class.forName((String)delegate);
			} catch (ClassNotFoundException e) {
				throw new NSForwardException(e);
			}
		} else {
			delClass = delegate.getClass();
		}
		return delClass;
	}
	
	public void removeDelegate(Object delegate) {
		if(orderedDelegates.count() > 0) {
			Class delClass = delegateClass(delegate);
			Enumeration enu = orderedDelegates.immutableClone().objectEnumerator();
			while (enu.hasMoreElements()) {
				NSDictionary dict = (NSDictionary) enu.nextElement();
				if(delClass == null) {
					if(delegate.equals(dict.valueForKey("sort")))
						delegates.removeObject(dict);
				} else if(delClass.isInstance(dict.valueForKey("delegate"))) {
					delegates.removeObject(dict);
				}
			}
		}
		if(delegate instanceof Number)
			return;
		if(delegates.count() > 0 && !delegates.removeObject(delegate)) {
			Class delClass = delegateClass(delegate);
			Enumeration enu = delegates.immutableClone().objectEnumerator();
			while (enu.hasMoreElements()) {
				Object cur = enu.nextElement();
				if(delClass.isInstance(cur))
					delegates.removeObject(cur);
			}
		}
	}
	
	public Object useDelegates(NSSelector method, Object[] params) {
		if(orderedDelegates.count() == 0 && delegates.count() == 0)
			return null;
		Enumeration enu = orderedDelegates.objectEnumerator();
		while (enu.hasMoreElements()) {
			NSDictionary dict = (NSDictionary)enu.nextElement();
			Object delegate = dict.valueForKey("delegate");
			try {
				Object curResult = method.invoke(delegate, params);
				if(curResult != null)
					return curResult;
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NSForwardException(e,"Error executing delegate method");
			}
		}
		if(delegates.count() == 0)
			return null;
		enu = delegates.objectEnumerator();
		while (enu.hasMoreElements()) {
			Object delegate = (Object) enu.nextElement();
			try {
				Object curResult = method.invoke(delegate, params);
				if(curResult != null)
					return curResult;
			} catch (NoSuchMethodException e) {
				continue;
			} catch (Exception e) {
				throw new NSForwardException(e,"Error executing delegate method");
			}
		}
		return null;
	}

}
