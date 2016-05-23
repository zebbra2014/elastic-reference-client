package nxt.http;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

public final class GetNextBlockGenerators extends APIServlet.APIRequestHandler {

    static final GetNextBlockGenerators instance = new GetNextBlockGenerators();

    private GetNextBlockGenerators() {
        super(new APITag[] {APITag.FORGING});
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {
    		// Disable that feature once for all
            return JSONResponses.FEATURE_NOT_AVAILABLE;
    }
}
