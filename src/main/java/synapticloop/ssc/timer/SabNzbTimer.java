package synapticloop.ssc.timer;

import java.io.IOException;
import java.util.Properties;
import java.util.TimerTask;

public class SabNzbTimer extends TimerTask {
	private Properties properties = null;
	private long lastLookupTime = 0;
	private static final String SAB_API = "https://10.0.1.115:9095/api?mode=history&start=START&limit=LIMIT&output=json&apikey=e9e0978a55509e681ae60808aabf7013";
	public SabNzbTimer() {
		properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream("/timer.properties"));
		} catch (IOException ioex) {
			// do nothing
		}

		String lastLookupTimeString = properties.getProperty("lastLookupTime");
		if(null != lastLookupTimeString) {
			// try and convert it to a long
			try {
				lastLookupTime = Long.parseLong(lastLookupTimeString);
			} catch(NumberFormatException nfex) {
			}
		}
	}

	public void run() {
		long currentTimeMillis = System.currentTimeMillis();
		if(lastLookupTime < currentTimeMillis) {
			lastLookupTime = currentTimeMillis;
		}

		// now connect to sabnzbd
	}

	public boolean cancel() {
		// do something
		return super.cancel();
	}

}
