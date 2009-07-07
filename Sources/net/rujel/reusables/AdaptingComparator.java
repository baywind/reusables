// AdaptingComparator.java

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

import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSComparator;

public class AdaptingComparator extends NSComparator {
	
	protected Class sortingClass;
	protected EOSortOrdering.ComparisonSupport support;

	public AdaptingComparator() {
		super();
	}
	public AdaptingComparator(Class predefineClass) throws ComparisonException {
		super();
		setClass(predefineClass);
	}

	public void setClass(Class predefineClass) throws ComparisonException{
		sortingClass = predefineClass;
		if(predefineClass == null) {
			support = null;
			return;
		}
		support = EOSortOrdering.ComparisonSupport.supportForClass(sortingClass);
		if(support == null)
			throw new ComparisonException("Could not get ComparisonSupport for given class: "
					+ predefineClass.getCanonicalName());
	}
	
	public void setSupport (EOSortOrdering.ComparisonSupport newSupport) {
		support = newSupport;
	}
	
	public int compare(Object arg0, Object arg1) throws ComparisonException {
		Object nonNull = arg0;
		if(nonNull == null)
			nonNull = arg1;
		if(nonNull == null)
			return OrderedSame;
		if(support == null || !sortingClass.isInstance(nonNull))
			setClass(nonNull.getClass());
		try {
			return support.compareAscending(arg0, arg1);
		} catch (Exception e) {
			throw new ComparisonException(e.toString());
		}
	}
}
