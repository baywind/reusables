package net.rujel.reusables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
		if(key.equals("@localisationFolder"))
			return folder;
		NSDictionary dict = (NSDictionary)_strings.valueForKey(key);
		if(dict != null)
			return dict;
		String res = key + ".plist";
		NSData data = null;
		if(folder != null) {
			if(res.indexOf('_') < 0)
				res = System.getProperty("ApplicationName") + '_' + res;
			File file = new File(folder,res);
			if(file.exists()) {
				try {
					InputStream stream = new FileInputStream(file);
					data = new NSData(stream,(int)file.length());
				} catch (IOException e) {
					Logger.getLogger("rujel.reusables").log(WOLogLevel.WARNING,
							"Could not load dictionary in file " + res,e);
				}
			}
		} else if (resourceManager != null) {
			String frw = null;
			int idx = key.indexOf('-');
			if (idx < 0)
				idx = key.indexOf('_');
			if (idx > 0) {
				frw = key.substring(0, idx);
				res = res.substring(idx + 1);
			}
			byte[] bytes = resourceManager.bytesForResourceNamed(res, frw, null);
			data = new NSData(bytes);
		}
		if(data != null && data.length() > 0) {
			dict = (NSDictionary)NSPropertyListSerialization
			.propertyListFromData(data,"UTF8");
/*		} else if(parent != null) {
			dict = parent.valueForKey(key);
			if(dict == null || dict.count() == 0)
				dict = appStringStorage.valueForKey(key);
			_strings.setObjectForKey(dict,key);
			return dict;*/
		}
		if(dict == null) {
			dict = NSDictionary.EmptyDictionary;
			Logger.getLogger("rujel.reusables").log(WOLogLevel.INFO,
					"Could not load dictionary for resource named " + key, folder);
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
			if(parent != null)
				result = parent.valueForKeyPath(keyPath);
			else if(this != appStringStorage)
				result = appStringStorage.valueForKeyPath(keyPath);
		}
		return result;
	}
}
