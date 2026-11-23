package org.schabi.newpipe.extractor.services.youtube.stream;

import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.deliverysource.DeliverySource;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class representing YouTube video demuxed streams available with an individual streaming URL.
 *
 * <p>
 * Instances of this class aren't meant to be built by extractor users, only YouTube extractors.
 * </p>
 */
public final class YoutubeUriVideoStream extends YoutubeBaseVideoDemuxedStream implements Stream {

    @Nonnull
    private final DeliverySource deliverySource;

    /**
     * Construct a new {@link YoutubeUriVideoStream} instance.
     *
     * @param id                              the ID of the stream, which should be of the form
     *                                        {@code InnerTube_client_name-Itag_ID}
     * @param name                            the displayed name of the stream in the quality
     *                                        settings ({@code qualityLabel} property)
     * @param isAutomaticallyServiceGenerated whether it's an AI upscaled (named super resolution)
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
     * @param deliverySource                  the {@link DeliverySource} of the stream, which
     *                                        should contain the streaming URL in the {@code url}
     *                                        property and fallback URLs, constructed from the
     *                                        second part of the {@code mn} query parameter from
     *                                        this URL and with the
     *                                        {@code redirector.googlevideo.com} domain
     */
    public YoutubeUriVideoStream(@Nonnull final String id,
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
                                 @Nullable final ColorInfo colorInfo,
                                 @Nonnull final DeliverySource deliverySource) {
        super(id, name, isAutomaticallyServiceGenerated, mediaFormat, projectionType,
                averageBitrate, width, height, framerate, codec, isHdr, itag, colorInfo);
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
        if (!(o instanceof YoutubeUriVideoStream that) || !super.equals(o)) {
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
        return "YoutubeUriVideoStream{"
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
                + ", itag=" + getItag()
                + ", colorInfo=" + getColorInfo()
                + ", deliverySource=" + deliverySource
                + "}";
    }
}
