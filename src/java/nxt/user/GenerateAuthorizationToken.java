package nxt.user;

import static nxt.user.JSONResponses.INVALID_SECRET_PHRASE;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import nxt.Token;
import nxt.http.ParameterParser;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class GenerateAuthorizationToken extends UserServlet.UserRequestHandler {

    static final GenerateAuthorizationToken instance = new GenerateAuthorizationToken();

    private GenerateAuthorizationToken() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {
        String secretPhrase = ParameterParser.getParameterMultipart(req, "secretPhrase");
        if (! user.getSecretPhrase().equals(secretPhrase)) {
            return INVALID_SECRET_PHRASE;
        }

        String tokenString = Token.generateToken(secretPhrase, ParameterParser.getParameterMultipart(req, "website").trim());

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
