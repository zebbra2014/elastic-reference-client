package nxt.http;

import nxt.Block;
import nxt.Constants;
import nxt.Hub;
import nxt.Nxt;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;


import java.util.Iterator;

public final class GetNextBlockGenerators extends APIServlet.APIRequestHandler {

    static final GetNextBlockGenerators instance = new GetNextBlockGenerators();

    private GetNextBlockGenerators() {
        super(new APITag[] {APITag.FORGING});
    }

    @Override
    JSONStreamAware processRequest(FakeServletRequest req) {
    		// Disable that feature once for all
            return JSONResponses.FEATURE_NOT_AVAILABLE;
    }
}
