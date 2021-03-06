package nxt.http;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nxt.Block;
import nxt.Nxt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class PopOff extends APIServlet.APIRequestHandler {

    static final PopOff instance = new PopOff();

    private PopOff() {
        super(new APITag[] {APITag.DEBUG}, "numBlocks", "height");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        JSONObject response = new JSONObject();
        int numBlocks = 0;
        try {
            numBlocks = Integer.parseInt(ParameterParser.getParameterMultipart(req, "numBlocks"));
        } catch (NumberFormatException e) {}
        int height = 0;
        try {
            height = Integer.parseInt(ParameterParser.getParameterMultipart(req, "height"));
        } catch (NumberFormatException e) {}

        List<? extends Block> blocks;
        JSONArray blocksJSON = new JSONArray();
        try {
            Nxt.getBlockchainProcessor().setGetMoreBlocks(false);
            if (numBlocks > 0) {
                blocks = Nxt.getBlockchainProcessor().popOffTo(Nxt.getBlockchain().getHeight() - numBlocks);
            } else if (height > 0) {
                blocks = Nxt.getBlockchainProcessor().popOffTo(height);
            } else {
                response.put("error", "invalid numBlocks or height");
                return response;
            }
        } finally {
            Nxt.getBlockchainProcessor().setGetMoreBlocks(true);
        }
        for (Block block : blocks) {
            blocksJSON.add(JSONData.block(block, true));
        }
        response.put("blocks", blocksJSON);
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
