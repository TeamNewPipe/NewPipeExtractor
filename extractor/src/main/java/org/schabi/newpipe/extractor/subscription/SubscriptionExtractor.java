package org.schabi.newpipe.extractor.subscription;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public abstract class SubscriptionExtractor {

    /**
     * Exception that should be thrown when the input <b>do not</b> contain valid content that the
     * extractor can parse (e.g. nonexistent user in case of a url extraction).
     */
    public static class InvalidSourceException extends ParsingException {
        public InvalidSourceException() {
            this(null, null);
        }

        public InvalidSourceException(@Nullable final String detailMessage) {
            this(detailMessage, null);
        }

        public InvalidSourceException(final Throwable cause) {
            this(null, cause);
        }

        public InvalidSourceException(@Nullable final String detailMessage, final Throwable cause) {
            super("Not a valid source" + (detailMessage == null ? "" : " (" + detailMessage + ")"),
                    cause);
        }
    }

    public enum ContentSource {
        CHANNEL_URL, INPUT_STREAM
    }

    private final List<ContentSource> supportedSources;
    protected final StreamingService service;

    public SubscriptionExtractor(final StreamingService service,
                                 final List<ContentSource> supportedSources) {
        this.service = service;
        this.supportedSources = Collections.unmodifiableList(supportedSources);
    }

    public List<ContentSource> getSupportedSources() {
        return supportedSources;
    }

    /**
     * Returns an url that can help/guide the user to the file (or channel url) to extract the
     * subscriptions.
     * <p>For example, in YouTube, the export subscriptions url is a good choice to return here.</p>
     */
    @Nullable
    public abstract String getRelatedUrl();

    /**
     * Reads and parse a list of {@link SubscriptionItem} from the given channel url.
     *
     * @throws InvalidSourceException when the channelUrl doesn't exist or is invalid
     */
    public List<SubscriptionItem> fromChannelUrl(final String channelUrl)
            throws IOException, ExtractionException {
        throw new UnsupportedOperationException("Service " + service.getServiceInfo().getName()
                + " doesn't support extracting from a channel url");
    }

    /**
     * Reads and parse a list of {@link SubscriptionItem} from the given InputStream.
     *
     * @throws InvalidSourceException when the content read from the InputStream is invalid and can
     *                                not be parsed
     */
    public List<SubscriptionItem> fromInputStream(@Nonnull final InputStream contentInputStream)
            throws ExtractionException {
        throw new UnsupportedOperationException("Service " + service.getServiceInfo().getName()
                + " doesn't support extracting from an InputStream");
    }

    /**
     * Reads and parse a list of {@link SubscriptionItem} from the given InputStream.
     *
     * @throws InvalidSourceException when the content read from the InputStream is invalid and can
     *                                not be parsed
     */
    public List<SubscriptionItem> fromInputStream(@Nonnull final InputStream contentInputStream,
                                                  @Nonnull final String contentType)
            throws ExtractionException {
        throw new UnsupportedOperationException("Service " + service.getServiceInfo().getName()
                + " doesn't support extracting from an InputStream");
    }
}
