package net.rujel.reusables;

import java.util.logging.*;

public class BufferHandler extends Handler {

	protected StringBuffer buf;

	public BufferHandler() {
		super();
		buf = new StringBuffer();
	}

	public BufferHandler(String initMessage) {
		super();
		buf = new StringBuffer(initMessage);
	}

	public void append(String string) {
		buf.append(string);
	}
	
	public void publish(LogRecord record) {
		if(record.getLevel().intValue() >= getLevel().intValue()) {
			Formatter formatter = getFormatter();
			if(formatter == null) {
				formatter = new WOLogFormatter();
				setFormatter(formatter);
			}
			buf.append(formatter.format(record));
		}
	}
	public void close() throws SecurityException {
		buf = null;
	}
	public void flush() {
		buf = new StringBuffer();
	}

	public String toString() {
		return buf.toString();
	}
}
