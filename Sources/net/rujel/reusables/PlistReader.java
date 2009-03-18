// PlistReader.java

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
import java.io.File;
import java.io.FileInputStream;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.*;

public class PlistReader extends SettingsReader {
	
	public static final String LinkKey = "thisIsPlistLink";
	
	protected NSDictionary pref;
	protected String inputFilePath;
	protected String outputFilePath;
	protected String encoding;
	protected StringBuffer innerKeyPath;

	protected NSDictionary rootDict;
	
	public PlistReader() {
		inputFilePath = System.getProperty("PlistReader.filePath",null);
		if(inputFilePath == null) {
			System.out.println("Using default settings");
			inputFilePath = WOApplication.application().resourceManager().
				pathURLForResourceNamed("defaultSettings.plist", null, null).getFile();
			if(inputFilePath == null)
				throw new IllegalStateException(
						"Required environment variable \"PlistReader.filePath\"");
		} else {
			inputFilePath = inputFilePath.replaceFirst("LOCALROOT",
					System.getProperty("WOLocalRootDirectory",""));
			inputFilePath = inputFilePath.replaceFirst("WOROOT",
					System.getProperty("WORootDirectory","/System"));
			if(File.separatorChar != '/')
				inputFilePath = inputFilePath.replaceAll("/", File.separator);
		}
		encoding = System.getProperty("PlistReader.encoding","utf8");
	 	innerKeyPath = new StringBuffer(System.getProperty("PlistReader.innerKeyPath",""));
	 	refresh();
	}
	
	public static PlistReader readerForFile(String inputFilePath, String encoding) {
		PlistReader result = new PlistReader(inputFilePath,encoding,null);
		result.refresh();
		return result;
	}
	
	protected PlistReader(String inputFilePath, String encoding, CharSequence innerKeyPath) {
		this.inputFilePath = inputFilePath;
		this.encoding = encoding;
		this.innerKeyPath = (innerKeyPath==null)?new StringBuffer():
			new StringBuffer(innerKeyPath);
		//refresh();
	}
	
	public PlistReader(NSDictionary dict) {
		innerKeyPath = new StringBuffer();
		rootDict = dict;
		pref = rootDict;
	}

	public void refresh() {
		if(inputFilePath == null){
			System.err.println("No input filePath specified for reading preferences");
			return;
		}
		rootDict = (NSDictionary)readPlist(inputFilePath, encoding);
		if(rootDict == null)
			throw new NullPointerException("Could not read plist at specified path: " + inputFilePath);
		System.out.println("Settings are read from " + inputFilePath);
		if(innerKeyPath != null && innerKeyPath.length() > 0) {
			pref = (NSDictionary)rootDict.valueForKeyPath(innerKeyPath.toString());
			System.out.println("with inner keyPath " + innerKeyPath.toString());
		} else
			pref = rootDict;
	}
	
	public static Object readPlist(String filePath, String encoding) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			NSData data = new NSData(fis,fis.available());
			fis.close();
			//System.out.println("Settings are read from " + filePath);
			return NSPropertyListSerialization.propertyListFromData(data, encoding);
		} catch (java.io.IOException ioex) {
			System.err.println("Error reading plist " + filePath + " using encoding " + encoding);
			ioex.printStackTrace(System.err);
			return null;
		}
	}
	
	protected static boolean dictIslink(NSDictionary dict) {
		Object isLink = dict.valueForKey(LinkKey);
		if(isLink == null)
			return false;
		if(isLink instanceof String)
			return NSPropertyListSerialization.booleanForString((String)isLink);
		else if(isLink instanceof Boolean)
			return ((Boolean)isLink).booleanValue();
		return false;
	}
	
	protected Object resolveLink(NSDictionary linkDict, PlistReader updateReader) {
		if(dictIslink(linkDict)) {
			NSDictionary newRootDict = null;
			String keyPath = (String)linkDict.valueForKey("innerKeyPath");
			if(updateReader != null) {
				updateReader.innerKeyPath = (keyPath==null)?new StringBuffer():
					new StringBuffer(keyPath);
			}
			String filePath = (String)linkDict.valueForKey("inputFilePath");
			if(filePath != null) {
				String newEncoding = (String)linkDict.valueForKey("encoding");
				if(newEncoding == null)
					newEncoding = encoding;
				newRootDict = (NSDictionary)readPlist(filePath, newEncoding);
				if(updateReader != null) {
					updateReader.rootDict = newRootDict;
					updateReader.encoding = newEncoding;
					updateReader.inputFilePath = filePath;
					updateReader.pref = newRootDict;
				}
			} else {
				newRootDict = rootDict;
			}
			if(keyPath != null) {
				Object result = newRootDict.valueForKeyPath(keyPath);
				if(updateReader != null) {
					updateReader.pref = (result instanceof NSDictionary)?(NSDictionary)result:null;
				}
				return result;
			}
			return newRootDict;
		} else {
			return linkDict;
		}
	}
	
	protected static StringBuffer appendKey(StringBuffer buf, CharSequence key) {
		if(buf.length() > 0)
			buf.append('.');
		return buf.append(key);
	}
	
	public Object traverseKeyPath(String keyPath, boolean createIfNeeded, PlistReader updateReader) {
		String[] path = keyPath.split("\\.");
		NSDictionary dict = pref;
		Object next = pref;
		for (int i = 0; i < path.length; i++) {
			String key = path[i];
			next = dict.valueForKey(key);
			if(next == null) {
				if(!createIfNeeded)
					return null;
				next = new NSMutableDictionary();
				dict.takeValueForKey(next, key);
			} else if (next instanceof NSDictionary) {
				next = resolveLink((NSDictionary)next, updateReader);
			}
			
			if(next instanceof NSDictionary) {
				dict = (NSDictionary)next;
				if(updateReader != null) {
					appendKey(updateReader.innerKeyPath, key);
					updateReader.pref = dict;
				}
			} else if (i < (path.length -1)) {
				i++;
				StringBuffer subPath = new StringBuffer(path[i]);
				i++;
				while(i < path.length){
					subPath.append('.').append(path[i]);
					i++;
				} 
				return NSKeyValueCodingAdditions.Utility.valueForKeyPath(next, subPath.toString());
			}
		}
		return next;
	}
	
	public static NSMutableDictionary cloneDictionary (NSDictionary dict, boolean recurse) {
		if(dict == null) return null;
		NSMutableDictionary result = new NSMutableDictionary();
		Enumeration enu = dict.keyEnumerator();
		while (enu.hasMoreElements()) {
			Object key = enu.nextElement();
			Object obj = dict.objectForKey(key);
			if(obj instanceof NSDictionary) {
				if(recurse)
				result.setObjectForKey(cloneDictionary((NSDictionary)obj, recurse), key);
			} else if (obj instanceof NSArray) {
				if(recurse)
					result.setObjectForKey(cloneArray((NSArray)obj, recurse), key);
			} else {
				result.setObjectForKey(obj, key);
			}
		}
		return result;
	}
	
	public static NSMutableArray cloneArray(NSArray array, boolean recurse) {
		if(array ==null) return null;
		NSMutableArray result = new NSMutableArray();
		Enumeration enu = array.objectEnumerator();
		while (enu.hasMoreElements()) {
			Object obj = enu.nextElement();
			if(obj instanceof NSDictionary) {
				if(recurse)
					result.addObject(cloneDictionary((NSDictionary)obj,recurse));
			} else if (obj instanceof NSArray) {
				if(recurse)
					result.addObject(cloneArray((NSArray)obj,recurse));
			} else {
				result.addObject(obj);
			}
		}
		return result;
	}
	
	public NSDictionary shallowDictionary() {
		return cloneDictionary(pref, false);
	}

	public NSDictionary fullDictionary() {
		return cloneDictionary(pref, true);
	}

	public Enumeration keyEnumerator() {
		return pref.keyEnumerator();
	}

	public SettingsReader subreaderForPath(String path, boolean createIfNeeded) {
		PlistReader result = new PlistReader(inputFilePath,encoding,innerKeyPath);
		result.rootDict = rootDict;
		Object target = traverseKeyPath(path, createIfNeeded, result);
		if(!(target instanceof NSDictionary)) {
			return null;
		}
		return result;
	}
	
	public void save() throws Exception {
		super.save();
		// TODO implement save method
	}

	protected NSMutableSet toSave;
	public void takeValueForKeyPath(Object value, String keyPath) {
		int lastDot = keyPath.lastIndexOf('.');
		if(lastDot < 0) {
			takeValueForKey(value, keyPath);
		} else {
			SettingsReader next = subreaderForPath(keyPath.substring(0, lastDot), true);
			next.takeValueForKey(value, keyPath.substring(lastDot + 1));
			if(next instanceof PlistReader) {
				String nextPath = ((PlistReader)next).inputFilePath;
				if(!(inputFilePath == nextPath)) {
					if(toSave == null) {
						toSave = new NSMutableSet(nextPath);
					} else {
						toSave.addObject(nextPath);
					}

				}
			} else {
				if(toSave == null) {
					toSave = new NSMutableSet(next);
				} else {
					toSave.addObject(next);
				}
			}
		}
	}

	public Object valueForKeyPath(String keyPath) {
		Object result = pref.valueForKeyPath(keyPath);
		if(result == null || result instanceof NSDictionary) {
			PlistReader next = new PlistReader(inputFilePath,encoding,innerKeyPath);
			next.rootDict = rootDict;
			if(result == null) {
				result = traverseKeyPath(keyPath, false, next);
			} else {
				next.pref = (NSDictionary)result;
				appendKey(next.innerKeyPath, keyPath);
				result = resolveLink((NSDictionary)result, next);
			}
			if(result instanceof NSDictionary) {
				return next;
			}
		}
		return result;
	}

	public void takeValueForKey(Object value, String key) {
		if(value instanceof SettingsReader)
			value = ((SettingsReader)value).fullDictionary();
		pref.takeValueForKey(value, key);
	}

	public Object valueForKey(String key) {
		Object result =  pref.valueForKey(key);
		if (result instanceof NSDictionary) {
			PlistReader next = new PlistReader(inputFilePath,encoding,innerKeyPath);
			appendKey(next.innerKeyPath, key);
			next.rootDict = rootDict;
			next.pref = (NSDictionary)result;
			result = resolveLink((NSDictionary)result, next);
		}
		return result;
	}

}
