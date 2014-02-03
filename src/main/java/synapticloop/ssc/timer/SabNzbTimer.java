package synapticloop.ssc.timer;

import java.util.TimerTask;

import synapticloop.ssc.utils.Logger;
import synapticloop.ssc.utils.NzbCache;
import synapticloop.ssc.utils.SetupManager;

public class SabNzbTimer extends TimerTask {
	private static Logger logger = Logger.getLogger(SabNzbTimer.class);

	public SabNzbTimer() {
		logger.info("SabNzbTimer starting...");
	}

	public void run() {
		SetupManager setupManager = SetupManager.INSTANCE;
		if(setupManager.getIsSetup()) {
			logger.info("Timer kicked...");
			NzbCache nzbCache = NzbCache.INSTANCE;
			nzbCache.refreshCache();
			logger.info("Timer sleeping...");
		} else {
			logger.warn("SetupManager not setup");
		}
	}

	public boolean cancel() {
		logger.info("SabNzbTimer shutting down...");
		return super.cancel();
	}

}
