package synapticloop.ssc.servant;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import synapticloop.nanohttpd.router.RestRoutable;
import synapticloop.nanohttpd.utils.HttpUtils;
import synapticloop.ssc.utils.NzbCache;
import synapticloop.ssc.utils.SetupManager;
import synapticloop.templar.Parser;
import synapticloop.templar.exception.ParseException;
import synapticloop.templar.exception.RenderException;
import synapticloop.templar.utils.TemplarContext;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class SabSpotCommentServant extends RestRoutable {

	public SabSpotCommentServant(String routeContext, ArrayList<String> params) {
		super(routeContext, params);
	}

	@Override
	public Response doGet(File rootDir, IHTTPSession httpSession, HashMap<String, String> restParams, String unmappedParams) {
		SetupManager setupManager = SetupManager.INSTANCE;
		if(!setupManager.getIsSetup()) {
			try {
				TemplarContext templarContext = new TemplarContext();
				Parser parser = new Parser(this.getClass().getResourceAsStream("/synapticloop/ssc/template/not-setup.templar"));
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
		} else {
			// set up and ready to go

			NzbCache nzbCache = NzbCache.INSTANCE;
//			nzbCache.refreshCache();

			TemplarContext templarContext = new TemplarContext();
			templarContext.add("downloads", nzbCache.getDownloads());
			templarContext.add("setupManager", setupManager);

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
