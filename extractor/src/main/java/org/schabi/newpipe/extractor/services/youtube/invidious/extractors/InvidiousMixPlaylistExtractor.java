package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import javax.annotation.Nonnull;

public class InvidiousMixPlaylistExtractor extends PlaylistExtractor {

    private JsonObject json;
    private final String baseUrl;

    public InvidiousMixPlaylistExtractor(
            final InvidiousService service,
            final ListLinkHandler linkHandler
    ) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/mixes/" + getId();
        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        json.getArray("videos").stream()
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

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return YoutubeMixPlaylistExtractor
                .getThumbnailUrlFromPlaylistId(json.getString("mixId"));
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return Utils.EMPTY_STRING;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return "YouTube";
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return ListExtractor.ITEM_COUNT_INFINITE;
    }
}
