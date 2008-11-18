//  WOLogLevel.java

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
import java.util.logging.Level;

public class WOLogLevel extends Level {
	
	public static final WOLogLevel UNCOUGHT_EXCEPTION = new WOLogLevel("UNCOUGHT_EXCEPTION",1000);
	
	public static final WOLogLevel BREAKIN_ATTEMPT = new WOLogLevel("BREAKIN_ATTEMPT",900);
	
	public static final WOLogLevel SETTINGS_EDITING = new WOLogLevel("SETTINGS_EDITING",850);
	
	public static final WOLogLevel COREDATA_EDITING = new WOLogLevel("COREDATA_EDITING",825);
	
	public static final WOLogLevel SESSION = new WOLogLevel("SESSION",800);
	
	public static final WOLogLevel MASS_EDITING = new WOLogLevel("MASS_EDITING",750);
	
	public static final WOLogLevel MASS_READING = new WOLogLevel("MASS_READING",725);
	
	public static final WOLogLevel UNOWNED_EDITING = new WOLogLevel("UNOWNED_EDITING",550);
	
	public static final WOLogLevel OWNED_EDITING = new WOLogLevel("OWNED_EDITING",500);
	
	public static final WOLogLevel READING = new WOLogLevel("READING",450);
	
	protected WOLogLevel(String name, int value) {
		super(name,value);
	}
	
	protected WOLogLevel(String name, int value, String resourceBundleName) {
		super(name,value,resourceBundleName);
	}
}
