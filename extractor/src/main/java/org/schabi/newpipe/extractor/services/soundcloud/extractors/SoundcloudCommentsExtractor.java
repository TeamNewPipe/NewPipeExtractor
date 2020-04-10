package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class SoundcloudCommentsExtractor extends CommentsExtractor {

    private JsonObject json;

    public SoundcloudCommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());

        collectStreamsFrom(collector, json.getArray("collection"));

        return new InfoItemsPage<>(collector, json.getString("next_href"));
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();
        Response rp = dl.get(pageUrl);
        try {
            json = JsonParser.object().from(rp.responseBody());
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json", e);
        }

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectStreamsFrom(collector, json.getArray("collection"));

        return new InfoItemsPage<>(collector, json.getString("next_href"));
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        Response response = downloader.get(getUrl());
        try {
            json = JsonParser.object().from(response.responseBody());
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json", e);
        }
    }

    private void collectStreamsFrom(final CommentsInfoItemsCollector collector, final JsonArray entries) throws ParsingException {
        final String url = getUrl();
        for (Object comment : entries) {
            collector.commit(new SoundcloudCommentsInfoItemExtractor((JsonObject) comment, url));
        }
    }
}
