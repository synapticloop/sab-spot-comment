package synapticloop.ssc.servant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import synapticloop.nanohttpd.router.Routable;
import synapticloop.nanohttpd.utils.HttpUtils;
import synapticloop.ssc.utils.SetupManager;
import synapticloop.templar.Parser;
import synapticloop.templar.exception.ParseException;
import synapticloop.templar.exception.RenderException;
import synapticloop.templar.utils.TemplarContext;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.ResponseException;

public class SabSpotCommentAdminServant extends Routable {

	public SabSpotCommentAdminServant(String routeContext) {
		super(routeContext);
	}

	public Response serve(File file, IHTTPSession session) {
		HashMap<String, String> files = new HashMap<String, String>();
		if(session.getMethod() == Method.POST) {
			try {
				session.parseBody(files);
			} catch (IOException ioex) { 
			} catch (ResponseException rex) { 
			}
		}

		// get the parameters
		parseParameters(session);
		// not setup yet - need to run through the parameters
		TemplarContext templarContext = new TemplarContext();
		templarContext.add("sabNzbUrl", SetupManager.getSabNzbUrl());
		templarContext.add("sabNzbApiKey", SetupManager.getSabNzbApiKey());
		templarContext.add("isSabNzbSetup", SetupManager.getIsSabNzbSetup());
		templarContext.add("sabNzbErrorMessage", SetupManager.getSabNzbErrorMessage());
		templarContext.add("newznabServers", SetupManager.getNewznabServers());

		templarContext.add("newznabUrl", SetupManager.getNewznabUrl());
		templarContext.add("newznabApiKey", SetupManager.getNewznabApiKey());
		templarContext.add("isNewznabSetup", SetupManager.getIsNewznabSetup());
		templarContext.add("newznabErrorMessage", SetupManager.getNewznabErrorMessage());

		try {
			Parser parser = new Parser(this.getClass().getResourceAsStream("/synapticloop/ssc/template/admin.templar"));
			return(HttpUtils.okResponse(NanoHTTPD.MIME_HTML, parser.render(templarContext)));
		} catch (ParseException pex) {
			// TODO stacktrace printer
			pex.printStackTrace();
			return(HttpUtils.internalServerErrorResponse());
		} catch (RenderException rex) {
			// TODO stacktrace printer
			rex.printStackTrace();
			return(HttpUtils.internalServerErrorResponse());
		}
	}

	private void parseParameters(IHTTPSession session) {
		Map<String, String> parms = session.getParms();
		
		if(parms.containsKey("submit")) {
			// some data has been submitted
			SetupManager.setNewznabApiKey(parms.get("newznabApiKey"));
			SetupManager.setNewznabUrl(parms.get("newznabUrl"));
			SetupManager.setSabNzbApiKey(parms.get("sabNzbApiKey"));
			SetupManager.setSabNzbUrl(parms.get("sabNzbUrl"));

			SetupManager.validate();
		}
	}

}
