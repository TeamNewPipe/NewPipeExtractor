package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler.InvidiousTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class InvidiousTrendingExtractor extends KioskExtractor<StreamInfoItem> {

    private final String baseUrl;
    private JsonArray videos;

    public InvidiousTrendingExtractor(
            final InvidiousService streamingService,
            final ListLinkHandler linkHandler,
            final String kioskId
    ) {
        super(streamingService, linkHandler, kioskId);
        baseUrl = streamingService.getInstance().getUrl();
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {

        String apiUrl = getUrl();
        if (getId().equals(InvidiousTrendingLinkHandlerFactory.KIOSK_TRENDING)) {
            // The trending endpoint accepts regions
            // see https://docs.invidious.io/api/#get-apiv1trending
            apiUrl += "?region=" + getExtractorContentCountry();
        }

        final Response response = downloader.get(apiUrl);
        videos = InvidiousParsingHelper.getValidJsonArrayFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        videos.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(o -> new InvidiousStreamInfoItemExtractor(o, baseUrl))
                .forEach(collector::commit);

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        return InfoItemsPage.emptyPage();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return getId();
    }

}
