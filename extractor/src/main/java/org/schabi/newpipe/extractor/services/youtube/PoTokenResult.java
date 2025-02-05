package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The result of a supported/successful {@code poToken} extraction request by a
 * {@link PoTokenProvider}.
 */
public final class PoTokenResult {

    /**
     * The visitor data associated with a {@code poToken}.
     */
    @Nonnull
    public final String visitorData;

    /**
     * The {@code poToken} of a player request, a Protobuf object encoded as a base 64 string.
     */
    @Nonnull
    public final String playerRequestPoToken;

    /**
     * The {@code poToken} to be appended to streaming URLs, a Protobuf object encoded as a base
     * 64 string.
     *
     * <p>
     * It may be required on some clients such as HTML5 ones and may also differ from the player
     * request {@code poToken}.
     * </p>
     */
    @Nullable
    public final String streamingDataPoToken;

    /**
     * Construct a {@link PoTokenResult} instance.
     *
     * @param visitorData          see {@link #visitorData}
     * @param playerRequestPoToken see {@link #playerRequestPoToken}
     * @param streamingDataPoToken see {@link #streamingDataPoToken}
     * @throws NullPointerException if a non-null parameter is null
     */
    public PoTokenResult(@Nonnull final String visitorData,
                         @Nonnull final String playerRequestPoToken,
                         @Nullable final String streamingDataPoToken) {
        this.visitorData = Objects.requireNonNull(visitorData);
        this.playerRequestPoToken = Objects.requireNonNull(playerRequestPoToken);
        this.streamingDataPoToken = streamingDataPoToken;
    }
}
