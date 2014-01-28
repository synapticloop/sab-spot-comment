package synapticloop.ssc.timer;

import synapticloop.ssc.utils.SetupManager;
import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;
import synapticloop.ssc.utils.Logger;
import synapticloop.ssc.utils.NzbCache;

public class SabNzbTimer extends TimerTask {
	private Properties properties = null;
	private long lastLookupTime = 0;
	private static Logger logger = Logger.getLogger(SabNzbTimer.class);

	public SabNzbTimer() {
		logger.info("SabNzbTimer starting...");
	}

	public void run() {
		SetupManager setupManager = SetupManager.INSTANCE;
		if(setupManager.getIsSetup()) {
			logger.info("Cache refreshing...");
			NzbCache nzbCache = NzbCache.INSTANCE;
			nzbCache.refreshCache();
			logger.info("Cache refreshed...");
		} else {
			logger.warn("SetupManager not setup");
		}
		// now connect to sabnzbd
	}

	public boolean cancel() {
		logger.info("SabNzbTimer shutting down...");
		return super.cancel();
	}

}
