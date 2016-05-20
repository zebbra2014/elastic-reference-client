package nxt.http;

import nxt.Account;
import nxt.NxtException;
import nxt.util.Convert;
import nxt.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccountPublicKey extends APIServlet.APIRequestHandler {

    static final GetAccountPublicKey instance = new GetAccountPublicKey();

    private GetAccountPublicKey() {
        super(new APITag[] {APITag.ACCOUNTS}, "account");
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) throws NxtException {

        Account account = ParameterParser.getAccount(req);

        if (account.getPublicKey() != null) {
            JSONObject response = new JSONObject();
            response.put("publicKey", Convert.toHexString(account.getPublicKey()));
            return response;
        } else {
            return JSON.emptyJSON;
        }
    }

}
