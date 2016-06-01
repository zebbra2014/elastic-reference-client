package nxt.http;

import static nxt.http.JSONResponses.MISSING_PROGAMCODE;

import javax.servlet.http.HttpServletRequest;

import nxt.NxtException;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;


public final class CreateNewWork extends APIServlet.APIRequestHandler {

	static final CreateNewWork instance = new CreateNewWork();

	private CreateNewWork() {
		super(new APITag[] { APITag.ACCOUNTS, APITag.WC }, "account",
				"timestamp", "type", "subtype", "firstIndex", "lastIndex",
				"numberOfConfirmations", "withMessage");
	}

	
	@SuppressWarnings("unchecked")
	@Override
	JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
		JSONObject response = new JSONObject();

		
		System.out.println(ParameterParser.getParameterMultipart(req, "source_code"));
		
		/*long workId;
		try {
			workId = Long.parseLong(ParameterParser.getParameterMultipart(req, "workId"));
		} catch (NumberFormatException e) {
			return response;
		}*/
		
		
		response.put("success", false);
		response.put("error_text", "Not implemented");
		response.put("error_code", 100);
		
		
		return MISSING_PROGAMCODE;

	}

}
