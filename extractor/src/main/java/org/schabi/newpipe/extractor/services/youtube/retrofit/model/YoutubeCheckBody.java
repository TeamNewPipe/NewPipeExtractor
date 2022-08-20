package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

public class YoutubeCheckBody {
    private final YoutubeContext context;
    private final User user = new User();
    private final boolean fetchLiveState = false;

    public YoutubeCheckBody() {
        this(new YoutubeContext());
    }

    protected YoutubeCheckBody(final YoutubeContext context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "YoutubeCheckBody{context=" + context
                + ", user=" + user
                + ", fetchLiveState=" + fetchLiveState + '}';
    }

    public static class User {
        private final boolean lockedSafetyMode = false;

        public boolean isLockedSafetyMode() {
            return lockedSafetyMode;
        }

        @Override
        public String toString() {
            return "User{lockedSafetyMode=" + lockedSafetyMode + '}';
        }
    }
}
