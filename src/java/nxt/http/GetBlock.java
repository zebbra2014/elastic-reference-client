package nxt.http;

import static nxt.http.JSONResponses.INCORRECT_BLOCK;
import static nxt.http.JSONResponses.INCORRECT_HEIGHT;
import static nxt.http.JSONResponses.INCORRECT_TIMESTAMP;
import static nxt.http.JSONResponses.UNKNOWN_BLOCK;

import javax.servlet.http.HttpServletRequest;

import nxt.Block;
import nxt.Nxt;
import nxt.util.Convert;

import org.json.simple.JSONStreamAware;

public final class GetBlock extends APIServlet.APIRequestHandler {

    static final GetBlock instance = new GetBlock();

    private GetBlock() {
        super(new APITag[] {APITag.BLOCKS}, "block", "height", "timestamp", "includeTransactions");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        Block blockData;
        String blockValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "block"));
        String heightValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "height"));
        String timestampValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "timestamp"));
        if (blockValue != null) {
            try {
                blockData = Nxt.getBlockchain().getBlock(Convert.parseUnsignedLong(blockValue));
            } catch (RuntimeException e) {
                return INCORRECT_BLOCK;
            }
        } else if (heightValue != null) {
            try {
                int height = Integer.parseInt(heightValue);
                if (height < 0 || height > Nxt.getBlockchain().getHeight()) {
                    return INCORRECT_HEIGHT;
                }
                blockData = Nxt.getBlockchain().getBlockAtHeight(height);
            } catch (RuntimeException e) {
                return INCORRECT_HEIGHT;
            }
        } else if (timestampValue != null) {
            try {
                int timestamp = Integer.parseInt(timestampValue);
                if (timestamp < 0) {
                    return INCORRECT_TIMESTAMP;
                }
                blockData = Nxt.getBlockchain().getLastBlock(timestamp);
            } catch (RuntimeException e) {
                return INCORRECT_TIMESTAMP;
            }
        } else {
            blockData = Nxt.getBlockchain().getLastBlock();
        }

        if (blockData == null) {
            return UNKNOWN_BLOCK;
        }

        boolean includeTransactions = "true".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "includeTransactions"));

        return JSONData.block(blockData, includeTransactions);

    }

}