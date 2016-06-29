//  WOLogFormatter.java

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
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import java.util.logging.*;
import java.text.*;
import java.util.Date;
import java.util.Enumeration;

public class WOLogFormatter extends Formatter {
	public static final String SESSION = "session";
	public static final String EO = "eo";
	protected static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd\tHH:mm:ss\t");
	

	public static String formatTrowable(Throwable t) {
		StringBuffer result = new StringBuffer();
		formatTrowable(t,result);
		return result.toString();
	}
	public static String formatTrowableHTML(Throwable t) {
		StringBuffer result = new StringBuffer();
		formatTrowable(t,result,null,true);
		return result.toString();
	}
	
	public static StringBuffer formatTrowable(Throwable t,StringBuffer toAppend) {
		return formatTrowable(t,toAppend,null,false);
	}

	protected static boolean myClass(String className) {
		if(className.indexOf('.') < 0) return true;
		if(className.startsWith("net.rujel.")) return true;
		if(className.startsWith("java")) return false;
		if(className.startsWith("com.webobjects.")) return false;
		if(className.startsWith("com.sun.")) return false;
		if(className.startsWith("sun.")) return false;
		if(className.startsWith("org.omg.")) return false;
		if(className.startsWith("org.w3c.")) return false;
		if(className.startsWith("org.xml.")) return false;
		return true;
	}
	
	protected static StringBuffer formatTrowable(Throwable t,StringBuffer toAppend,StackTraceElement lastElem,boolean html) {
		if(html) toAppend.append("<b>");
		toAppend.append(t.getClass().getName());
		if(html) toAppend.append("</b>");
		toAppend.append(':').append(' ');
		if(html)
			toAppend.append(WOMessage.stringByEscapingHTMLString(t.getMessage()));
		else
			toAppend.append(t.getMessage());
		StackTraceElement[] trace = t.getStackTrace();
		boolean fin = false;
		for (int i = 0; i < trace.length; i++) {
			fin = (lastElem != null && trace[i].equals(lastElem));
			if(i==0 || fin || myClass(trace[i].getClassName())) {
				if(html) toAppend.append("<br/>");
				toAppend.append('\n').append('\t');
				//if(html) toAppend.append("<span style=\"width:3em;\">");
				toAppend.append('[').append(i).append(']');
				//if(html) toAppend.append("</span> ");
				//else 
				toAppend.append('\t');
				toAppend.append(trace[i].getClassName()).append('.');
				if(html) toAppend.append("<i>");
				toAppend.append(trace[i].getMethodName());
				if(html) toAppend.append("</i>");
				toAppend.append(':').append(trace[i].getLineNumber());
			}
			if(fin)
				break;
		}
		if(t.getCause() != null && t.getCause() != t) {
			if(html) toAppend.append("<br/>");
			toAppend.append('\n').append('\t');
			formatTrowable(t.getCause(),toAppend,trace[0],html);
		} else if(t instanceof NSForwardException) {
			if(html) toAppend.append("<br/>");
			toAppend.append('\n').append('\t');
			formatTrowable(((NSForwardException)t).originalException(),toAppend,trace[0],html);
		} else if(t instanceof NSValidation.ValidationException) {
			NSValidation.ValidationException vex = (NSValidation.ValidationException)t;
			if(html) toAppend.append("<br/>");
			toAppend.append('\n').append('\t');
			String key = vex.key();
			Object obj = vex.object();
			if(key != null) {
				if(obj == null)
					toAppend.append("key: ");
				toAppend.append(key);
			}
			if(obj != null) {
				try {
					Object value = NSKeyValueCoding.Utility.valueForKey(obj, key);
					toAppend.append(" =\t");
					formatObject(value, toAppend);
				} finally {
					toAppend.append("\tin ");
					formatObject(obj, toAppend);
				}
			}
		}
		return toAppend;
	}

	public static String formatEO(EOEnterpriseObject eo) {
		StringBuffer result = new StringBuffer();
		formatEO(eo,result);
		return result.toString();
	}
	
	public static StringBuffer formatEO(EOEnterpriseObject eo,StringBuffer toAppend) {
		toAppend.append(eo.entityName()).append(':');
		EOEditingContext ec = eo.editingContext();
		if(ec == null) {
			toAppend.append("null");
		} else {
			EOGlobalID gid = ec.globalIDForObject(eo);
			if(gid == null) {
				toAppend.append("null");
			} else if(gid.isTemporary()) {
				toAppend.append("new").append(gid.hashCode());
			} else {
				Object[] keys = ((EOKeyGlobalID)gid).keyValues();
				if(keys.length == 1) {
					toAppend.append(keys[0]);
				} else {
					NSDictionary pkey = EOUtilities.primaryKeyForObject(ec,eo);
					if(pkey == null) {
						toAppend.append("null");
					} else {
						toAppend.append(pkey);
					}
				}
			}
		}
		return toAppend;
	}
	
	public static StringBuffer formatEOGID(EOGlobalID gid,StringBuffer toAppend) {
		if (gid instanceof EOKeyGlobalID) {
			EOKeyGlobalID kGid = (EOKeyGlobalID) gid;
			toAppend.append(kGid.entityName()).append(':');
			Object[] keys = kGid.keyValues();
			for (int i = 0; i < keys.length; i++) {
				if(i > 0)
					toAppend.append('-');
				toAppend.append(keys[i]);
			}
		} else {
			toAppend.append("new");
		}
		return toAppend;
	}
	
	protected static StringBuffer formatDictionary(NSDictionary dict, StringBuffer buf) {
		Enumeration den = dict.keyEnumerator();
		Object key = null;
		while (den.hasMoreElements()) {
			key = den.nextElement();
			buf.append(key).append(" =\t");
			formatObject(dict.objectForKey(key),buf).append('\n').append('\t');
		}
		return buf;
	}
	
	protected static StringBuffer formatArray(NSArray array, StringBuffer buf) {
		Enumeration aen = array.objectEnumerator();
		Object obj = null;
		buf.append('{');
		while (aen.hasMoreElements()) {
			obj = aen.nextElement();
			formatObject(obj,buf);
			if(aen.hasMoreElements())
				buf.append(" ; ");
		}
		buf.append('}');
		return buf;
	}
	
	protected static StringBuffer formatObject(Object obj, StringBuffer buf) {
		if(obj instanceof EOEnterpriseObject) {
			formatEO((EOEnterpriseObject)obj,buf);
		} else if(obj instanceof NSDictionary) {
			formatDictionary((NSDictionary)obj,buf);
		} else if(obj instanceof NSArray) {
			formatArray((NSArray)obj,buf);
		} else if(obj instanceof WOSession) {
			buf.append(obj.getClass().getName()).append(':').append(((WOSession)obj).sessionID());
		} else {
			buf.append(obj);
		}
		return buf;
	}
	
	public String format(LogRecord record) {
		WOSession ses = null;
		EOEnterpriseObject eo = null;
		EOGlobalID gid = null;
		Throwable t = record.getThrown();
		NSMutableArray otherObjects = new NSMutableArray();
		
		Object[] param = record.getParameters();
		if(param != null && param.length > 0) {
			for (int i = 0; i < param.length; i++) {
				if(param[i] instanceof WOSession) {
					ses = (WOSession)param[i];
				} else if (param[i] instanceof EOEnterpriseObject && eo == null) {
					eo = (EOEnterpriseObject)param[i];
					if(ses == null) {
						ses = SessionedEditingContext.sessionForObject(eo);
					}
				} else if(param[i] instanceof EOGlobalID && gid == null) {
					gid = (EOGlobalID)param[i];
				} else if(param[i] instanceof Throwable && t == null) {
					t = (Throwable)param[i];
				} else if(param[i] instanceof NSDictionary) {
					NSMutableDictionary tmp = ((NSDictionary)param[i]).mutableClone();
					if(ses == null) ses = (WOSession)tmp.objectForKey(SESSION);
					if(eo == null) eo = (EOEnterpriseObject)tmp.objectForKey(EO);
					tmp.removeObjectForKey(SESSION);
					tmp.removeObjectForKey(EO);
					if(tmp.count() > 0) {
						otherObjects.addObject(tmp);
					}
				} else if(param[i] == null) {
					otherObjects.addObject(NSKeyValueCoding.NullValue);
				} else {
					otherObjects.addObject(param[i]);
				}
			}
		}
		
		StringBuffer result = new StringBuffer();
		Date dt = new Date(record.getMillis());
		FieldPosition fp = new FieldPosition(DateFormat.MILLISECOND_FIELD);
		df.format(dt,result,fp);
		result.append(record.getLevel().getName()).append('\t');
		if(ses != null) {
			result.append(ses.sessionID());
		}
		
		result.append('\t').append(formatMessage(record));
		
		if(eo != null) {
			result.append('\t').append('<');
			formatEO(eo,result).append('>');
		}
		if(gid != null) {
			if(eo == null) {
				result.append('\t').append('<');
				formatEOGID(gid,result).append('>');
			} else {
				otherObjects.addObject(gid);
			}
		}
		if(t != null) {
			result.append('\n').append('\t');
			formatTrowable(t,result);
		}
		if(otherObjects.count() > 0) {
			Enumeration en = otherObjects.objectEnumerator();
			while(en.hasMoreElements()) {
				result.append('\n').append('\t');
				formatObject(en.nextElement(),result);
			}
		}
		return result.append("\n").toString();
	}
}
