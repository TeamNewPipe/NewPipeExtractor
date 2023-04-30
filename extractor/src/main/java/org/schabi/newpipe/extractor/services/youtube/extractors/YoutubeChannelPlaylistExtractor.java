package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

public class YoutubeChannelPlaylistExtractor extends ListExtractor<PlaylistInfoItem> {

    private final String browseId;
    private final String params;
    private final String canonicalBaseUrl;
    private JsonObject browseResponse;
    private JsonObject playlistTab;

    YoutubeChannelPlaylistExtractor(final StreamingService service,
            final ListLinkHandler linkHandler, final JsonObject browseEndpoint) {
        super(service, linkHandler);
        this.browseId = browseEndpoint.getString("browseId");
        this.params = browseEndpoint.getString("params");
        this.canonicalBaseUrl = browseEndpoint.getString("canonicalBaseUrl");
    }

    @Override
    public InfoItemsPage<PlaylistInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        final PlaylistInfoItemsCollector pic = new PlaylistInfoItemsCollector(getServiceId());

        final JsonArray playlistItems = playlistTab.getObject("content")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("gridRenderer").getArray("items");
        final var continuation = collectPlaylistsFrom(playlistItems, pic);
        return new InfoItemsPage<>(pic, continuation);
    }

    private Page collectPlaylistsFrom(final JsonArray playlistItems,
            final PlaylistInfoItemsCollector collector)
            throws UnsupportedEncodingException, IOException, ExtractionException {
        Page continuation = null;
        for (final var item : playlistItems) {
            if (item instanceof JsonObject) {
                final JsonObject jsonItem = (JsonObject) item;
                if (jsonItem.has("gridPlaylistRenderer")) {
                    collector.commit(new GridPlaylistRendererExtractor(
                            jsonItem.getObject("gridPlaylistRenderer")));
                } else if (jsonItem.has("continuationItemRenderer")) {
                    continuation = YoutubeParsingHelper.getNextPageFromItem(jsonItem,
                            getExtractorLocalization(), getExtractorContentCountry());
                }
            }
        }
        return continuation;
    }

    @Override
    public InfoItemsPage<PlaylistInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }
        final PlaylistInfoItemsCollector collector = new PlaylistInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonArray continuation = ajaxJson.getArray("onResponseReceivedActions").getObject(0)
                .getObject("appendContinuationItemsAction").getArray("continuationItems");

        final var cont = collectPlaylistsFrom(continuation, collector);

        return new InfoItemsPage<>(collector, cont);
    }

    @Override
    public void onFetchPage(final Downloader downloader) throws IOException, ExtractionException {
        final Localization localization = getExtractorLocalization();
        final byte[] body = JsonWriter
                .string(prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
                        .value("browseId", browseId)
                        .value("params", params)
                        .value("canonicalBaseUrl", canonicalBaseUrl).done())
                .getBytes(UTF_8);

        browseResponse = getJsonPostResponse("browse", body, localization);
        playlistTab = YoutubeParsingHelper.getPlaylistsTab(browseResponse);

        YoutubeParsingHelper.defaultAlertsCheck(browseResponse);
    }

    @Override
    public String getName() throws ParsingException {
        return browseResponse.getObject("metadata").getObject("channelMetadataRenderer")
                .getString("title");
    }

}
