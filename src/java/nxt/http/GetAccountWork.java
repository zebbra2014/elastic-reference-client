package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.NxtException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccountWork extends APIServlet.APIRequestHandler {

	static final GetAccountWork instance = new GetAccountWork();
	
	// Just in case we need it in the future, but i think this can be safely removed
    public double round(final double value, final int frac) {
        return Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
    }
    
    public int getCurrentPowReward(){
    	return 10;
    }
    
    public int getPercentWork(){
    	return 60;
    }
    
    public int getPercentBounty(){
    	return 40;
    }
    
	@SuppressWarnings("unchecked")
	private JSONObject workEntry(long workId, int time_created, int time_closed,
			int was_cancel, String title, String account, String language,
			int num_input, int num_output,
			long balance_original, int bounties_connected, int pow_connected, int refund, int timeout_at_block, int script_size_bytes, long fee) {
		JSONObject response = new JSONObject();
		response.put("workId", workId);
		response.put("time_created", time_created);
		response.put("time_closed", time_closed);
		response.put("was_cancel", was_cancel);
		response.put("title", title);
		response.put("account", account);
		response.put("language", language);
		response.put("num_input", num_input);
		response.put("num_output", num_output);
		response.put("percent_work", getPercentWork());
		response.put("percent_bounties", getPercentBounty());
		
		response.put("balance_original", balance_original);
		
		long balance_work = balance_original*getPercentWork()/100-(pow_connected*getCurrentPowReward());
		long balance_bounties = balance_original*getPercentBounty()/100;
		
		response.put("balance_remained", balance_work+balance_bounties);
		response.put("balance_work", balance_work);
		response.put("balance_bounties", balance_bounties);
		
		double done = 100-Math.round(((balance_work+balance_bounties) * 1.0/balance_original)*100.0);
		
		response.put("percent_done", done);
		response.put("pow_connected", pow_connected);
		response.put("bounties_connected", bounties_connected);
		response.put("refund", refund);
		response.put("timeout_at_block", timeout_at_block);
		response.put("script_size_bytes", script_size_bytes);
		response.put("fee", fee);

		return response;
	}

	private GetAccountWork() {
		super(new APITag[] { APITag.ACCOUNTS, APITag.WC }, "account",
				"timestamp", "type", "subtype", "firstIndex", "lastIndex",
				"numberOfConfirmations", "withMessage");
	}

	@Override
	JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

		Account account = ParameterParser.getAccount(req);
		int timestamp = ParameterParser.getTimestamp(req);
		int numberOfConfirmations = ParameterParser
				.getNumberOfConfirmations(req);

		byte type;
		try {
			type = Byte.parseByte(ParameterParser.getParameterMultipart(req, "type"));
		} catch (NumberFormatException e) {
			type = -1;
		}

		JSONArray work_packages = new JSONArray();

		// HERE, LOAD THE WORK FROM THE DB
		/*
		 * try (DbIterator<? extends Transaction> iterator =
		 * Nxt.getBlockchain().getTransactions(account, numberOfConfirmations,
		 * type, subtype, timestamp, withMessage, firstIndex, lastIndex)) {
		 * while (iterator.hasNext()) { Transaction transaction =
		 * iterator.next(); transactions.add(JSONData.transaction(transaction));
		 * } }
		 */

		// Testing, return some dummy elements


		JSONObject item1 = workEntry(199381883,1,0,0,"Prime Number Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", 12, 12, 5000, 25 /*bounties*/, 110, 0, 100000 /* block timeout */, 9086, 12);
		JSONObject item2 = workEntry(199381883,3,9,2,"Hash Collision Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", 12, 12, 5000, 1 /*bounties*/, 12, 0, 100000 /* block timeout */, 1024, 12);
		work_packages.add(item1);
		work_packages.add(item2);
		
		JSONObject response = new JSONObject();
		response.put("work_packages", work_packages);
		return response;

	}

}
