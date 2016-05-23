package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.util.Convert;
import nxt.util.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class BroadcastTransaction extends APIServlet.APIRequestHandler {

    static final BroadcastTransaction instance = new BroadcastTransaction();

    private BroadcastTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transactionBytes", "transactionJSON");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        String transactionBytes = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "transactionBytes"));
        String transactionJSON = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "transactionJSON"));
        Transaction transaction = ParameterParser.parseTransaction(transactionBytes, transactionJSON);
        JSONObject response = new JSONObject();
        try {
            Nxt.getTransactionProcessor().broadcast(transaction);
            response.put("transaction", transaction.getStringId());
            response.put("fullHash", transaction.getFullHash());
        } catch (NxtException.ValidationException|RuntimeException e) {
            Logger.logDebugMessage(e.getMessage(), e);
            response.put("errorCode", 4);
            response.put("errorDescription", e.toString());
            response.put("error", e.getMessage());
        }
        return response;

    }

    @Override
    boolean requirePost() {
        return true;
    }

}
