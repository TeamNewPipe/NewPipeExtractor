package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

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
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;

public class PeertubeCommentsExtractor extends CommentsExtractor {
    private InfoItemsPage<CommentsInfoItem> initPage;
    private long total;

    public PeertubeCommentsExtractor(final StreamingService service, final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage;
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector, final JsonObject json) throws ParsingException {
        final JsonArray contents = json.getArray("data");

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                if (!item.getBoolean("isDeleted")) {
                    final PeertubeCommentsInfoItemExtractor extractor = new PeertubeCommentsInfoItemExtractor(item, this);
                    collector.commit(extractor);
                }
            }
        }
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage.getNextPageUrl();
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        final Response response = getDownloader().get(pageUrl);
        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for comments info", e);
            }
        }

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        if (json != null) {
            final Number number = JsonUtils.getNumber(json, "total");
            if (number != null) this.total = number.longValue();
            collectCommentsFrom(collector, json);
        } else {
            throw new ExtractionException("Unable to get peertube comments info");
        }
        return new InfoItemsPage<>(collector, PeertubeParsingHelper.getNextPageUrl(pageUrl, total));
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        this.initPage = getPage(getUrl() + "?" + START_KEY + "=0&" + COUNT_KEY + "=" + ITEMS_PER_PAGE);
    }
}
