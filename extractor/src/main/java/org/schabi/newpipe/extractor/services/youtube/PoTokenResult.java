package org.schabi.newpipe.extractor.services.youtube;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PoTokenResult {

    /**
     * The visitor data associated with a poToken.
     */
    public final String visitorData;

    /**
     * The poToken, a Protobuf object encoded as a base 64 string.
     */
    public final String poToken;

    public PoTokenResult(@Nonnull final String visitorData, @Nonnull final String poToken) {
        this.visitorData = Objects.requireNonNull(visitorData);
        this.poToken = Objects.requireNonNull(poToken);
    }
}
