package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Account;
import nxt.NxtException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccountWork extends APIServlet.APIRequestHandler {

	static final GetAccountWork instance = new GetAccountWork();

	

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
