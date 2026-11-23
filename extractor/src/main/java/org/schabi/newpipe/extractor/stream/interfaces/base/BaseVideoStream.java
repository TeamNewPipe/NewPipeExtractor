package org.schabi.newpipe.extractor.stream.interfaces.base;

import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base interface for video streams.
 *
 * <p>
 * It does not represent individual video streams to be directly used by clients, but the ones in
 * extractor sub-definitions and custom implementations. See
 * {@link org.schabi.newpipe.extractor.stream.interfaces.VideoStream VideoStream} for an interface
 * to be used directly.
 * </p>
 */
public interface BaseVideoStream extends BaseDemuxedStream {

    /**
     * Get the {@link VideoMediaFormat} of this video stream.
     *
     * @return the {@link VideoMediaFormat} of this video stream
     */
    @Nonnull
    VideoMediaFormat getMediaFormat();

    /**
     * Get the {@link ProjectionType} of this video stream.
     *
     * @return the {@link ProjectionType} of this video stream
     */
    @Nonnull
    ProjectionType getProjectionType();

    /**
     * Get the average bitrate of this video stream if it is known or {@code null} otherwise.
     *
     * @return the average bitrate or {@code null} if unknown
     */
    @Nullable
    Integer getAverageBitrate();

    /**
     * Get the width of this video stream's resolution or {@code null} if it is not known.
     *
     * @return the width or {@code null} if unknown
     */
    @Nullable
    Integer getWidth();

    /**
     * Get the height of this video stream's resolution or {@code null} if it is not known.
     *
     * @return the height or {@code null} if unknown
     */
    @Nullable
    Integer getHeight();

    /**
     * Get the frame rate per second of this video stream or {@code null} if it is not known.
     *
     * @return the frame rate per second or {@code null} if unknown
     */
    @Nullable
    Integer getFramerate();

    /**
     * Get the codec this video stream uses or {@code null} if not known.
     *
     * @return the codec this video stream uses or {@code null} if unknown
     */
    @Nullable
    String getCodec();

    /**
     * Get whether this video stream is using HDR or not (SDR).
     *
     * @return whether this video stream is HDR ({@code true}) or SDR ({@code false})
     */
    @Nullable
    Boolean isHdr();

    /**
     * Enum describing video projection types.
     */
    enum ProjectionType {

        /**
         * Constant representing the projection type is unknown.
         */
        UNKNOWN,

        /**
         * 2D standard video projection.
         */
        RECTANGULAR,

        EQUIRECTANGULAR,
        EQUIRECTANGULAR_THREED_TOP_BOTTOM,
        MESH;
    }
}
