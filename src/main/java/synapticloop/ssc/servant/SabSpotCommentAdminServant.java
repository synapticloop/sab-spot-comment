package synapticloop.ssc.servant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import synapticloop.nanohttpd.router.Routable;
import synapticloop.nanohttpd.utils.HttpUtils;
import synapticloop.ssc.bean.Download;
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

		templarContext.add("setupManager", SetupManager.INSTANCE);

		templarContext.add("dummySuccessComment", new Download("Dummy Success Download", "http://www.example.com/", "SAB_NZB_ID", "", System.currentTimeMillis(), "GUID", false).getComment());
		templarContext.add("dummyFailedComment", new Download("Dummy Failed Download", "http://www.example.com/", "SAB_NZB_ID", "Unpacking failed, archive requires a password", System.currentTimeMillis(), "GUID", false).getComment());
		templarContext.add("dummyIgnoredDownload", new Download("Dummy Ignored Download", "http://www.example.com/", "SAB_NZB_ID", "Unpacking failed, archive requires a password", System.currentTimeMillis(), "GUID", true));

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
			SetupManager setupManager = SetupManager.INSTANCE;
			setupManager.setNewznabApiKey(parms.get("newznabApiKey"));
			setupManager.setNewznabUrl(parms.get("newznabUrl"));
			setupManager.setSabNzbApiKey(parms.get("sabNzbApiKey"));
			setupManager.setSabNzbUrl(parms.get("sabNzbUrl"));
			setupManager.setFailedCommentFormat(parms.get("failedCommentFormat"));
			setupManager.setSuccessCommentFormat(parms.get("successCommentFormat"));

			String isDemoParam = parms.get("isDemo");
			if(null != isDemoParam) {
				setupManager.setIsDemo(isDemoParam.equalsIgnoreCase("true"));
			} else {
				setupManager.setIsDemo(true);
			}

			String numLastCommentDaysString = parms.get("numLastCommentDays");
			try { setupManager.setNumLastCommentDays(Integer.parseInt(numLastCommentDaysString)); } catch(NumberFormatException nfex) { }
			String numSuccessCommentsString = parms.get("numSuccessComments");
			try { setupManager.setNumSuccessComments(Integer.parseInt(numSuccessCommentsString)); } catch(NumberFormatException nfex) { }
			String numFailureCommentsString = parms.get("numFailureComments");
			try { setupManager.setNumFailureComments(Integer.parseInt(numFailureCommentsString)); } catch(NumberFormatException nfex) { }
			setupManager.validate();
		}
	}

}
