// UploadAnchor.java: Class file for WO Component 'UploadAnchor'

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

public class UploadAnchor extends WOComponent {

    public UploadAnchor(WOContext context) {
        super(context);
    }
	
	protected SmartUpload _uploadPage;

    public SmartUpload gogogo() {
			if(_uploadPage == null) {
        _uploadPage = (SmartUpload)pageWithName("SmartUpload");
			} else {
				_uploadPage.reset();
			}
				_uploadPage.takeValueForKey(this,"anchor");
				_uploadPage.takeValueForKey(valueForBinding("targetFilePath"),"targetFilePath");
				_uploadPage.takeValueForKey(preferences(),"prefs");
//			nextPage.takeValueForKey(valueForBinding("saveAction"),"saveAction");
//			nextPage.takeValueForKey(parent(),"returnPage");
//			nextPage.takeValueForKey(valueForBinding("attributeToSet"),"attributeToSet");

        return _uploadPage;
    }

		public NSDictionary preferences() {
			NSDictionary result = (NSDictionary)valueForBinding("prefsDict");
			if (result == null) {
					java.io.InputStream inputStream = application().resourceManager().inputStreamForResourceNamed((String)valueForBinding("prefsFile"), (String)valueForBinding("framework"), null);
					
					// read in the data, log exceptions
					try {
						NSData prefsData = new NSData(inputStream, inputStream.available());
						result = (NSDictionary)NSPropertyListSerialization.propertyListFromData(prefsData, "UTF8");
						inputStream.close();
					}  catch (Exception exception) {
						NSLog.out.appendln("UploadAnchor:  failed to read preferences file" + exception);
					}
			}
			return result;
		}
	
	/*
		public boolean isStateless() {
			return true;
		}
	*/	
		public boolean synchronizesVariablesWithBindings() {
        return false;
		}
	
	public WOComponent applyResult(String resultPath) {
		setValueForBinding(resultPath,"attributeToSet");
		WOComponent result = (WOComponent)valueForBinding("saveAction");
		if(result == null) {
			WOComponent temp = parent();
			do {
				result = temp;
				temp = result.parent();
			} while (temp != null);
			result.ensureAwakeInContext(context());
		}
		return result;
	}

    public String title() {
			String title = (String)valueForBinding("title");
			return (title == null)?"Загрузить файл":title;
    }
}
