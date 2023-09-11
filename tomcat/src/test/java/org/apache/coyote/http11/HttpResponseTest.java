package org.apache.coyote.http11;

import java.util.Map;
import org.apache.coyote.ResponseHeader;
import org.apache.coyote.http11.httprequest.HttpRequest;
import org.apache.coyote.http11.httpresponse.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.apache.coyote.http11.HttpMethod.GET;
import static org.apache.coyote.http11.HttpVersion.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class HttpResponseTest {

    @Test
    void prepareResponse() {
        //given
        final var request = HttpRequest.builder()
                .version(HTTP_1_1)
                .method(GET)
                .path(new HttpPath("/index.html"))
                .build();

        //when
        HttpResponse response = HttpResponse.prepareFrom(request);

        //then
        final var expected = new HttpResponse(HTTP_1_1);


        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void addHeader() {
        //given
        final var response = new HttpResponse(HTTP_1_1);
        response.setStatus(HttpStatus.OK);

        //when
        response.addHeader("key", "value");

        //then
        String responseString = response.buildResponse();
        assertThat(responseString).contains("key: value");
    }

    @Test
    void addResponseHeader() {
        //given
        Headers headers = new Headers();
        final var response = new HttpResponse(HTTP_1_1, null, null, headers);
        final var responseHeader = ResponseHeader.CONTENT_LENGTH;

        //when
        response.addHeader(responseHeader, "value");

        //then
        final var actual = headers.get(responseHeader.getName());
        assertThat(actual).isEqualTo("value");

    }

    @ParameterizedTest
    @EnumSource(HttpStatus.class)
    void setStatus(HttpStatus httpStatus) {
        //given
        final var response = new HttpResponse(HTTP_1_1);

        //when
        response.setStatus(httpStatus);

        //then
        assertThat(response).extracting("httpStatus").isEqualTo(httpStatus);
    }

    @Test
    void setBody() {
        //given
        final var response = new HttpResponse(HTTP_1_1);
        response.setStatus(HttpStatus.OK);
        final var body = ResponseBody.from("body");

        //when
        response.setBody(body);

        //then
        assertThat(response).extracting("body").isEqualTo(body);
    }

    @Test
    void setHeaderWhenSetBody() {
        //given
        final var headers = new Headers();
        final var response = new HttpResponse(HTTP_1_1, null, null, headers);
        response.setStatus(HttpStatus.OK);
        final var body = ResponseBody.from("body");

        //when
        response.setBody(body);

        //then
        assertAll(
                () -> assertThat(headers.get(ResponseHeader.CONTENT_TYPE.getName()))
                        .isEqualTo("text/plain;charset=utf-8"),
                () -> assertThat(headers.get(ResponseHeader.CONTENT_LENGTH.getName()))
                        .isEqualTo("4")
        );

    }

    @Test
    void setCookie() {
        //given
        final var response = new HttpResponse(HTTP_1_1);
        final var cookie = new HttpCookie(Map.of("key", "value", "key2", "value2"));
        response.setStatus(HttpStatus.OK);

        //when
        response.setCookie(cookie);

        //then
        String responseString = response.buildResponse();
        assertThat(responseString)
                .containsAnyOf("Set-Cookie: key=value; key2=value2", "Set-Cookie: key2=value2; key=value");
    }

    @Test
    void sendRedirect() {
        //given
        final var response = new HttpResponse(HTTP_1_1);
        final var location = "/index.html";
        response.setStatus(HttpStatus.OK);

        //when
        response.sendRedirect(location);

        //then
        String responseString = response.buildResponse();
        assertThat(responseString).contains("Location: " + location);
    }



}

