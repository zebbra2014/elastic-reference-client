package nxt.http;

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import nxt.NxtException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetAccountWorkEfficiencyPlot extends APIServlet.APIRequestHandler {

	static final GetAccountWorkEfficiencyPlot instance = new GetAccountWorkEfficiencyPlot();

	private GetAccountWorkEfficiencyPlot() {
		super(new APITag[] { APITag.ACCOUNTS, APITag.WC }, "account",
				"timestamp", "type", "subtype", "firstIndex", "lastIndex",
				"numberOfConfirmations", "withMessage");
	}

	@SuppressWarnings("unchecked")
	JSONArray dateValuePair(long timestamp, double d){
		JSONArray nullValue = new JSONArray();
		nullValue.add(timestamp);
		nullValue.add(d);
		return nullValue;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
		JSONObject response = new JSONObject();

		

		long workId = 0;
		try {
			String readParam = ParameterParser.getParameterMultipart(req, "workId");
			BigInteger b = new BigInteger(readParam);
			workId = b.longValue();
		} catch (Exception e) {
			e.printStackTrace();
			workId = 0;
		}
		
		long common_timestamp = (new Date()).getTime();
		
		JSONArray computation_power = getComputationPlot(workId, common_timestamp);
		JSONArray solution_rate = getSolutionPlot(workId, common_timestamp);
		
		
		/*for(int i=0;i<100;i++){
			common_timestamp = common_timestamp - 20;
			computation_power.add(dateValuePair(common_timestamp, 1337.0 - i*10));
			solution_rate.add(dateValuePair(common_timestamp, 1337.0 - i*5));
		}*/
		solution_rate.add(dateValuePair(0,0));
		
		response.put("computation_power", computation_power);
		response.put("solution_rate", solution_rate);
		
		// Delay to test Javascript Loading Indicator
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {

		}
		
		return response;

	}

	private JSONArray getSolutionPlot(long workId, long common_timestamp) {
		JSONArray solution_rate = new JSONArray();
		
		return solution_rate;
	}

	private JSONArray getComputationPlot(long workId, long common_timestamp) {
		JSONArray computation_power = new JSONArray();
		
		return computation_power;
	}

}
