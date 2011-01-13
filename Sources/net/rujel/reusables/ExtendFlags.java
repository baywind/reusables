package net.rujel.reusables;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

public class ExtendFlags extends NamedFlags {

	protected String attr;
	public ExtendFlags(Object toSync, String syncAttribute, NSArray flagNames) {
		super(flagNames);
		_syncWith = toSync;
		attr = syncAttribute;
		Number has = (Number)NSKeyValueCoding.Utility.valueForKey(_syncWith, attr);
		if(has != null)
			flags = has.intValue();
	}
	
	public void sync() {
		Number has = (Number)NSKeyValueCoding.Utility.valueForKey(_syncWith, attr);
		if(has == null || has.intValue() != flags)
			NSKeyValueCoding.Utility.takeValueForKey(_syncWith, new Integer(flags), attr);
	}
	
	public void read() {
		Number has = (Number)NSKeyValueCoding.Utility.valueForKey(_syncWith, attr);
		if(has != null)
			flags = has.intValue();
		else
			flags = 0;
	}
}
