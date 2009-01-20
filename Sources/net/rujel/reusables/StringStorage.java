package net.rujel.reusables;

import java.util.logging.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOResourceManager;
import com.webobjects.foundation.*;

public class StringStorage implements NSKeyValueCoding {
	protected NSMutableDictionary _strings = new NSMutableDictionary();
	protected WOResourceManager resourceManager;

	public StringStorage(WOResourceManager rm) {
		super();
		resourceManager = rm;
		if(resourceManager == null)
			resourceManager = WOApplication.application().resourceManager();
	}
	
	public Object valueForKey(String key) {
		NSDictionary dict = (NSDictionary)_strings.valueForKey(key);
		if(dict == null) {
			String frw = null;
			String res = key + ".plist";
			int idx = key.indexOf('-');
			if(idx < 0) idx = key.indexOf('_');
			if(idx > 0) {
				frw = key.substring(0,idx);
				res = res.substring(idx + 1);
			}
			byte[] bytes = resourceManager.bytesForResourceNamed(res,frw,null);
			if(bytes != null && bytes.length > 0)
				dict = (NSDictionary)NSPropertyListSerialization
								.propertyListFromData(new NSData(bytes),"UTF8");
			if(dict == null) {
				dict = NSDictionary.EmptyDictionary;
				Logger.getLogger("rujel.reusables").log(WOLogLevel.WARNING,
						"Could not load dictionary for resource named " + key);
			}
			_strings.setObjectForKey(dict,key);
		}
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
}
