package org.schabi.newpipe.extractor.services.youtube.retrofit.model;

import org.schabi.newpipe.extractor.services.youtube.retrofit.service.YoutubeRetrofitService;

public class ValidityCheckBody {
    private final Context context = new Context();
    private final User user = new User();
    private final boolean fetchLiveState = false;

    public Context getContext() {
        return context;
    }

    public User getUser() {
        return user;
    }

    public boolean isFetchLiveState() {
        return fetchLiveState;
    }

    @Override
    public String toString() {
        return "ValidityCheckBody{" +
                "context=" + context +
                ", user=" + user +
                ", fetchLiveState=" + fetchLiveState +
                '}';
    }

    public static class Context {
        private final Client client = new Client();

        public Client getClient() {
            return client;
        }

        @Override
        public String toString() {
            return "Context{" +
                    "client=" + client +
                    '}';
        }
    }

    public static class Client {
        private final String hl = "en-GB";
        private final String gl = "GB";
        private final String clientName = "WEB";
        private final String clientVersion = YoutubeRetrofitService.HARDCODED_CLIENT_VERSION;

        public String getHl() {
            return hl;
        }

        public String getGl() {
            return gl;
        }

        public String getClientName() {
            return clientName;
        }

        public String getClientVersion() {
            return clientVersion;
        }

        @Override
        public String toString() {
            return "Client{" +
                    "hl='" + hl + '\'' +
                    ", gl='" + gl + '\'' +
                    ", clientName='" + clientName + '\'' +
                    ", clientVersion='" + clientVersion + '\'' +
                    '}';
        }
    }

    public static class User {
        private final boolean lockedSafetyMode = false;

        public boolean isLockedSafetyMode() {
            return lockedSafetyMode;
        }

        @Override
        public String toString() {
            return "User{" +
                    "lockedSafetyMode=" + lockedSafetyMode +
                    '}';
        }
    }
}
