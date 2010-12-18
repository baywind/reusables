package net.rujel.reusables;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.webobjects.appserver.*;
import com.webobjects.foundation.NSData;

// Generated by the WOLips Templateengine Plug-in at May 13, 2009 4:09:09 PM
public abstract class Export implements WOActionResults {
	protected WOResponse response;
	protected String filename;
	protected boolean rowOpen = false;
	protected boolean valueOpen = false;
	protected boolean firstValue = true;
    
	public Export(WOResponse aResponse,String fileName) {
		response = aResponse;
		filename = fileName;
		beginFile();
    }
	
	public String contentType() {
		return "application/octet-stream";
	}
    
	public WOResponse response() {
		return response;
	}

	public static String makeFilename(String format, String fileExt) {
    	StringBuffer buf = new StringBuffer();
    	try {
    		SimpleDateFormat sdf = new SimpleDateFormat(
    				(format == null) ? "yyyyMMdd" : format);
    		sdf.format(new Date(), buf, new FieldPosition(SimpleDateFormat.YEAR_FIELD));
    	} catch (Exception e) {
    		buf.append(format);
    	}
    	if(format.indexOf('.') < 0)
    		buf.append('.').append(fileExt);
    	return buf.toString();
	}

	public WOResponse generateResponse() {
		endFile();
    	response.setHeader(contentType(),"Content-Type");
    	if(filename != null) {
    		StringBuilder buf = new StringBuilder("attachment; filename=\"");
    		buf.append(filename).append('"');
    		response.setHeader(buf.toString(),"Content-Disposition");
    	}
		return response;
	}
		
	protected void beginFile() {
		NSData content = response.content();
		if(content == null || content.length() == 0)
			return;
		response.setContent(NSData.EmptyData);
		rowOpen = false;
		valueOpen = false;
		firstValue = true;
	}
	
	protected void endFile() {
		if(valueOpen)
			endValue();
		if(rowOpen)
			endRow();
	}
	
	public void beginRow() {
		if(rowOpen)
			endRow();
		rowOpen = true;
		firstValue = true;
	}
	
	public void endRow() {
		if(valueOpen)
			endValue();
		rowOpen = false;
	}
	
	public void beginValue() {
		if(!rowOpen)
			beginRow();
		if(valueOpen)
			endValue();
		valueOpen = true;
	}
	
	public void endValue() {
		if(!valueOpen)
			return;
		valueOpen = false;
		firstValue = false;
	}
	
	protected void appendValue(Object value) {
		if(value == null)
			return;
		String vs = value.toString();
		if(vs == null)
			return;
		response.appendContentString(vs);
	}
	
	public void addValue(Object value) {
		beginValue();
		appendValue(value);
		endValue();
	}
}