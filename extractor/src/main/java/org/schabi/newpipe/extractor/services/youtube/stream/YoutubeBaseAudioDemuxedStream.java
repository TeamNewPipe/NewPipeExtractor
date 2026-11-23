package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.impl.base.BaseAudioStreamImpl;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;

/**
 * Base class for YouTube audio demuxed streams.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public class YoutubeBaseAudioDemuxedStream extends BaseAudioStreamImpl
        implements YoutubeBaseDemuxedStream {

    @Nonnull
    private final Itag itag;

    @Nullable
    private final Double loudnessDb;

    @Nullable
    private final Double trackAbsoluteLoudnessLkfs;

    /**
     * Construct a new {@link YoutubeBaseAudioDemuxedStream} instance.
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
     * @param isUsingDrc                      whether the stream uses DRC (called stable volume in
     *                                        the YouTube UI), used when {@code isDrc} is returned
     *                                        and set to {@code true}
     * @param codec                           the codec string returned in the {@code mimeType}
     *                                        property
     * @param itag                            an {@link Itag} instance for this stream
     * @param loudnessDb                      the {@code loudnessDb} value for this stream,
     *                                        {@code null} for non-regular videos
     * @param trackAbsoluteLoudnessLkfs       the {@code trackAbsoluteLoudnessLkfs} value for this
     *                                        stream, {@code null} for non-regular videos
     */
    public YoutubeBaseAudioDemuxedStream(@Nonnull final String id,
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
                                         @Nullable final Double trackAbsoluteLoudnessLkfs) {
        super(id, name, locale, isAutomaticallyServiceGenerated, mediaFormat, averageBitrate,
                sampleRate, channelsCount, trackType, isUsingDrc, codec);
        this.itag = itag;
        this.loudnessDb = loudnessDb;
        this.trackAbsoluteLoudnessLkfs = trackAbsoluteLoudnessLkfs;
    }

    @Nonnull
    @Override
    public Itag getItag() {
        return itag;
    }

    /**
     * Get the {@code loudnessDb} value for this stream, as returned by YouTube.
     *
     * <p>
     * It is {@code null} for non-regular videos.
     * </p>
     *
     * @return the {@code loudnessDb} value for this stream or {@code null}
     */
    @Nullable
    public Double getLoudnessDb() {
        return loudnessDb;
    }

    /**
     * Get the {@code trackAbsoluteLoudnessLkfs} value for this stream, as returned by YouTube.
     *
     * <p>
     * It is {@code null} for non-regular videos.
     * </p>
     *
     * @return the {@code trackAbsoluteLoudnessLkfs} value for this stream or {@code null}
     */
    @Nullable
    public Double getTrackAbsoluteLoudnessLkfs() {
        return trackAbsoluteLoudnessLkfs;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof YoutubeBaseAudioDemuxedStream that) || !super.equals(o)) {
            return false;
        }

        return itag.equals(that.itag)
                && Objects.equals(loudnessDb, that.loudnessDb)
                && Objects.equals(trackAbsoluteLoudnessLkfs, that.trackAbsoluteLoudnessLkfs);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + itag.hashCode();
        result = 31 * result + Objects.hashCode(loudnessDb);
        result = 31 * result + Objects.hashCode(trackAbsoluteLoudnessLkfs);
        return result;
    }

    @Override
    public String toString() {
        return "YoutubeBaseAudioStream{"
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
                + ", itag=" + itag
                + ", loudnessDb=" + loudnessDb
                + ", trackAbsoluteLoudnessLkfs=" + trackAbsoluteLoudnessLkfs
                + "}";
    }
}
