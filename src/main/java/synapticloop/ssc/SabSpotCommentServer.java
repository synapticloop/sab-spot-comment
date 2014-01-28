package synapticloop.ssc;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import synapticloop.nanohttpd.router.RouteMaster;
import synapticloop.nanohttpd.utils.SimpleLogger;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.ServerRunner;
import synapticloop.nanohttpd.RouteMasterServer;
import java.util.Timer;
import java.util.TimerTask;
import synapticloop.ssc.timer.SabNzbTimer;
import synapticloop.ssc.utils.SetupManager;


public class SabSpotCommentServer {
	private static Timer timer = null;
	// run every 5 minutes
	private static final long NUM_MILLIS_THREAD = 1000 * 60 * 5;

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