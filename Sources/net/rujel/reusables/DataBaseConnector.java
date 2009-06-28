// DataBaseConnector.java

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
//import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.EOEditingContext;

import java.util.Enumeration;
import java.util.logging.Logger;
import net.rujel.reusables.WOLogLevel;

public class DataBaseConnector {
	
	protected static final String[] keys = new String[] {"username","password","driver","plugin"};
	
	protected static NSMutableDictionary connectionDictionaryFromSettings (SettingsReader settings, NSMutableDictionary dict) {
		if(dict == null)
			dict = new NSMutableDictionary();
		
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			String value = settings.get(key, null);
			if(value != null)
				dict.takeValueForKey(value, key);
		}
		return dict;
	}

	public static void makeConnections() {
		Logger logger = Logger.getLogger("rujel.dbConnection");
		SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
		if(dbSettings == null) {
			logger.log(WOLogLevel.CONFIG, "No database connection settings found. Using predefined connections.");
			return;
		}
		EOModelGroup mg = EOModelGroup.defaultGroup();
		Enumeration models = mg.models().objectEnumerator();
		
		String serverURL = dbSettings.get("serverURL",null);
		boolean onlyHostname = !serverURL.startsWith("jdbc");
		String urlSuffix = dbSettings.get("urlSuffix",null);
		
		NSMutableDictionary connectionDictionary = connectionDictionaryFromSettings(dbSettings, null);
		EOEditingContext ec = new EOEditingContext();
		ec.lock();
		while (models.hasMoreElements()) {
			EOModel model = (EOModel) models.nextElement();
			SettingsReader currSettings = dbSettings.subreaderForPath(model.name(), false);
			if((currSettings != null)?
					currSettings.getBoolean("skip", false):
						model.name().endsWith("Prototypes")) {
				logger.config("Skipping model '" + model.name() + '\'');
				continue;
			}
			NSMutableDictionary cd = connectionDictionary.mutableClone();
			String url = null;
			String dbName = null;
			if(currSettings != null) {
				cd = connectionDictionaryFromSettings(currSettings, cd);
				url = currSettings.get("URL", null);
				if(url == null)
					dbName = currSettings.get("dbName", null);
			}
			if(url == null && serverURL != null) {
				String urlFromModel = (String)model.connectionDictionary().valueForKey("URL");
				if(dbName == null && onlyHostname) {
					url = urlFromModel.replaceFirst("localhost", serverURL);
				} else {
					int index = urlFromModel.indexOf("localhost");
					StringBuffer buf = new StringBuffer(serverURL);
					if (onlyHostname)
						buf.insert(0, urlFromModel.substring(0, index));
					if(dbName == null) {
						buf.append(urlFromModel.substring(index + 9));
					} else {
						if(onlyHostname)
							buf.append(urlFromModel.charAt(index + 9));
						else {
							char c = buf.charAt(buf.length() -1);
							if((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9'))
								buf.append('/');
						}
						buf.append(dbName);
						if(urlSuffix != null)
							buf.append(urlSuffix);
					}
					url = buf.toString();
				}
			} // if(url == null && serverURL != null)
			if(url != null)
				cd.takeValueForKey(url, "URL");
			if(cd.count() > 0) {
				try {
					EODatabaseContext.forceConnectionWithModel(model, cd, ec);
					String message = "Model '" + model.name() + "' connected to database";
					if(url != null)
						message = message + '\n' + url;
					logger.config(message);
				} catch (Exception e) {
					String message = "Model '" + model.name() + "' could not connect to database";
					if(url != null)
						message = message + '\n' + url;
					logger.log(WOLogLevel.WARNING, message, e);
				}
			}
		} // while (models.hasMoreElements())
		ec.unlock();
	}
}
