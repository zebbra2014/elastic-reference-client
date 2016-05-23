package nxt.user;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONStreamAware;

public final class GetNewData extends UserServlet.UserRequestHandler {

    static final GetNewData instance = new GetNewData();

    private GetNewData() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {
        return null;
    }
}
