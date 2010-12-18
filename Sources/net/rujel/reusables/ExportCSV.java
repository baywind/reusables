package net.rujel.reusables;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSNumberFormatter;

public class ExportCSV extends Export {

	protected NSNumberFormatter numberFormat;
	
	public ExportCSV(WOResponse aResponse,String fileName) {
		super(aResponse, makeFilename(fileName, "csv"));
	}
	
	public ExportCSV(WOContext context,String fileName) {
		super(WOApplication.application().createResponseInContext(context), 
				makeFilename(fileName, "csv"));
	}
		
	public String fileExt() {
		return ".csv";
	}
	
	public void beginValue() {
		super.beginValue();
		if(!firstValue)
			response.appendContentCharacter(',');
		response.appendContentCharacter('"');
	}
	public void endValue() {
		super.endValue();
		response.appendContentCharacter('"');
	}
	
	public void endRow() {
		super.endRow();
		response.appendContentCharacter('\r');
	}
	
	protected void appendValue(Object value) {
		if(value == null)
			return;
		if(value instanceof Number) {
			if(numberFormat == null) {
				numberFormat = new NSNumberFormatter();
				numberFormat.setDecimalSeparator(SettingsReader.stringForKeyPath(
						"ui.decimalSeparator", ","));
				numberFormat.setThousandSeparator(SettingsReader.stringForKeyPath(
						"ui.thousandSeparator", " "));
			}
			response.appendContentString(numberFormat.format(value));
		} else {
			String vs = value.toString();
			if(vs == null)
				return;
			vs = vs.replaceAll("\"", "\"\"");
			response.appendContentString(vs);
		}
	}
}
