package net.rujel.reusables.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

public class TransormationErrorListener implements ErrorListener {
	protected static Logger logger = Logger.getLogger("rujel.xml");
	private Object info;
	
	public TransormationErrorListener(Object userinfo) {
		super();
		info = userinfo;
	}

	public void error(TransformerException e) throws TransformerException {
		logger.log(Level.WARNING,"XML transformation error",
				new Object[] {info,e.getLocationAsString(),e});
	}

	public void fatalError(TransformerException e)
			throws TransformerException {
		logger.log(Level.WARNING,"XML transformation fatal error",
				new Object[] {info,e.getLocationAsString(),e});
	}

	public void warning(TransformerException e) throws TransformerException {
		logger.log(Level.INFO,"XML transformation warning",
				new Object[] {info,e.getLocationAsString(),e});
	}

}
