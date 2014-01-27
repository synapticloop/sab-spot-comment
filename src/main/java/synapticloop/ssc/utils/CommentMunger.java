package synapticloop.ssc.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import synapticloop.ssc.bean.Download;

public class CommentMunger {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	public static String mungeComment(Download download) {
		SetupManager setupManager = SetupManager.INSTANCE;
		String format = null;
		if(download.getIsFailed()) {
			format = setupManager.getFailedCommentFormat();
		} else {
			format = setupManager.getSuccessCommentFormat();
		}

		// now that we have the correct format
		StringBuilder servers = new StringBuilder();

		for (Iterator<String> iterator = setupManager.getNewznabServers().iterator(); iterator.hasNext(); ) {
			String server = (String) iterator.next();
			servers.append(server);
			if(iterator.hasNext()) {
				servers.append(", ");
			}
		}
		format = format.replaceAll("%DATE%", SIMPLE_DATE_FORMAT.format(new Date(download.getCompletedTime())));
		format = format.replaceAll("%MESSAGE%", download.getFailMessage());
		format = format.replaceAll("%SERVERS%", servers.toString());
		
		return(format);
	}
}
