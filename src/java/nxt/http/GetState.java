package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.Constants;
import nxt.Generator;
import nxt.Nxt;
import nxt.peer.Peers;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetState extends APIServlet.APIRequestHandler {

    static final GetState instance = new GetState();

    private GetState() {
        super(new APITag[] {APITag.INFO}, "includeCounts");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = GetBlockchainStatus.instance.processRequest(req);

        /*
        long totalEffectiveBalance = 0;
        try (DbIterator<Account> accounts = Account.getAllAccounts(0, -1)) {
            for (Account account : accounts) {
                long effectiveBalanceNXT = account.getEffectiveBalanceNXT();
                if (effectiveBalanceNXT > 0) {
                    totalEffectiveBalance += effectiveBalanceNXT;
                }
            }
        }
        response.put("totalEffectiveBalanceNXT", totalEffectiveBalance);
        */

        if (!"false".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "includeCounts"))) {
            response.put("numberOfTransactions", Nxt.getBlockchain().getTransactionCount());
            response.put("numberOfAccounts", Account.getCount());
            //response.put("numberOfPolls", Poll.getCount());
            //response.put("numberOfVotes", Vote.getCount());
        }
        response.put("numberOfPeers", Peers.getAllPeers().size());
        response.put("numberOfUnlockedAccounts", Generator.getAllGenerators().size());
        response.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        response.put("maxMemory", Runtime.getRuntime().maxMemory());
        response.put("totalMemory", Runtime.getRuntime().totalMemory());
        response.put("freeMemory", Runtime.getRuntime().freeMemory());
        response.put("peerPort", Peers.getDefaultPeerPort());
        response.put("isTestnet", Constants.isTestnet);
        response.put("isOffline", Constants.isOffline);
        return response;
    }

}
