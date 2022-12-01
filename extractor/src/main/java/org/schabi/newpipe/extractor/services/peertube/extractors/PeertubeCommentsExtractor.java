package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import javax.annotation.Nonnull;

public class PeertubeCommentsExtractor extends CommentsExtractor {

    /**
     * Use {@link #isReply()} to access this variable.
     */
    private Boolean isReply = null;

    public PeertubeCommentsExtractor(final StreamingService service,
                                     final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        if (isReply()) {
            return getPage(new Page(getOriginalUrl()));
        } else {
            return getPage(new Page(getUrl() + "?" + START_KEY + "=0&"
                    + COUNT_KEY + "=" + ITEMS_PER_PAGE));
        }
    }

    private boolean isReply() throws ParsingException {
        if (isReply == null) {
            if (getOriginalUrl().contains("/videos/watch/")) {
                isReply = false;
            } else {
                isReply = getOriginalUrl().contains("/comment-threads/");
            }
        }
        return isReply;
    }

    private void collectCommentsFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                     @Nonnull final JsonObject json) throws ParsingException {
        final JsonArray contents = json.getArray("data");

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                if (!item.getBoolean("isDeleted")) {
                    collector.commit(new PeertubeCommentsInfoItemExtractor(item, this));
                }
            }
        }
    }

    private void collectRepliesFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                    @Nonnull final JsonObject json) throws ParsingException {
        final JsonArray contents = json.getArray("children");

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = ((JsonObject) c).getObject("comment");
                if (!item.getBoolean("isDeleted")) {
                    collector.commit(new PeertubeCommentsInfoItemExtractor(item, this));
                }
            }
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (final Exception e) {
                throw new ParsingException("Could not parse json data for comments info", e);
            }
        }

        if (json != null) {
            PeertubeParsingHelper.validate(json);
            final long total;
            final CommentsInfoItemsCollector collector
                    = new CommentsInfoItemsCollector(getServiceId());

            if (isReply() || json.has("children")) {
                total = json.getArray("children").size();
                collectRepliesFrom(collector, json);
            } else {
                total = json.getLong("total");
                collectCommentsFrom(collector, json);
            }

            return new InfoItemsPage<>(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total));
        } else {
            throw new ExtractionException("Unable to get PeerTube kiosk info");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }
}
