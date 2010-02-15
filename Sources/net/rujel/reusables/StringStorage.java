package net.rujel.reusables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.*;

public class StringStorage implements NSKeyValueCodingAdditions {
	public static final StringStorage appStringStorage = new StringStorage(null); 
	protected NSMutableDictionary _strings = new NSMutableDictionary();
	protected WOResourceManager resourceManager;
	protected File folder;
	protected StringStorage parent;

	public StringStorage(File localisationFolder, StringStorage defaultLocalisation) {
		super();
		folder = localisationFolder;
		parent = defaultLocalisation;
	}

	public StringStorage(String localisationFolder, StringStorage defaultLocalisation) {
		super();
		folder = new File(Various.convertFilePath(localisationFolder));
		parent = defaultLocalisation;
	}
	
	public StringStorage(WOResourceManager rm) {
		super();
		resourceManager = rm;
		if(resourceManager == null)
			resourceManager = WOApplication.application().resourceManager();
	}
	
	public Object valueForKey(String key) {
		boolean str = (key.charAt(0) == '@');
		if(str && key.equals("@localisationFolder"))
			return folder;
		NSDictionary dict = (NSDictionary)_strings.valueForKey(key);
		if(dict != null)
			return dict;
		String res = (str)?key.substring(1):key + ".plist";
		NSData data = null;
		if(folder != null) {
			if(res.indexOf('_') < 0)
				res = System.getProperty("ApplicationName") + '_' + res;
			File file = new File(folder,res);
			if(file.exists()) {
				try {
					InputStream stream = new FileInputStream(file);
					if(str)
						return stream;
					data = new NSData(stream,(int)file.length());
				} catch (IOException e) {
					Logger.getLogger("rujel.reusables").log(WOLogLevel.WARNING,
							"Could not load dictionary in file " + res,e);
				}
			}
		} else if (resourceManager != null) {
			String frw = null;
			int idx = res.indexOf('-');
			if (idx < 0)
				idx = res.indexOf('_');
			if (idx > 0) {
				frw = res.substring(0, idx);
				res = res.substring(idx + 1);
			}
			if(str)
				return resourceManager.inputStreamForResourceNamed(res, frw, null);
			byte[] bytes = resourceManager.bytesForResourceNamed(res, frw, null);
			if(bytes != null && bytes.length > 0)
				data = new NSData(bytes);
		}
		if(data != null && data.length() > 0) {
			dict = (NSDictionary)NSPropertyListSerialization
				.propertyListFromData(data,"UTF8");
			
		}
		if(dict == null) {
			if(this == appStringStorage) {
				dict = NSDictionary.EmptyDictionary;
				Logger.getLogger("rujel.reusables").log(WOLogLevel.INFO,
						"Could not load dictionary for resource named " + key, folder);
			} else if(parent == null)
				return appStringStorage.valueForKey(key);
			else
				return parent.valueForKey(key);
		} else  if(this != appStringStorage) {
			NSDictionary base = (NSDictionary)((parent == null)? 
					appStringStorage.valueForKey(key) : parent.valueForKey(key));
			dict = updateDictFromOther(base, dict);
		}
		_strings.setObjectForKey(dict,key);
		return dict;
	}
	
	public void takeValueForKey(Object value, String key) {
		//if(value == null || value instanceof NSKeyValueCoding)
			_strings.takeValueForKey(value, key);
		//throw new IllegalArgumentException("You can only add dictionaries to StringStorage");
	}
	
	public void flush() {
		_strings.removeAllObjects();
	}

	public void takeValueForKeyPath(Object arg0, String arg1) {
		throw new UnsupportedOperationException("Localisation is immutable");
	}

	public Object valueForKeyPath(String keyPath) {
		if(keyPath.charAt(0) == '@')
			return valueForKey(keyPath);
		int idx = keyPath.indexOf('.');
		if(idx < 0)
			return valueForKey(keyPath);
		String key = keyPath.substring(0,idx);
		String path = keyPath.substring(idx + 1);
		NSDictionary dict = (NSDictionary)valueForKey(key);
		Object result = dict.valueForKeyPath(path);
		if(result == null) {
			if(dict != null && dict.count() > 0)
				Logger.getLogger("rujel.reusables").log(WOLogLevel.INFO,
						"Could not get value for keyPath " + keyPath, folder);
//			if(parent != null)
//				result = parent.valueForKeyPath(keyPath);
//			else if(this != appStringStorage)
//				result = appStringStorage.valueForKeyPath(keyPath);
		}
		return result;
	}
	
	public static NSMutableDictionary updateDictFromOther(
					NSDictionary base, NSDictionary update) {
		NSMutableDictionary result = new NSMutableDictionary();
		Enumeration enu = base.keyEnumerator();
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			Object updValue = update.valueForKey(key);
			Object baseValue = base.valueForKey(key);
			boolean isDict = (baseValue instanceof NSDictionary);
			boolean isArray = (baseValue instanceof NSArray);
			if(updValue == null) {
				if(isDict)
					updValue = PlistReader.cloneDictionary((NSDictionary)baseValue, true);
				else if(isArray)
					updValue = PlistReader.cloneArray((NSArray)baseValue, true);
				else
					updValue = baseValue;
			} else {
				Class valClass = (isDict)? NSDictionary.class : 
					(isArray)? NSArray.class : baseValue.getClass();
				if(valClass.isInstance(updValue)) {
					if(isDict)
						updValue = updateDictFromOther(
								(NSDictionary)baseValue, (NSDictionary)updValue);
					else if(isArray)
						updValue = updateArrayFromOther(
								(NSArray)baseValue, (NSArray)updValue);
				} else {
					updValue = baseValue;
				}
			}
			result.takeValueForKey(updValue, key);
		}
		return result;
	}
	
	protected static NSDictionary updateWithID(NSArray list, Object id) {
		if(list == null || list.count() == 0)
			return null;
		Enumeration enu = list.objectEnumerator();
		while (enu.hasMoreElements()) {
			Object obj = enu.nextElement();
			if(obj instanceof NSDictionary) {
				NSDictionary dict = (NSDictionary)obj;
				if(id.equals(dict.valueForKey("id")))
					return dict;
			}
		}
		return null;
	}
	
	public static NSMutableArray updateArrayFromOther(NSArray base, NSArray update) {
		if(base == null)
			return null;
		if(base.count() == 0)
			return new NSMutableArray();
		boolean eq = (update != null && base.count() == update.count());
		NSMutableArray result = new NSMutableArray(base.count());
		for(int i = 0; i < base.count(); i++) {
			Object bObj = base.objectAtIndex(i);
			Object uObj = update.objectAtIndex(i);
			if(bObj instanceof NSDictionary && uObj instanceof NSDictionary) {
				Object id = ((NSDictionary)bObj).valueForKey("id");
				if(id != null) {
					NSDictionary upd = updateWithID(update, id);
					if(upd != null)
						bObj = updateDictFromOther((NSDictionary)bObj, upd);
					result.addObject(PlistReader.cloneDictionary((NSDictionary)bObj, true));
				} else if(eq) {
					result.addObject(updateDictFromOther(
								(NSDictionary)bObj, (NSDictionary)uObj));
				}
			} else {
				if(eq) {
					if(bObj instanceof NSArray && uObj instanceof NSArray) {
						result.addObject(updateArrayFromOther((NSArray)bObj, (NSArray)uObj));
					} else {
						Class cl = bObj.getClass();
						if(cl.isInstance(uObj))
							result.addObject(uObj);
						else
							result.addObject(PlistReader.
									cloneDictionary((NSDictionary)bObj, true));
					}
				} else {
					return base.mutableClone();
				}
			}
		}
		return result;
	}
}
