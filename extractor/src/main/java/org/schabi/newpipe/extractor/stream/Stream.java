package org.schabi.newpipe.extractor.stream;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Creates a stream object.
 */
public abstract class Stream implements Serializable {
    private final String id;
    @Nullable private final MediaFormat mediaFormat;
    private final String content;
    private final boolean isUrl;
    private final DeliveryMethod deliveryMethod;
    @Nullable private final String baseUrl;

    public static final int FORMAT_ID_UNKNOWN = -1;

    /**
     * Instantiates a new stream object.
     *
     * @param id             the ID which uniquely identifies the file, e.g. for YouTube this would
     *                       be the itag
     * @param content        the content or URL, depending on whether isUrl is true
     * @param isUrl          whether content is the URL or the actual content of e.g. a DASH
     *                       manifest
     * @param format         the {@link MediaFormat}, which can be null
     * @param deliveryMethod the delivery method
     * @param baseUrl        the base URL of the content if the stream is a DASH or an HLS
     *                       manifest, which can be null
     */
    protected Stream(final String id,
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
     * Check if the list already contains one stream with equals stats.
     *
     * @param stream the stream which will be compared to the streams in the stream list
     * @param streamList the list of {@link Stream Streams} which will be compared
     */
    public static boolean containSimilarStream(final Stream stream,
                                               final List<? extends Stream> streamList) {
        if (isNullOrEmpty(streamList)) return false;
        for (final Stream cmpStream : streamList) {
            if (stream.equalStats(cmpStream)) return true;
        }
        return false;
    }

    /**
     * Reveals whether two streams have the same stats (format and bitrate, for example).
     *
     * <p>
     * It returns always false if the id of the stream passed is unknown (or also if the stream
     * passed is null) or if the format of the stream compared is not known.
     * </p>
     * @param cmp the stream object to be compared to this stream object
     */
    public boolean equalStats(final Stream cmp) {
        if (getFormat() != null && cmp != null && cmp.getFormat() != null) {
            return getFormat().id == cmp.getFormat().id;
        }
        return false;
    }

    /**
     * Reveals whether two streams are equal.
     *
     * @param cmp the stream object to be compared to this stream object
     */
    public boolean equals(final Stream cmp) {
        return equalStats(cmp) && content.equals(cmp.content);
    }

    /**
     * Gets the ID for this stream, e.g. itag for YouTube.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     * @deprecated Use {@link #getContent()} instead
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
     * Return if the content is a URL or not.
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
     * <p>
     * If the stream is not a YouTube stream, this value will always be null.
     * </p>
     *
     * @return the {@link ItagItem} of the stream or {@code null}
     */
    @Nullable
    public abstract ItagItem getItagItem();
}
