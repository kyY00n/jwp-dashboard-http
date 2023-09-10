package org.apache.coyote.http11.httpresponse;

import org.apache.coyote.http11.Headers;
import org.apache.coyote.http11.HttpCookie;
import org.apache.coyote.http11.HttpStatus;
import org.apache.coyote.http11.HttpVersion;
import org.apache.coyote.http11.ResourceResponseBuilder;
import org.apache.coyote.http11.ResponseBody;
import org.apache.coyote.http11.httprequest.HttpRequest;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.apache.coyote.http11.HttpVersion.HTTP_1_1;

public class HttpResponse {

    public static final String CRLF = "\r\n";
    public static final String EMPTY = "";
    public static final String BLANK = " ";

    private final HttpVersion httpVersion;
    private HttpStatus httpStatus;
    private ResponseBody body;
    private final Headers headers;

    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    public HttpResponse(final HttpVersion httpVersion) {
        this.httpVersion = requireNonNullElse(httpVersion, HTTP_1_1);
        this.headers = new Headers();
    }

    public HttpResponse(final HttpVersion httpVersion, final HttpStatus httpStatus,
                        final ResponseBody body,
                        final Headers headers) {
        this.httpVersion = httpVersion;
        this.httpStatus = httpStatus;
        this.body = body;
        this.headers = headers;
    }

    public static HttpResponse prepareFrom(final HttpRequest request) {
        return new HttpResponse(request.getVersion());
    }


    public String buildResponse() {
        httpStatus = requireNonNull(httpStatus);
        body = requireNonNullElse(body, ResponseBody.EMPTY);
        StringBuilder stringBuilder = new StringBuilder();

        String startLine = buildStartLine();
        String joinedHeader = buildHeaders();
        String messageBody = buildMessageBody();

        return stringBuilder.append(startLine).append(CRLF)
                .append(joinedHeader).append(CRLF)
                .append(messageBody)
                .toString();
    }

    private String buildStartLine() {
        return httpVersion.getVersion() + BLANK + httpStatus + BLANK;
    }

    private String buildHeaders() {
        String joinedHeaders = headers.join();
        String bodyHeaders = buildBodyHeaders();
        if (!joinedHeaders.isEmpty() && !bodyHeaders.isEmpty()) {
            return joinedHeaders + CRLF + bodyHeaders;
        }
        return joinedHeaders + bodyHeaders;
    }

    private String buildBodyHeaders() {
        if (body.isEmpty()) {
            return EMPTY;
        }
        StringBuilder builder = new StringBuilder();
        return builder.append(body.getContentType())
                .append(CRLF)
                .append(body.getContentLength())
                .append(BLANK)
                .toString();
    }

    private String buildMessageBody() {
        if (body.isEmpty()) {
            return EMPTY;
        }
        return CRLF + body.getBody();
    }


    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    public void setStatus(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void setBody(final ResponseBody body) {
        this.body = body;
    }

    public void setBody(final String resourcePath) {
        final var resourceUrl = getClass().getClassLoader().getResource("static" + resourcePath);
        final var body = ResourceResponseBuilder.build(resourceUrl);
        this.body = body;
    }

    public void setCookie(final HttpCookie cookie) {
        headers.put("Set-Cookie", cookie.toString());
    }

    public void sendRedirect(final String location) {
        headers.put("Location", location);
    }

}
