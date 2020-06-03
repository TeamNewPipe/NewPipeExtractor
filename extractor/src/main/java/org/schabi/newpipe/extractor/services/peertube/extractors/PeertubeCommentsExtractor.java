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
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.*;

public class PeertubeCommentsExtractor extends CommentsExtractor {

    private InfoItemsPage<CommentsInfoItem> initPage;
    private long total;

    public PeertubeCommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage;
    }

    private void collectStreamsFrom(CommentsInfoItemsCollector collector, JsonObject json, String pageUrl) throws ParsingException {
        JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (Exception e) {
            throw new ParsingException("unable to extract comments info", e);
        }

        for (Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                PeertubeCommentsInfoItemExtractor extractor = new PeertubeCommentsInfoItemExtractor(item, this);
                collector.commit(extractor);
            }
        }

    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage.getNextPageUrl();
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        Response response = getDownloader().get(pageUrl);
        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for comments info", e);
            }
        }

        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        if (json != null) {
            Number number = JsonUtils.getNumber(json, "total");
            if (number != null) this.total = number.longValue();
            collectStreamsFrom(collector, json, pageUrl);
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
