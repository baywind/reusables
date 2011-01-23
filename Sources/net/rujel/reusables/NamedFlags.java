//  NamedFlags.java

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

public class NamedFlags extends MutableFlags implements NSKeyValueCoding,Cloneable {
//	protected NSArray keys;
	protected Numerator numerator;
	public static interface Numerator {
		public int numberForKey (Object key);
//		public Object keyForNumber (int number);
	}
/*	
	public NamedFlags () {
		super();
	}
	
	public NamedFlags (int source) {
		super (source);
	}
*/	
	public Object clone() {
		return new NamedFlags(flags,numerator);
	}
	
	public NamedFlags mutableClone() {
		return (NamedFlags)clone();
	}
	
	public ImmutableNamedFlags immutableClone() {
		if(numerator != null)
			return new ImmutableNamedFlags(flags,numerator);
//		if(keys != null)
//			return new ImmutableNamedFlags(flags,keys);
		return new ImmutableNamedFlags(flags);		
	}
	
	public NamedFlags() {
		super();
	}
	
	public NamedFlags(int source) {
		super(source);
	}
	
	public NamedFlags (NSArray initKeys) {
		super();
		numerator = numeratorFromArray(initKeys);
//		keys = (initKeys==null)?null:initKeys.immutableClone();
	}
	
	public NamedFlags (Numerator initNum) {
		super();
		numerator = initNum;
	}
	
	public NamedFlags (int source, NSArray initKeys) {
		super(source);
		numerator = numeratorFromArray(initKeys);
//		keys = (initKeys==null)?null:initKeys.immutableClone();
	}
	
	public NamedFlags (int source, Numerator initNum) {
		super(source);
		numerator = initNum;
	}
	
	
	public void setKeys (NSArray newKeys) {
		numerator = numeratorFromArray(newKeys);
	//	keys = newKeys.immutableClone();
	//	numerator = null;
	}
	/*
	public NSArray getKeys () {
		return keys;
	}*/
	
	public void setNumerator (Numerator newNum) {
		numerator = newNum;
//		keys = null;
	}
	
	public int getIndexForKey(Object key) {
	/*	if (keys != null) {
			int idx = keys.indexOfObject(key);
			if(idx == NSArray.NotFound)
				throw new IllegalArgumentException("Provided key was not specified.");
			return idx;
		} else { */
			if (numerator != null) {
				return numerator.numberForKey(key);
			} else
				throw new IllegalStateException("NamedFlags class was not properly initialised.");
	//	}
	}

	
	public boolean flagForKey (Object key) {
		try {
			return getFlag(getIndexForKey(key));
		} catch (IllegalArgumentException ex) {
			try {
				if(!ex.getStackTrace()[0].getMethodName().equals("numberForKey"))
					throw ex;
			} catch (NullPointerException nex) {
				;
			}
		}
		return false;
	}
	
	public void setFlagForKey(boolean flag, Object key) {
		try {
			int idx = getIndexForKey(key);
			setFlag (idx,flag);
		}
		catch (NSKeyValueCoding.UnknownKeyException ex) {
			throw new NSKeyValueCoding.UnknownKeyException("Provided key (" + key + 
					") was not specified.",new Boolean(flag),key.toString());
		}
	}

	public Object valueForKey(String key) {
		boolean result;
		if (key.charAt(0)=='@') {
			result = getFlag(Integer.parseInt(key.substring(2)));
			if(key.charAt(1)=='_')
				result = !result;
		} else {
			String subKey = key;
			if (key.charAt(0)=='_')
				subKey = key.substring(1);
			try {
				result = flagForKey(subKey);
				if(subKey != key)
					result = !result;
			} catch (IllegalArgumentException ex) {
				throw new NSKeyValueCoding.UnknownKeyException("Provided key (" + key +
						") was not specified.",null,key);
			}
		}
		return Boolean.valueOf(result);
	}

	public void takeValueForKey(Object value, String key) {
		boolean flag;
		if(value instanceof Boolean)
			flag = ((Boolean)value).booleanValue();
		else {
			if(value instanceof Number)
				flag = (((Number)value).intValue() > 0);
			if(value instanceof CharSequence)
				flag = Boolean.parseBoolean(value.toString());
			else
				throw new IllegalArgumentException ("Provided value (" + value +
						") could not be converted to boolean.");
		}
		int idx;
		if (key.charAt(0)=='@') {
			idx = Integer.parseInt(key.substring(1));
			setFlag (idx,flag);
		} else {
			setFlagForKey(flag,key);
		}
	}
	
	protected Numerator numeratorFromArray(NSArray array) {
		final NSArray tmp = array;
		return new Numerator() {
			private final NSArray keys = tmp;
			public int numberForKey (Object key) {
				int result = keys.indexOfObject(key);
				if (result == NSArray.NotFound)
					throw new IllegalArgumentException(
							"Provided key (" + key + ") was not specified.");
				return result;
			} //numberForKey
			
			public String toString() {
				StringBuffer buf = new StringBuffer(this.getClass().getName());
				buf.append(" : ");
				Object[] temp = keys.objects();
				for (int i = 0; i < temp.length; i++) {
					buf.append(temp[i]).append (" ; ");
				}
				return buf.toString();
			} //toString
		}; //Numerator
	}
	
	public NamedFlags and(int otherFlags) {
		return new NamedFlags (flags & otherFlags,numerator);
	}
	
	public NamedFlags and(Number otherFlags) {
		return and(otherFlags.intValue());
	}
	
	public NamedFlags or(int otherFlags) {
		return new NamedFlags (flags | otherFlags,numerator);
	}
	
	public NamedFlags or(Number otherFlags) {
		return or(otherFlags.intValue());
	}
	
	public NamedFlags xor(int otherFlags) {
		return new NamedFlags (flags ^ otherFlags,numerator);
	}
	
	public NamedFlags xor(Number otherFlags) {
		return xor(otherFlags.intValue());
	}
/*	
	public String toString() {
		return super.toString() + ' ' + numerator.toString();
	}*/
}
