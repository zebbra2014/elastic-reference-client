package nxt.http;

import nxt.Account;
import nxt.Nxt;
import nxt.NxtException;
import nxt.Transaction;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountWork extends APIServlet.APIRequestHandler {

    static final GetAccountWork instance = new GetAccountWork();

    private GetAccountWork() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.WC}, "account", "timestamp", "type", "subtype", "firstIndex", "lastIndex", "numberOfConfirmations", "withMessage");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account account = ParameterParser.getAccount(req);
        int timestamp = ParameterParser.getTimestamp(req);
        int numberOfConfirmations = ParameterParser.getNumberOfConfirmations(req);
        
        byte type;
        try {
            type = Byte.parseByte(req.getParameter("type"));
        } catch (NumberFormatException e) {
            type = -1;
        }
       

        JSONArray work_packages = new JSONArray();
        
        
        // HERE, LOAD THE WORK FROM THE DB
        /*
        try (DbIterator<? extends Transaction> iterator = Nxt.getBlockchain().getTransactions(account, numberOfConfirmations, type, subtype, timestamp,
                withMessage, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                transactions.add(JSONData.transaction(transaction));
            }
        }*/

        JSONObject response = new JSONObject();
        response.put("work_packages", work_packages);
        return response;

    }

}
