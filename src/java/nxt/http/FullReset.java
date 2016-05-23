package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Nxt;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class FullReset extends APIServlet.APIRequestHandler {

    static final FullReset instance = new FullReset();

    private FullReset() {
        super(new APITag[] {APITag.DEBUG});
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        try {
            Nxt.getBlockchainProcessor().fullReset();
            response.put("done", true);
        } catch (RuntimeException e) {
            response.put("error", e.toString());
        }
        return response;
    }

    @Override
    final boolean requirePost() {
        return true;
    }

    @Override
    boolean requirePassword() {
        return true;
    }
}
