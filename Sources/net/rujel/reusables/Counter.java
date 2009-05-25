// Counter.java

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

public class Counter extends Number implements NSKeyValueCoding, Cloneable, Comparable {
	
	protected int counter = 0;
	
	public Counter () {
		super();
	}
	
	public Counter (int value) {
		super();
		counter = value;
	}

	public Counter (Number value) {
		super();
		counter = value.intValue();
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public static boolean canAccessFieldsDirectly() {
		return false;
	}
	
	public int value() {
		return counter;
	}

	public void setValue(Number value) {
		counter = value.intValue();
	}

	public void setValue(int value) {
		counter = value;
	}
	
	public int raise() {
		counter++;
		return counter;
	}
	
	public int lower() {
		counter--;
		return counter;
	}
	
	public void setAdd(Number toAdd) {
		add(toAdd.intValue());
	}
	
	public void add(int toAdd) {
		counter = counter + toAdd;
	}
	
	public void setSubstract(Number toSubstract) {
		substract(toSubstract.intValue());
	}

	public void substract(int toSubstract) {
		counter = counter - toSubstract;
	}

	public int nullify() {
		int value = counter;
		counter = 0;
		return value;
	}

	public int intValue() {
		return counter;
	}

	public double doubleValue() {
		return (double)counter;
	}

	public float floatValue() {
		return (float)counter;
	}

	public long longValue() {
		return (long)counter;
	}
	
	public boolean equals(Object arg) {
		if(arg instanceof Number)
			return (counter == ((Number)arg).intValue());
		else
			return false;
	}
	
	public Counter clone() {
		return new Counter(counter);
	}

	public int compareTo(Object arg0) {
		int compare = ((Number)arg0).intValue();
		return counter - compare;
	}
	
	public String toString() {
		return String.valueOf(counter);
	}
}
