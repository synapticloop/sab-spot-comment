package synapticloop.ssc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import synapticloop.ssc.bean.Download;

public class NzbCache {
	private static final String SSC_F = "[SSC-F] ";
	private static final String SSC_T = "[SSC-T] ";
	private static final String NZB_CACHE_PROPERTIES = "nzbcache.properties";
	private static final Logger LOGGER = Logger.getLogger(NzbCache.class);
	public static NzbCache INSTANCE = new NzbCache();

	private ConcurrentHashMap<String, Long> downloadedNzbIds = new ConcurrentHashMap<String, Long>();
	private ArrayList<Download> downloads = new ArrayList<Download>();
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("E, d MM, yyyy hh:mm:ss Z");

	private NzbCache() {
		LOGGER.info("Cache initialising...");
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
				LOGGER.info("Added to cache " + key + "@" + timeDownloaded);
			}
		} catch (IOException ioex) {
			// couldn't find the file - ignore
			LOGGER.info("No cache properties found.");
		}
		LOGGER.info("Cache initialised...");
	}

	public void refreshCache() {
		LOGGER.info("Cache refreshing...");
		SetupManager setupManager = SetupManager.INSTANCE;
		if(setupManager.getIsSetup()) {
			String content = ConnectionHelper.getUrlContentsAsString(setupManager.getSabNzbUrl() + "/api/?apikey=" + setupManager.getSabNzbApiKey() + "&mode=history&start=0&limit=20&output=json");

			long maxCompletedTime = 0l;

			try {
				JSONObject jsonObject = new JSONObject(content);
				JSONObject history = jsonObject.getJSONObject("history");
				JSONArray slots = history.getJSONArray("slots");

				// run through all of the history (slots) is JSON parlance
				for(int i = 0; i < slots.length(); i++) {
					JSONObject slot = null;
					try {
						slot = slots.getJSONObject(i);

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
	
						if(!downloadedNzbIds.containsKey(sabNzbId)) {
							if(external) {
								LOGGER.info("Skipping external sabnzbid '" + sabNzbId + "'.");
							} else {
								LOGGER.info("Found new sabnzbid '" + sabNzbId + "' with guid:" + guid);
							}
							downloadedNzbIds.put(sabNzbId, completedTime * 1000);
							downloads.add(new Download(name, url, sabNzbId, failMessage, completedTime * 1000, guid, external));
						}
	
						if(completedTime > maxCompletedTime) {
							maxCompletedTime = completedTime;
						}
					} catch(JSONException jsonex) {
						LOGGER.fatal("Exception parsing JSON, message was: '" + jsonex.getMessage() + "' for slot: '" + slot + "'.");
					}
				}
			} catch(JSONException jsonex) {
				LOGGER.fatal("Exception parsing JSON, message was: '" + jsonex.getMessage() + "'.");
			}

			// time to truncate the arraylist
			for(int i = 20; i < downloads.size(); i++) {
				downloads.remove(i);
			}

			// now is the hour to go and grab the comments

			for (Download download : downloads) {
				if(!download.getExternal() && !download.getCommitted()) {
					String urlString = setupManager.getNewznabUrl() + "/api?apikey=" + setupManager.getNewznabApiKey() + "&t=comments&o=json&id=" + download.getGuid();

					try {
						String urlContentsAsString = ConnectionHelper.getUrlContentsAsString(urlString);
						JSONObject comments = new JSONObject(urlContentsAsString);

						JSONObject channelObject = comments.getJSONObject("channel");
						JSONArray itemsArray = channelObject.optJSONArray("item");
						if(shouldComment(setupManager, itemsArray, download)) {
							String sscMessage = SSC_T;
							if(download.getIsFailed()) {
								sscMessage = SSC_F;
							}
							String commentAddString = setupManager.getNewznabUrl() + "/api?apikey=" + setupManager.getNewznabApiKey() + "&t=commentadd&o=json&id=" + download.getGuid() + "&text=" + URLEncoder.encode(sscMessage + download.getComment(), "UTF-8");
							ConnectionHelper.getUrlContentsAsString(commentAddString);
							LOGGER.info("Committed message for " + download.getGuid() + " (" + download.getName() + ")");
						}
					} catch(JSONException jsonex) {
						LOGGER.fatal(jsonex.getMessage());
						jsonex.printStackTrace();
					} catch (UnsupportedEncodingException ueex) {
						LOGGER.fatal(ueex.getMessage());
					} finally {
						download.setCommitted(true);
					}
				}
			}

			LOGGER.info("Cache refreshed...");
			setupManager.setLastCompletedTime(maxCompletedTime * 1000);
			saveProperties();
		}
	}
	private boolean shouldComment(SetupManager setupManager, JSONArray itemsArray, Download download) {
		if(setupManager.getIsDemo()) {
			LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - [WE ARE IN DEMO MODE].");
			return(false);
		}

		String sscMessage = SSC_T;
		if(download.getIsFailed()) {
			sscMessage = SSC_F;
		}

		if(null == itemsArray) {
			// if we have no comments - we want to add one
			LOGGER.info("Commenting on " + download.getGuid() + " (" + download.getName() +") - [FOUND 0 COMMENTS].");
			return(true);
		} 

		// go through and figure out whether we have SSC comments on the thread
		int numSuccessComments = 0;
		int numFailedComments = 0;
		long maxTime = 0;

		for(int i = 0; i < itemsArray.length(); i++) {
			JSONObject item = itemsArray.getJSONObject(i);
			String comment = item.optString("description");
			// are there any ssc failed/success comments?
			if(null != comment) {
				// strip out all whitespace characters which can wreak havoc depending 
				// on encoding of newlines etc.
				if(comment.replaceAll("\\s+","").compareTo((sscMessage + download.getComment()).replaceAll("\\s+","")) == 0) {
					// we have an identical message - we don't want to re-comment
					LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - [IDENTICAL COMMENT FOUND].");
					return(false);
				}

				if(comment.startsWith(SSC_F)) {
					numFailedComments++;
				} else if(comment.startsWith(SSC_T)) {
					numSuccessComments++;
				}
			}

			// what about the pubdate?
			String pubDate = item.optString("pubDate");
			if(null != pubDate) {
				try {
					long time = SIMPLE_DATE_FORMAT.parse(pubDate).getTime();
					if(time > maxTime) {
						maxTime = time;
					}
				} catch (ParseException pex) {
					// do nothing
				}
			}
		}

		// if there hasn't been a comment in the last 'setupManager.getNumLastCommentDays()' 
		// number of days - then we will post it, irrespective of anything else

		if(maxTime != 0 && (maxTime + (setupManager.getNumLastCommentDays() * 24 * 60 * 60 * 1000) < System.currentTimeMillis())) {
			LOGGER.info("Commenting on " + download.getGuid() + " (" + download.getName() +") - as the last comment was more than " + setupManager.getNumLastCommentDays() + " days ago.");
			return(true);
		}

		// if there are too many comments - skip it
		if(itemsArray.length() > setupManager.getNumMaxComments()) {
			LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - more than " + setupManager.getNumMaxComments() + " comments found.");
			return(false);
		}

		// how may SSC failed comments do we have?
		if(download.getIsFailed() && numFailedComments >= setupManager.getNumFailureComments()) {
			LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - already have enough failed comments.");
			return(false);
		}

		// how may SSC success comments do we have?
		if(!download.getIsFailed() && numSuccessComments >= setupManager.getNumSuccessComments()) {
			LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - already have enough success comments.");
			return(false);
		}

		// how many comments do we have
		if(itemsArray.length() >= setupManager.getNumMaxComments()) {
			// too many comments on this thread - ignore
			LOGGER.info("Not commenting on " + download.getGuid() + " (" + download.getName() +") - too many comments in this thread.");
			return(false);
		}

		LOGGER.info("Commenting on " + download.getGuid() + " (" + download.getName() +") - falling through.");
		// at this point 
		return(true);
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
