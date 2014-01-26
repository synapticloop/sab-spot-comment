package synapticloop.ssc.bean;

public class Download {
	private String sabNzbId = null;
	private String failMessage = null;
	private long completedTime = 0l;
	private boolean committed = false;
	private String guid = null;
	private boolean ignored = false;
	private String name = null;

	public Download(String name, String sabNzbId, String failMessage, long completedTime, String guid, boolean ignored) {
		this.name = name;
		this.sabNzbId = sabNzbId;
		this.failMessage = failMessage;
		this.completedTime = completedTime;
		this.guid = guid;
		this.ignored = ignored;
	}

	public String getSabNzbId() { return sabNzbId; }
	public String getFailMessage() { return failMessage; }
	public long getCompletedTime() { return completedTime; }
	public boolean getIsFailed() { return(null != failMessage && failMessage.length() > 0); }
	public boolean getCommitted() { return committed; }
	public String getGuid() { return guid; }
	public boolean getIgnored() { return ignored; }
	public String getName() { return name; }
}
