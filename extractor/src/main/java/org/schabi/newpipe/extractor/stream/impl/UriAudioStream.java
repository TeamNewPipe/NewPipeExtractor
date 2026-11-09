package org.schabi.newpipe.extractor.stream.impl;

import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
import org.schabi.newpipe.extractor.stream.impl.base.BaseAudioStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.AudioStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * An {@link AudioStream} implementation supporting URIs.
 */
public class UriAudioStream extends BaseAudioStreamImpl implements AudioStream {

    @Nonnull
    private final UriDeliverySource uriDeliverySource;
    @Nonnull
    private final StreamingProtocol streamingProtocol;

    public UriAudioStream(@Nullable final String id,
                          @Nullable final String name,
                          @Nullable final Locale locale,
                          @Nullable final Boolean isAutomaticallyServiceGenerated,
                          @Nonnull final AudioMediaFormat audioMediaFormat,
                          @Nullable final Integer averageBitrate,
                          @Nullable final Integer sampleRate,
                          @Nullable final Integer channelsCount,
                          @Nullable final AudioTrackType trackType,
                          @Nullable final Boolean isUsingDrc,
                          @Nullable final String codec,
                          @Nonnull final UriDeliverySource uriDeliverySource,
                          @Nonnull final StreamingProtocol streamingProtocol) {
        super(id, name, locale, isAutomaticallyServiceGenerated, audioMediaFormat, averageBitrate,
                channelsCount, sampleRate, trackType, isUsingDrc, codec);
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
        if (!(o instanceof UriAudioStream that) || !super.equals(o)) {
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
        return "UriAudioStream{"
                + "id=" + getId()
                + ", name='" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + getMediaFormat()
                + ", averageBitrate=" + getAverageBitrate()
                + ", sampleRate=" + getSampleRate()
                + ", channelsCount=" + getChannelsCount()
                + ", trackType=" + getTrackType()
                + ", isUsingDrc=" + isUsingDrc()
                + ", codec=" + getCodec()
                + ", uriDeliverySource=" + uriDeliverySource
                + ", streamingProtocol=" + streamingProtocol
                + "}";
    }
}
