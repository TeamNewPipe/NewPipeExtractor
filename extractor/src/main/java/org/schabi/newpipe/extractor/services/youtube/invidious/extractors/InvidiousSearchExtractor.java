package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Search extractor for Invidious.
 * <br/>
 * API-Docs: https://docs.invidious.io/api/#get-apiv1search
 */
public class InvidiousSearchExtractor extends SearchExtractor {

    private final String baseUrl;
    private JsonArray results;

    public InvidiousSearchExtractor(
            final InvidiousService service,
            final SearchQueryHandler linkHandler
    ) {
        super(service, linkHandler);
        baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(getUrl()));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        if (results == null) {
            return InfoItemsPage.emptyPage();
        }

        int pageNumber = 1;
        try {
            if (page.getId() != null) {
                pageNumber = Integer.parseInt(page.getId());
            }
        } catch (final NumberFormatException ignored) {
            // Use default
        }

        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        results.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEach(o -> collectStreamsFrom(collector, o));

        final Page nextPage = new Page(getUrl() + "&page=" + (pageNumber + 1));
        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final Response response = downloader.get(getUrl());
        results = InvidiousParsingHelper.getValidJsonArrayFromResponse(response, getUrl());
    }

    private void collectStreamsFrom(
            final MultiInfoItemsCollector collector,
            final JsonObject json
    ) {
        final String type = json.getString("type");

        switch (type) {
            case "video":
                collector.commit(new InvidiousStreamInfoItemExtractor(json, baseUrl));
                break;
            case "playlist":
                collector.commit(new InvidiousPlaylistInfoItemExtractor(json, baseUrl));
                break;
            case "channel":
                collector.commit(new InvidiousChannelInfoItemExtractor(json, baseUrl));
                break;
        }
    }

}
