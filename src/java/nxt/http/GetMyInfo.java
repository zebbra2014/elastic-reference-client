package nxt.http;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetMyInfo extends APIServlet.APIRequestHandler {

    static final GetMyInfo instance = new GetMyInfo();

    private GetMyInfo() {
        super(new APITag[] {APITag.INFO});
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) {

        JSONObject response = new JSONObject();
        response.put("host", req.getRemoteHost());
        response.put("address", req.getRemoteAddr());
        return response;
    }

}
