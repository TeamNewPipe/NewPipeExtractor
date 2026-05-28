package org.schabi.newpipe.extractor.stream.impl;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.interfaces.ManifestStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An {@link ManifestStream} implementation supporting URIs.
 */
public class UriManifestStream implements ManifestStream {

    @Nullable
    private final String id;
    @Nullable
    private final String name;
    @Nonnull
    private final DeliverySource deliverySource;
    @Nonnull
    private final StreamingProtocol streamingProtocol;

    public UriManifestStream(@Nullable final String id,
                             @Nullable final String name,
                             @Nonnull final DeliverySource deliverySource,
                             @Nonnull final StreamingProtocol streamingProtocol) {
        this.id = id;
        this.name = name;
        this.deliverySource = deliverySource;
        this.streamingProtocol = streamingProtocol;
    }

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public DeliverySource getDeliverySource() {
        return deliverySource;
    }

    @Nonnull
    @Override
    public StreamingProtocol getStreamingProtocol() {
        return streamingProtocol;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UriManifestStream that)) {
            return false;
        }

        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && deliverySource.equals(that.deliverySource)
                && streamingProtocol == that.streamingProtocol;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + deliverySource.hashCode();
        result = 31 * result + streamingProtocol.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UriManifestStream{"
                + "id=" + id
                + ", name=" + name
                + ", deliverySource=" + deliverySource
                + ", streamingProtocol=" + streamingProtocol
                + "}";
    }
}
