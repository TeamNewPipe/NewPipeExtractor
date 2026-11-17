package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Class representing YouTube audio demuxed streams available with an individual streaming URL.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class YoutubeUriAudioStream extends YoutubeBaseAudioDemuxedStream implements Stream {

    @Nonnull
    private final DeliverySource deliverySource;

    /**
     * Construct a new {@link YoutubeUriAudioStream} instance.
     *
     * @param id                              the ID of the stream, which should be of the form
     *                                        {@code InnerTube_client_name-Itag_ID}
     * @param name                            the displayed name of the stream in the audio track
     *                                        settings ({@code displayName} property)
     * @param locale                          a {@link Locale} built with the language code from
     *                                        the {@code xtags} Base64 encoded Protobuf property or
     *                                        {@code null} if not present such as on videos with
     *                                        one audio track
     * @param isAutomaticallyServiceGenerated {@link Boolean#TRUE} if voice boosted ({@code isVb}
     *                                        returned and set to {@code true}) or automatically
     *                                        dubbed ({@code isAutoDubbed} returned and set to
     *                                        {@code true}); {@link Boolean#FALSE} otherwise
     * @param mediaFormat                     an appropriate {@link AudioMediaFormat} value based
     *                                        on the {@code mimeType} property returned
     * @param averageBitrate                  the average bitrate
     * @param sampleRate                      the sample rate
     * @param channelsCount                   the channels count
     * @param trackType                       the track type deducted from the {@code xtags} Base64
     *                                        encoded Protobuf property or
     *                                        {@link AudioTrackType#ORIGINAL} if not present
     * @param isUsingDrc                      whether the stream uses DRC (called Stable volume in
     *                                        the YouTube UI), used when {@code isDrc} is returned
     *                                        and set to {@code true}
     * @param codec                           the codec string returned in the {@code mimeType}
     *                                        property
     * @param itag                            an {@link Itag} instance for this stream
     * @param loudnessDb                      the {@code loudnessDb} value for this stream,
     *                                        {@code null} for non-regular videos
     * @param trackAbsoluteLoudnessLkfs       the {@code trackAbsoluteLoudnessLkfs} value for this
     *                                        stream, {@code null} for non-regular videos
     * @param deliverySource                  the {@link DeliverySource} of the stream, which
     *                                        should contain the streaming URL in the {@code url}
     *                                        property and fallback URLs, constructed from the
     *                                        second part of the {@code mn} query parameter from
     *                                        this URL and with the
     *                                        {@code redirector.googlevideo.com} domain
     */
    public YoutubeUriAudioStream(@Nonnull final String id,
                                 @Nullable final String name,
                                 @Nullable final Locale locale,
                                 @Nonnull final Boolean isAutomaticallyServiceGenerated,
                                 @Nonnull final AudioMediaFormat mediaFormat,
                                 @Nullable final Integer averageBitrate,
                                 @Nullable final Integer sampleRate,
                                 @Nullable final Integer channelsCount,
                                 @Nonnull final AudioTrackType trackType,
                                 @Nonnull final Boolean isUsingDrc,
                                 @Nullable final String codec,
                                 @Nonnull final Itag itag,
                                 @Nullable final Double loudnessDb,
                                 @Nullable final Double trackAbsoluteLoudnessLkfs,
                                 @Nonnull final DeliverySource deliverySource) {
        super(id, name, locale, isAutomaticallyServiceGenerated, mediaFormat, averageBitrate,
                sampleRate, channelsCount, trackType, isUsingDrc, codec, itag, loudnessDb,
                trackAbsoluteLoudnessLkfs);
        this.deliverySource = deliverySource;
    }

    @Nonnull
    @Override
    public DeliverySource getDeliverySource() {
        return deliverySource;
    }

    @Nonnull
    @Override
    public StreamingProtocol getStreamingProtocol() {
        return StreamingProtocol.DASH;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof YoutubeUriAudioStream that) || !super.equals(o)) {
            return false;
        }

        return deliverySource.equals(that.deliverySource);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + deliverySource.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "YoutubeUriAudioStream{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", locale=" + getLocale()
                + ", isAutomaticallyServiceGenerated=" + isAutomaticallyServiceGenerated()
                + ", mediaFormat=" + getMediaFormat()
                + ", averageBitrate=" + getAverageBitrate()
                + ", sampleRate=" + getSampleRate()
                + ", channelsCount=" + getChannelsCount()
                + ", trackType=" + getTrackType()
                + ", isUsingDrc=" + isUsingDrc()
                + ", codec=" + getCodec()
                + ", itag=" + getItag()
                + ", loudnessDb=" + getLoudnessDb()
                + ", trackAbsoluteLoudnessLkfs=" + getTrackAbsoluteLoudnessLkfs()
                + ", deliverySource=" + deliverySource
                + "}";
    }
}
