package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

public class YoutubeContext {
    private final YoutubeClient client;

    public YoutubeContext() {
        this(new YoutubeClient());
    }

    protected YoutubeContext(final YoutubeClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "YoutubeContext{client=" + client + '}';
    }
}
