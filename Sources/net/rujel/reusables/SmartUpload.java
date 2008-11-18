// SmartUpload.java: Class file for WO Component 'SmartUpload'

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
//import com.webobjects.eocontrol.*;
//import com.webobjects.eoaccess.*;
import java.io.*;
import java.util.zip.*;
//import java.util.*;

public class SmartUpload extends WOComponent {

	public String targetFilePath;
//	public String saveAction;
//	public WOComponent returnPage;
	public UploadAnchor anchor;
//	public String attributeToSet;
	public NSDictionary _prefs = NSDictionary.EmptyDictionary;
	
	public String mimeType;
	public String fileName;
    public InputStream inputStream;
    public String message = "Пожалуйста, укажите файл";
//    public String resultPath;
//		public NSMutableDictionary results = new NSMutableDictionary();
		
		protected File _targetFile;
//		protected File _targetDir;
		protected String _webRoot;
		protected FileOutputStream outputStream;
		protected int _batchSize = 0;
		protected long _maxFileSize = 0;
		protected ZipInputStream zipIput = null;
		
		public int fileSize = 0;
		public long completed = 0;
		public boolean done = false;

    public SmartUpload(WOContext context) {
        super(context);
    }

		protected boolean booleanPref(String key) {
			String val = (String)_prefs.objectForKey(key);
			Boolean bool =(val==null) ? Boolean.FALSE : Boolean.valueOf(val);
			return bool.booleanValue();
		}

		protected int intPref(String key) {
			String val = (String)_prefs.objectForKey(key);
			return (val==null) ? 0 :Integer.parseInt(val);
		}
		
		public boolean synchronizesVariablesWithBindings() {
        return false;
		}

    public WOComponent performUpload() {
			boolean allow = (mimeType != null);
			if(!allow) {
				message = "Не удается выяснить тип файла";
        return null;
			}
			allow = booleanPref("anyFileType");
			if(!allow)
				allow = mimeType.toUpperCase().equals("TEXT/HTML");
			if(!allow) {
				allow = mimeType.toUpperCase().endsWith("ZIP");
				if(allow)
					zipIput = new ZipInputStream(inputStream);
			}
			if(!allow) {
				message = "Не разрешенный тип файла";
        return null;
			}
			try {
/*				fileSize = inputStream.available();
				if(fileSize > 0) {
					int maxFileSize = intPref("maxFileSize");
					if(maxFileSize > 0 && fileSize > maxFileSize) {
						message = "Размер файла превышает допустимый";
						return null;
					}
				} else {
					message = inputStream.getClass().toString();
					return null;
				} //fileSize
				
				_targetFile = new File((String)_prefs.objectForKey("uploadFolder"),targetFilePath + "." + NSPathUtilities.pathExtension(fileName));
				boolean exists = _targetFile.createNewFile();
				if (!exists && !booleanPref("overwrite")) */
				
				_targetFile = createLocalFile(NSPathUtilities.lastPathComponent(fileName));
				//outputStream = new FileOutputStream(_targetFile);
				
 			} catch (Exception cex) {
				message = "Не удается закачать файл на сервер";
//				inputStream.close();
//				outputStream.close();
				return null;
			}
			
			_batchSize = intPref("batchBytes");
			_maxFileSize = (long)intPref("maxFileSize");
        return nextPortion();
    }
		
		protected File createLocalFile(String theName) throws Exception{
			_webRoot = (String)_prefs.objectForKey("webRoot");
			if (_webRoot == null)
				_webRoot = "/Library/WebServer/Documents/";
			
			String uploadFolderPath = (String)_prefs.objectForKey("uploadFolder");
			if(!(uploadFolderPath.charAt(0) == File.separatorChar))
				uploadFolderPath = NSPathUtilities.stringByAppendingPathComponent(_webRoot,uploadFolderPath);
			
			File destDir;
			String prefix = "";
			String suffix = "_" + theName;
			
			if(targetFilePath.charAt(targetFilePath.length() - 1)==File.separatorChar) {
				destDir = new File(uploadFolderPath,targetFilePath);
			 }
			else {
				destDir = new File(uploadFolderPath,NSPathUtilities.stringByDeletingLastPathComponent(targetFilePath));
				prefix = NSPathUtilities.stringByDeletingPathExtension(NSPathUtilities.lastPathComponent(targetFilePath));
			 }
			
			int numIndex = prefix.indexOf('#');
			if(numIndex > 0) {
//					prefix = prefix.replaceFirst("#",Integer.toString(nextFileNum(destDir,prefix.substring(0,numIndex))));
				prefix = prefix.substring(0,numIndex - 1);
				suffix = prefix.substring(numIndex) + suffix;
			}
			
			if(booleanPref("uniqPrefix")) {
				final String prfx = prefix;
				File [] oldFiles = destDir.listFiles(new FilenameFilter () {
					public boolean accept(File dir, String name) {
						return name.startsWith(prfx);
					}
				});
				if (oldFiles != null && oldFiles.length > 0) {
					for(int of = 0;of < oldFiles.length;of++) {
						delete(oldFiles[of]);
					}
				}
			}
				
			File result = new File(destDir,prefix + suffix);
			boolean createdNew = (zipIput == null)?result.createNewFile():result.mkdir();
			
			if (!createdNew) {
				if(!booleanPref("overwrite")) {
					result = File.createTempFile(prefix + "_",suffix,destDir);
					if(zipIput != null) {
						result.delete();
						result.mkdir();
					}
				} else {
					
				}
			}
				
//				prefix = prefix + Integer.toString(nextFileNum(destDir,prefix)) + "_";
//				result = new File(destDir,prefix + suffix);
//				createdNew = (zipIput == null)?result.createNewFile():result.mkdir();
				
			return result;
		}
		
		protected boolean delete(File theFile) {
			if(theFile.isDirectory()) {
				File[] inside = theFile.listFiles();
				for(int i = 0;i < inside.length;i++) {
					delete(inside[i]);
				}
			}
			return theFile.delete();
		}
		/*
		public static int nextFileNum(File folder,String prefx) {
			String [] oldFiles = folder.list(new FilenameFilter () {
							public boolean accept(File dir, String aName) {
								return aName.startsWith(prefx);
							}
						});
			int length = aName.length();
			int result = 0;
			int recent = 0;
			Enumeration enum = (new NSSet(oldFiles)).objectEnumerator();
			
			while(enum.hasMoreElements()) {
				fileName = (String)enum.nextElement();
				fileName = NSPathUtilities.stringByDeletingPathExtension(fileName);
				if(fileName.length() > length) {
					recent = Integer.parseInt(fileName.substring(length));
					if(recent > result)
						result = recent;
				}
			} //while
			return result + 1;
		}
*/
		protected long batchLoad (InputStream inStr) throws Exception{
			long startTime = System.currentTimeMillis();
			int bytesRead = 0;
			int maxTime = intPref("batchMilliseconds");
//			int maxFileSize = intPref("maxFileSize");
			long complete = 0;

			do {
				byte [] buff = new byte[_batchSize];
				bytesRead = inStr.read(buff);
				
				if (bytesRead > 0) {
					outputStream.write(buff,0,bytesRead);
				} else {
					if(bytesRead < 0) {
//						fin = true;
						return complete;
					}
				}
				
				complete = complete + bytesRead;
				if(_maxFileSize > 0 && complete + this.completed > _maxFileSize) {
//					overflow = true;
					
					return complete;
				}
			} while(maxTime <= 0 || ((int)(System.currentTimeMillis() - startTime) < maxTime));
			
			return -complete;
		}
		
		
    public WOComponent nextPortion() {
			long bytesRead = 0;
			
			try {
				if(zipIput == null) {
					outputStream = new FileOutputStream(_targetFile);
					bytesRead = batchLoad(inputStream);
					completed = bytesRead;
				} else {
					bytesRead = readZip();
				}
					
				done = (bytesRead <= 0);
				if(done) {
					inputStream.close();
					if(outputStream != null)
						outputStream.close();
				}
 			} catch (Exception cex) {
				message = "Ошибка при загрузке";
				completed = 0;
//				inputStream.close();
//				outputStream.close();
				return null;
			}
			if(bytesRead > 0) {
				if(bytesRead > _maxFileSize) 
					message = "Вы пытаетесь закачать файл больше допустимого размера!";
				else
					message = "Загрузка завершена";
				saveResult();
/*				String resultPath = _targetFile.getAbsolutePath();
				if(zipSelection != null) {
					resultPath = NSPathUtilities.stringByAppendingPathComponent(resultPath,zipSelection.getName());
//					message = message + "\n";
				}
				resultPath = resultPath.substring(_webRoot.length());
				resultPath = NSPathUtilities.stringByAppendingPathComponent("/",resultPath);
				
				returnPage.ensureAwakeInContext(context());
				returnPage.takeValueForKeyPath(resultPath,attributeToSet);
				returnPage.valueForKey(saveAction);*/
			} else {
	//			} else
					message = "Идет загрузка, загружено " + completed + " байт";
			}
			return null;
    } //nextPortion()
		
		public WOComponent saveResult() {
			String resultPath = _targetFile.getAbsolutePath();
			if(zipSelection != null && _targetFile.isDirectory()) {
				String entryName = zipSelection.getName();
				int firstSeparator = entryName.indexOf(File.separatorChar,2);
				if (firstSeparator > 0 && resultPath.endsWith(entryName.substring(0,firstSeparator))) {
					entryName = entryName.substring(firstSeparator);
				}
				resultPath = NSPathUtilities.stringByAppendingPathComponent(resultPath,entryName);
				//					message = message + "\n";
			}
			resultPath = resultPath.substring(_webRoot.length());
			resultPath = NSPathUtilities.stringByAppendingPathComponent("/",resultPath);
/*			
			returnPage.ensureAwakeInContext(context());
			returnPage.takeValueForKeyPath(resultPath,attributeToSet);
			returnPage.valueForKey(saveAction);
			return returnPage;
*/
			onLoad = null;
			return anchor.applyResult(resultPath);
		}
		
		public NSMutableArray zipContent;
	//	public ZipEntry zEn;
		public ZipEntry zipSelection;
		protected int _sDepth = Integer.MAX_VALUE;
		protected int _sExt = Integer.MAX_VALUE;
		protected boolean acceptEntry(ZipEntry entry) {
			if (entry.getName().startsWith("__MACOSX"))
				return false;
			if(!entry.isDirectory()) {
				NSArray allowedFileTypes = (NSArray)_prefs.objectForKey("allowedFileTypes");
				if(allowedFileTypes == null)
					return true;
				int extIndex = allowedFileTypes.indexOfObject(NSPathUtilities.pathExtension(entry.getName()).toLowerCase());
				if(extIndex == NSArray.NotFound) {
					boolean unzip = booleanPref("unzipAllFiles");
					if (unzip && zipSelection == null && !entry.getName().startsWith(".")) {
						zipSelection = entry;
						_sExt = Integer.MAX_VALUE;
						_sDepth = Integer.MAX_VALUE;
					}
					return unzip;
				 }
				int eDepth = entry.getName().lastIndexOf(File.separatorChar);	//.split(File.separator).length();
				if(zipSelection == null || (_sExt > 1 && extIndex < _sExt)) {
					zipSelection = entry;
					_sExt = extIndex;
					_sDepth = eDepth;	
				} else {	//zipSelection == null
					if(extIndex > 1 && extIndex > _sExt)
						return true;
					if(_sDepth < eDepth)
						return true;
					if(_sDepth == eDepth) {
						if(!NSPathUtilities.lastPathComponent(entry.getName()).toLowerCase().startsWith("index"))
							return true;
						//if(NSPathUtilities.lastPathComponent(entry.getName()).toLowerCase().startsWith("index"));
					}	// _sDepth == eDepth
					zipSelection = entry;
					_sExt = extIndex;
					_sDepth = eDepth;
				}	// ! zipSelection == null
			}	//!entry.isDirectory()
			return true;
		}	//acceptEntry(ZipEntry entry)
		
		protected ZipEntry _entry;
    public String onLoad;
		
		protected long readZip() throws Exception{
			zipContent = new NSMutableArray();
//			long maxFileSize = (long)intPref("maxFileSize");
//			long totalRead = completed;
			long bytesRead = 0;
			File output;
//			ZipEntry entry; //= (totalRead == 0)?:null;
			if(_entry == null)
				_entry = zipIput.getNextEntry();
btch:			do {
//			if(entry == null) return 0;
				if(acceptEntry(_entry)) {
					zipContent.addObject(_entry);
					output = new File(_targetFile,_entry.getName());
					if(_entry.isDirectory())
						output.mkdirs();
					else {
						outputStream = new FileOutputStream(output);
						bytesRead = batchLoad(zipIput);
						outputStream.close();
					 }
				}
				if(bytesRead < 0)
					return bytesRead - completed;
				zipIput.closeEntry();
				bytesRead = _entry.getCompressedSize();
				completed = completed + bytesRead;
				if(completed > _maxFileSize) {
						//zipIput.close();
						//return completed;
					break btch;
				}
				_entry = zipIput.getNextEntry();
			} while(_entry != null);
			zipIput.close();
			File [] ls = _targetFile.listFiles();
			if(ls.length == 1) {
				//String prefix = _targetFile.getName().substring(0,_targetFile.getName().indexOf('_') + 1);
				File newFile = createLocalFile(ls[0].getName()); //new File (_targetFile.getParentFile(), prefix + ls[0].getName());
				newFile.delete();
				if(ls[0].renameTo(newFile)) {
					_targetFile.delete();
					_targetFile = newFile;
				}
			}
			return completed;
		}

    public void finish() {
			//reset();
			onLoad = "finish();";
    }
	
		public void reset() {
			targetFilePath = null;
			//anchor = null;
			_prefs = NSDictionary.EmptyDictionary;
			mimeType = null;
			fileName = null;
			inputStream = null;
			message = "Пожалуйста, укажите файл";			
			_targetFile = null;
			_webRoot = null;
			outputStream = null;
			_batchSize = 0;
			_maxFileSize = 0;
			zipIput = null;
			fileSize = 0;
			completed = 0;
			done = false;
			zipContent = null;
			//	public ZipEntry zEn;
			zipSelection = null;
			_sDepth = Integer.MAX_VALUE;
			_sExt = Integer.MAX_VALUE;
			_entry = null;
			onLoad = null;
		}
}
