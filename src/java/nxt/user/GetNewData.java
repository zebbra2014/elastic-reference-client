package nxt.user;

import nxt.http.FakeServletRequest;

import org.json.simple.JSONStreamAware;


import java.io.IOException;

public final class GetNewData extends UserServlet.UserRequestHandler {

    static final GetNewData instance = new GetNewData();

    private GetNewData() {}

    @Override
    JSONStreamAware processRequest(FakeServletRequest req, User user) throws IOException {
        return null;
    }
}
