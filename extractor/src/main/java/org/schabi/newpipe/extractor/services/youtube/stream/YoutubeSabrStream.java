package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.impl.UriManifestStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseMuxedStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseSubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * Class for YouTube SABR streams, the custom delivery protocol of YouTube for playback.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class YoutubeSabrStream extends UriManifestStream implements BaseMuxedStream {

    @Nonnull
    private final List<YoutubeBaseAudioDemuxedStream> audioStreams;
    @Nonnull
    private final List<YoutubeBaseVideoDemuxedStream> videoStreams;
    @Nonnull
    private final List<YoutubeBaseLiveSubtitlesDemuxedStream> liveSubtitlesStreams;
    @Nonnull
    private final SabrData sabrData;
    @Nullable
    private final String poToken;

    /**
     * Construct a new {@link YoutubeSabrStream} instance.
     *
     * @param id                   the ID of the stream, which should be of the form
     *                             {@code InnerTube_client_name-SABR}
     * @param audioStreams         supported demuxed audio streams by the client this SABR stream
     *                             comes from, they shouldn't be empty in order to get media data
     * @param videoStreams         supported demuxed video streams by the client this SABR stream
     *                             comes from, which may be empty
     * @param liveSubtitlesStreams supported demuxed subtitles streams by the client this SABR
     *                             stream comes from, which may be empty
     * @param sabrData             {@link SabrData} for this stream
     * @param poToken              the poToken of the stream, which shouldn't be added to URLs, as
     *                             it is added to the request body of SABR requests
     * @param deliverySource       the {@link DeliverySource} of the stream, which should contain
     *                             the streaming URL in the {@code sabrStreamingUrl} property and
     *                             fallback URLs, constructed from the second part of the
     *                             {@code mn} query parameter from this URL and with the
     *                             {@code redirector.googlevideo.com} domain
     */
    public YoutubeSabrStream(
            @Nonnull final String id,
            @Nonnull final List<YoutubeBaseAudioDemuxedStream> audioStreams,
            @Nonnull final List<YoutubeBaseVideoDemuxedStream> videoStreams,
            @Nonnull final List<YoutubeBaseLiveSubtitlesDemuxedStream> liveSubtitlesStreams,
            @Nonnull final SabrData sabrData,
            @Nullable final String poToken,
            @Nonnull final DeliverySource deliverySource) {
        super(id, null, deliverySource, StreamingProtocol.CUSTOM);
        this.audioStreams = audioStreams;
        this.videoStreams = videoStreams;
        this.liveSubtitlesStreams = liveSubtitlesStreams;
        this.sabrData = sabrData;
        this.poToken = poToken;
    }

    @Nonnull
    @Override
    public List<? extends BaseAudioStream> getDemuxedAudioStreams() {
        return audioStreams;
    }

    @Nonnull
    @Override
    public List<? extends BaseVideoStream> getDemuxedVideoStreams() {
        return videoStreams;
    }

    @Nonnull
    @Override
    public List<? extends BaseSubtitlesStream> getDemuxedSubtitlesStreams() {
        return liveSubtitlesStreams;
    }

    @Nullable
    @Override
    public List<? extends BaseMuxedStream> getMuxedStreams() {
        // There is no muxed stream in SABR streams
        return null;
    }

    @Nullable
    @Override
    public Integer getAverageBitrate() {
        return null;
    }

    @Nullable
    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof YoutubeSabrStream that) || !super.equals(o)) {
            return false;
        }

        return audioStreams.equals(that.audioStreams)
                && videoStreams.equals(that.videoStreams)
                && liveSubtitlesStreams.equals(that.liveSubtitlesStreams)
                && sabrData.equals(that.sabrData)
                && Objects.equals(poToken, that.poToken);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + audioStreams.hashCode();
        result = 31 * result + videoStreams.hashCode();
        result = 31 * result + liveSubtitlesStreams.hashCode();
        result = 31 * result + sabrData.hashCode();
        result = 31 * result + Objects.hashCode(poToken);
        return result;
    }

    @Override
    public String toString() {
        return "YoutubeSabrStream{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", deliverySource=" + getDeliverySource()
                + ", streamingProtcol=" + getStreamingProtocol()
                + ", demuxedAudioStreams=" + audioStreams
                + ", demuxedVideoStreams=" + videoStreams
                + ", demuxedLiveSubtitlesStreams=" + liveSubtitlesStreams
                + ", muxedStreams=" + getMuxedStreams()
                + ", getAverageBitrate=" + getAverageBitrate()
                + ", sabrData=" + sabrData
                + ", poToken=" + poToken
                + "}";
    }
}
