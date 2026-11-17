package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.impl.base.BaseVideoStreamImpl;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Base class for YouTube video demuxed streams.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public class YoutubeBaseVideoDemuxedStream extends BaseVideoStreamImpl
        implements YoutubeBaseDemuxedStream {

    @Nonnull
    private final Itag itag;
    @Nullable
    private final ColorInfo colorInfo;

    /**
     * Construct a new {@link YoutubeBaseVideoDemuxedStream} instance.
     *
     * @param id                              the ID of the stream, which should be of the form
     *                                        {@code InnerTube_client_name-Itag_ID}
     * @param name                            the displayed name of the stream in the quality
     *                                        settings ({@code qualityLabel} property)
     * @param isAutomaticallyServiceGenerated whether it's an AI upscaled (named Super resolution)
     *                                        stream (the {@code sr} property is present in the
     *                                        {@code xtags} Base64 encoded Protobuf and set to
     *                                        {@code true})
     * @param mediaFormat                     an appropriate {@link VideoMediaFormat} value based
     *                                        on the {@code mimeType} property returned
     * @param projectionType                  an appropriate {@link ProjectionType} instance from
     *                                        the {@code projectionType} value if it exists, or
     *                                        {@link ProjectionType#UNKNOWN} otherwise
     * @param averageBitrate                  the average bitrate
     * @param width                           the video width
     * @param height                          the video height
     * @param framerate                       the framerate returned by YouTube, which may be
     *                                        rounded
     * @param codec                           the codec string returned in the {@code mimeType}
     *                                        property
     * @param isHdr                           whether the stream is an HDR one, which can be
     *                                        checked using the {@code qualityLabel} value
     * @param itag                            an {@link Itag} instance for this stream
     * @param colorInfo                       the values of the {@code colorInfo} object if
     *                                        available as a {@link ColorInfo} instance, or
     *                                        {@code null} if not present
     */
    public YoutubeBaseVideoDemuxedStream(@Nonnull final String id,
                                         @Nullable final String name,
                                         @Nullable final Boolean isAutomaticallyServiceGenerated,
                                         @Nonnull final VideoMediaFormat mediaFormat,
                                         @Nonnull final ProjectionType projectionType,
                                         @Nullable final Integer averageBitrate,
                                         @Nullable final Integer width,
                                         @Nullable final Integer height,
                                         @Nullable final Integer framerate,
                                         @Nullable final String codec,
                                         @Nullable final Boolean isHdr,
                                         @Nonnull final Itag itag,
                                         @Nullable final ColorInfo colorInfo) {
        // Only one video stream language is available for YouTube videos, so locale is always null
        super(id, name, null, isAutomaticallyServiceGenerated, mediaFormat, projectionType,
                averageBitrate, width, height, framerate, codec, isHdr);
        this.itag = itag;
        this.colorInfo = colorInfo;
    }

    @Nonnull
    @Override
    public Itag getItag() {
        return itag;
    }

    /**
     * Get the values of the {@code colorInfo} object if available as a {@link ColorInfo} instance,
     * or {@code null} if not present
     *
     * @return a {@link ColorInfo} instance or {@code null}
     */
    @Nullable
    public ColorInfo getColorInfo() {
        return colorInfo;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof YoutubeBaseVideoDemuxedStream that) || !super.equals(o)) {
            return false;
        }

        return itag.equals(that.itag) && Objects.equals(colorInfo, that.colorInfo);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + itag.hashCode();
        result = 31 * result + Objects.hashCode(colorInfo);
        return result;
    }

    @Override
    public String toString() {
        return "YoutubeBaseVideoStream{"
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
                + ", itag=" + itag
                + ", colorInfo=" + colorInfo
                + "}";
    }
}
