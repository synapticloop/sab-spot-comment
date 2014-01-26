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
	private static final String SETUP_PROPERTIES = "setup.properties";
	private static Properties properties = new Properties();
	private static long lastCompletedTime = 0l;
	
	// sabnzbd stuff
	private static boolean isSabNzbSetup = false;
	private static String sabNzbUrl = "";
	private static String sabNzbApiKey = "";
	private static String sabNzbErrorMessage = null;

	private static ArrayList<String> newznabServers = new ArrayList<String>();
	private static HashSet<String> serverCache = new HashSet<String>();

	// newznab stuff
	private static boolean isNewznabSetup = false;
	private static String newznabUrl = "";
	private static String newznabApiKey = "";
	private static String newznabErrorMessage = null;

	static {
		// load up the properties if they exist
		try {
			properties.load(new FileInputStream(new File(SETUP_PROPERTIES)));
			sabNzbUrl = properties.getProperty("sabNzbUrl");
			sabNzbApiKey = properties.getProperty("sabNzbApiKey");
			sabNzbErrorMessage = properties.getProperty("sabNzbErrorMessage");
			newznabUrl = properties.getProperty("newznabUrl");
			newznabApiKey = properties.getProperty("newznabApiKey");
			newznabErrorMessage = properties.getProperty("newznabErrorMessage");
			String lastCompletedTimeString = properties.getProperty("lastCompletedTime");
			try {
				lastCompletedTime = Long.parseLong(lastCompletedTimeString);
			} catch(NumberFormatException nfex) {
				// do nothing
			}
			validate();
		} catch (IOException ioex) {
			// couldn't find the file - ignore
		}
	}
	
	public static void validate() {
		serverCache.clear();
		newznabServers.clear();

		if(null != sabNzbUrl && null != sabNzbApiKey) {
			// try and validate that we can connect with the values
			String content = ConnectionHelper.getUrl(sabNzbUrl + "/api/?apikey=" + sabNzbApiKey + "&mode=get_config&output=json");
			// TODO remove
			System.out.println(content);

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
				sabNzbErrorMessage = "Expected JSON, got '" + content + "'";
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
					newznabErrorMessage = "Expected JSON config, got '" + content + "'";
					isNewznabSetup = false;
				} else {
					newznabErrorMessage = null;
					isNewznabSetup = true;
				}
			} catch(JSONException josnex) {
				// we should be receiving back json
				newznabErrorMessage = "Expected JSON, got '" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "'";
				isNewznabSetup = false;
			}
		}
		saveProperties();
	}

	public static void saveProperties() {
		try {
			properties.clear();
			if(null != sabNzbUrl) { properties.put("sabNzbUrl", sabNzbUrl); }
			if(null != sabNzbApiKey) { properties.put("sabNzbApiKey", sabNzbApiKey); }
			if(null != sabNzbErrorMessage) { properties.put("sabNzbErrorMessage", sabNzbErrorMessage); }
			if(null != newznabUrl) { properties.put("newznabUrl", newznabUrl); }
			if(null != newznabApiKey) { properties.put("newznabApiKey", newznabApiKey); }
			if(null != newznabErrorMessage) { properties.put("newznabErrorMessage", newznabErrorMessage); }
			properties.put("lastCompletedTime", lastCompletedTime + "");
			properties.store(new FileOutputStream(new File(SETUP_PROPERTIES)), " --- sab spot comment properties file ---");
		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

	public static boolean getIsSetup() { return(isSabNzbSetup && isNewznabSetup); }

	// SABNZBD setup stuff
	public static boolean getIsSabNzbSetup() { return(isSabNzbSetup); }

	public static String getSabNzbUrl() { return cleanNull(sabNzbUrl); }
	public static void setSabNzbUrl(String sabNzbUrl) { SetupManager.sabNzbUrl = cleanNull(sabNzbUrl); }
	public static String getSabNzbApiKey() { return cleanNull(sabNzbApiKey); }
	public static void setSabNzbApiKey(String sabNzbApiKey) { SetupManager.sabNzbApiKey = cleanNull(sabNzbApiKey); }
	public static String getSabNzbErrorMessage() { return cleanNull(sabNzbErrorMessage); }

	// NEWZNAB setup stuff
	public static boolean getIsNewznabSetup() { return(isNewznabSetup); }

	public static String getNewznabUrl() { return cleanNull(newznabUrl); }
	public static void setNewznabUrl(String newznabUrl) { SetupManager.newznabUrl = cleanNull(newznabUrl); }
	public static String getNewznabApiKey() { return cleanNull(newznabApiKey); }
	public static void setNewznabApiKey(String newznabApiKey) { SetupManager.newznabApiKey = cleanNull(newznabApiKey); }
	public static String getNewznabErrorMessage() { return cleanNull(newznabErrorMessage); }

	public static long getLastCompletedTime() { return lastCompletedTime; }
	public static void setLastCompletedTime(long lastCompletedTime) { SetupManager.lastCompletedTime = lastCompletedTime; }

	public static ArrayList<String> getNewznabServers() { return(newznabServers); }

	private static String cleanNull(String value) {
		if(null == value) {
			return("");
		} else {
			return(value);
		}
	}

}
