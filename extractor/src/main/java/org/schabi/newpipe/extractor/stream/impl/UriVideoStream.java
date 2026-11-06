package org.schabi.newpipe.extractor.stream.impl;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
import org.schabi.newpipe.extractor.stream.impl.base.BaseVideoStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.VideoStream;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * An {@link VideoStream} implementation supporting URIs.
 */
public class UriVideoStream extends BaseVideoStreamImpl implements VideoStream {

    @Nonnull
    private final UriDeliverySource uriDeliverySource;
    @Nonnull
    private final StreamingProtocol streamingProtocol;

    public UriVideoStream(@Nullable final String id,
                          @Nullable final String name,
                          @Nullable final Locale locale,
                          @Nullable final Boolean isAutomaticallyServiceGenerated,
                          @Nonnull final VideoMediaFormat mediaFormat,
                          @Nonnull final ProjectionType projectionType,
                          @Nullable final Integer averageBitrate,
                          @Nullable final Integer width,
                          @Nullable final Integer height,
                          @Nullable final Integer framerate,
                          @Nullable final String codec,
                          @Nullable final Boolean isHdr,
                          @Nonnull final UriDeliverySource uriDeliverySource,
                          @Nonnull final StreamingProtocol streamingProtocol) {
        super(id, name, locale, isAutomaticallyServiceGenerated, mediaFormat, projectionType,
                averageBitrate, width, height, framerate, codec, isHdr);
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
        if (!(o instanceof UriVideoStream that) || !super.equals(o)) {
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
        return "UriVideoStream{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + getMediaFormat()
                + ", projectionType=" + getProjectionType()
                + ", averageBitrate=" + getAverageBitrate()
                + ", width=" + getWidth()
                + ", height=" + getHeight()
                + ", framerate=" + getFramerate()
                + ", codec=" + getCodec()
                + ", isHdr=" + isHdr()
                + ", uriDeliverySource=" + uriDeliverySource
                + ", streamingProtocol=" + streamingProtocol
                + "}";
    }
}
