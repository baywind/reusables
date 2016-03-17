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
import com.webobjects.jdbcadaptor.JDBCAdaptorException;
//import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;

import java.util.Enumeration;
import java.util.logging.Logger;
import net.rujel.reusables.WOLogLevel;

public class DataBaseConnector {
	
	protected static final String[] keys = new String[] 
	                                      {"username","password","driver","plugin"};
	
	protected static NSMutableDictionary coordinatorsByTag;
	protected static NSMutableDictionary cdForDB = new NSMutableDictionary();
	
	protected static NSMutableDictionary connectionDictionaryFromSettings (
			SettingsReader settings, NSMutableDictionary dict) {
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

	protected static EOEntity prototypesEntity;
	public static NSArray makeConnections(String tag, NSArray models, boolean canCreate) {
		Logger logger = Logger.getLogger("rujel.dbConnection");
		SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
		if(dbSettings == null) {
			logger.log(WOLogLevel.CONFIG,
					"No database connection settings found. Using predefined connections.");
			return NSArray.EmptyArray;
		}
		EOModelGroup mg = EOModelGroup.defaultGroup();
		String prototypes = dbSettings.get("prototypes",null);
		if(prototypes != null) {
	        EOEntity jdbcPrototypesEntity = mg.entityNamed("EOJDBCPrototypes");
	        if(prototypesEntity == null)
	        	prototypesEntity = mg.entityNamed(prototypes);
			if(prototypesEntity == null) {
				NSDictionary plist = (NSDictionary)PlistReader.readPlist(prototypes, null);
				if(plist != null) {
					EOModel model = jdbcPrototypesEntity.model();
					prototypesEntity = new EOEntity(plist,model);
					model.addEntity(prototypesEntity);
				}
			}
			if(prototypesEntity != null && !prototypesEntity.equals(jdbcPrototypesEntity)) {
				logger.log(WOLogLevel.CONFIG,"Using prototypes from " + prototypes);
				Enumeration enu = jdbcPrototypesEntity.attributes().objectEnumerator();
				while (enu.hasMoreElements()) {
					EOAttribute jdbcPrototype =  (EOAttribute)enu.nextElement();
					String prototypesName = (String)jdbcPrototype.name();
					EOAttribute dbPrototype = 
						(EOAttribute)prototypesEntity.attributeNamed(prototypesName);
					if (dbPrototype != null) {
						jdbcPrototype.setDefinition(dbPrototype.definition());
						jdbcPrototype.setExternalType(dbPrototype.externalType()); 
						jdbcPrototype.setPrecision(dbPrototype.precision()); 
						jdbcPrototype.setReadFormat(dbPrototype.readFormat()); 
						jdbcPrototype.setScale(dbPrototype.scale()); 
						jdbcPrototype.setUserInfo(dbPrototype.userInfo()); 
						jdbcPrototype.setValueType(dbPrototype.valueType()); 
						jdbcPrototype.setWidth(dbPrototype.width()); 
						jdbcPrototype.setWriteFormat(dbPrototype.writeFormat());
	                }
				}
			} else {
				logger.log(WOLogLevel.WARNING,"Could not load prototypes " + prototypes);
			}
		}
		Enumeration enu = mg.models().immutableClone().objectEnumerator();
		
		String serverURL = dbSettings.get("serverURL",null);
		String urlSuffix = dbSettings.get("urlSuffix",null);
		boolean disableSchemaUpdate = dbSettings.getBoolean("disableSchemaUpdate", false);
		
		NSMutableArray success = new NSMutableArray();
		NSMutableDictionary connDict = connectionDictionaryFromSettings(dbSettings, null);
//		EOEditingContext ec = (os != null)?new EOEditingContext(os):new EOEditingContext();
		SettingsReader dbMapping = dbSettings.subreaderForPath("dbMapping", false);
		cdForDB.removeAllObjects();
		while (enu.hasMoreElements()) {
			EOModel model = (EOModel) enu.nextElement();
			if(model.name().endsWith("Prototypes")) {
				Enumeration ents = model.entityNames().immutableClone().objectEnumerator();
				while (ents.hasMoreElements()) {
					String entName = (String) ents.nextElement();
					if(!entName.equals("EOJDBCPrototypes")) {
						model.removeEntity(model.entityNamed(entName));
					}
 				}
				continue;
			}
			SettingsReader currSettings = dbSettings.subreaderForPath(model.name(), false);
			if((model != null && !models.containsObject(model.name())) ||
					(currSettings != null && currSettings.getBoolean("skip", false))) {
				mg.removeModel(model);
				logger.config("Omitting model '" + model.name() + '\'');
				continue;
			}
			NSMutableDictionary cd = null;
			String url = dbSettings.get("globalURL",null);
			String dbName = null;
			if(currSettings != null) {
				url = currSettings.get("URL", url);
				dbName = currSettings.get("dbName", null);
				if(dbName != null) {
					if(dbMapping != null) {
						Object mapped = dbMapping.valueForKey(dbName);
						if(mapped != null) {
							if(mapped instanceof String) {
								dbName = (String)mapped;
							} else if(mapped instanceof SettingsReader) {
								cd = connDict.mutableClone();
								cd = connectionDictionaryFromSettings((SettingsReader)mapped, cd);
								url = ((SettingsReader)mapped).get("URL", null);
								if(url == null)
									dbName = ((SettingsReader)mapped).get("dbName", null);
							}
						}
					}
					if(tag != null && dbName.contains("%s"))
						dbName = String.format(dbName, tag);
					if(dbName.startsWith("jdbc")) {
						url = dbName;
						dbName = null;
					} else {
						cd = (NSMutableDictionary)cdForDB.valueForKey(dbName);
						if(cd == null) {
							cd = connDict.mutableClone();
							cd = connectionDictionaryFromSettings(currSettings, cd);
							cdForDB.takeValueForKey(cd, dbName);
						} else if(cd.valueForKey("error") != null) {
							continue;
						} else {
							model.setConnectionDictionary(cd);
							Object res = verifyConnection(EOObjectStoreCoordinator.defaultCoordinator(),
									model, logger, disableSchemaUpdate, !canCreate);
							if(res != null) {
								if(res instanceof Number)
									model.userInfo().takeValueForKey(res, "versionFound");
								success.addObject(model);
							}
							continue;
						}
					}
				} //(dbName != null)
			}
			if(cd == null)
				cd = connDict.mutableClone();
			if(cd.count() > 0) {
				if(url == null && serverURL != null)
					url = prepareConnectionURL(serverURL, urlSuffix, model, dbName);
				if(url != null) {
					cd.takeValueForKey(url, "URL");
				}
				String changeCase = dbSettings.get("changeCase",null);
				if(changeCase != null) {
					if(dbName == null)
						dbName = DataBaseUtility.extractDBfromURL(url)[1];
					String tmp = dbName;
					if(changeCase.equalsIgnoreCase("lowercase"))
						dbName = dbName.toLowerCase();
					else if(changeCase.equalsIgnoreCase("uppercase"))
						dbName = dbName.toUpperCase();
					if(url != null) {
						url = url.replaceAll(tmp, dbName);
						cd.takeValueForKey(url, "URL");
					}
				}
				String usernameForDB = dbSettings.get("usernameForDB",null);
				if(Various.boolForObject(usernameForDB)) {
					if(dbName == null)
						dbName = model.name();
					if(usernameForDB.equalsIgnoreCase("lowercase"))
						dbName = dbName.toLowerCase();
					else if(usernameForDB.equalsIgnoreCase("uppercase"))
						dbName = dbName.toUpperCase();
					cd.takeValueForKey(dbName, "username");
				} // usernameForDB
				model.setConnectionDictionary(cd);
			}
			/*
			if(cd.count() > 0) {
				EODatabaseContext dc = null;
				try {
					ec.lock();
					dc = EODatabaseContext.forceConnectionWithModel(model,cd,ec);
					dc.lock();
					success &= verifyConnection(dc, model,logger, url, disableSchemaUpdate);
				} catch (Exception e) {
					StringBuilder message = new StringBuilder("Model '");
					message.append(model.name());
					int len = message.length();
					message.append("' could not connect to database");
					if(url != null)
						message.append('\n').append(url);
					logger.log((noSettings)? WOLogLevel.INFO : WOLogLevel.WARNING,
							message.toString(), e);
					if(!disableSchemaUpdate 
							&& e.getMessage().toLowerCase().startsWith("unknown database")
							&& DataBaseUtility.createDatabaseForModel(model)) {
						logger.log(WOLogLevel.INFO,
								"Autocreated database for model " + model.name());
						try {
							dc = EODatabaseContext.forceConnectionWithModel(model,cd,ec);
							dc.lock();
							if(verifyConnection(dc, model,logger, url, disableSchemaUpdate)) {
								continue;
							}
						} catch (Exception e2) {
							message.delete(len, message.length());
							message.append("' also could not connect to autocreated database");
							message.append('\n').append(url);
							logger.log(WOLogLevel.WARNING, message.toString(), e2);
						}
					}
					if(noSettings) {
						logger.config("Skipping model '" + model.name() + '\'');
						mg.removeModel(model);
					} else {
						boolean retry = false;
						if(url != null) {
							String untagged = (currSettings==null)?null:
								currSettings.get("untagged", null);
							if(untagged != null) { // try connecting to untagged
								url = url.replaceFirst(dbName, untagged);
								cd.takeValueForKey(url, "URL");
								message.delete(len, message.length());
								try {
									logger.info("Trying to connect to untagged database");
									dc = EODatabaseContext.forceConnectionWithModel(model, cd, ec);
									dc.lock();
									retry = verifyConnection(
											dc, model,logger,url, disableSchemaUpdate);
								} catch (Exception ex) {
									message.append("' also could not connect to database");
									message.append('\n').append(url);
									logger.log(WOLogLevel.WARNING, message.toString(), ex);
								}
							}
						}
						success &= retry;
					}
				} finally {
					if(dc != null)
						dc.unlock();
					ec.unlock();
				} 
			} // cd.count() > 0
			 */			
//			EODatabaseContext dc = EODatabaseContext.registeredDatabaseContextForModel(model, os);
//			EOAdaptorChannel ac = dc.availableChannel().adaptorChannel();
//			ac.closeChannel();
			Object res = verifyConnection(EOObjectStoreCoordinator.defaultCoordinator(),
					model, logger, disableSchemaUpdate, !canCreate);
			if(res instanceof Exception && !(res instanceof NumberFormatException)) {
				cd.takeValueForKey(res,"error");
//				cd.takeValueForKey(model, "onModel");
				success.addObject(cd);
			} else if(res != null) {
				if(res instanceof Number)
					model.userInfo().takeValueForKey(res, "versionFound");
				success.addObject(model);
			}
		} // while (models.hasMoreElements())
		if(tag != null && success.count() == 0) {
			EOObjectStoreCoordinator.defaultCoordinator().setUserInfoForKey(tag, "tag");
			coordinatorsByTag = new NSMutableDictionary(
					EOObjectStoreCoordinator.defaultCoordinator(),tag);
		}
//		ec.dispose();
		if(success.count() == 0)
			return null;
		return success;
	}

	protected static String prepareConnectionURL(String serverURL,
			String urlSuffix, EOModel model, String dbName) {
		String url;
		boolean onlyHostname = !serverURL.startsWith("jdbc");
		String urlFromModel = (String)model.connectionDictionary().valueForKey("URL");
		if(dbName == null && onlyHostname) {
			url = urlFromModel.replaceFirst("localhost", serverURL);
			if(urlSuffix != null) {
				int idx = url.indexOf('?');
				if(idx > 0)
					url = url.substring(0,idx);
				url = url + urlSuffix;
			}
		} else {
			StringBuffer buf = new StringBuffer(serverURL);
			if(buf.charAt(buf.length() -1) == '/')
				buf.deleteCharAt(buf.length() -1);
			int index = urlFromModel.indexOf("localhost");
			if(index < 0) {
				if(urlFromModel.startsWith(serverURL))
					index = serverURL.length();
				if(urlFromModel.charAt(index) != '/')
					index--;
			} else {
				if (onlyHostname)
					buf.insert(0, urlFromModel.substring(0, index));
				index += 9;
			}
			if(dbName == null) {
				int idx = urlFromModel.indexOf('?',index);
				if(idx > 0 && urlSuffix != null) {
					buf.append(urlFromModel.substring(index,idx));
				} else {
					buf.append(urlFromModel.substring(index));
				}
			} else {
				if(onlyHostname)
					buf.append(urlFromModel.charAt(index));
				else {
					char c = buf.charAt(buf.length() -1);
					if((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9'))
						buf.append('/');
				}
				buf.append(dbName);
			}
			if(urlSuffix != null)
				buf.append(urlSuffix);
			url = buf.toString();
		}
		return url;
	}
		
	protected static Object verifyConnection(EOObjectStoreCoordinator os, EOModel model,
			Logger logger, boolean noUpd, boolean noCre) {
		NSDictionary modelInfo = model.userInfo();
		if(modelInfo != null)
			modelInfo = (NSDictionary)modelInfo.valueForKey("schemaVersion");
		NSDictionary cd = model.connectionDictionary();
		if(modelInfo != null) {
			int modelNum = -1;
			NSDictionary schemaVersion = null;
			StringBuilder buf = new StringBuilder(
"SELECT VERSION_NUMBER, VERSION_TITLE FROM SCHEMA_VERSION WHERE MODEL_NAME = '");
			buf.append(model.name()).append("' ORDER BY VERSION_NUMBER DESC;");
			EODatabaseContext dc = EODatabaseContext.registeredDatabaseContextForModel(model, os);
			dc.lock();
			EOAdaptorChannel ac = null;
			try {
				cd = dc.database().adaptor().connectionDictionary();
				ac = dc.availableChannel().adaptorChannel();
			} catch (JDBCAdaptorException e) {
				String msg = "Failed to connect to database "
					+ model.connectionDictionary().valueForKey("URL");
				logger.log(WOLogLevel.INFO,msg,e);
				dc.unlock();
				Exception ex = e.sqlException();
				if(ex == null) ex = e;
				return ex;
			}
			try {
				modelNum = Integer.parseInt(modelInfo.valueForKey("number").toString());
				EOSQLExpression expr = ac.adaptorContext().adaptor().
						expressionFactory().expressionForString(buf.toString());
				ac.evaluateExpression(expr);
				NSArray descResults = ac.describeResults();
				EOAttribute attr = (EOAttribute)descResults.objectAtIndex(0);
				attr.setName("number");
				attr = (EOAttribute)descResults.objectAtIndex(1);
				attr.setName("title");
				ac.setAttributesToFetch(descResults);
				schemaVersion = ac.fetchRow();
				if(ac.isFetchInProgress())
					ac.cancelFetch();
			} catch (Exception e) {
				String msg = "Failed to fetch schema info for model " + model.name();
				logger.log(WOLogLevel.INFO,msg,e);
				dc.unlock();
				return e;
			}
			buf.delete(0, buf.length());
			buf.append("Model '").append(model.name()).append('\'');
			int schemNum = (schemaVersion == null)?0:
				((Number)schemaVersion.valueForKey("number")).intValue();
			if(modelNum != schemNum) {
				buf.append(" requires schema version ").append(modelNum);
				buf.append('(').append(modelInfo.valueForKey("title")).append(')');
				if(schemaVersion != null) {
					buf.append(". found version ").append(schemNum).append('(');
					buf.append(schemaVersion.valueForKey("title")).append(')');
				} else {
					buf.append(". No schema info found.");
				}
				if(noUpd || schemNum > modelNum) {
					logger.severe(buf.toString());
					return schemaVersion.valueForKey("number");
				} else {
					if(noCre && schemaVersion == null) {
						logger.severe(buf.toString());
						dc.unlock();
						return new Integer(0);
					}
					logger.info(buf.toString());
					buf.delete(0, buf.length());
					try {
						if (schemaVersion == null) {
							DataBaseUtility.createTables(model, ac);
						} else {
							DataBaseUtility.updateModel(model, schemNum, ac);
						}
						buf.append("Schema for model '").append(model.name());
						if(schemaVersion == null) {
							buf.append("' created with v");
						} else {
							buf.append("' updated from v").append(schemNum).append(' ').append('(');
							buf.append(schemaVersion.valueForKey("title")).append(") to v");
						}
						buf.append(modelNum).append(' ').append('(');
						buf.append(modelInfo.valueForKey("title")).append(')');
						logger.info(buf.toString());
					} catch (Exception e) {
						if(schemaVersion == null)
							buf.append("Failed to create schema for model '");
						else
							buf.append("Failed to update schema for model '");
						buf.append(model.name()).append('\'');
						logger.log(WOLogLevel.SEVERE,buf.toString(),e);
						return buf;
					}
				} // if can update
			} // (modelNum != schemNum)
//			ac.closeChannel();
			dc.unlock();
		} // check modelInfo
			StringBuilder message = new StringBuilder("Model '");
			message.append(model.name()).append("' connected to database");
			if(modelInfo != null) {
				message.append(", schema version: ");
				message.append(modelInfo.valueForKey("title"));
			}
			String url = (String)cd.valueForKey("URL");
			if(url != null)
				message.append('\n').append(url);
			logger.config(message.toString());
		return null;
	}
	
	public static EOObjectStore objectStoreForTag(String tag) {
		return objectStoreForTag(tag,false);
	}
	public static EOObjectStore objectStoreForTag(String tag, boolean force) {
		Object os = coordinatorsByTag.valueForKey(tag);
		if(force || os == null) {
			SettingsReader dbSettings = SettingsReader.settingsForPath("dbConnection", false);
			String serverURL = dbSettings.get("serverURL",null);
			String urlSuffix = dbSettings.get("urlSuffix",null);
			boolean disableSchemaUpdate = dbSettings.getBoolean("disableSchemaUpdate", false);
			NSMutableDictionary connDict = connectionDictionaryFromSettings(dbSettings, null);
			os = new EOObjectStoreCoordinator();
			EOEditingContext ec = new EOEditingContext((EOObjectStoreCoordinator)os);
			ec.lock();
//			EOModelGroup mg = new EOModelGroup();
//			mg.setDelegate(EOModelGroup.defaultGroup().delegate());
//			EOModelGroup.setModelGroupForObjectStoreCoordinator((EOObjectStoreCoordinator)os, mg);
			SettingsReader dbMapping = dbSettings.subreaderForPath("dbMapping", false);
			Enumeration enu = EOModelGroup.defaultGroup().models().objectEnumerator();
			Logger logger = Logger.getLogger("rujel.dbConnection");
			while (enu.hasMoreElements()) {
				EOModel model = (EOModel) enu.nextElement();
				NSMutableDictionary cd = model.connectionDictionary().mutableClone();
//				model = mg.addModelWithPathURL(model.pathURL());
//				model.setConnectionDictionary(cd);
				if(model.name().endsWith("Prototypes"))
					continue;
				SettingsReader currSettings = dbSettings.subreaderForPath(model.name(), false);
				if(currSettings == null)
					continue;
				String dbName = null;
				dbName = currSettings.get("dbName", null);
				if(dbMapping != null) {
					Object mapped = dbMapping.valueForKey(dbName);
					if(mapped != null) {
						if(mapped instanceof String) {
							dbName = (String)mapped;
						} else if(mapped instanceof SettingsReader) {
							dbName = ((SettingsReader)mapped).get("dbName", null);
							if(dbName == null)
								continue;
						}
					}
				}
				if(dbName == null || !dbName.contains("%s"))
					continue;
				dbName = String.format(dbName, tag);
				
				cd = (NSMutableDictionary)cdForDB.valueForKey(dbName);
				if(cd == null) {
					String url = dbName;
					try {
						cd = connDict.mutableClone();
						cd = connectionDictionaryFromSettings(currSettings, cd);
						url = prepareConnectionURL(serverURL, urlSuffix, model, dbName);
						cd.takeValueForKey(url, "URL");
						cdForDB.takeValueForKey(cd, dbName);
						EODatabaseContext.forceConnectionWithModel(model, cd, ec);
					} catch (Exception e) {
						logger.log(WOLogLevel.INFO, "Failed connecting to database '"
								+ dbName + "' with url " + url, e);
						ec.unlock();
						return null;
					}
//					EOAdaptorChannel ac = dc.availableChannel().adaptorChannel();
//					ac.closeChannel();
				}
//				model.setConnectionDictionary(cd);
				if(verifyConnection((EOObjectStoreCoordinator)os,
						model, logger, disableSchemaUpdate, true) != null) {
					coordinatorsByTag.setObjectForKey(NSKeyValueCoding.NullValue, tag);
					ec.unlock();
					return null;
				}
			} // EOModelGroup.defaultGroup().models().objectEnumerator();
			
			coordinatorsByTag.setObjectForKey(os, tag);
			ec.unlock();
		} else if(os == NSKeyValueCoding.NullValue) {
			return null;
		}
		return (EOObjectStore)os;
	}
	
}
