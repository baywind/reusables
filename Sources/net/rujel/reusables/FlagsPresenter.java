// FlagsPresenter.java: Class file for WO Component 'FlagsPresenter'

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
import com.webobjects.appserver.*;

public class FlagsPresenter extends WOComponent {
    public int idx;

    /** @TypeInfo java.lang.String */
    protected NSArray flagNames;
	protected Flags _flags;
    public String item;

    public FlagsPresenter(WOContext context) {
        super(context);
    }

    /** @TypeInfo java.lang.String */
    public NSArray flagNames() {
		if(flagNames == null)
			flagNames = (NSArray)valueForBinding("flagNames");
        return flagNames;
    }
	
    public boolean flag() {
        if(_flags == null)
			_flags = (Flags)valueForBinding("flags");
		return _flags.getFlag(idx);
    }
    public void setFlag(boolean newFlag) {
        if(_flags == null)
			_flags = (Flags)valueForBinding("flags");
        ((MutableFlags)_flags).setFlag(idx,newFlag);
    }
	
    public Boolean hasName() {
    	if(item == null || item.length() == 0)
    		return Boolean.FALSE;
    	if(item.charAt(0) == '-' && item.charAt(item.length() -1) == '-') {
    		if(item.length() < 3)
    			return Boolean.FALSE;
    		String substr = item.substring(1,item.length() -1).trim();
    		if(substr == null || substr.length() == 0)
    			return Boolean.FALSE;
    		try {
				int num = Integer.parseInt(substr);
				if(num > 0)
					return Boolean.FALSE;
			} catch (NumberFormatException e) {}
    	}
    	return Boolean.TRUE;
    }
    
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return false;
	}
	
	public void reset() {
		flagNames = null;
		_flags = null;
		item = null;
	}	

}
