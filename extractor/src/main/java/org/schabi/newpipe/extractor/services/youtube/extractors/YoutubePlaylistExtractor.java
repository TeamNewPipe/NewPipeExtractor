package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getUrlFromNavigationEndpoint;

@SuppressWarnings("WeakerAccess")
public class YoutubePlaylistExtractor extends PlaylistExtractor {
    private JsonObject initialData;
    private JsonObject playlistInfo;

    public YoutubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getUrl() + "&pbj=1";

        final JsonArray ajaxJson = getJsonResponse(url, getExtractorLocalization());

        initialData = ajaxJson.getObject(1).getObject("response");
        YoutubeParsingHelper.defaultAlertsCheck(initialData);

        playlistInfo = getPlaylistInfo();
    }

    private JsonObject getUploaderInfo() throws ParsingException {
        JsonArray items = initialData.getObject("sidebar").getObject("playlistSidebarRenderer").getArray("items");
        try {
            JsonObject uploaderInfo = items.getObject(1).getObject("playlistSidebarSecondaryInfoRenderer")
                    .getObject("videoOwner").getObject("videoOwnerRenderer");
            if (uploaderInfo != null) {
                return uploaderInfo;
            }
        } catch (Exception ignored) {}

        // we might want to create a loop here instead of using duplicated code
        try {
            JsonObject uploaderInfo = items.getObject(items.size()).getObject("playlistSidebarSecondaryInfoRenderer")
                    .getObject("videoOwner").getObject("videoOwnerRenderer");
            if (uploaderInfo != null) {
                return uploaderInfo;
            }
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader info", e);
        }
        throw new ParsingException("Could not get uploader info");
    }

    private JsonObject getPlaylistInfo() throws ParsingException {
        try {
            return initialData.getObject("sidebar").getObject("playlistSidebarRenderer").getArray("items")
                    .getObject(0).getObject("playlistSidebarPrimaryInfoRenderer");
        } catch (Exception e) {
            throw new ParsingException("Could not get PlaylistInfo", e);
        }
    }

    @Override
    public String getNextPageUrl() {
        return getNextPageUrlFrom(initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs").getObject(0).getObject("tabRenderer").getObject("content")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("playlistVideoListRenderer").getArray("continuations"));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            String name = getTextFromObject(playlistInfo.getObject("title"));
            if (name != null) return name;
        } catch (Exception ignored) {}
        try {
            return initialData.getObject("microformat").getObject("microformatDataRenderer").getString("title");
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist name", e);
        }
    }

    @Override
    public Image getThumbnail() throws ParsingException {
        JsonObject thumbnail = null;

        try {
            thumbnail = playlistInfo.getObject("thumbnailRenderer").getObject("playlistVideoThumbnailRenderer")
                    .getObject("thumbnail").getArray("thumbnails").getObject(0);
        } catch (Exception ignored) {}

        if (thumbnail == null) {
            try {
                thumbnail = initialData.getObject("microformat").getObject("microformatDataRenderer")
                        .getObject("thumbnail").getArray("thumbnails").getObject(0);
            } catch (Exception ignored) {}

            if (thumbnail == null) throw new ParsingException("Could not get playlist thumbnail");
        }

        return new Image(fixThumbnailUrl(thumbnail.getString("url")),
                thumbnail.getInt("width"), thumbnail.getInt("height"));
    }

    @Override
    public Image getBanner() {
        return null;      // Banner can't be handled by frontend right now.
        // Whoever is willing to implement this should also implement it in the frontend.
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return getUrlFromNavigationEndpoint(getUploaderInfo().getObject("navigationEndpoint"));
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(getUploaderInfo().getObject("title"));
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader name", e);
        }
    }

    @Override
    public Image getUploaderAvatar() throws ParsingException {
        try {
            JsonObject thumbnail = getUploaderInfo().getObject("thumbnail").getArray("thumbnails").getObject(0);

            return new Image(fixThumbnailUrl(thumbnail.getString("url")),
                    thumbnail.getInt("width"), thumbnail.getInt("height"));
        } catch (Exception e) {
            throw new ParsingException("Could not get playlist uploader avatar", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            String viewsText = getTextFromObject(getPlaylistInfo().getArray("stats").getObject(0));
            return Long.parseLong(Utils.removeNonDigitCharacters(viewsText));
        } catch (Exception e) {
            throw new ParsingException("Could not get video count from playlist", e);
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        JsonArray videos = initialData.getObject("contents").getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs").getObject(0).getObject("tabRenderer").getObject("content")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("playlistVideoListRenderer").getArray("contents");

        collectStreamsFrom(collector, videos);
        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final JsonArray ajaxJson = getJsonResponse(pageUrl, getExtractorLocalization());

        JsonObject sectionListContinuation = ajaxJson.getObject(1).getObject("response")
                .getObject("continuationContents").getObject("playlistVideoListContinuation");

        collectStreamsFrom(collector, sectionListContinuation.getArray("contents"));

        return new InfoItemsPage<>(collector, getNextPageUrlFrom(sectionListContinuation.getArray("continuations")));
    }

    private String getNextPageUrlFrom(JsonArray continuations) {
        if (continuations == null) {
            return "";
        }

        JsonObject nextContinuationData = continuations.getObject(0).getObject("nextContinuationData");
        String continuation = nextContinuationData.getString("continuation");
        String clickTrackingParams = nextContinuationData.getString("clickTrackingParams");
        return "https://www.youtube.com/browse_ajax?ctoken=" + continuation + "&continuation=" + continuation
                + "&itct=" + clickTrackingParams;
    }

    private void collectStreamsFrom(StreamInfoItemsCollector collector, JsonArray videos) {
        collector.reset();

        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object video : videos) {
            if (((JsonObject) video).getObject("playlistVideoRenderer") != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(((JsonObject) video).getObject("playlistVideoRenderer"), timeAgoParser) {
                    @Override
                    public long getViewCount() {
                        return -1;
                    }
                });
            }
        }
    }
}
