package net.rujel.reusables;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class ExportHTML extends Export {

	public ExportHTML(WOResponse aResponse,String fileName) {
		super(aResponse, fileName);
	}
	
	public ExportHTML(WOContext context,String fileName) {
		super(WOApplication.application().createResponseInContext(context), fileName);
	}
	
	public String title;
	
	public String fileExt() {
		return ".html";
	}
	
	public String contentType() {
		return "text/html";
	}
	
	protected void beginFile() {
		super.beginFile();
		response.appendContentString("<!DOCTYPE html PUBLIC  \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
		response.appendContentString(" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r");
		response.appendContentString("<html xmlns=\"http://www.w3.org/1999/xhtml\">\r<head>\r");
		response.appendContentString(
			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\r");
		response.appendContentString("<title>");
		if(title == null)
			response.appendContentString(filename);
		else 
			response.appendContentString(title);
		response.appendContentString("</title>\r</head>\r<body>");
		if(title != null) {
			response.appendContentString("\r<h1>");
			response.appendContentString(title);
			response.appendContentString("</h1>");
		}
		response.appendContentString("\r<table border = \"1\">");
	}
	
	protected void endFile() {
		super.endFile();
		response.appendContentString("</table>\r</body>\r</html>");
	}
	
	public void beginRow() {
		super.beginRow();
		response.appendContentString("<tr>\r");
	}

	public void endRow() {
		super.endRow();
		response.appendContentString("\r</tr>");
	}
	
	public void beginValue() {
		super.beginValue();
		response.appendContentString("<td>");
	}
	
	public void endValue() {
		super.endValue();
		response.appendContentString("</td>");
	}
}
