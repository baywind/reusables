//  SessionedEditingContext.java

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

import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class SessionedEditingContext extends EOEditingContext {
	protected WOSession session;
	
	protected Counter failures = new Counter();
	
	public SessionedEditingContext (WOSession ses){
			super((ses.objectForKey("objectStore")!=null)?
					(EOObjectStore)ses.objectForKey("objectStore"):
						EOObjectStoreCoordinator.defaultCoordinator());
		if (ses == null) throw new 
			NullPointerException ("You should define a session to instantiate SessionedEditingContext");
		session = ses;
	}
	
	public SessionedEditingContext (EOObjectStore parent,WOSession ses){
		super(parent);
		if (ses == null) throw new 
			NullPointerException ("You should define a session to instantiate SessionedEditingContext");
		session = ses;
	}
	
	public WOSession session() {
		return session;
	}
	
	public void saveChanges() {
		try {
			super.saveChanges();
			failures.nullify();
		} catch (RuntimeException ex) {
			failures.raise();
			throw ex;
		}
	}
	
	public int failuresCount () {
		return failures.value();
	}
}
