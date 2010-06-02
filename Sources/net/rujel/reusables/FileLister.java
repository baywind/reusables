// FileLister: Class file for WO Component 'FileLister'

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

import java.io.File;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.foundation.NSArray;

public class FileLister extends WOComponent {
	public File file;
	public File item;
	public String target;
	protected boolean justNavigate = false;
	
	static {
		WOApplication app = WOApplication.application();
		WORequestHandler rh = app.requestHandlerForKey(FileRequestHandler.handlerKey);
		if(rh == null)
			app.registerRequestHandler(new FileRequestHandler(), FileRequestHandler.handlerKey);
	}
	
    public FileLister(WOContext context) {
        super(context);
    }
    
    public NSArray files() {
    	return new NSArray(file.listFiles());
    }
    
    public void setFilePath(String filePath) {
    	file = new File(filePath);
    }
    
    public void setJustNavigate(Object value) {
    	justNavigate = Various.boolForObject(value);
    }
    
    public String href() {
		if(standalone()) {
			StringBuilder buf = new StringBuilder();
			String path = context().request().requestHandlerPath();
			int idx = path.lastIndexOf('/');
			if(idx < path.length() -1) {
				buf.append(path.substring(idx +1)).append('/');
			}
			buf.append(item.getName());
			if(item.isDirectory() && !justNavigate)
				buf.append('/');
			return buf.toString();
		} else if(justNavigate) {
			return context().componentActionURL();
    	} else {
    		return FileRequestHandler.fileActionURLForFile(item, session(), null, null);
    	}
    }
    
    public boolean disableLink() {
    	return (!standalone() && justNavigate && file.isFile());
    }
    
    public WOActionResults open() {
    	file = item;
    	setValueForBinding(item, "file");
    	return null;
    }
    
    public boolean standalone() {
    	return context().page() == this;
    }
    
	public boolean isStateless() {
		return true;
	}
	
	public boolean synchronizesVariablesWithBindings() {
        return true;
	}
	
	public void reset() {
		super.reset();
		file = null;
		item = null;
		justNavigate = false;
		target = null;
	}
}