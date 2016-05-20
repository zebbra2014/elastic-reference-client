package nxt.user;

import nxt.http.FakeServletRequest;
import org.json.simple.JSONStreamAware;
import java.io.IOException;

import static nxt.user.JSONResponses.LOCK_ACCOUNT;

public final class LockAccount extends UserServlet.UserRequestHandler {

    static final LockAccount instance = new LockAccount();

    private LockAccount() {}

    @Override
    JSONStreamAware processRequest(FakeServletRequest req, User user) throws IOException {

        user.lockAccount();

        return LOCK_ACCOUNT;
    }
}
