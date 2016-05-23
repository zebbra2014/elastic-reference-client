package nxt.http;

import static nxt.http.JSONResponses.MISSING_PEER;
import static nxt.http.JSONResponses.UNKNOWN_PEER;

import javax.servlet.http.HttpServletRequest;

import nxt.peer.Peer;
import nxt.peer.Peers;

import org.json.simple.JSONStreamAware;

public final class GetPeer extends APIServlet.APIRequestHandler {

    static final GetPeer instance = new GetPeer();

    private GetPeer() {
        super(new APITag[] {APITag.INFO}, "peer");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        String peerAddress = ParameterParser.getParameterMultipart(req, "peer");
        if (peerAddress == null) {
            return MISSING_PEER;
        }

        Peer peer = Peers.getPeer(peerAddress);
        if (peer == null) {
            return UNKNOWN_PEER;
        }

        return JSONData.peer(peer);

    }

}
