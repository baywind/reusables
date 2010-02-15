//  PrefsReader.java

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

import com.webobjects.foundation.*;
import java.util.prefs.*;
import java.util.Enumeration;

//import com.webobjects.eocontrol.*;

public class PrefsReader extends SettingsReader {
	protected static Preferences globalPrefs;
	static {
		String source = System.getProperty("PrefsReader.source","System");
		String nodePath = System.getProperty("PrefsReader.nodePath","/net/rujel");
		if(source.equalsIgnoreCase("User")) {
			globalPrefs = Preferences.userRoot().node(nodePath);
		} else {
			globalPrefs = Preferences.systemRoot().node(nodePath);
		}
	}
	
	private Preferences pref;
	
	public PrefsReader() {
		pref = globalPrefs;
		System.out.println("Settings are read from " + pref.absolutePath());
	}
	
	public PrefsReader(Class forClass) {
		String source = System.getProperty("PrefsReader.source","System");
		if(source.equalsIgnoreCase("User")) {
			pref = Preferences.userNodeForPackage(forClass);
		} else {
			pref = Preferences.systemNodeForPackage(forClass);
		}
		System.out.println("Settings are read from " + pref.absolutePath());
	}
	
	public PrefsReader(String nodePath) {
		pref = globalPrefs.node(nodePath.replace('.','/'));
		System.out.println("Settings are read from " + pref.absolutePath());
	}
	
	protected PrefsReader(Preferences node) {
		pref = node;
	}
	
	protected Preferences getNode(String nodePath, boolean create) throws BackingStoreException{
		if(nodePath.indexOf('.') <= 0)
			return getNode(pref,nodePath,create);
		Preferences node = pref;
		String[] path = nodePath.split("\\.");
		//int count = path.length;
		for (int i = 0; i < path.length; i++) {
			node = getNode(node,path[i],create);
			if(node == null) return null;
		}
		return node;
	}
	
	protected static Preferences getNode(Preferences node, String name, boolean create) throws BackingStoreException {
		if(node.nodeExists(name))
			return node.node(name);
		
		String alias = node.get(name,null);
		if(alias == null) {
			if (create)
				return node.node(name);
			else
				return null;
		}
		if(globalPrefs.nodeExists(alias))
			return globalPrefs.node(alias);
		else
			throw new IllegalStateException("Aliased node '" + alias + "' does not exist");
	}
	
	public Object valueForKeyPath(String keyPath) {
		int lastDot = keyPath.lastIndexOf('.');
		if(lastDot < 0) return valueForKey(keyPath);
		//String nodePath = keyPath.substring(0,lastDot).replace('.','/');
		String prefKey = keyPath.substring(lastDot + 1);
		try {
			//if(!pref.nodeExists(nodePath)) return null;
			Preferences targetNode = getNode(keyPath.substring(0,lastDot),false);
			if(targetNode == null)
				return null;
			return valueForKey(prefKey,targetNode);
		} catch (BackingStoreException bex) {
			throw new NSForwardException(bex);
		}
	}
	
	public void takeValueForKeyPath(Object value, String keyPath) {
		int lastDot = keyPath.lastIndexOf('.');
		if(lastDot < 0) {
			takeValueForKey(value,keyPath);
			return;
		}
		//String nodePath = keyPath.substring(0,lastDot).replace('.','/');
		try {
			Preferences targetNode = getNode(keyPath.substring(0,lastDot),true);
			String prefKey = keyPath.substring(lastDot + 1);
			//targetNode.put(prefKey,(String)value);
			takeValueForKey(value,prefKey,targetNode);
		} catch (BackingStoreException bex) {
			throw new NSForwardException(bex);
		}
	}
	
	protected static Object valueForKey(String key, Preferences node) {
		Object result = node.get(key,null);
		if(result != null)
			return result;
		try {
			if(node.nodeExists(key)) {
				result = new PrefsReader(node.node(key));
			}
		}
		catch (BackingStoreException ex) {
			;
		}
		finally {
			return result;
		}
	}
	
	public Object valueForKey(String key) {
		return valueForKey(key,pref);
	}
	/*
	protected static void takeValuesFromDictionary(NSDictionary dictionary, Preferences node) {
		Enumeration enu = dictionary.keyEnumerator();
		while (enu.hasMoreElements()) {
			String key = (String)enu.nextElement();
			Object value = dictionary.valueForKey(key);
			if(value instanceof NSDictionary) {
				takeValueForKey(value,key);
			} else 
		}
	}*/
	
	protected static void takeValueForKey(Object value, String key, Preferences node) {
		Enumeration enu = null;
		if(value instanceof NSDictionary) {
			enu = ((NSDictionary)value).keyEnumerator();
		} else if (value instanceof SettingsReader) {
			enu = ((SettingsReader)value).keyEnumerator();
		}
		if(enu == null) {
			node.put(key,value.toString());
		} else {
			NSKeyValueCoding map = (NSKeyValueCoding)value;
			node = node.node(key);
			while (enu.hasMoreElements()) {
				key = (String)enu.nextElement();
				takeValueForKey(map.valueForKey(key),key,node);
			}
		}
	}
	
	public void takeValueForKey(Object value, String key) {
		takeValueForKey(value,key,pref);
	}

	public PrefsReader subreaderForPath(String nodePath, boolean createIfNeeded) {
		try {
			Preferences node = getNode(nodePath,createIfNeeded);
			if(node == null) return null;
			return new PrefsReader(node);
		} catch (BackingStoreException bex) {
			throw new NSForwardException(bex);
		}
	}

	public void refresh() {
		try {
			pref.sync();
		} catch (Exception ex) {
			System.out.println("Exception refreshing preferences: " + ex);
		}
	}
	
	public void save() throws BackingStoreException {
		pref.flush();
	}
	
	public Enumeration keyEnumerator() {
		try {
			NSMutableArray result = new NSMutableArray(pref.keys());
			result.addObjects(pref.childrenNames());
			return result.objectEnumerator();
		} catch (BackingStoreException ex) {
			throw new NSForwardException(ex);
		}
	}
	
	public static NSDictionary shallowDictionary(Preferences node) {
		try {
			String[] keys = node.keys();
			if(keys == null || keys.length == 0)
				return NSDictionary.EmptyDictionary;
			NSMutableDictionary result = new NSMutableDictionary();
			for (int i = 0; i < keys.length; i++) {
				Object value = node.get(keys[i],null);
				if(value == null) value = NSKeyValueCoding.NullValue;
				result.setObjectForKey(value,keys[i]);
			}
			return result;
		} catch (BackingStoreException ex) {
			throw new NSForwardException(ex);
		}
	}
	public NSDictionary shallowDictionary() {
		return shallowDictionary(pref);
	}
	
	public static NSDictionary fullDictionary(Preferences node) {
		try {
			String[] keys = node.childrenNames();
			if(keys == null || keys.length == 0)
				return shallowDictionary(node);
			
			NSMutableDictionary result = new NSMutableDictionary();
			result.addEntriesFromDictionary(shallowDictionary(node));
			for (int i = 0; i < keys.length; i++) {
				result.setObjectForKey(fullDictionary(node.node(keys[i])),keys[i]);
			}
			return result;
		} catch (BackingStoreException ex) {
			throw new NSForwardException(ex);
		}
	}
	
	public NSDictionary fullDictionary() {
		return fullDictionary(pref);
	}
	
	public String get(String key, String def) {
		return pref.get(key, def);
	}
	
	public int getInt(String key, int def) {
		return pref.getInt(key, def);
	}
	public void putInt(String key, int value) {
		pref.putInt(key, value);
	}

	public boolean getBoolean(String key, boolean def) {
		return pref.getBoolean(key, def);
	}	
	public void putBoolean(String key, boolean value) {
		pref.putBoolean(key, value);
	}
	
	public void mergeValueToKeyPath(Object value, String keyPath) {
		throw new UnsupportedOperationException(
				"Merge operation is not supported by PrefsReader");
	}
}
