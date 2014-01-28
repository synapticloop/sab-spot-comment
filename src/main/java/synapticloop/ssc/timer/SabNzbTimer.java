package synapticloop.ssc.timer;

import synapticloop.ssc.utils.SetupManager;
import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;

public class SabNzbTimer extends TimerTask {
	private Properties properties = null;
	private long lastLookupTime = 0;

	public SabNzbTimer() {
		System.out.println("SabNzbTimer started...");
	}

	public void run() {
		SetupManager setupManager = SetupManager.INSTANCE;
		if(setupManager.getIsSetup()) {
			long currentTimeMillis = System.currentTimeMillis();
			if(lastLookupTime < currentTimeMillis) {
				lastLookupTime = currentTimeMillis;
			}
		} else {
			System.out.println("WARN: not setup");
		}
		// now connect to sabnzbd
	}

	public boolean cancel() {
		System.out.println("SabNzbTimer shutting down...");
		return super.cancel();
	}

}
