// LogInitialiser.java

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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.*;

public class LogInitialiser {

	public static void initLogging(InputStream propsIn,String propertiesPath,Logger logger) {
		try {
			LogManager lm = LogManager.getLogManager();
			if(propertiesPath != null) {
				propertiesPath = Various.convertFilePath(propertiesPath);
				propsIn = new FileInputStream(propertiesPath);
				System.out.println("Using logging.properties from: " + propertiesPath);
			}
			lm.reset();
			lm.readConfiguration(propsIn);
		
				//Formatter f = (Formatter)Class.forName(tmp).getConstructor((Class[])null).newInstance((Object[])null);
				Logger tmpLog = logger;
				while (tmpLog != null){
					Handler[] hs = tmpLog.getHandlers();
					for (int i = 0; i < hs.length; i++) {
						String formatterName = hs[i].getClass().getName();
						formatterName = lm.getProperty(formatterName + ".formatter");
						if(formatterName != null) {
							Class formatterClass = Class.forName(formatterName); 
							hs[i].setFormatter((Formatter)formatterClass.newInstance());
						}
						
					}
					tmpLog = tmpLog.getParent();
				}
		} catch (Exception ex) {
			System.err.println("Could not read logging properties, using system default. "+ ex);
		}
	}
}
