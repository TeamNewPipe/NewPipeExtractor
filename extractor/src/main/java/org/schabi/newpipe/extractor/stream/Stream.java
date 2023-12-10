package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Abstract class which represents streams in the extractor.
 */
public abstract class Stream implements Serializable {
    public static final int FORMAT_ID_UNKNOWN = -1;
    public static final String ID_UNKNOWN = " ";

    /**
     * An integer to represent that the itag ID returned is not available (only for YouTube; this
     * should never happen) or not applicable (for other services than YouTube).
     *
     * <p>
     * An itag should not have a negative value, so {@code -1} is used for this constant.
     * </p>
     */
    public static final int ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE = -1;

    private final String id;
    @Nullable private final MediaFormat mediaFormat;
    private final String content;
    private final boolean isUrl;
    private final DeliveryMethod deliveryMethod;
    @Nullable private final String manifestUrl;

    /**
     * Instantiates a new {@code Stream} object.
     *
     * @param id             the identifier which uniquely identifies the file, e.g. for YouTube
     *                       this would be the itag
     * @param content        the content or URL, depending on whether isUrl is true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat}, which can be null
     * @param deliveryMethod the delivery method of the stream
     * @param manifestUrl    the URL of the manifest this stream comes from (if applicable,
     *                       otherwise null)
     */
    public Stream(final String id,
                  final String content,
                  final boolean isUrl,
                  @Nullable final MediaFormat format,
                  final DeliveryMethod deliveryMethod,
                  @Nullable final String manifestUrl) {
        this.id = id;
        this.content = content;
        this.isUrl = isUrl;
        this.mediaFormat = format;
        this.deliveryMethod = deliveryMethod;
        this.manifestUrl = manifestUrl;
    }

    /**
     * Checks if the list already contains a stream with the same statistics.
     *
     * @param stream the stream to be compared against the streams in the stream list
     * @param streamList the list of {@link Stream}s which will be compared
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
     * Reveals whether two streams have the same statistics ({@link MediaFormat media format} and
     * {@link DeliveryMethod delivery method}).
     *
     * <p>
     * If the {@link MediaFormat media format} of the stream is unknown, the streams are compared
     * by using only the {@link DeliveryMethod delivery method} and their ID.
     * </p>
     *
     * <p>
     * Note: This method always returns false if the stream passed is null.
     * </p>
     *
     * @param other the stream object to be compared to this stream object
     * @return whether the stream have the same stats or not, based on the criteria above
     */
    public boolean equalStats(@Nullable final Stream other) {
        if (other == null || mediaFormat == null || other.mediaFormat == null) {
            return false;
        }
        return mediaFormat.id == other.mediaFormat.id && deliveryMethod == other.deliveryMethod
                && isUrl == other.isUrl;
    }

    /**
     * Gets the identifier of this stream, e.g. the itag for YouTube.
     *
     * <p>
     * It should normally be unique, but {@link #ID_UNKNOWN} may be returned as the identifier if
     * the one used by the stream extractor cannot be extracted, which could happen if the
     * extractor uses a value from a streaming service.
     * </p>
     *
     * @return the identifier (which may be {@link #ID_UNKNOWN})
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the URL of this stream if the content is a URL, or {@code null} otherwise.
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
     * Returns whether the content is a URL or not.
     *
     * @return {@code true} if the content of this stream is a URL, {@code false} if it's the
     * actual content
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
     * Gets the format ID, which can be unknown.
     *
     * @return the format ID or {@link #FORMAT_ID_UNKNOWN}
     */
    public int getFormatId() {
        if (mediaFormat != null) {
            return mediaFormat.id;
        }
        return FORMAT_ID_UNKNOWN;
    }

    /**
     * Gets the {@link DeliveryMethod}.
     *
     * @return the delivery method
     */
    @Nonnull
    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Gets the URL of the manifest this stream comes from (if applicable, otherwise null).
     *
     * @return the URL of the manifest this stream comes from or {@code null}
     */
    @Nullable
    public String getManifestUrl() {
        return manifestUrl;
    }

    /**
     * Gets the {@link ItagItem} of a stream.
     *
     * <p>
     * If the stream is not from YouTube, this value will always be null.
     * </p>
     *
     * @return the {@link ItagItem} of the stream or {@code null}
     */
    @Nullable
    public abstract ItagItem getItagItem();
}
