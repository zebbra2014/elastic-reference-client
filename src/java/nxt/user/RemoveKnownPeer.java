package nxt.user;

import static nxt.user.JSONResponses.LOCAL_USERS_ONLY;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.http.HttpServletRequest;

import nxt.http.ParameterParser;
import nxt.peer.Peer;

import org.json.simple.JSONStreamAware;

public final class RemoveKnownPeer extends UserServlet.UserRequestHandler {

    static final RemoveKnownPeer instance = new RemoveKnownPeer();
    private RemoveKnownPeer() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {
        if (Users.allowedUserHosts == null && ! InetAddress.getByName(req.getRemoteAddr()).isLoopbackAddress()) {
            return LOCAL_USERS_ONLY;
        } else {
            int index = Integer.parseInt(ParameterParser.getParameterMultipart(req, "peer"));
            Peer peer = Users.getPeer(index);
            if (peer != null) {
                peer.remove();
            }
        }
        return null;
    }
}
