package org.apache.coyote;

import org.apache.coyote.http11.httprequest.HttpRequest;
import org.apache.coyote.http11.httpresponse.HttpResponse;

public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        if (request.isPost()) {
            doPost(request, response);
            return;
        }
        doGet(request, response);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception { /* NOOP */ }
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception { /* NOOP */ }
}
