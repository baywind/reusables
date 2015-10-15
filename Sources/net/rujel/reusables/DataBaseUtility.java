// DataBaseUtility.java

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;


public class DataBaseUtility {
	
	public static Logger logger = Logger.getLogger("rujel.dbConnection");

	public static String dbEngine(String url) {
		if(!url.startsWith("jdbc:"))
			throw new IllegalArgumentException("URL string should begin with 'jdbc:'");
		int idx = url.indexOf(':', 5);
		return url.substring(5,idx);
	}
	
	public static void executeScript(EOObjectStore ec, String model, InputStream script) 
																				throws Exception {
		EODatabaseContext dctx;
		if(ec instanceof EODatabaseContext) {
			dctx = (EODatabaseContext)ec;
		} else {
			EOObjectStoreCoordinator os = null;
			if(ec instanceof EOObjectStoreCoordinator)
				os = (EOObjectStoreCoordinator)ec;
			else if(ec instanceof EOEditingContext)
				os = (EOObjectStoreCoordinator)((EOEditingContext)ec).rootObjectStore();
			EOModelGroup mg = EOModelGroup.modelGroupForObjectStoreCoordinator(os);
			EOModel mdl = mg.modelNamed(model);
			dctx = EODatabaseContext.registeredDatabaseContextForModel(mdl, os);
		}
		dctx.lock();
		try {
			EODatabaseChannel dcnl = dctx.availableChannel();
			EOAdaptorChannel acnl = dcnl.adaptorChannel();
			if(!acnl.isOpen()) {
				acnl = dctx.adaptorContext().createAdaptorChannel();
				acnl.openChannel();
			}
			executeScript(acnl, script);
		} finally {
			dctx.unlock();
		}
	}
	
	public static void executeScript(EOAdaptorChannel ac, InputStream script) throws Exception {
		BufferedReader update = new BufferedReader(new InputStreamReader(script,"utf8"));
		StringBuilder buf = new StringBuilder();
		EOSQLExpressionFactory exprFactory = ac.adaptorContext().adaptor().expressionFactory();
		while(true) {
			String line = update.readLine();
			if(line == null)
				break;
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.startsWith("--"))
				continue;
			if(buf.length() > 0)
				buf.append('\n');
			buf.append(line);
			if(line.charAt(line.length() -1) == ';') {
				line = buf.toString();
				buf.delete(0, buf.length());
				ac.evaluateExpression(exprFactory.expressionForString(line));
				if(ac.isFetchInProgress())
					ac.cancelFetch();
			}
		}
	}
	
	public static void createTables(EOModel model, EOAdaptorChannel ac) 
																		throws Exception {
		String framework = (model.userInfo() == null) ? null :
				(String)model.userInfo().valueForKey("framework");
		String filename = (String)model.connectionDictionary().valueForKey("URL");
		filename = "cre" + model.name() + '.' + dbEngine(filename);
		InputStream cre = WOApplication.application().resourceManager().inputStreamForResourceNamed(
				filename, framework, null);
		if(cre == null)
			throw new IllegalStateException("No tables creation script found");
		executeScript(ac, cre);
	}
	
	public static void updateModel(EOModel model, int fromVer, EOAdaptorChannel ac) 
																		throws Exception {
		NSDictionary modelInfo = model.userInfo();
		int modelVersion; 
		if(modelInfo == null) {
			throw new IllegalStateException("No modelInfo found");
		} else {
			String verStr = (String)modelInfo.valueForKeyPath("schemaVersion.number");
			if(verStr == null)
				throw new IllegalStateException("Model has no schema version");
			modelVersion = Integer.parseInt(verStr);
		}
		String framework = (String)modelInfo.valueForKey("framework");
		String filename = (String)model.connectionDictionary().valueForKey("URL");
		filename = "upd" + model.name() + '.' + dbEngine(filename);
		InputStream upd = WOApplication.application().resourceManager().inputStreamForResourceNamed(
				filename, framework, null);
		if(upd == null)
			throw new IllegalStateException("No schema update script found");
		BufferedReader update = new BufferedReader(new InputStreamReader(upd,"utf8"));
		int recentVer = 0;
		StringBuilder buf = new StringBuilder();
		EOSQLExpressionFactory exprFactory = ac.adaptorContext().adaptor().expressionFactory();
		lines:
		while(true) {
			String line = update.readLine();
			if(line == null)
				break;
			line = line.trim();
			if(line.length() == 0)
				continue;
			if(line.startsWith("--")) {
				StringBuilder num = null;
				for (int i = 2; i < line.length(); i++) {
					char c = line.charAt(i);
					if(Character.isWhitespace(c) || c == '-') {
						if(num != null && num.length() > 0)
							break;
						continue;
					}
					if(c == 'v') {
						num = new StringBuilder();
						continue;
					}
					if(num == null)
						continue lines;
					if(Character.isDigit(c))
						num.append(c);
					else
						break;
				}
				if(num != null && num.length() > 0) {
					if(fromVer < recentVer)
						fromVer = recentVer;
					recentVer = Integer.parseInt(num.toString());
					if(recentVer > modelVersion)
						break lines;
					if(fromVer < recentVer) {
						buf.append("Updating schema for model '").append(model.name()).append('\'');
						if(fromVer > 0) {
							buf.append(" from v").append(fromVer);
						}
						for (int i = line.length(); i > 3; i--) {
							char c = line.charAt(i -1);
							if(c != '-' && !Character.isWhitespace(c)) {
								buf.append(" to ").append(line.substring(2,i));
								break;
							}
						}
						logger.fine(buf.toString());
					}
					buf.delete(0, buf.length());
				}
				continue;
			} else if(recentVer <= fromVer) {
				continue;
			}
			if(buf.length() > 0)
				buf.append('\n');
			buf.append(line);
			if(line.charAt(line.length() -1) == ';') {
				line = buf.toString();
				buf.delete(0, buf.length());
				ac.evaluateExpression(exprFactory.expressionForString(line));
				if(ac.isFetchInProgress())
					ac.cancelFetch();
			}
		} // cycle lines
		upd.close();
		if(recentVer != modelVersion)
			throw new IllegalStateException(
					"Update script failed to update schema to actual model version");
	}
	
	@Deprecated
	public static boolean createDatabaseForModel(EOModel model) {
		NSDictionary modelInfo = model.userInfo();
		if(modelInfo == null) {
			return false;
		}
		String scriptName = (String)modelInfo.valueForKey("creationScript");
		if(scriptName == null)
			return false;
		String framework = (String)modelInfo.valueForKey("framework");
		InputStream script = WOApplication.application().resourceManager().
				inputStreamForResourceNamed(scriptName, framework, null);
		if(script == null) {
			logger.log(WOLogLevel.WARNING, "Autocreation script '" + scriptName + 
					"' for model '" + model.name() + "' was not found.");
			return false;
		}
		return executeScript(script);
	}
	
	public static boolean executeScript(InputStream script) {
		return executeScript(script, null);
	}
	
	public static boolean executeScript(InputStream script, NSDictionary params) {
		if(script == null)
			return false;
		SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
		if(dbSettings == null)
			return false;
		SettingsReader dbMapping = dbSettings.subreaderForPath("dbMapping", false);
		NSMutableDictionary mapping = null;
		if(params != null) {
			mapping = new NSMutableDictionary();
			Enumeration enu = params.keyEnumerator();
			while (enu.hasMoreElements()) {
				String key = (String) enu.nextElement();
				Object value = params.objectForKey(key);
				if(value instanceof CharSequence) {
					mapping.takeValueForKey(value.toString(), key);
					continue;
				}
				NSDictionary map = (NSDictionary)value;
				String dbName = (String)map.valueForKey("dbName");
				if(dbMapping != null)
					dbName = dbMapping.get(dbName, dbName);
				Object args = map.valueForKey("args");
				if(args != null) {
					if(args instanceof Object[])
						dbName = String.format(dbName, (Object[])args);
					else
						dbName = String.format(dbName, args);
				}
				mapping.takeValueForKey(dbName, key);
			} // params enumeration
		}
		if (dbMapping != null) {
			Enumeration enu = dbMapping.keyEnumerator();
			while (enu.hasMoreElements()) {
				String key = (String) enu.nextElement();
				if(key.indexOf("%s") >= 0)
					continue;
				if(mapping == null)
					mapping = new NSMutableDictionary(dbMapping.get(key, key), key);
				else
					mapping.takeValueForKey(dbMapping.get(key, key), key);
			}
		}
		try {
		      Connection conn = getConnection(dbSettings);
		      if(conn == null) {
		    	  logger.log(WOLogLevel.SEVERE,
		    			  "Failed to get database connection for script execution");
		    	  return false;
		      }
		      executeScript(conn, script, mapping);
		} catch (Exception e) {
			logger.log(WOLogLevel.SEVERE,"Error executing database script",e);
			return false;
		}
		return true;
	}
	
	protected static Connection getConnection(SettingsReader dbSettings) throws Exception {
		if(dbSettings == null)
			dbSettings = SettingsReader.settingsForPath("dbConnection", false);
		String serverURL = dbSettings.get("serverURL",null);
		String urlSuffix = dbSettings.get("urlSuffix",null);
		if(serverURL == null)
			return null;
		if(urlSuffix != null)
			serverURL = serverURL + urlSuffix; 
		Properties connectionProps = new Properties();
		connectionProps.put("user", dbSettings.get("username", "rujel"));
		connectionProps.put("password", dbSettings.get("password", "RUJELpassword"));
		urlSuffix = dbSettings.get("driver",null);
		if(urlSuffix != null) {
			Class driverClass = Class.forName(urlSuffix);
			Constructor driverConstructor = driverClass.getConstructor((Class[])null);
			Driver driver = (Driver)driverConstructor.newInstance((Object[])null);
			DriverManager.registerDriver(driver);
		}
		return DriverManager.getConnection(serverURL,connectionProps);
	}

	public static String[] extractDBfromURL(String url) {
		String[] out = new String[2];
		out[0] = url;
		int idx = url.indexOf(':', 5);
		if(idx <= 0) {
			return out;
		}
		if(url.charAt(idx +1) == '/') {
			idx = url.indexOf('/', idx +3);
			if(idx < 0)
				return out;
			idx++;
		}
		int idx2 = url.indexOf('?', idx);
		if(idx2 < 0) {
			out[0] = url.substring(0, idx);
			out[1] = url.substring(idx);
		} else {
			out[0] = url.substring(0, idx) + url.substring(idx2);
			out[1] = url.substring(idx,idx2);
		}
		return out;
	}
		
	public static Connection getConnection(NSDictionary connectionDictionary) throws Exception {
		String serverURL = (String)connectionDictionary.valueForKey("URL");
		if(serverURL == null)
			return null;
		String user = (String)connectionDictionary.valueForKey("username");
		String password = (String)connectionDictionary.valueForKey("password");
		String driverName = (String)connectionDictionary.valueForKey("driver");
		if(driverName != null) {
			Class driverClass = Class.forName(driverName);
			Constructor driverConstructor = driverClass.getConstructor((Class[])null);
			Driver driver = (Driver)driverConstructor.newInstance((Object[])null);
			DriverManager.registerDriver(driver);
		}
		return DriverManager.getConnection(serverURL,user,password);
	}
	
	public static void executeScript(Connection conn, InputStream script, NSDictionary mapping)
			throws SQLException, IOException {
	      Statement stmnt = conn.createStatement();
	      StringBuilder buf = new StringBuilder();
	      BufferedReader sql = new BufferedReader(new InputStreamReader(script,"utf8"));
	      int upd = 0;
	      while(true) {
	    	  String line = sql.readLine();
	    	  if(line == null)
	    		  break;
	    	  line = line.trim();
	    	  if(line.length() == 0)
	    		  continue;
	    	  if(line.startsWith("--"))
	    		  continue;
	    	  if(buf.length() > 0)
	    		  buf.append('\n');
	    	  buf.append(line);
	    	  if(line.charAt(line.length() -1) == ';') {
	    		  replace(buf, mapping);
	    		  line = buf.toString();
	    		  buf.delete(0, buf.length());
	    		  upd += stmnt.executeUpdate(line);
	    	  }
	      }
	      if(upd > 0) {
	    	  
	      }
	}
	
//	private boolean appendWithoutComments(String line, boolean cmnt, StringBuilder buf, int idx) {
//		if(cmnt) {
//			int end = line.indexOf("*/",idx);
//			if(end < 0)
//				return true;
//			end +=2;
//			if(end >= line.length())
//				return false;
//			return appendWithoutComments(line, false, buf, end);
//		} else {
//			int c = line.indexOf("*/",idx);
//			if(c < 0) {
//				buf.append(line.substring(idx));
//				return false;
//			}
//			if(c > 0)
//				buf.append(line.substring(idx, c));
//			c +=2;
//			return appendWithoutComments(line, true, buf, c);
//		}
//	}
	
	private static void replace(StringBuilder buf, NSDictionary mapping) {
		if(mapping == null)
			return;
		Enumeration enu = mapping.keyEnumerator();
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			String replacement = (String)mapping.valueForKey(key);
			int idx = buf.indexOf(key);
			while (idx >=0) {
				if(buf.charAt(idx -1) == '`')
					buf.replace(idx, idx + key.length(), replacement);
				idx = buf.indexOf(key, idx + replacement.length());
			}
		}
	}
}
