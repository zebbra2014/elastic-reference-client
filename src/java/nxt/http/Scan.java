package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.Nxt;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class Scan extends APIServlet.APIRequestHandler {

    static final Scan instance = new Scan();

    private Scan() {
        super(new APITag[] {APITag.DEBUG}, "numBlocks", "height", "validate");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        try {
            boolean validate = "true".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "validate"));
            int numBlocks = 0;
            try {
                numBlocks = Integer.parseInt(ParameterParser.getParameterMultipart(req, "numBlocks"));
            } catch (NumberFormatException e) {}
            int height = -1;
            try {
                height = Integer.parseInt(ParameterParser.getParameterMultipart(req, "height"));
            } catch (NumberFormatException ignore) {}
            long start = System.currentTimeMillis();
            try {
                Nxt.getBlockchainProcessor().setGetMoreBlocks(false);
                if (numBlocks > 0) {
                    Nxt.getBlockchainProcessor().scan(Nxt.getBlockchain().getHeight() - numBlocks + 1, validate);
                } else if (height >= 0) {
                    Nxt.getBlockchainProcessor().scan(height, validate);
                } else {
                    response.put("error", "invalid numBlocks or height");
                    return response;
                }
            } finally {
                Nxt.getBlockchainProcessor().setGetMoreBlocks(true);
            }
            long end = System.currentTimeMillis();
            response.put("done", true);
            response.put("scanTime", (end - start)/1000);
        } catch (RuntimeException e) {
            response.put("error", e.toString());
        }
        return response;
    }

    @Override
    final boolean requirePost() {
        return true;
    }

    @Override
    boolean requirePassword() {
        return true;
    }
}
