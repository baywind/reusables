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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
			toDo = toDo.substring(1);
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
			if(session == null)
				return application.handleSessionRestorationErrorInContext(context);
		} else {
			toDo = toDo.substring(separator + 1);
			separator = toDo.indexOf('/');
			if(separator == 0) {
				toDo = toDo.substring(1);
				separator = toDo.indexOf('/');
			}
		}
		if(separator > 0) {
			fragment = toDo.substring(0,separator);
			if(separator == toDo.length() -1) {
				toDo = null;
			} else {
				toDo = toDo.substring(separator +1);
//				if(toDo.charAt(toDo.length()-1) != '/')
//					separator = -1;
			}
		} else {
			fragment = toDo;
			toDo = "";
		}
		
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
		WOResponse response = null;
		if(!dir.exists()) {
			response = error("notExist", context);
		} else if(dir.isDirectory()) {
			response = fromFolder(dir, toDo, context);
		} else if(dir.getName().endsWith(".zip")) {
			response = fromZip(dir, toDo, context);
		} else {
			try {
				response = application.createResponseInContext(context); 
				String conType = application.resourceManager().
				contentTypeForResourceNamed(dir.getName());
				response.setHeader(conType,"Content-Type");
				response.setContentStream(new FileInputStream(dir),4096,dir.length());
				StringBuilder buf = new StringBuilder("attachment; filename=\"");
				buf.append(dir.getName()).append('"');
				response.setHeader(buf.toString(),"Content-Disposition");
				response.disableClientCaching();
			} catch (Exception e) {
				response = error("errorReading", context);
			}
		}
		application.saveSessionForContext(context);
		return response;
	}
	
	protected WOResponse fromFolder(File dir, String toDo, WOContext context) {
		boolean folder = (toDo == null || toDo.length() > 0 &&
				toDo.charAt(toDo.length() -1) == '/');
		if(folder && toDo != null)
			toDo = (toDo.length() <= 1)?null:toDo.substring(0,toDo.length() -1);
		try {
			File toLoad = dir;
			if(toDo != null && toDo.length() > 0)
				toLoad = new File(dir,toDo);
			if (toLoad.exists()) {
				if(toLoad.isDirectory() && folder) {
					File test = new File(toLoad,"index.html");
					if(!test.exists())
						test = new File(toLoad,"index.htm");
					if(test.exists())
						toLoad = test;
				}
				if(toLoad.isDirectory()) {
					WOComponent nextPage = WOApplication.application().pageWithName(
							"FileLister",context);
					nextPage.takeValueForKey(toLoad,"file");
					if(!folder)
						nextPage.takeValueForKey(Boolean.TRUE,"justNavigate");
					return nextPage.generateResponse();
				} else {
					WOApplication application = WOApplication.application();
					WOResponse response = application.createResponseInContext(context); 
//					response.setContentEncoding(_NSUtilities.WindowsCP1251StringEncoding);
					String conType = application.resourceManager().
					contentTypeForResourceNamed(toLoad.getName());
					response.setHeader(conType,"Content-Type");
					response.setContentStream(new FileInputStream(toLoad),4096,toLoad.length());
					response.disableClientCaching();
					return response;
				}
			} else {
				return error("notExist", context);
			}
		} catch (Exception exc) {
			return error("errorReading", context);
		}
//		return null;
	}
	
	protected WOResponse fromZip(File dir, String toDo, WOContext context) {
		boolean folder = (toDo == null || toDo.length() > 0 &&
				toDo.charAt(toDo.length() -1) == '/');
//		if(folder)
//			toDo = (toDo.length() <= 1)?null:toDo.substring(0,toDo.length() -1);
		try {
			final ZipFile zip = new ZipFile(dir);
			if(toDo==null || toDo.length() == 0) {
				Enumeration enu = zip.entries();
				String index = null;
				int depth = Integer.MAX_VALUE;
				while (enu.hasMoreElements()) {
					ZipEntry entry = (ZipEntry) enu.nextElement();
					if(entry.isDirectory())
						continue;
					String path = entry.getName();
					int idx = path.lastIndexOf('/');
					if(idx > 0)
						path = path.substring(idx +1);
					if(path.startsWith("index.")) {
						path = entry.getName();
						int count = 0;
						while (idx > 0) {
							count++;
							idx = path.lastIndexOf('/',idx -1);
						}
						if(count < depth) {
							index = entry.getName();
							depth = count;
						}
					}
				}
				if(index != null) {
					WORedirect rdr = new WORedirect(context);
					String path = context.request().uri();
					if(!folder)
						path = path + '/';
					path = path.concat(index);
					rdr.setUrl(path);
					zip.close();
					return rdr.generateResponse();
				}
				zip.close();
				return error("errorReading", context);
			}
			ZipEntry entry = zip.getEntry(toDo);
			if(entry == null) {
				zip.close();
				return error("notExist", context);
			}
			if(entry.isDirectory()) {
				if(!folder)
					toDo = toDo + '/';
				toDo = toDo.concat("index.htm");
				entry = zip.getEntry(toDo);
				if(entry == null) {
					toDo = toDo + 'l';
					entry = zip.getEntry(toDo);
				}
				if(entry == null) {
					zip.close();
					return error("notExist", context);
				}
			}
			FilterInputStream stream = new FilterInputStream(zip.getInputStream(entry)) {
				@Override
				public void close() throws IOException {
					super.close();
					zip.close();
				}
			};
			WOApplication application = WOApplication.application();
			WOResponse response = application.createResponseInContext(context); 
			
			String conType = application.resourceManager().
					contentTypeForResourceNamed(toDo);
			response.setHeader(conType,"Content-Type");
			response.setContentStream(stream,4096,entry.getSize());
			response.disableClientCaching();
			return response;
		} catch (Exception e) {
			return error("errorReading", context);
		}
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
