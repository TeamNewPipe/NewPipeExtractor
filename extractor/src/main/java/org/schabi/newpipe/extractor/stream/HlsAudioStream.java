package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.exceptions.ExtractionException;

import java.io.IOException;

import javax.annotation.Nonnull;

public class HlsAudioStream extends AudioStream implements RefreshableStream {
    private final String apiStreamUrl;
    private final String playlistId;

    HlsAudioStream(final Builder builder) {
        super(builder);
        apiStreamUrl = builder.apiStreamUrl;
        playlistId = builder.playlistId;
    }

    @Nonnull
    public String fetchLatestUrl() throws IOException, ExtractionException {
        return SoundcloudHlsUtils.getStreamContentUrl(apiStreamUrl);
    }

    @Nonnull
    public String initialUrl() {
        return getContent();
    }

    @Override
    public String playlistId() {
        return playlistId;
    }

    @SuppressWarnings({"checkstyle:HiddenField", "UnusedReturnValue"})
    public static class Builder extends AudioStream.Builder {
        private String apiStreamUrl;
        private String playlistId;

        public Builder() {
            setDeliveryMethod(DeliveryMethod.HLS);
        }

        @Override
        @Nonnull
        public HlsAudioStream build() {
            validateBuild();
            return new HlsAudioStream(this);
        }

        public Builder setApiStreamUrl(@Nonnull final String apiStreamUrl) {
            this.apiStreamUrl = apiStreamUrl;
            return this;
        }

        public Builder setPlaylistId(@Nonnull final String playlistId) {
            this.playlistId = playlistId;
            return this;
        }
    }
}
