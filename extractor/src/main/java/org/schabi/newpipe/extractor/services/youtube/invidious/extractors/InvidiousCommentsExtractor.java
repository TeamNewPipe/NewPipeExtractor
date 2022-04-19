package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;

import java.io.IOException;

import javax.annotation.Nonnull;

public class InvidiousCommentsExtractor extends CommentsExtractor {

    private final String baseUrl;
    private JsonObject json;

    public InvidiousCommentsExtractor(
            final InvidiousService service,
            final ListLinkHandler uiHandler
    ) {
        super(service, uiHandler);
        baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws ExtractionException {
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());

        collectStreamsFrom(collector, json.getArray("comments"), getUrl());

        return new InfoItemsPage<>(collector, getNextPage());
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        if (page == null) {
            return InfoItemsPage.emptyPage();
        }

        final Downloader dl = NewPipe.getDownloader();
        final Response response = dl.get(page.getUrl());

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, page.getUrl());

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectStreamsFrom(collector, json.getArray("comments"), page.getUrl());
        return new InfoItemsPage<>(collector, getNextPage());
    }


    public Page getNextPage() throws ParsingException {
        final String continuation = json.getString("continuation");

        if (continuation == null || continuation.isEmpty()) {
            return null;
        }

        return new Page(baseUrl + "/api/v1/comments/" + getId()
                + "?continuation=" + continuation);
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/comments/" + getId();
        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);

    }

    private void collectStreamsFrom(
            final CommentsInfoItemsCollector collector,
            final JsonArray entries,
            final String url
    ) {
        entries.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(commentObj -> new InvidiousCommentsInfoItemExtractor(commentObj, url))
                .forEach(collector::commit);
    }

}
