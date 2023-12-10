package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.schabi.newpipe.extractor.services.youtube.ItagItem;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Class to build easier {@link org.schabi.newpipe.extractor.stream.Stream}s for
 * {@link YoutubeStreamExtractor}.
 *
 * <p>
 * It stores, per stream:
 * <ul>
 *     <li>its content (the URL/the base URL of streams);</li>
 *     <li>whether its content is the URL the content itself or the base URL;</li>
 *     <li>its associated {@link ItagItem}.</li>
 * </ul>
 * </p>
 */
final class ItagInfo implements Serializable {
    @Nonnull
    private final String content;
    @Nonnull
    private final ItagItem itagItem;
    private boolean isUrl;

    /**
     * Creates a new {@code ItagInfo} instance.
     *
     * @param content  the content of the stream, which must be not null
     * @param itagItem the {@link ItagItem} associated with the stream, which must be not null
     */
    ItagInfo(@Nonnull final String content,
             @Nonnull final ItagItem itagItem) {
        this.content = content;
        this.itagItem = itagItem;
    }

    /**
     * Sets whether the stream is a URL.
     *
     * @param isUrl whether the content is a URL
     */
    void setIsUrl(final boolean isUrl) {
        this.isUrl = isUrl;
    }

    /**
     * Gets the content stored in this {@code ItagInfo} instance, which is either the URL to the
     * content itself or the base URL.
     *
     * @return the content stored in this {@code ItagInfo} instance
     */
    @Nonnull
    String getContent() {
        return content;
    }

    /**
     * Gets the {@link ItagItem} associated with this {@code ItagInfo} instance.
     *
     * @return the {@link ItagItem} associated with this {@code ItagInfo} instance, which is not
     * null
     */
    @Nonnull
    ItagItem getItagItem() {
        return itagItem;
    }

    /**
     * Gets whether the content stored is the URL to the content itself or the base URL of it.
     *
     * @return whether the content stored is the URL to the content itself or the base URL of it
     * @see #getContent() for more details
     */
    boolean getIsUrl() {
        return isUrl;
    }
}
