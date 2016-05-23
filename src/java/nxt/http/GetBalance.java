package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.NxtException;

import org.json.simple.JSONStreamAware;



public final class GetBalance extends APIServlet.APIRequestHandler {

    static final GetBalance instance = new GetBalance();

    private GetBalance() {
        super(new APITag[] {APITag.ACCOUNTS}, "account");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        return JSONData.accountBalance(ParameterParser.getAccount(req));
    }

}
