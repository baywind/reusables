//
//  FileRequestHandler.java
//  Experiment
//
//  Created by Gennady Kushnir on 27.11.06.
//  Copyright (c) 2006 __MyCompanyName__. All rights reserved.
//
package net.rujel.reusables;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import java.io.*;

public class FileRequestHandler extends WORequestHandler {
	
	public static final String handlerKey = "fs";
	
	protected NSKeyValueCoding messages;
	protected static final NSDictionary errors = new NSDictionary(
			new String[] {"Wrong url format","You are trying to read from an uninitialised index",
					"requested file does not exist","An error occured reading requested file"},
			new String[] {"urlFormat","unknownIndex","notExist","errorReading"});
	
	public FileRequestHandler () {
		super();
	}
	
	public FileRequestHandler (NSKeyValueCoding localisation) {
		super();
		messages = localisation;
	}

	protected WOResponse error(String errorKey, WOContext context) {
		String error = (messages==null)?null:(String)messages.valueForKey(errorKey);
		if(error == null)
			error = (String)errors.valueForKey(errorKey);
		WOResponse response = WOApplication.application().createResponseInContext(context);
		response.setContent(error);
		response.setHeader("text/plain","Content-Type");
		return response;
	}
	
	public WOResponse handleRequest(WORequest aRequest) {
		WOApplication application = WOApplication.application();
		WOContext context = application.createContextForRequest(aRequest);
		String toDo = aRequest.requestHandlerPath();

		//try to restore session
		
		int separator = toDo.indexOf('/');
		if(separator == 0) {
			toDo = toDo.substring(1,toDo.length());
			separator = toDo.indexOf('/');
		}
//		if(separator <= 0)
//			return error("urlFormat", context);
		
		String fragment = (separator < 0)?toDo:toDo.substring(0,separator);
		WOSession session = (separator <= 0)?null:
			application.restoreSessionWithID(fragment,context);
		if(session == null) {
			String wosid = application.sessionIdKey();
			String sesId = aRequest.cookieValueForKey(wosid);
			if(sesId == null)
				sesId = aRequest.stringFormValueForKey(wosid);
			if(sesId != null)
				session = application.restoreSessionWithID(sesId,context);
		} else {
			toDo = toDo.substring(separator + 1);
			separator = toDo.indexOf('/');
			if(separator >= 0) {
				fragment = toDo.substring(0,separator);
				if(separator == toDo.length() -1) {
					toDo = null;
				} else {
					toDo = toDo.substring(separator + 1);
					if(toDo.charAt(toDo.length()-1) != '/')
						separator = -1;
				}
			} else {
				fragment = toDo;
				toDo = null;
			}
		}
		if(session == null)
			return application.handleSessionRestorationErrorInContext(context);
		File dir = null;
		try {
			int index = Integer.parseInt(fragment);
			NSMutableArray files = (NSMutableArray)session.objectForKey("FileRequestHandler");
			if(files == null || files.count() <= index) {
				application.saveSessionForContext(context);
				return error("unknownIndex", context);
			}
			dir = (File)files.objectAtIndex(index);
		} catch (NumberFormatException e) {
			application.saveSessionForContext(context);
			return error("urlFormat", context);
		}		
		WOResponse response;
		try {
			File toLoad = dir;
			if(toDo != null && toDo.length() > 0)
				toLoad = new File(dir,toDo);
			if (toLoad.exists()) {
				if(toLoad.isDirectory() && separator >= 0) {
					File test = new File(toLoad,"index.html");
					if(!test.exists())
						test = new File(toLoad,"index.htm");
					if(test.exists())
						toLoad = test;
				}
				if(toLoad.isDirectory()) {
					WOComponent nextPage = application.pageWithName("FileLister",context);
					nextPage.takeValueForKey(toLoad,"file");
					if(separator < 0)
						nextPage.takeValueForKey(Boolean.TRUE,"justNavigate");
					response = nextPage.generateResponse();
				} else {
					response = application.createResponseInContext(context); 
//					response.setContentEncoding(_NSUtilities.WindowsCP1251StringEncoding);
					String conType = application.resourceManager().
					contentTypeForResourceNamed(toLoad.getName());
					response.setHeader(conType,"Content-Type");
					response.setContentStream(new FileInputStream(toLoad),4096,toLoad.length());
					response.disableClientCaching();
				}
			} else {
				response = error("notExist", context);
			}
		} catch (Exception exc) {
			response = error("errorReading", context);
		}
		application.saveSessionForContext(context);
		return response;
	}
	
	public static String fileActionURLForFile(File file, WOSession session,
			String subPath, String modifiers) {
		NSMutableArray files = (NSMutableArray)session.objectForKey("FileRequestHandler");
		int idx = 0;
		if(files == null) {
			files = new NSMutableArray(file);
			session.setObjectForKey(files, "FileRequestHandler");
		} else {
			idx = files.indexOfObject(file);
			if(idx < 0) {
				idx = files.count();
				files.addObject(file);
			}
		}
		StringBuilder buf = new StringBuilder();
		if(session.storesIDsInURLs())
			buf.append(session.sessionID()).append('/');
		buf.append(idx);
		if(subPath != null) {
			if(subPath.charAt(0) != '/')
				buf.append('/');
			buf.append(subPath);
		} else if(file.isDirectory()) {
			buf.append('/');
		}
		return session.context().urlWithRequestHandlerKey(handlerKey, buf.toString(), modifiers);
	}
}
