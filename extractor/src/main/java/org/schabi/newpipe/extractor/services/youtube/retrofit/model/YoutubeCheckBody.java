package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

public class YoutubeCheckBody {
    private final YoutubeContext context;

    public YoutubeCheckBody() {
        this(new YoutubeContext());
    }

    protected YoutubeCheckBody(final YoutubeContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "YoutubeCheckBody{context=" + context + '}';
    }
}
