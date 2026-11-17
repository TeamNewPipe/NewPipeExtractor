package org.schabi.newpipe.extractor.stream.impl;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriDeliverySource;
import org.schabi.newpipe.extractor.stream.impl.base.BaseMuxedStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.MuxedStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * An {@link MuxedStream} implementation supporting URIs.
 */
public class UriMuxedStream extends BaseMuxedStreamImpl implements MuxedStream {

    @Nonnull
    private final UriDeliverySource uriDeliverySource;
    @Nonnull
    private final StreamingProtocol streamingProtocol;

    public UriMuxedStream(@Nullable final String id,
                          @Nullable final String name,
                          @Nullable final List<BaseAudioStream> demuxedAudioStreams,
                          @Nullable final List<BaseVideoStream> demuxedVideoStreams,
                          @Nullable final List<BaseSubtitlesStream> demuxedSubtitlesStreams,
                          @Nullable final List<BaseMuxedStream> muxedStreams,
                          @Nullable final Integer averageBitrate,
                          @Nonnull final UriDeliverySource uriDeliverySource,
                          @Nonnull final StreamingProtocol streamingProtocol) {
        super(id, name, demuxedAudioStreams, demuxedVideoStreams, demuxedSubtitlesStreams,
                muxedStreams, averageBitrate);
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
        if (!(o instanceof UriMuxedStream that) || !super.equals(o)) {
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
        return "UriMuxedStream{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", demuxedAudioStreams=" + getDemuxedAudioStreams()
                + ", demuxedVideoStreams=" + getDemuxedVideoStreams()
                + ", demuxedSubtitlesStreams=" + getDemuxedSubtitlesStreams()
                + ", muxedStreams=" + getMuxedStreams()
                + "; averageBitrate=" + getAverageBitrate()
                + ", uriDeliverySource=" + uriDeliverySource
                + ", streamingProtocol=" + streamingProtocol
                + "}";
    }
}
