package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.HttpResponseException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

class YoutubeJsonResponseTest {
    private static final String URL = "https://www.youtube.com/youtubei/v1/browse";
    private static final String JSON_BODY =
            "{\"responseContext\":{\"visitorData\":\"visitor\"},\"contents\":[]}";
    private static final String HTML_BODY =
            "<html><body>The server is temporarily unavailable.</body></html>";

    static Stream<Arguments> unsuccessfulResponses() {
        return Stream.of(
                Arguments.of(302, "Found", "application/json", JSON_BODY),
                Arguments.of(400, "Bad Request", "application/json", "{}"),
                Arguments.of(403, "Forbidden", "application/json",
                        "{\"error\":{\"code\":403,\"message\":\"The request was forbidden\"}}"),
                Arguments.of(429, "Too Many Requests", "text/html", ""),
                Arguments.of(500, "Internal Server Error", "text/html", HTML_BODY),
                Arguments.of(503, "Service Unavailable", "text/html", HTML_BODY));
    }

    @ParameterizedTest
    @MethodSource("unsuccessfulResponses")
    void reportsHttpFailureBeforeInspectingBody(final int code, final String message,
                                               final String contentType, final String body) {
        final Response response = response(code, message, contentType, body, URL);
        final HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> getValidJsonResponseBody(response));
        assertTrue(exception.getMessage().contains(code + " - " + message));
        assertTrue(exception.getMessage().contains(URL));
    }

    @Test
    void preservesNotFoundError() {
        final Response response = response(404, "Not Found", "text/html", "", URL);
        final ContentNotAvailableException exception = assertThrows(
                ContentNotAvailableException.class, () -> getValidJsonResponseBody(response));
        assertTrue(exception.getMessage().contains("404 Not Found"));
    }

    @Test
    void returnsSuccessfulJsonResponse() throws Exception {
        assertEquals(JSON_BODY, getValidJsonResponseBody(
                response(200, "OK", "application/json", JSON_BODY, URL)));
    }

    @Test
    void stillRejectsShortSuccessfulResponse() {
        final ParsingException exception = assertThrows(ParsingException.class,
                () -> getValidJsonResponseBody(
                        response(200, "OK", "application/json", "{}", URL)));
        assertEquals("JSON response is too short", exception.getMessage());
    }

    @Test
    void stillRejectsHtmlSuccessfulResponse() {
        final ParsingException exception = assertThrows(ParsingException.class,
                () -> getValidJsonResponseBody(
                        response(200, "OK", "text/html", HTML_BODY, URL)));
        assertTrue(exception.getMessage().contains("Got HTML document, expected JSON response"));
    }

    @Test
    void preservesRedirectToYoutubeErrorPage() {
        assertThrows(ContentNotAvailableException.class, () -> getValidJsonResponseBody(
                response(200, "OK", "text/html", HTML_BODY, "https://www.youtube.com/oops")));
    }

    private static Response response(final int code, final String message,
                                     final String contentType, final String body,
                                     final String url) {
        return new Response(code, message, Map.of("Content-Type", List.of(contentType)), body, url);
    }
}
