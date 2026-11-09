package org.schabi.newpipe.extractor.services.youtube.stream;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Required SABR specific data to play SABR streams, which is not changing between requests and
 * formats.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class SabrData implements Serializable {

    @Nonnull
    private final String ustreamerConfig;
    private final int clientId;
    @Nonnull
    private final String clientVersion;

    /**
     * Construct a new {@link SabrData} instance.
     *
     * @param ustreamerConfig the {@code ustreamerConfig} property in the player response (in the
     *                        {@code mediaUstreamerRequestConfig} object itself in the
     *                        {@code mediaCommonConfig} object itself in the {@code playerConfig}
     *                        one)
     * @param clientId        the client ID of the client this SABR stream comes from
     * @param clientVersion   the client version of the client this SABR stream comes from
     */
    public SabrData(@Nonnull final String ustreamerConfig,
                    final int clientId,
                    @Nonnull final String clientVersion) {
        this.ustreamerConfig = ustreamerConfig;
        this.clientId = clientId;
        this.clientVersion = clientVersion;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof SabrData that)) {
            return false;
        }

        return clientId == that.clientId
                && ustreamerConfig.equals(that.ustreamerConfig)
                && clientVersion.equals(that.clientVersion);
    }

    @Override
    public int hashCode() {
        int result = ustreamerConfig.hashCode();
        result = 31 * result + clientId;
        result = 31 * result + clientVersion.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SabrData{"
                + "ustreamerConfig=" + ustreamerConfig
                + ", clientId=" + clientId
                + ", clientVersion=" + clientVersion
                + "}";
    }
}
