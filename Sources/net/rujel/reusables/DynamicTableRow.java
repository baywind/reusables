//DynamicTableRow.java: Class file for WO Component 'DynamicTableRow'

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

import com.webobjects.appserver.*;

public class DynamicTableRow extends WOComponent {
	public int index;
	protected Object lastParam;
	protected boolean _isGerade = true;

	public DynamicTableRow(WOContext context) {
		super(context);
	}
	public boolean isStateless() {
		return true;
	}
	
	public void reset() {
		index = 0;
		lastParam = null;
		_isGerade = true;
	}
	
	public boolean isGerade() {
		if(hasBinding("switchParam")) {
			Object switchParam = valueForBinding("switchParam");
			if(switchParam == null) {
				if(lastParam != null) {
					lastParam = null;
					_isGerade = !_isGerade;
				}
			} else {
				if(!switchParam.equals(lastParam)) {
					lastParam = switchParam;
					_isGerade = !_isGerade;
				}
			}
			return _isGerade;
		} else {
			return ((index+1)%2==0);
		}
	}
	
	public String elementName() {
		String result = (String)valueForBinding("elementName");
		if(result == null)
			result = "tr";
		return result;
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public String color() {
		if(Various.boolForObject((valueForBinding("useStyles"))))
			return null;
		Object color;
		if (valueForBinding("item").equals(valueForBinding("selection"))) {
			color = valueForBinding("selectedColor");
		} else {
			color = (isGerade())?valueForBinding("geradeColor"):valueForBinding("ungeradeColor");
		}
		return (String)color;
	}

	public boolean disabled () {
		return ((!hasBinding("inactivateSelection") 
				|| Boolean.TRUE.equals(valueForBinding("inactivateSelection"))) 
				&& valueForBinding("item").equals(valueForBinding("selection")));
	}

	public String styleClass() {
		if(!(Various.boolForObject(valueForBinding("useStyles")) || hasBinding("class")))
			return null;
		String result = (hasBinding("class"))?(String)valueForBinding("class"):
			(isGerade())?"gerade":"ungerade";
		if ((hasBinding("isSelected"))?Various.boolForObject(valueForBinding("isSelected")):
				valueForBinding("item").equals(valueForBinding("selection"))) {
			result = "selection";
		}
		String suffix = (String)valueForBinding("styleSuffix");
		if(suffix != null)
			result = result + suffix;
		return result;
	}
}
