package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelPlaylistsExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;

public class YoutubeChannelPlaylistsExtractor extends ChannelPlaylistsExtractor {
    private JsonObject initialData;
    private JsonObject playlistsTab;
    private String channelName;

    public YoutubeChannelPlaylistsExtractor(StreamingService service, ListLinkHandler linkHandler, String channelName) {
        super(service, linkHandler);

        this.channelName = channelName;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = super.getUrl() + "/playlists?pbj=1&view=1&flow=grid";

        final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());

        initialData = ajaxJson.getObject(1).getObject("response");
        YoutubeParsingHelper.defaultAlertsCheck(initialData);
        playlistsTab = getTab("Playlists");
    }

    @Override
    public String getNextPageUrl() {
        return getNextPageUrlFrom(playlistsTab.getObject("content").getObject("sectionListRenderer")
                .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                .getArray("contents").getObject(0).getObject("gridRenderer").getArray("continuations"));
    }
    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Playlists";
    }

    @Nonnull
    @Override
    public InfoItemsPage<PlaylistInfoItem> getInitialPage() throws ExtractionException {
        PlaylistInfoItemsCollector collector = new PlaylistInfoItemsCollector(getServiceId());

        JsonArray playlists = playlistsTab.getObject("content").getObject("sectionListRenderer").getArray("contents")
                .getObject(0).getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("gridRenderer").getArray("items");
        collectPlaylistsFrom(collector, playlists);

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public InfoItemsPage<PlaylistInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        // Unfortunately, we have to fetch the page even if we are only getting next streams,
        // as they don't deliver enough information on their own (the channel name, for example).
        fetchPage();

        PlaylistInfoItemsCollector collector = new PlaylistInfoItemsCollector(getServiceId());
        final JsonArray ajaxJson = getJsonResponse(pageUrl, getExtractorLocalization());

        JsonObject sectionListContinuation = ajaxJson.getObject(1).getObject("response")
                .getObject("continuationContents").getObject("gridContinuation");

        collectPlaylistsFrom(collector, sectionListContinuation.getArray("items"));

        return new InfoItemsPage<>(collector, getNextPageUrlFrom(sectionListContinuation.getArray("continuations")));
    }


    private String getNextPageUrlFrom(JsonArray continuations) {
        if (continuations == null) return "";

        JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        String continuation = nextContinuationData.getString("continuation");
        String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");
        return "https://www.youtube.com/browse_ajax?ctoken=" + continuation + "&continuation=" + continuation
                + "&itct=" + clickTrackingParams;
    }

    private void collectPlaylistsFrom(PlaylistInfoItemsCollector collector, JsonArray playlists) throws ParsingException {
        collector.reset();

        for (Object playlist : playlists) {
            if (((JsonObject) playlist).getObject("gridPlaylistRenderer") != null) {
                collector.commit(new YoutubePlaylistInfoItemExtractor(((JsonObject) playlist).getObject("gridPlaylistRenderer")) {
                    @Override
                    public String getUploaderName() {
                        return channelName;
                    }
                });
            }
        }
    }

    private JsonObject getTab(String tabName) {
        JsonArray tabs = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs");
        JsonObject videoTab = null;

        for (Object tab : tabs) {
            if (((JsonObject) tab).getObject("tabRenderer") != null) {
                if (((JsonObject) tab).getObject("tabRenderer").getString("title").equals(tabName)) {
                    videoTab = ((JsonObject) tab).getObject("tabRenderer");
                    break;
                }
            }
        }

        try {
            if (getTextFromObject(videoTab.getObject("content").getObject("sectionListRenderer")
                    .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                    .getArray("contents").getObject(0).getObject("messageRenderer")
                    .getObject("text")).startsWith("This channel has no "))
                return null;
        } catch (Exception ignored) {}

        return videoTab;
    }
}
