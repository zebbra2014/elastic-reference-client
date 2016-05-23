package nxt.http;

import java.math.BigInteger;

import javax.servlet.http.HttpServletRequest;

import nxt.util.Convert;
import nxt.util.JSON;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class LongConvert extends APIServlet.APIRequestHandler {

    static final LongConvert instance = new LongConvert();

    private LongConvert() {
        super(new APITag[] {APITag.UTILS}, "id");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {
        String id = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "id"));
        if (id == null) {
            return JSON.emptyJSON;
        }
        JSONObject response = new JSONObject();
        BigInteger bigInteger = new BigInteger(id);
        if (bigInteger.signum() < 0) {
            if (bigInteger.negate().compareTo(Convert.two64) > 0) {
                response.put("error", "overflow");
            } else {
                response.put("stringId", bigInteger.add(Convert.two64).toString());
                response.put("longId", String.valueOf(bigInteger.longValue()));
            }
        } else {
            if (bigInteger.compareTo(Convert.two64) >= 0) {
                response.put("error", "overflow");
            } else {
                response.put("stringId", bigInteger.toString());
                response.put("longId", String.valueOf(bigInteger.longValue()));
            }
        }
        return response;
    }

}
