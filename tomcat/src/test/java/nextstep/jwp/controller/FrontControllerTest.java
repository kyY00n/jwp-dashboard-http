package nextstep.jwp.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.HttpPath;
import org.apache.coyote.http11.HttpStatus;
import org.apache.coyote.http11.ResourceResponseBuilder;
import org.apache.coyote.http11.ResponseBody;
import org.apache.coyote.http11.httprequest.HttpRequest;
import org.apache.coyote.http11.httpresponse.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.apache.coyote.http11.HttpMethod.GET;
import static org.apache.coyote.http11.HttpVersion.HTTP_1_1;
import static org.assertj.core.api.Assertions.assertThat;

class FrontControllerTest {

    private FrontController frontController = new FrontController();

    @Test
    void base() {
        //given
        final var request = new HttpRequest(
                GET,
                new HttpPath("/"),
                HTTP_1_1,
                Map.of(),
                Map.of(),
                HttpRequest.EMPTY,
                Map.of()
        );

        final var response = HttpResponse.prepareFrom(request);

        //when
        frontController.service(request, response);

        //then
        final var expected = HttpResponse.builder()
                .httpVersion(HTTP_1_1)
                .httpStatus(HttpStatus.OK)
                .body(ResponseBody.from("Hello world!"))
                .build();

        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/index.html", "/login.html", "/register.html"})
    void responseIndex(String uri) throws Exception {
        //given
        final var request = new HttpRequest(
                GET,
                new HttpPath(uri),
                HTTP_1_1,
                Map.of(),
                Map.of(),
                "",
                Map.of()
        );
        final var response = HttpResponse.prepareFrom(request);

        //when
        frontController.service(request, response);

        //then
        final var body = buildMessageBody(uri);
        final var expected = HttpResponse.builder()
                .httpVersion(HTTP_1_1)
                .httpStatus(HttpStatus.OK)
                .body(ResponseBody.of(new ContentType("text/html"), body))
                .build();
        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expected);
    }
    private String buildMessageBody(String staticFileUri) throws Exception {
        final var resource = getClass().getClassLoader().getResource("static" + staticFileUri);
        return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
    }

    @Test
    void notFoundResource() {
        //given
        final var request = new HttpRequest(
                GET,
                new HttpPath("/notfound"),
                HTTP_1_1,
                Map.of(),
                Map.of(),
                HttpRequest.EMPTY,
                Map.of()
        );
        final var response = HttpResponse.prepareFrom(request);

        //when
        frontController.service(request, response);

        //then
        final var responseBody = ResourceResponseBuilder.build(getClass().getClassLoader().getResource("static/404.html"));
        final var expected = HttpResponse.builder()
                .httpVersion(HTTP_1_1)
                .httpStatus(HttpStatus.NOT_FOUND)
                .body(responseBody)
                .build();

        assertThat(response).usingRecursiveComparison()
                .isEqualTo(expected);
    }

}
