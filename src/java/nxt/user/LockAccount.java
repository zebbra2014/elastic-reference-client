package nxt.user;

import static nxt.user.JSONResponses.LOCK_ACCOUNT;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

public final class LockAccount extends UserServlet.UserRequestHandler {

    static final LockAccount instance = new LockAccount();

    private LockAccount() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {

        user.lockAccount();

        return LOCK_ACCOUNT;
    }
}
