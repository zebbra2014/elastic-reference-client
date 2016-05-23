package nxt.http;

import javax.servlet.http.HttpServletRequest;

import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;



public final class GetPeers extends APIServlet.APIRequestHandler {

    static final GetPeers instance = new GetPeers();

    private GetPeers() {
        super(new APITag[] {APITag.INFO}, "active", "state", "includePeerInfo");
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {

        boolean active = "true".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "active"));
        String stateValue = Convert.emptyToNull(ParameterParser.getParameterMultipart(req, "state"));
        boolean includePeerInfo = "true".equalsIgnoreCase(ParameterParser.getParameterMultipart(req, "includePeerInfo"));

        JSONArray peers = new JSONArray();
        for (Peer peer : active ? Peers.getActivePeers() : stateValue != null ? Peers.getPeers(Peer.State.valueOf(stateValue)) : Peers.getAllPeers()) {
            peers.add(includePeerInfo ? JSONData.peer(peer) : peer.getPeerAddress());
        }

        JSONObject response = new JSONObject();
        response.put("peers", peers);
        return response;
    }

}
