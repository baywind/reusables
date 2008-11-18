//KeyValueCache.java

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

import com.webobjects.foundation.NSKeyValueCoding;

public class KeyValueCache implements NSKeyValueCoding {
	protected String[] keys;
	protected Object[] values;
	protected int last = 0;
	
	public KeyValueCache(int size) {
		keys = new String[size];
		values = new Object[size];
	}
	
	protected int indexForKey(String key) {
		if(key == null)
			throw new NullPointerException("Trying to operate with NULL key");
		for (int i = last; i >= 0; i--) {
			if(key.equals(keys[i]))
				return i;
		}
		for (int i = keys.length -1; i > last; i--) {
			if(key.equals(keys[i]))
				return i;
		}
		return -1;
	}

	public void takeValueForKey(Object value, String key) {
		int idx = indexForKey(key);
		if(idx != last) {
			last++;
			if(last >= keys.length)
				last = 0;
			if(idx >= 0) {
				keys[idx] = keys[last];
				values[idx] = values[last];
			}
		}
		keys[last] = key;
		values[last] = (value==null)?NullValue:value;
	}

	public Object valueForKey(String key) {
		int idx = indexForKey(key);
		if(idx < 0)
			return null;
		return values[idx];
	}
	
	public void reset() {
		for (int i = 0; i < keys.length; i++) {
			keys[i] = null;
			values[i] = null;
		}
	}

}
