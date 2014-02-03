package synapticloop.ssc;

import java.util.Timer;

import synapticloop.nanohttpd.RouteMasterServer;
import synapticloop.ssc.timer.SabNzbTimer;
import synapticloop.ssc.utils.SetupManager;


public class SabSpotCommentServer {
	private static Timer timer = null;
	// run every 5 minutes
	private static final long NUM_MILLIS_THREAD = 1000 * 60 * 5;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		// initialise the SetupManager
		SetupManager setupManager = SetupManager.INSTANCE;

		// now set up the timer
		if(null == timer) {
			timer = new Timer();
			timer.schedule(new SabNzbTimer(), 0, NUM_MILLIS_THREAD);
		}

		RouteMasterServer.main(args);
	}
}