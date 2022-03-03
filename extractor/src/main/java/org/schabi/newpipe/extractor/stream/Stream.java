package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Abstract class which represents streams in the extractor.
 */
public abstract class Stream implements Serializable {
    public static final int FORMAT_ID_UNKNOWN = -1;
    public static final String ID_UNKNOWN = " ";

    /**
     * An integer to represent that the itag id returned is not available (only for YouTube, this
     * should never happen) or not applicable (for other services than YouTube).
     *
     * <p>
     * An itag should not have a negative value so {@code -1} is used for this constant.
     * </p>
     */
    public static final int ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE = -1;

    private final String id;
    @Nullable private final MediaFormat mediaFormat;
    private final String content;
    private final boolean isUrl;
    private final DeliveryMethod deliveryMethod;
    @Nullable private final String baseUrl;

    /**
     * Instantiates a new {@code Stream} object.
     *
     * @param id             the ID which uniquely identifies the file, e.g. for YouTube this would
     *                       be the itag
     * @param content        the content or URL, depending on whether isUrl is true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat}, which can be null
     * @param deliveryMethod the delivery method of the stream
     * @param baseUrl        the base URL of the content if the stream is a DASH or an HLS
     *                       manifest, which can be null
     */
    public Stream(final String id,
                  final String content,
                  final boolean isUrl,
                  @Nullable final MediaFormat format,
                  final DeliveryMethod deliveryMethod,
                  @Nullable final String baseUrl) {
        this.id = id;
        this.content = content;
        this.isUrl = isUrl;
        this.mediaFormat = format;
        this.deliveryMethod = deliveryMethod;
        this.baseUrl = baseUrl;
    }

    /**
     * Checks if the list already contains one stream with equals stats.
     *
     * @param stream the stream which will be compared to the streams in the stream list
     * @param streamList the list of {@link Stream Streams} which will be compared
     * @return whether the list already contains one stream with equals stats
     */
    public static boolean containSimilarStream(final Stream stream,
                                               final List<? extends Stream> streamList) {
        if (isNullOrEmpty(streamList)) {
            return false;
        }
        for (final Stream cmpStream : streamList) {
            if (stream.equalStats(cmpStream)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reveals whether two streams have the same stats ({@link MediaFormat media format} and
     * {@link DeliveryMethod delivery method}).
     *
     * <p>
     * If the {@link MediaFormat media format} of the stream is unknown, the streams are compared
     * by only using the {@link DeliveryMethod delivery method} and their id.
     * </p>
     *
     * <p>
     * Note: This method always returns always false if the stream passed is null.
     * </p>
     *
     * @param cmp the stream object to be compared to this stream object
     * @return whether the stream have the same stats or not, based on the criteria above
     */
    public boolean equalStats(@Nullable final Stream cmp) {
        if (cmp == null) {
            return false;
        }

        Boolean haveSameMediaFormatId = null;
        if (mediaFormat != null && cmp.mediaFormat != null) {
            haveSameMediaFormatId = mediaFormat.id == cmp.mediaFormat.id;
        }
        final boolean areUsingSameDeliveryMethodAndAreUrlStreams =
                deliveryMethod == cmp.deliveryMethod && isUrl == cmp.isUrl;

        return haveSameMediaFormatId != null
                ? haveSameMediaFormatId && areUsingSameDeliveryMethodAndAreUrlStreams
                : areUsingSameDeliveryMethodAndAreUrlStreams;
    }

    /**
     * Reveals whether two streams are equal.
     *
     * @param cmp the stream object to be compared to this stream object
     * @return whether streams are equal
     */
    public boolean equals(final Stream cmp) {
        return equalStats(cmp) && content.equals(cmp.content);
    }

    /**
     * Gets the identifier of this stream, e.g. the itag for YouTube.
     *
     * <p>
     * It should be normally unique but {@link #ID_UNKNOWN} may be returned as the identifier if
     * one used by the stream extractor cannot be extracted, if the extractor uses a value from a
     * streaming service.
     * </p>
     *
     * @return the id (which may be {@link #ID_UNKNOWN})
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the URL of this stream if the content is a URL, or {@code null} if that's the not case.
     *
     * @return the URL if the content is a URL, {@code null} otherwise
     * @deprecated Use {@link #getContent()} instead.
     */
    @Deprecated
    @Nullable
    public String getUrl() {
        return isUrl ? content : null;
    }

    /**
     * Gets the content or URL.
     *
     * @return the content or URL
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns if the content is a URL or not.
     *
     * @return {@code true} if the content of this stream content is a URL, {@code false}
     * if it is the actual content
     */
    public boolean isUrl() {
        return isUrl;
    }

    /**
     * Gets the {@link MediaFormat}, which can be null.
     *
     * @return the format
     */
    @Nullable
    public MediaFormat getFormat() {
        return mediaFormat;
    }

    /**
     * Gets the format id, which can be unknown.
     *
     * @return the format id or {@link #FORMAT_ID_UNKNOWN}
     */
    public int getFormatId() {
        if (mediaFormat != null) {
            return mediaFormat.id;
        }
        return FORMAT_ID_UNKNOWN;
    }

    /**
     * Gets the delivery method.
     *
     * @return the delivery method
     */
    @Nonnull
    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Gets the base URL of a stream.
     *
     * <p>
     * If the stream is not a DASH stream or an HLS stream, this value will always be null.
     * It may be also null for these streams too.
     * </p>
     *
     * @return the base URL of the stream or {@code null}
     */
    @Nullable
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the {@link ItagItem} of a stream.
     *
     * <p>
     * If the stream is not a YouTube stream, this value will always be null.
     * </p>
     *
     * @return the {@link ItagItem} of the stream or {@code null}
     */
    @Nullable
    public abstract ItagItem getItagItem();

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final Stream stream = (Stream) obj;
        return id.equals(stream.id) && mediaFormat == stream.mediaFormat
                && deliveryMethod == stream.deliveryMethod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mediaFormat, deliveryMethod);
    }
}