package nxt.user;

import nxt.Token;
import nxt.http.FakeServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;


import java.io.IOException;

import static nxt.user.JSONResponses.INVALID_SECRET_PHRASE;

public final class GenerateAuthorizationToken extends UserServlet.UserRequestHandler {

    static final GenerateAuthorizationToken instance = new GenerateAuthorizationToken();

    private GenerateAuthorizationToken() {}

    @Override
    JSONStreamAware processRequest(FakeServletRequest req, User user) throws IOException {
        String secretPhrase = req.getParameter("secretPhrase");
        if (! user.getSecretPhrase().equals(secretPhrase)) {
            return INVALID_SECRET_PHRASE;
        }

        String tokenString = Token.generateToken(secretPhrase, req.getParameter("website").trim());

        JSONObject response = new JSONObject();
        response.put("response", "showAuthorizationToken");
        response.put("token", tokenString);

        return response;
    }

    @Override
    boolean requirePost() {
        return true;
    }

}
