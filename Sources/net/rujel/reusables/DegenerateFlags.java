//  DegenerateFlags.java

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

public class DegenerateFlags extends ImmutableNamedFlags {
	public static final DegenerateFlags ALL_TRUE = new DegenerateFlags(true);
	
	public static final DegenerateFlags ALL_FALSE = new DegenerateFlags(false);
	
	private boolean value;
	
	public DegenerateFlags(boolean init) {
		super(0);
		value = init;
		if(value) flags = -1;
	}
	
	public void setKeys (NSArray newKeys) {
		;
	}
	
	public NSArray getKeys () {
		return null;
	}
	
	public void setNumerator (Numerator newNum) {
		;
	}
	
	public int getIndexForKey(Object key) {
		return -1;
	}
	
	
	public boolean flagForKey (Object key) {
		return value;
	}
	
	public Object valueForKey(String key) {
		return new Boolean(value ^ (key.charAt(0)=='_'));
	}
	
	public boolean getFlag(int reg) {
		return value;
	}
	
	public ImmutableNamedFlags and(int otherFlags) {
		if(value)
			return new ImmutableNamedFlags (otherFlags);
		else
			return this;
	}
	
	public ImmutableNamedFlags and(Number otherFlags) {
		if(otherFlags instanceof NamedFlags) {
			if(value) {
				return ((NamedFlags)otherFlags).immutableClone();
			} else {
				ImmutableNamedFlags othr = ((NamedFlags)otherFlags).immutableClone();
				othr.flags = flags;
				return othr;
			}
		}
		return and(otherFlags.intValue());
	}
	
	public ImmutableNamedFlags or(int otherFlags) {
		if(value)
			return this;
		else
			return new ImmutableNamedFlags (otherFlags);
	}
	
	public ImmutableNamedFlags or(Number otherFlags) {
		if(otherFlags instanceof NamedFlags) {
			if(value) {
				ImmutableNamedFlags othr = ((NamedFlags)otherFlags).immutableClone();
				othr.flags = flags;
				return othr;
			} else {
				return ((NamedFlags)otherFlags).immutableClone();
			}
		}
		return or(otherFlags.intValue());
	}
	
	public ImmutableNamedFlags xor(int otherFlags) {
		if(value)
			return new ImmutableNamedFlags(~otherFlags);
		else
			return new ImmutableNamedFlags (otherFlags);
	}
	
	public ImmutableNamedFlags xor(Number otherFlags) {
		if(otherFlags instanceof NamedFlags) {
			if(value) {
				ImmutableNamedFlags othr = ((NamedFlags)otherFlags).immutableClone();
				othr.flags = ~othr.flags;
				return othr;				
			} else {
				return ((NamedFlags)otherFlags).immutableClone();
			}
		}
		return xor(otherFlags.intValue());
	}
	
	public String toString() {
		return this.getClass().getName() + " : " + value;
	}
	
}
