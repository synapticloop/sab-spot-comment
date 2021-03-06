package synapticloop.ssc.bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import synapticloop.ssc.utils.SetupManager;

public class Download {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

	private String sabNzbId = null;
	private String url = null;
	private String failMessage = null;
	private long completedTime = 0l;
	private boolean committed = false;
	private String guid = null;
	private boolean external = false;
	private boolean ignored = false;
	private String name = null;

	public Download(String name, String url, String sabNzbId, String failMessage, long completedTime, String guid, boolean external) {
		this.name = name;
		this.url = url;
		this.sabNzbId = sabNzbId;
		this.failMessage = failMessage;
		this.completedTime = completedTime;
		this.guid = guid;
		this.external = external;
	}

	public String getSabNzbId() { return sabNzbId; }
	public String getFailMessage() { return failMessage; }
	public long getCompletedTime() { return completedTime; }
	public boolean getIsFailed() { return(null != failMessage && failMessage.length() > 0); }
	public boolean getCommitted() { return committed; }
	public void setCommitted(boolean committed) { this.committed = committed; }
	public String getGuid() { return guid; }
	public boolean getExternal() { return external; }
	public void setIgnored(boolean ignored) { this.ignored = ignored; }
	public boolean getIgnored() { return ignored; }
	public String getName() { return name; }
	public String getUrl() {return(url); }

	public String getComment() {
		SetupManager setupManager = SetupManager.INSTANCE;
		String format = null;
		if(getIsFailed()) {
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
		format = format.replaceAll("%DATE%", SIMPLE_DATE_FORMAT.format(new Date(completedTime)));
		format = format.replaceAll("%MESSAGE%", failMessage);
		format = format.replaceAll("%SERVERS%", servers.toString());

		return(format);
	}
}
