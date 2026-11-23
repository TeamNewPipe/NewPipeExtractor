package org.schabi.newpipe.extractor.stream.interfaces.base;

import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base interface for audio streams.
 *
 * <p>
 * It does not represent individual audio streams to be directly used by clients, but the ones in
 * extractor sub-definitions and custom implementations. See
 * {@link org.schabi.newpipe.extractor.stream.interfaces.AudioStream AudioStream} for an interface
 * to be used directly.
 * </p>
 */
public interface BaseAudioStream extends BaseDemuxedStream {

    /**
     * Get the {@link AudioMediaFormat} of this audio stream.
     *
     * @return the {@link AudioMediaFormat} of this audio stream
     */
    @Nonnull
    AudioMediaFormat getMediaFormat();

    /**
     * Get the average bitrate of this audio stream if it is known or {@code null} otherwise.
     *
     * @return the average bitrate or {@code null} if unknown
     */
    @Nullable
    Integer getAverageBitrate();

    /**
     * Get the sample rate of this audio stream if it is known or {@code null} otherwise.
     *
     * @return the sample rate or {@code null} if unknown
     */
    @Nullable
    Integer getSampleRate();

    /**
     * Get the channels count of this audio stream if it is known or {@code null} otherwise.
     *
     * @return the channels count or {@code null} if unknown
     */
    @Nullable
    Integer getChannelsCount();

    /**
     * Get the {@link AudioTrackType} of this audio stream.
     *
     * @return the {@link AudioTrackType} or {@code null} if unknown
     */
    @Nullable
    AudioTrackType getTrackType();

    /**
     * Get whether this audio stream is using Dynamic Range Compression or {@code null} if not
     * known.
     *
     * @return whether the audio stream is using DRC or {@code null} if unknown
     * @see <a href="https://en.wikipedia.org/wiki/Dynamic_range_compression">
     * https://en.wikipedia.org/wiki/Dynamic_range_compression</a>
     */
    @Nullable
    Boolean isUsingDrc();

    /**
     * Get the codec this audio stream uses or {@code null} if not known.
     *
     * @return the codec this audio stream uses or {@code null} unknown
     */
    @Nullable
    String getCodec();
}
