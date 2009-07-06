//  ModulesInitialiser.java

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

//import java.util.prefs.*;
import java.lang.reflect.Method;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.appserver.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Enumeration;


public class ModulesInitialiser implements NSKeyValueCoding {
	protected static Logger logger = Logger.getLogger("modules");
	protected static Method[] modules;
	public static NSArray sorter = new NSArray(
			EOSortOrdering.sortOrderingWithKey("sort",EOSortOrdering.CompareAscending));
	
	public static void readModules(SettingsReader list) {
		if(list == null) {
			modules = null;
			return;
		}
		NSMutableArray mods = new NSMutableArray();
		Enumeration enu = list.keyEnumerator();
		while (enu.hasMoreElements()) {
			String name = (String)enu.nextElement();
			if(list.getBoolean(name,false)) {
				try {
					Class cl = Class.forName(name);
					mods.addObject(cl.getMethod("init",Object.class,WOContext.class));
				} catch (Exception ex) {
					logger.logp(Level.WARNING,"ModulesInitialiser","readModules","Could not get 'init' method for module " + name,ex);
				}
			}
		}
		if(mods.count() > 0) {
			modules = new Method[mods.count()];
			for (int i = 0; i < mods.count(); i++) {
				modules[i] = (Method)mods.objectAtIndex(i);
			}
		}
	}
	/*
	public static void readModules(Preferences node) {
		if(node == null) {
			modules = null;
			return;
		}
		String[] names = null;
		try {
			names = node.keys();
		} catch (BackingStoreException bex) {
			logger.logp(Level.SEVERE,"ModulesInitialiser","readModules","Could not read preferences node for modules");
		}
		if(names.length > 0) {
			modules = new Method[names.length];
			for (int i = 0; i < names.length; i++) {
				if(Various.boolForObject(node.get(names[i],"false"))) {
					try {
						modules[i] = Class.forName(names[i]).getMethod("init",Object.class,WOContext.class);
					} catch (Exception ex) {
						logger.logp(Level.WARNING,"ModulesInitialiser","readModules","Could not get 'init' method for module " + names[i],ex);
					}
				}
			}
		}
	}*/
	public static Object[] initModules(SettingsReader list,Object param) {
		return initModules(list, param, null);
	}

	public static Object[] initModules(SettingsReader list,Object param,WOContext ctx) {
		if(modules == null)
			readModules(list);
		if(modules == null) return null;
		Object[] result = new Object[modules.length];
		for (int i = 0; i < modules.length; i++) {
			if(modules[i] != null) {
				try {
					result[i] = modules[i].invoke(null,param,ctx);
				} catch (java.lang.reflect.InvocationTargetException tex) {
					logger.logp(Level.WARNING,"ModulesInitialiser","initModules","Error performing initialisation of module " + modules[i],tex);
					throw new RuntimeException("Error performing initialisation" + tex.getCause(),tex.getCause());
				} catch (Exception ex) {
					logger.logp(Level.WARNING,"ModulesInitialiser","initModules","Error performing initialisation of module " + modules[i],ex);
					throw new RuntimeException("Initialisation error: " + ex,ex);
				}
			}
		}
		return result;
	}
	
	protected WOSession ses;
	
	public ModulesInitialiser (WOSession session) {
		ses = session;
	}
	
	public NSArray useModules(Object param) {
		return useModules(ses.context(),param);
	}
	
	public static NSArray useModules(WOContext ctx,Object param) {
		if(modules == null) return null;
		WOSession ses = (ctx.hasSession())?ctx.session():null;
		NSMutableArray result = new NSMutableArray();
		for (int i = 0; i < modules.length; i++) {
			if(modules[i] != null) {
				try {
					Object res = modules[i].invoke(null,param,ctx);
					if(res == null) continue;
					if(res instanceof NSArray) {
						result.addObjectsFromArray((NSArray)res);
					} else if(res instanceof Object[]) {
						result.addObjects((Object[])res);
					} else
						result.addObject(res);
				} catch (java.lang.reflect.InvocationTargetException tex) {
					logger.logp(Level.WARNING,"ModulesInitialiser","useModules","Error executing module method :" + modules[i] + " with parameter \'" + param + '\'',new Object[] {ses,tex.getCause()});
//					String message = (String)WOApplication.application().valueForKeyPath("extStrings.Strings.messages.errorInModule") + modules[i].getDeclaringClass().getName() + '(' + param + ')' + tex;
//					ses.takeValueForKey(message,"message");
				} catch (Exception ex) {
					logger.logp(Level.WARNING,"ModulesInitialiser","useModules","Could not execute module method : " + modules[i] + " with parameter \'" + param + '\'',new Object[] {ses,ex});
				}
			}
		}
		try {
			EOSortOrdering.sortArrayUsingKeyOrderArray(result,sorter);
		} catch (Exception e) {
			logger.logp(Level.FINER,"ModulesInitialiser","useModules","Could not sort results of executing modules for '" + param +'\'',new Object[] {ses,e});
		}
		return result;
	}
	
	public Object valueForKey(String key) {
		return useModules(key);
	}
	
	public void takeValueForKey(Object value, String key) {
		throw new UnsupportedOperationException("No values can be set");
	}
}
