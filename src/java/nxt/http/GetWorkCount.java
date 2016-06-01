package nxt.http;

import nxt.Account;
import nxt.NxtException;
import nxt.WorkLogicManager;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetWorkCount extends APIServlet.APIRequestHandler {

    static final GetWorkCount instance = new GetWorkCount();

    private GetWorkCount() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.WC}, "account", "height");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

    	Account account = ParameterParser.getAccount(req);
        JSONObject response = new JSONObject();
        response.put("closed_work_count", WorkLogicManager.getClosedNumber(account.getId()));
        response.put("open_work_count", WorkLogicManager.getOpenNumber(account.getId()));
        return response;
    }

}