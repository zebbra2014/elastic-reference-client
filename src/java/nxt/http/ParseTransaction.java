package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.NxtException;
import nxt.Transaction;
import nxt.util.Convert;
import nxt.util.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class ParseTransaction extends APIServlet.APIRequestHandler {

    static final ParseTransaction instance = new ParseTransaction();

    private ParseTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transactionBytes", "transactionJSON");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        String transactionBytes = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "transactionBytes"));
        String transactionJSON = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "transactionJSON"));
        Transaction transaction = ParameterParser.parseTransaction(transactionBytes, transactionJSON);
        JSONObject response = JSONData.unconfirmedTransaction(transaction);
        try {
            transaction.validate();
        } catch (NxtException.ValidationException|RuntimeException e) {
            Logger.logDebugMessage(e.getMessage(), e);
            response.put("validate", false);
            response.put("errorCode", 4);
            response.put("errorDescription", "Invalid transaction: " + e.toString());
            response.put("error", e.getMessage());
        }
        response.put("verify", transaction.verifySignature());
        return response;
    }

}
