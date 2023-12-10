package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
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
import java.nio.charset.StandardCharsets;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import javax.annotation.Nonnull;

public class PeertubeCommentsExtractor extends CommentsExtractor {
    static final String CHILDREN = "children";
    private static final String IS_DELETED = "isDeleted";
    private static final String TOTAL = "total";

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
                if (!item.getBoolean(IS_DELETED)) {
                    collector.commit(new PeertubeCommentsInfoItemExtractor(
                            item, null, getUrl(), getBaseUrl(), isReply()));
                }
            }
        }
    }

    private void collectRepliesFrom(@Nonnull final CommentsInfoItemsCollector collector,
                                    @Nonnull final JsonObject json) throws ParsingException {
        final JsonArray contents = json.getArray(CHILDREN);

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject content = (JsonObject) c;
                final JsonObject item = content.getObject("comment");
                final JsonArray children = content.getArray(CHILDREN);
                if (!item.getBoolean(IS_DELETED)) {
                    collector.commit(new PeertubeCommentsInfoItemExtractor(
                            item, children, getUrl(), getBaseUrl(), isReply()));
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

        JsonObject json = null;
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        final long total;
        if (page.getBody() == null) {
            final Response response = getDownloader().get(page.getUrl());
            if (response != null && !Utils.isBlank(response.responseBody())) {
                try {
                    json = JsonParser.object().from(response.responseBody());
                } catch (final Exception e) {
                    throw new ParsingException("Could not parse json data for comments info", e);
                }
            }
            if (json != null) {
                PeertubeParsingHelper.validate(json);
                if (isReply() || json.has(CHILDREN)) {
                    total = json.getArray(CHILDREN).size();
                    collectRepliesFrom(collector, json);
                } else {
                    total = json.getLong(TOTAL);
                    collectCommentsFrom(collector, json);
                }
            } else {
                throw new ExtractionException("Unable to get PeerTube kiosk info");
            }
        } else {
            try {
                json = JsonParser.object().from(new String(page.getBody(), StandardCharsets.UTF_8));
                isReply = true;
                total = json.getArray(CHILDREN).size();
                collectRepliesFrom(collector, json);
            } catch (final JsonParserException e) {
                throw new ParsingException(
                        "Could not parse json data for nested comments  info", e);
            }
        }

        return new InfoItemsPage<>(collector,
                PeertubeParsingHelper.getNextPage(page.getUrl(), total));


    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }
}
