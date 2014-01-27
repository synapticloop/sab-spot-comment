package synapticloop.ssc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import synapticloop.ssc.bean.Download;

public class NzbCache {
	public static NzbCache INSTANCE = new NzbCache();
	private static final String NZB_CACHE_PROPERTIES = "nzbcache.properties";

	private ConcurrentHashMap<String, Long> downloadedNzbIds = new ConcurrentHashMap<String, Long>();
	private long lastCompletedTime = 0l;
	private ArrayList<Download> downloads = new ArrayList<Download>();

	private NzbCache() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(NZB_CACHE_PROPERTIES)));
			Enumeration<Object> keys = properties.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				Long timeDownloaded = 0l;
				try {
					timeDownloaded = Long.parseLong(properties.getProperty(key));
				} catch (NumberFormatException nfex) {
					// do nothing
				}
				downloadedNzbIds.put(key, timeDownloaded);
			}
		} catch (IOException ioex) {
			// couldn't find the file - ignore
		}
	}

	public void refreshCache() {
		SetupManager setupManager = SetupManager.INSTANCE;
		String content = ConnectionHelper.getUrl(setupManager.getSabNzbUrl() + "/api/?apikey=" + setupManager.getSabNzbApiKey() + "&mode=history&start=0&limit=20&output=json");

		long maxCompletedTime = 0l;

		try {
			JSONObject jsonObject = new JSONObject(content);
			JSONObject history = jsonObject.getJSONObject("history");
			JSONArray slots = history.getJSONArray("slots");
			for(int i = 0; i < slots.length(); i++) {
				JSONObject slot = slots.getJSONObject(i);

				String name = slot.getString("name");
				String failMessage = slot.getString("fail_message");
				long completedTime = slot.getLong("completed");
				String sabNzbId = slot.getString("nzo_id");
				String url = slot.getString("url").toLowerCase();
				boolean external = true;
				String guid = null;

				if(url.startsWith(setupManager.getNewznabUrl().toLowerCase())) {
					external = false;
					// now grab the nzbid
					int indexOfGetNzb = url.indexOf("/getnzb/") + "/getnzb/".length();
					int indexOfDotNzb = url.indexOf(".nzb");
					guid = url.substring(indexOfGetNzb, indexOfDotNzb);
				}

				if(!downloadedNzbIds.contains(sabNzbId) && completedTime > lastCompletedTime) {
					downloadedNzbIds.put(sabNzbId, completedTime * 1000);
					downloads.add(new Download(name, sabNzbId, failMessage, completedTime * 1000, guid, external));
				}

				if(completedTime > maxCompletedTime) {
					maxCompletedTime = completedTime;
				}
			}
		} catch(JSONException jsonex) {
			// TODO - do something
		}

		// time to truncate the arraylist
		for(int i = 20; i < downloads.size(); i++) {
			downloads.remove(i);
		}

		setupManager.setLastCompletedTime(maxCompletedTime * 1000);
		saveProperties();
	}

	private void saveProperties() {
		Properties properties = new Properties();
		Enumeration<String> keys = downloadedNzbIds.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			properties.put(key, downloadedNzbIds.get(key).longValue() + "");
		}

		try {
			properties.store(new FileOutputStream(new File(NZB_CACHE_PROPERTIES)), " --- sab spot comment nzb cache file ---");
		} catch (FileNotFoundException fnfex) {
			fnfex.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}		
	}
	public ArrayList<Download> getDownloads() { return(downloads); }
}
