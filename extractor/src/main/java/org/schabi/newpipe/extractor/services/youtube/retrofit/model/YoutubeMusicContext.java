package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

import java.util.Collections;
import java.util.List;

public class YoutubeMusicContext extends YoutubeContext {
    private final Object capabilities = new Object();
    private final Request request = new Request();
    private final Object activePlayers = new Object();
    private final YoutubeCheckBody.User user = new YoutubeCheckBody.User();

    public YoutubeMusicContext() {
        super(new YoutubeMusicClient());
    }

    @Override
    public String toString() {
        return "YoutubeMusicContext{capabilities=" + capabilities
                + ", request=" + request
                + ", activePlayers=" + activePlayers
                + ", user=" + user
                + "} " + super.toString();
    }

    public static class Request {
        private final List<String> internalExperimentalFlags = Collections.emptyList();
        private final Object sessionIndex = new Object();

        @Override
        public String toString() {
            return "Request{internalExperimentalFlags=" + internalExperimentalFlags
                    + ", sessionIndex=" + sessionIndex + '}';
        }
    }
}
