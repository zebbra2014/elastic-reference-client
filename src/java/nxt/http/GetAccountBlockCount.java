package nxt.http;

import nxt.Account;
import nxt.Nxt;
import nxt.NxtException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccountBlockCount extends APIServlet.APIRequestHandler {

    static final GetAccountBlockCount instance = new GetAccountBlockCount();

    private GetAccountBlockCount() {
        super(new APITag[] {APITag.ACCOUNTS}, "account");
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) throws NxtException {

        Account account = ParameterParser.getAccount(req);
        JSONObject response = new JSONObject();
        response.put("numberOfBlocks", Nxt.getBlockchain().getBlockCount(account));

        return response;
    }

}
