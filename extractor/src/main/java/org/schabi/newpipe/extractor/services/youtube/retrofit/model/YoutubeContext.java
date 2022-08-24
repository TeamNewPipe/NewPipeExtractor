package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

public class YoutubeContext {
    private final YoutubeClient client;
    private final User user = new User();
    private final boolean fetchLiveState = true;

    public YoutubeContext() {
        this(new YoutubeClient());
    }

    protected YoutubeContext(final YoutubeClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "YoutubeContext{client=" + client
                + ", user=" + user
                + ", fetchLiveState=" + fetchLiveState + '}';
    }

    public static class User {
        private final boolean lockedSafetyMode = false;

        @Override
        public String toString() {
            return "User{lockedSafetyMode=" + lockedSafetyMode + '}';
        }
    }
}
