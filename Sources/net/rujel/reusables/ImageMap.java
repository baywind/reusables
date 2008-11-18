// ImageMap.java: Class file for WO Component 'ImageMap'

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
import java.io.*;

public class ImageMap extends WOComponent {
    protected NSMutableDictionary allMaps;
    public NSDictionary area;

		public boolean isStateless() {
    return true;
		}
		
		public boolean synchronizesVariablesWithBindings() {
        return false;
		}
		
		public void appendToResponse(WOResponse response, WOContext context) {
			String perform = context().request().stringFormValueForKey("perform");
			if (perform != null && perform.equals("remap"))
				allMaps = new NSMutableDictionary();
			super.appendToResponse(response,context);
		}
		
    public ImageMap(WOContext context) {
        super(context);
				allMaps = new NSMutableDictionary();
    }

    public NSDictionary mapResources() {
			String recentMap = (String)valueForBinding("mapFile");
		    Object mapResources = allMaps.valueForKey(recentMap);
				if (mapResources == null) {
						InputStream inputStream = application().resourceManager().inputStreamForResourceNamed(recentMap, null, null);

				// read in the data, log exceptions
					try {
								NSData mapData = new NSData(inputStream, inputStream.available());
								mapResources = NSPropertyListSerialization.propertyListFromData(mapData, "UTF8");
								inputStream.close();
								allMaps.takeValueForKey(mapResources,recentMap);
					}  catch (Exception exception) {
								NSLog.out.appendln("Application:  failed to read in image map");
					}
				}
				return (NSDictionary)mapResources;
		}
    public String usemap() {
        return "#" + (String)mapResources().valueForKey("mapName");
    }
		
    public String imgSrc() {
        return application().resourceManager().urlForResourceNamed ((String)mapResources().valueForKey("imageFile"), null, null, context().request());

    }
		
    public String areaHref() {
				String href = (String)area.valueForKey("href");
        if (href == null) {
						href = context().directActionURLForActionNamed((String)area.valueForKey("directAction"), null);
				} else {
					href = href + "\" target=\"separate";
				}
		return href;
		}
}
