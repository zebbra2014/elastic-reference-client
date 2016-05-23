package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Nxt;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetTime extends APIServlet.APIRequestHandler {

    static final GetTime instance = new GetTime();

    private GetTime() {
        super(new APITag[] {APITag.INFO});
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = new JSONObject();
        response.put("time", Nxt.getEpochTime());

        return response;
    }

}
