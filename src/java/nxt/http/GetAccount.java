package nxt.http;

import nxt.Account;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccount extends APIServlet.APIRequestHandler {

    static final GetAccount instance = new GetAccount();

    private GetAccount() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "includeLessors", "includeAssets", "includeCurrencies");
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) throws NxtException {

        Account account = ParameterParser.getAccount(req);
        boolean includeLessors = !"false".equalsIgnoreCase(req.getParameter("includeLessors"));
        boolean includeAssets = !"false".equalsIgnoreCase(req.getParameter("includeAssets"));
        boolean includeCurrencies = !"false".equalsIgnoreCase(req.getParameter("includeCurrencies"));

        JSONObject response = JSONData.accountBalance(account);
        JSONData.putAccount(response, "account", account.getId());

        if (account.getPublicKey() != null) {
            response.put("publicKey", Convert.toHexString(account.getPublicKey()));
        }
        if (account.getName() != null) {
            response.put("name", account.getName());
        }
        if (account.getDescription() != null) {
            response.put("description", account.getDescription());
        }
        if (account.getMessagePattern() != null) {
            response.put("messagePatternRegex", account.getMessagePattern().pattern());
            response.put("messagePatternFlags", account.getMessagePattern().flags());
        }
        if (account.getCurrentLesseeId() != 0) {
            JSONData.putAccount(response, "currentLessee", account.getCurrentLesseeId());
            response.put("currentLeasingHeightFrom", account.getCurrentLeasingHeightFrom());
            response.put("currentLeasingHeightTo", account.getCurrentLeasingHeightTo());
            if (account.getNextLesseeId() != 0) {
                JSONData.putAccount(response, "nextLessee", account.getNextLesseeId());
                response.put("nextLeasingHeightFrom", account.getNextLeasingHeightFrom());
                response.put("nextLeasingHeightTo", account.getNextLeasingHeightTo());
            }
        }

        if (includeLessors) {
            try (DbIterator<Account> lessors = account.getLessors()) {
                if (lessors.hasNext()) {
                    JSONArray lessorIds = new JSONArray();
                    JSONArray lessorIdsRS = new JSONArray();
                    JSONArray lessorInfo = new JSONArray();
                    while (lessors.hasNext()) {
                        Account lessor = lessors.next();
                        lessorIds.add(Convert.toUnsignedLong(lessor.getId()));
                        lessorIdsRS.add(Convert.rsAccount(lessor.getId()));
                        lessorInfo.add(JSONData.lessor(lessor));
                    }
                    response.put("lessors", lessorIds);
                    response.put("lessorsRS", lessorIdsRS);
                    response.put("lessorsInfo", lessorInfo);
                }
            }
        }

        return response;

    }

}
