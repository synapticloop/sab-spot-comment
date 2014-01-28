package synapticloop.ssc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final int MAX_CLASSNAME_LENGTH = 24;
	private String className = null;

	public static Logger getLogger(Class clazz) {
		return(new Logger(clazz));
	}

	private Logger(Class clazz) {
		String canonicalName = clazz.getCanonicalName();
		int length = canonicalName.length();
		if(length < MAX_CLASSNAME_LENGTH) {
			this.className = clazz.getCanonicalName();
		} else {
			this.className = clazz.getCanonicalName().substring(length - MAX_CLASSNAME_LENGTH, length);
		}
	}

	private void log(String type, String message) {
		System.out.println(type + " [" + SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis())) +"] (..." + className + ") : " + message);
	}

	public void warn(String message) { log(" WARN", message); }
	public void info(String message) { log(" INFO", message); }
	public void error(String message) { log("ERROR", message); }
	public void fatal(String message) { log("FATAL", message); }
}