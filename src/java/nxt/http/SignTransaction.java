package nxt.http;

import static nxt.http.JSONResponses.MISSING_SECRET_PHRASE;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import nxt.NxtException;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.util.Convert;
import nxt.util.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class SignTransaction extends APIServlet.APIRequestHandler {

    static final SignTransaction instance = new SignTransaction();

    private SignTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "unsignedTransactionBytes", "unsignedTransactionJSON", "secretPhrase", "validate");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        String transactionBytes = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "unsignedTransactionBytes"));
        String transactionJSON = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "unsignedTransactionJSON"));
        Transaction transaction = ParameterParser.parseTransaction(transactionBytes, transactionJSON);

        String secretPhrase = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "secretPhrase"));
        if (secretPhrase == null) {
            return MISSING_SECRET_PHRASE;
        }

        boolean validate = !"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "validate"));

        JSONObject response = new JSONObject();
        try {
            if (validate) {
                transaction.validate();
            }
            if (transaction.getSignature() != null) {
                response.put("errorCode", 4);
                response.put("errorDescription", "Incorrect unsigned transaction - already signed");
                return response;
            }
            if (! Arrays.equals(Crypto.getPublicKey(secretPhrase), transaction.getSenderPublicKey())) {
                response.put("errorCode", 4);
                response.put("errorDescription", "Secret phrase doesn't match transaction sender public key");
                return response;
            }
            transaction.sign(secretPhrase);
            response.put("transaction", transaction.getStringId());
            response.put("fullHash", transaction.getFullHash());
            response.put("transactionBytes", Convert.toHexString(transaction.getBytes()));
            response.put("signatureHash", Convert.toHexString(Crypto.sha256().digest(transaction.getSignature())));
            response.put("verify", transaction.verifySignature());
        } catch (NxtException.ValidationException|RuntimeException e) {
            Logger.logDebugMessage(e.getMessage(), e);
            response.put("errorCode", 4);
            response.put("errorDescription", "Incorrect unsigned transaction: " + e.toString());
            response.put("error", e.getMessage());
            return response;
        }
        return response;
    }

}
