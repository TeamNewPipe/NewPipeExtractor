package org.schabi.newpipe.extractor.stream.interfaces.base;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base interface for a muxed stream, which aggregates multiple streams (whether demuxed or not).
 *
 * <p>
 * A muxed stream is a file containing video, audio and subtitle tracks or a manifest describing
 * available qualities.
 * </p>
 *
 * <p>
 * It does not represent the base of muxed streams to be directly used by clients, but the one of
 * extractor sub-definitions and custom implementations. See
 * {@link org.schabi.newpipe.extractor.stream.interfaces.MuxedStream MuxedStream} for an
 * interface to be used directly.
 * </p>
 */
public interface BaseMuxedStream extends BaseStream {

    /**
     * Get the demuxed audio streams this muxed stream contains.
     *
     * <p>
     * If there is no audio stream, this method returns an empty list. When the presence of audio
     * streams is not known, this method returns {@code null}.
     * </p>
     *
     * @return the demuxed audio streams as a list or {@code null} if they are not known
     */
    @Nullable
    List<? extends BaseAudioStream> getDemuxedAudioStreams();

    /**
     * Get the demuxed video streams this muxed stream contains.
     *
     * <p>
     * If there is no video stream, this method returns an empty list. When the presence of video
     * streams is not known, this method returns {@code null}.
     * </p>
     *
     * @return the demuxed video streams as a list or {@code null} if they are not known
     */
    @Nullable
    List<? extends BaseVideoStream> getDemuxedVideoStreams();

    /**
     * Get the demuxed subtitles streams this muxed stream contains.
     *
     * <p>
     * If there is no subtitles stream, this method returns an empty list. When the presence of
     * subtitles streams is not known, this method returns {@code null}.
     * </p>
     *
     * @return the demuxed subtitles streams as a list or {@code null} if they are not known
     */
    @Nullable
    List<? extends BaseSubtitlesStream> getDemuxedSubtitlesStreams();

    /**
     * Get the muxed streams this muxed stream contains.
     *
     * <p>
     * If there is no muxed stream, this method returns an empty list. When the presence of
     * muxed streams is not known, this method returns {@code null}.
     * </p>
     *
     * @return the muxed streams as a list or {@code null} if they are not known
     */
    @Nullable
    List<? extends BaseMuxedStream> getMuxedStreams();

    /**
     * Get the average bitrate of the muxed stream itself and not from an individual track or a
     * part of them, if known and applicable.
     *
     * <p>
     * If it is not known or applicable, this method returns {@code null}.
     * </p>
     *
     * @return the average bitrate of the muxed stream itself or {@code null}
     */
    @Nullable
    Integer getAverageBitrate();
}
