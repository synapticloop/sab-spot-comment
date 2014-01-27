package synapticloop.ssc.servant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import synapticloop.nanohttpd.router.RestRoutable;
import synapticloop.nanohttpd.utils.HttpUtils;
import synapticloop.ssc.bean.Download;
import synapticloop.ssc.utils.ConnectionHelper;
import synapticloop.ssc.utils.SetupManager;
import synapticloop.templar.Parser;
import synapticloop.templar.exception.ParseException;
import synapticloop.templar.exception.RenderException;
import synapticloop.templar.utils.TemplarContext;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class SabSpotCommentServant extends RestRoutable {
	private HashSet<String> downloadedNzbIds = new HashSet<String>();
	private long lastCompletedTime = 0l;
	private ArrayList<Download> downloads = new ArrayList<Download>();


	public SabSpotCommentServant(String routeContext, ArrayList<String> params) {
		super(routeContext, params);
	}

	@Override
	public Response doGet(File rootDir, IHTTPSession httpSession, HashMap<String, String> restParams, String unmappedParams) {
		SetupManager setupManager = SetupManager.INSTANCE;
		long maxCompletedTime = 0l;
		if(!setupManager.getIsSetup()) {
			// not setup - need to do the setup
			return(HttpUtils.redirectResponse("/admin/"));
		} else {
			// set up and ready to go

			String content = ConnectionHelper.getUrl(setupManager.getSabNzbUrl() + "/api/?apikey=" + setupManager.getSabNzbApiKey() + "&mode=history&start=0&limit=20&output=json");
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
					boolean ignored = true;
					String guid = null;

					if(url.startsWith(setupManager.getNewznabUrl().toLowerCase())) {
						ignored = false;
						// now grab the nzbid
						int indexOfGetNzb = url.indexOf("/getnzb/") + "/getnzb/".length();
						int indexOfDotNzb = url.indexOf(".nzb");
						guid = url.substring(indexOfGetNzb, indexOfDotNzb);
					}

					if(!downloadedNzbIds.contains(sabNzbId) && completedTime > lastCompletedTime) {
						downloadedNzbIds.add(sabNzbId);
						downloads.add(new Download(name, sabNzbId, failMessage, completedTime * 1000, guid, ignored));
					}
					if(completedTime > maxCompletedTime) {
						maxCompletedTime = completedTime;
					}
				}
			} catch(JSONException jsonex) {
				// TODO - do something
			}

			setupManager.setLastCompletedTime(maxCompletedTime * 1000);

			TemplarContext templarContext = new TemplarContext();
			templarContext.add("downloads", downloads);
			templarContext.add("setupManager", setupManager);

			setupManager.saveProperties();

			try {
				Parser parser = new Parser(this.getClass().getResourceAsStream("/synapticloop/ssc/template/home.templar"));
				return(HttpUtils.okResponse(NanoHTTPD.MIME_HTML, parser.render(templarContext)));
			} catch (ParseException pex) {
				// TODO Auto-generated catch block
				pex.printStackTrace();
				return(null);
			} catch (RenderException rex) {
				// TODO Auto-generated catch block
				rex.printStackTrace();
				return(null);
			}
		}
	}

}
