package org.schabi.newpipe.extractor.stream.impl;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
import org.schabi.newpipe.extractor.stream.impl.base.BaseSubtitlesStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

/**
 * An {@link SubtitlesStream} implementation supporting URIs.
 */
public class UriSubtitlesStream extends BaseSubtitlesStreamImpl implements SubtitlesStream {

    @Nonnull
    private final UriDeliverySource uriDeliverySource;
    @Nonnull
    private final StreamingProtocol streamingProtocol;

    public UriSubtitlesStream(@Nullable final String id,
                              @Nullable final String name,
                              @Nullable final Locale locale,
                              @Nullable final Boolean isAutomaticallyServiceGenerated,
                              @Nonnull final SubtitlesMediaFormat mediaFormat,
                              @Nonnull final List<BaseSubtitlesStream> translations,
                              @Nonnull final UriDeliverySource uriDeliverySource,
                              @Nonnull final StreamingProtocol streamingProtocol) {
        super(id, name, locale, isAutomaticallyServiceGenerated, mediaFormat, translations);
        this.uriDeliverySource = uriDeliverySource;
        this.streamingProtocol = streamingProtocol;
    }

    @Nonnull
    @Override
    public DeliverySource getDeliverySource() {
        return uriDeliverySource;
    }

    @Nonnull
    @Override
    public StreamingProtocol getStreamingProtocol() {
        return streamingProtocol;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UriSubtitlesStream that) || !super.equals(o)) {
            return false;
        }

        return uriDeliverySource.equals(that.uriDeliverySource)
                && streamingProtocol == that.streamingProtocol;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uriDeliverySource.hashCode();
        result = 31 * result + streamingProtocol.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UriSubtitlesStream{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + getMediaFormat()
                + ", uriDeliverySource=" + uriDeliverySource
                + ", streamingProtocol=" + streamingProtocol
                + "}";
    }
}
