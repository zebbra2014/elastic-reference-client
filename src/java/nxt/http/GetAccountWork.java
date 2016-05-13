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

	private JSONObject workEntry(int time_created, int time_closed,
			int was_cancel, String title, String account, String language,
			String code, String bounty_hooks, int num_input, int num_output,
			long balance_remained, long balance_work, long balance_bounties,
			float percent_done, int bounties_connected, int refund,
			int timeout_at_block) {
		JSONObject response = new JSONObject();
		response.put("time_created", time_created);
		response.put("time_closed", time_closed);
		response.put("was_cancel", was_cancel);
		response.put("title", title);
		response.put("account", account);
		response.put("language", language);
		response.put("code", code);
		response.put("bounty_hooks", bounty_hooks);
		response.put("num_input", num_input);
		response.put("num_output", num_output);
		response.put("balance_remained", balance_remained);
		response.put("balance_work", balance_work);
		response.put("balance_bounties", balance_bounties);
		response.put("percent_done", percent_done);
		response.put("bounties_connected", bounties_connected);
		response.put("refund", refund);
		response.put("timeout_at_block", timeout_at_block);

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
			type = Byte.parseByte(req.getParameter("type"));
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


		JSONObject item1 = workEntry(1,0,0,"Prime Number Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", "",  "", 0, 0, 1931, 1000,931,67,7,0,5525);
		JSONObject item2 = workEntry(3,9,2,"Hash Collision Example", "XEL-E8JD-FHKJ-CQ9H-5KGMQ", "LUA", "", "", 0, 0, 2009, 500,1509,25,2,0,6744);
		work_packages.add(item1);
		work_packages.add(item2);
		
		JSONObject response = new JSONObject();
		response.put("work_packages", work_packages);
		return response;

	}

}
