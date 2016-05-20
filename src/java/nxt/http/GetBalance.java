package nxt.http;

import nxt.NxtException;
import org.json.simple.JSONStreamAware;



public final class GetBalance extends APIServlet.APIRequestHandler {

    static final GetBalance instance = new GetBalance();

    private GetBalance() {
        super(new APITag[] {APITag.ACCOUNTS}, "account");
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) throws NxtException {
        return JSONData.accountBalance(ParameterParser.getAccount(req));
    }

}
