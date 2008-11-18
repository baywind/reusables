//  MutableFlags.java

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
import java.lang.reflect.Method;

public class MutableFlags extends Flags implements Cloneable {
	protected Object _syncWith;
	protected Method _syncMethod;

	public MutableFlags() {
		super(0);
	}
	
	public MutableFlags(int source) {
		super(source);
	}
	
	public Object clone () {
		return new MutableFlags(flags);
	}
	public void setFlags(int newFlags) {
		flags = newFlags;
		sync();
	}
	
	public void setFlag(int reg, boolean value) {
		if(value != getFlag(reg)) {
			int tmp = 1 << reg;
			flags = flags + (value?tmp:(-tmp));
		}
		sync();
	}
	
	public Flags immutableClone() {
		return new Flags(flags);
	}
	
	public void sync() {
		if(_syncWith != null && _syncMethod != null) {
			try {
				_syncMethod.invoke(_syncWith,this);
			} catch (Exception ex) {
				throw new RuntimeException("Error when flags syncing: " + ex.getCause(),ex);
			}
		}
	}
	
	public void setSyncParams(Object syncWith, String syncMethod) {
		try {
			Class cl = syncWith.getClass();
			Method meth = cl.getMethod(syncMethod, Number.class);
			setSyncParams(syncWith, meth);
		} catch (Exception e) {
			throw new RuntimeException("Error getting sync method",e);
		}
	}
	
	public void setSyncParams(Object syncWith, Method syncMethod) {
		if(syncWith == null || syncMethod == null)
			throw new NullPointerException("Requires non null parameters to sync");
		Class[] params = syncMethod.getParameterTypes();
		if(params == null || 
		   params.length != 1 || 
		   !(params[0].isInstance(this))) {
			throw new IllegalArgumentException("SyncMethod should take one parameter of type Flags");
		}
		_syncWith = syncWith;
		_syncMethod = syncMethod;
	}
	
	public MutableFlags and(int otherFlags) {
		return new MutableFlags (flags & otherFlags);
	}
	
	public MutableFlags and(Number otherFlags) {
		return and(otherFlags.intValue());
	}
	
	public MutableFlags or(int otherFlags) {
		return new MutableFlags (flags | otherFlags);
	}
	
	public MutableFlags or(Number otherFlags) {
		return or(otherFlags.intValue());
	}
	
	public MutableFlags xor(int otherFlags) {
		return new MutableFlags (flags ^ otherFlags);
	}
	
	public MutableFlags xor(Number otherFlags) {
		return xor(otherFlags.intValue());
	}	
}
