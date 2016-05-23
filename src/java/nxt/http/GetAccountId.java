package nxt.http;

import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.crypto.Crypto;
import nxt.util.Convert;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class GetAccountId extends APIServlet.APIRequestHandler {

    static final GetAccountId instance = new GetAccountId();

    private GetAccountId() {
        super(new APITag[] {APITag.ACCOUNTS}, "secretPhrase", "publicKey");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        long accountId;
        String secretPhrase = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "secretPhrase"));
        String publicKeyString = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "publicKey"));
        if (secretPhrase != null) {
            byte[] publicKey = Crypto.getPublicKey(secretPhrase);
            accountId = Account.getId(publicKey);
            publicKeyString = Convert.toHexString(publicKey);
        } else if (publicKeyString != null) {
            accountId = Account.getId(Convert.parseHexString(publicKeyString));
        } else {
            return MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;
        }

        JSONObject response = new JSONObject();
        JSONData.putAccount(response, "account", accountId);
        response.put("publicKey", publicKeyString);

        return response;
    }

    @Override
    boolean requirePost() {
        return true;
    }

}
