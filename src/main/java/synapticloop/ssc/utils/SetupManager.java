package synapticloop.ssc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SetupManager {
	public final static SetupManager INSTANCE = new SetupManager();

	private static final String SETUP_PROPERTIES = "sabspotcomment.properties";

	private Properties properties = new Properties();
	private long lastCompletedTime = 0l;
	
	private boolean isDemo = true;

	// sabnzbd stuff
	private boolean isSabNzbSetup = false;
	private String sabNzbUrl = "";
	private String sabNzbApiKey = "";
	private String sabNzbErrorMessage = null;

	private ArrayList<String> newznabServers = new ArrayList<String>();
	private HashSet<String> serverCache = new HashSet<String>();

	// newznab stuff
	private boolean isNewznabSetup = false;
	private String newznabUrl = "";
	private String newznabApiKey = "";
	private String newznabErrorMessage = null;

	// comment numbers/hours
	private int numSuccessHours = 4;
	private int numSuccessComments = 5;
	private int numFailureHours = 24;
	private int numFailureComments = 5;

	// comment format
	private String commentFormat = "[SSC] DOWNLOAD %RESULT% at %DATE%.\n%FAILED(Message was ')%%FAILED_MESSAGE%%FAILED(')%\nServers used: %SERVERS%";

	private SetupManager() {
		// load up the properties if they exist
		try {
			properties.load(new FileInputStream(new File(SETUP_PROPERTIES)));
			sabNzbUrl = properties.getProperty("sabNzbUrl");
			sabNzbApiKey = properties.getProperty("sabNzbApiKey");
			sabNzbErrorMessage = properties.getProperty("sabNzbErrorMessage");
			newznabUrl = properties.getProperty("newznabUrl");
			newznabApiKey = properties.getProperty("newznabApiKey");
			newznabErrorMessage = properties.getProperty("newznabErrorMessage");
			commentFormat = properties.getProperty("commentFormat");

			String lastCompletedTimeString = properties.getProperty("lastCompletedTime");
			try { lastCompletedTime = Long.parseLong(lastCompletedTimeString); } catch(NumberFormatException nfex) { }

			String numSuccessHoursString = properties.getProperty("numSuccessHours");
			try {	numSuccessHours = Integer.parseInt(numSuccessHoursString); } catch(NumberFormatException nfex) { }
			String numSuccessCommentsString = properties.getProperty("numSuccessComments");
			try {	numSuccessComments = Integer.parseInt(numSuccessCommentsString); } catch(NumberFormatException nfex) { }
			String numFailureHoursString = properties.getProperty("numFailureHours");
			try {	numFailureHours = Integer.parseInt(numFailureHoursString); } catch(NumberFormatException nfex) { }
			String numFailureCommentsString = properties.getProperty("numFailureComments");
			try {	numFailureComments = Integer.parseInt(numFailureCommentsString); } catch(NumberFormatException nfex) { }

			validate();
		} catch (IOException ioex) {
			// couldn't find the file - ignore
		}
	}
	
	public void validate() {
		serverCache.clear();
		newznabServers.clear();

		if(null != sabNzbUrl && null != sabNzbApiKey) {
			// try and validate that we can connect with the values
			String content = ConnectionHelper.getUrl(sabNzbUrl + "/api/?apikey=" + sabNzbApiKey + "&mode=get_config&output=json");

			try {
				JSONObject jsonObject = new JSONObject(content);
				// see if we get back the config
				JSONObject config = jsonObject.optJSONObject("config");
				if(null == config) {
					sabNzbErrorMessage = "Expected JSON config, got '" + content + "'";
					isSabNzbSetup = false;
				} else {
					// we have the config - try and get the servers from the config
					JSONArray servers = config.optJSONArray("servers");
					for (int i = 0; i < servers.length(); i++) {
						JSONObject serverObject = servers.getJSONObject(i);
						String host= serverObject.getString("host");
						if(!serverCache.contains(host)) {
							getNewznabServers().add(host);
							serverCache.add(host);
						}
					}
					sabNzbErrorMessage = null;
					isSabNzbSetup = true;
				}
			} catch(JSONException josnex) {
				// we should be receiving back json
				sabNzbErrorMessage = "FATAL: Expected JSON, got '" + content + "'";
				isSabNzbSetup = false;
			}
		}
		
		// now for the newznab
		if(null != newznabUrl && null != newznabApiKey) {
			// try and validate the newznab settings
			String content = ConnectionHelper.getUrl(newznabUrl + "/api/?t=search&o=json&apikey=" + newznabApiKey);
			try {
				JSONObject jsonObject = new JSONObject(content);
				// see if we get back the config
				JSONObject optJSONObject = jsonObject.optJSONObject("@attributes");
				if(null == optJSONObject) {
					newznabErrorMessage = "FATAL: Expected JSON config, got '" + content + "'";
					isNewznabSetup = false;
				} else {
					newznabErrorMessage = null;
					isNewznabSetup = true;
				}
			} catch(JSONException josnex) {
				// we should be receiving back json
				newznabErrorMessage = "FATAL: Expected JSON, got '" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "'";
				isNewznabSetup = false;
			}
		}
		saveProperties();
	}

	public void saveProperties() {
		try {
			properties.clear();
			if(null != sabNzbUrl) { properties.put("sabNzbUrl", sabNzbUrl); }
			if(null != sabNzbApiKey) { properties.put("sabNzbApiKey", sabNzbApiKey); }
			if(null != sabNzbErrorMessage) { properties.put("sabNzbErrorMessage", sabNzbErrorMessage); }
			if(null != newznabUrl) { properties.put("newznabUrl", newznabUrl); }
			if(null != newznabApiKey) { properties.put("newznabApiKey", newznabApiKey); }
			if(null != newznabErrorMessage) { properties.put("newznabErrorMessage", newznabErrorMessage); }
			properties.put("lastCompletedTime", lastCompletedTime + "");
			properties.put("numSuccessHours", numSuccessHours + "");
			properties.put("numSuccessComments", numSuccessComments + "");
			properties.put("numFailureHours", numFailureHours + "");
			properties.put("numFailureComments", numFailureComments + "");
			properties.put("commentFormat", commentFormat);

			properties.store(new FileOutputStream(new File(SETUP_PROPERTIES)), " --- sab spot comment properties file ---");
		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	public boolean getIsDemo() { return(isDemo); }
	public void setIsDemo(boolean isDemo) { this.isDemo = isDemo; }

	public boolean getIsSetup() { return(isSabNzbSetup && isNewznabSetup); }

	// SABNZBD setup stuff
	public boolean getIsSabNzbSetup() { return(isSabNzbSetup); }

	public String getSabNzbUrl() { return cleanNull(sabNzbUrl); }
	public void setSabNzbUrl(String sabNzbUrl) { this.sabNzbUrl = cleanNull(sabNzbUrl); }
	public String getSabNzbApiKey() { return cleanNull(sabNzbApiKey); }
	public void setSabNzbApiKey(String sabNzbApiKey) { this.sabNzbApiKey = cleanNull(sabNzbApiKey); }
	public String getSabNzbErrorMessage() { return cleanNull(sabNzbErrorMessage); }

	// NEWZNAB setup stuff
	public boolean getIsNewznabSetup() { return(isNewznabSetup); }

	public String getNewznabUrl() { return cleanNull(newznabUrl); }
	public void setNewznabUrl(String newznabUrl) { this.newznabUrl = cleanNull(newznabUrl); }
	public String getNewznabApiKey() { return cleanNull(newznabApiKey); }
	public void setNewznabApiKey(String newznabApiKey) { this.newznabApiKey = cleanNull(newznabApiKey); }
	public String getNewznabErrorMessage() { return cleanNull(newznabErrorMessage); }

	public long getLastCompletedTime() { return lastCompletedTime; }
	public void setLastCompletedTime(long lastCompletedTime) { this.lastCompletedTime = lastCompletedTime; }

	public void setNumSuccessHours(int numSuccessHours) { if(numSuccessHours >= 0) { this.numSuccessHours = numSuccessHours; } }
	public int getNumSuccessHours() { return(numSuccessHours); }
	public void setNumSuccessComments(int numSuccessComments) { if(numSuccessComments >= 0) { this.numSuccessComments = numSuccessComments; } }
	public int getNumSuccessComments() { return(numSuccessComments); }
	public void setNumFailureHours(int numFailureHours) { if(numFailureHours >= 0) { this.numFailureHours = numFailureHours; } }
	public int getNumFailureHours() { return(numFailureHours); }
	public void setNumFailureComments(int numFailureComments) { if(numFailureComments >= 0) { this.numFailureComments = numFailureComments; } }
	public int getNumFailureComments() { return(numFailureComments); }

	public ArrayList<String> getNewznabServers() { return(newznabServers); }
	public void setCommentFormat(String commentFormat) { this.commentFormat = commentFormat; }
	public String getCommentFormat() { return(commentFormat); }

	private String cleanNull(String value) {
		if(null == value) {
			return("");
		} else {
			return(value);
		}
	}

}
