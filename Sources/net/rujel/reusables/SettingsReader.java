//  SettingsReader.java

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
import java.lang.reflect.Constructor;

public abstract class SettingsReader implements NSKeyValueCodingAdditions {
	protected static SettingsReader _root;

	public static SettingsReader rootSettings() {
		if(_root == null) {
			String className = System.getProperty("Settings.className","net.rujel.reusables.PlistReader");
			System.out.println("Using " + className + " for reading settings");
			try {
				Class useClass = Class.forName(className);
				Constructor<SettingsReader> constructor = useClass.getConstructor();
				_root = constructor.newInstance();
			} catch (Exception ex) {
				String message = "Failed constructing SettingsReader class:" + className;
				System.out.println(message);
				throw new NSForwardException(ex,message);
			}
		}
		return _root;
	}
	
	public static SettingsReader settingsForPath(String path, boolean createIfNeeded) {
		return rootSettings().subreaderForPath(path,createIfNeeded);
	}
	
	public static String stringForKeyPath(String keyPath, String dflt) {
		Object result = rootSettings().valueForKeyPath(keyPath);
		if(result == null || result instanceof SettingsReader)
			return dflt;
		else
			return result.toString();
	}
	
	public static int intForKeyPath(String keyPath, int dflt) {
		Object result = rootSettings().valueForKeyPath(keyPath);
		if(result == null || result instanceof SettingsReader)
			return dflt;
		else if(result instanceof Number)
			return ((Number)result).intValue();
		else
			return Integer.parseInt(result.toString());
	}
	
	
	public static boolean boolForKeyPath(String keyPath, boolean dflt) {
		Object result = rootSettings().valueForKeyPath(keyPath);
		return parseBoolean(result,dflt);
	}
	
	public String get(String key, String def) {
		Object result = valueForKey(key);
		if(result == null || result instanceof SettingsReader)
			return def;
		else
			return result.toString();
	}
	/*
	public  void put(String key, String value) {
		takeValueForKey(key, value);
	}*/
	
	public int getInt(String key, int def) {
		Object result = valueForKey(key);
		if(result == null || result instanceof SettingsReader)
			return def;
		else
			return Integer.parseInt(result.toString());
	}
	public void putInt(String key, int value) {
		takeValueForKey(new Integer(value), key);
	}

	public boolean getBoolean(String key, boolean def) {
		Object result = valueForKey(key);
		return parseBoolean(result,def);
	}
	public void putBoolean(String key, boolean value) {
		takeValueForKey(new Boolean(value), key);
	}
	
	public static boolean parseBoolean(Object test, boolean def) {
		if(test == null || test instanceof SettingsReader)
			return def;
		else if (test instanceof Boolean)
			return ((Boolean)test).booleanValue();
		else if (test instanceof Number)
			return !(((Number)test).doubleValue() == 0.0);
		else if(!(test instanceof String))
			return def;
		else if(((String)test).equalsIgnoreCase("true"))
			return true;
		else if(((String)test).equalsIgnoreCase("false"))
			return false;
		else
			return def;		
	}
	
	public abstract SettingsReader subreaderForPath(String path, boolean createIfNeeded);
	
	public abstract NSDictionary fullDictionary();
	
	public abstract NSDictionary shallowDictionary();
	
	public abstract java.util.Enumeration keyEnumerator();
	
	public abstract void mergeValueToKeyPath(Object value, String keyPath);
	
	public abstract void refresh();
	
	public void save() throws Exception{
		throw new UnsupportedOperationException("Save method is not implemented in subclass " + this.getClass().getName());
	}
	
	public static final SettingsReader DUMMY = new SettingsReader() {
		public SettingsReader subreaderForPath(String path, boolean createIfNeeded) {
			return (createIfNeeded)?SettingsReader.DUMMY:null;
		}
		
		public NSDictionary fullDictionary() {
			return NSDictionary.EmptyDictionary;
		}
		
		public NSDictionary shallowDictionary() {
			return NSDictionary.EmptyDictionary;
		}
		
		public java.util.Enumeration keyEnumerator() {
			return NSArray.EmptyArray.objectEnumerator(); 
		}
		
		public void refresh() {
			
		}
		
		public Object valueForKeyPath(String keyPath) {
			return null;
		}
		public Object valueForKey(String key) {
			return null;
		}
		
		public void takeValueForKey(Object value, String key) {
			
		}
		public void takeValueForKeyPath(Object value, String keyPath) {
			
		}
		
		public void mergeValueToKeyPath(Object value, String keyPath) {
			
		}
	};

}
