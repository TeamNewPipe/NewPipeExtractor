package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

@SuppressWarnings("WeakerAccess")
public class YoutubePlaylistExtractor extends PlaylistExtractor {
    private JsonObject initialData;
    private JsonObject playlistInfo;

    public YoutubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorContentCountry()
                .getCountryCode())
                .value("browseId", "VL" + getId())
                .value("params", "wgYCCAA%3D") // show unavailable videos
                .done())
                .getBytes(UTF_8);

        initialData = getJsonPostResponse("browse", body, getExtractorLocalization());
        YoutubeParsingHelper.defaultAlertsCheck(initialData);

        playlistInfo = getPlaylistInfo();
    }

    private JsonObject getUploaderInfo() throws ParsingException {
        final JsonArray items = initialData.getObject("sidebar")
                .getObject("playlistSidebarRenderer").getArray("items");

        JsonObject videoOwner = items.getObject(1)
                .getObject("playlistSidebarSecondaryInfoRenderer").getObject("videoOwner");
        if (videoOwner.has("videoOwnerRenderer")) {
            return videoOwner.getObject("videoOwnerRenderer");
        }

        // we might want to create a loop here instead of using duplicated code
        videoOwner = items.getObject(items.size())
                .getObject("playlistSidebarSecondaryInfoRenderer").getObject("videoOwner");
        if (videoOwner.has("videoOwnerRenderer")) {
            return videoOwner.getObject("videoOwnerRenderer");
        }
        throw new ParsingException("Could not get uploader info");
    }

    private JsonObject getPlaylistInfo() throws ParsingException {
        try {
            return initialData.getObject("sidebar").getObject("playlistSidebarRenderer")
                    .getArray("items").getObject(0)
                    .getObject("playlistSidebarPrimaryInfoRenderer");
        } catch (final Exception e) {
            throw new ParsingException("Could not get PlaylistInfo", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(playlistInfo.getObject("title"));
        if (!isNullOrEmpty(name)) return name;

        return initialData.getObject("microformat").getObject("microformatDataRenderer").getString("title");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        String url = playlistInfo.getObject("thumbnailRenderer").getObject("playlistVideoThumbnailRenderer")
                .getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");

        if (isNullOrEmpty(url)) {
            url = initialData.getObject("microformat").getObject("microformatDataRenderer").getObject("thumbnail")
                    .getArray("thumbnails").getObject(0).getString("url");

            if (isNullOrEmpty(url)) throw new ParsingException("Could not get playlist thumbnail");
        }

        return fixThumbnailUrl(url);
    }

    @Override
    public String getBannerUrl() {
        // Banner can't be handled by frontend right now.
        // Whoever is willing to implement this should also implement it in the frontend.
        return "";
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            return getUrlFromNavigationEndpoint(getUploaderInfo().getObject("navigationEndpoint"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return getTextFromObject(getUploaderInfo().getObject("title"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader name", e);
        }
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        try {
            final String url = getUploaderInfo().getObject("thumbnail").getArray("thumbnails").getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist uploader avatar", e);
        }
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            final String viewsText = getTextFromObject(getPlaylistInfo().getArray("stats").getObject(0));
            return Long.parseLong(Utils.removeNonDigitCharacters(viewsText));
        } catch (final Exception e) {
            throw new ParsingException("Could not get video count from playlist", e);
        }
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        Page nextPage = null;

        final JsonArray contents = initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer").getArray("tabs").getObject(0)
                .getObject("tabRenderer").getObject("content").getObject("sectionListRenderer")
                .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                .getArray("contents");

        if (contents.getObject(0).has("playlistSegmentRenderer")) {
            for (final Object segment : contents) {
                if (((JsonObject) segment).getObject("playlistSegmentRenderer").has("trailer")) {
                    collectTrailerFrom(collector, ((JsonObject) segment));
                } else if (((JsonObject) segment).getObject("playlistSegmentRenderer")
                        .has("videoList")) {
                    collectStreamsFrom(collector, ((JsonObject) segment)
                            .getObject("playlistSegmentRenderer").getObject("videoList")
                            .getObject("playlistVideoListRenderer").getArray("contents"));
                }
            }

            return new InfoItemsPage<>(collector, null);
        } else if (contents.getObject(0).has("playlistVideoListRenderer")) {
            final JsonObject videos = contents.getObject(0).getObject("playlistVideoListRenderer");
            final JsonArray videosArray = videos.getArray("contents");
            collectStreamsFrom(collector, videosArray);

            nextPage = getNextPageFrom(videosArray);
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final Response response = getDownloader().post(page.getUrl(), null, page.getBody(),
                getExtractorLocalization());
        final JsonObject ajaxJson = JsonUtils.toJsonObject(getValidJsonResponseBody(response));

        final JsonArray continuation = ajaxJson.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems");

        collectStreamsFrom(collector, continuation);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation));
    }

    private Page getNextPageFrom(final JsonArray contents) throws IOException,
            ExtractionException {
        if (isNullOrEmpty(contents)) {
            return null;
        }

        final JsonObject lastElement = contents.getObject(contents.size() - 1);
        if (lastElement.has("continuationItemRenderer")) {
            final String continuation = lastElement
                    .getObject("continuationItemRenderer")
                    .getObject("continuationEndpoint")
                    .getObject("continuationCommand")
                    .getString("token");

            final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorContentCountry()
                    .getCountryCode())
                    .value("continuation", continuation)
                    .done())
                    .getBytes(UTF_8);

            return new Page("https://youtubei.googleapis.com/youtubei/v1/browse?key="
                    + getKey(), body);
        } else {
            return null;
        }
    }

    private void collectStreamsFrom(final StreamInfoItemsCollector collector,
                                    final JsonArray videos) {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Object video : videos) {
            if (((JsonObject) video).has("playlistVideoRenderer")) {
                collector.commit(new YoutubeStreamInfoItemExtractor(((JsonObject) video)
                        .getObject("playlistVideoRenderer"), timeAgoParser) {
                    @Override
                    public long getViewCount() {
                        return -1;
                    }
                });
            }
        }
    }

    private void collectTrailerFrom(final StreamInfoItemsCollector collector,
                                    final JsonObject segment) {
        collector.commit(new StreamInfoItemExtractor() {
            @Override
            public String getName() throws ParsingException {
                return getTextFromObject(segment.getObject("playlistSegmentRenderer")
                        .getObject("title"));
            }

            @Override
            public String getUrl() throws ParsingException {
                return YoutubeStreamLinkHandlerFactory.getInstance()
                        .fromId(segment.getObject("playlistSegmentRenderer").getObject("trailer")
                                .getObject("playlistVideoPlayerRenderer").getString("videoId"))
                        .getUrl();
            }

            @Override
            public String getThumbnailUrl() {
                return "";
                /*final JsonArray thumbnails = initialAjaxJson.getObject(1)
                        .getObject("playerResponse")
                        .getObject("videoDetails").getObject("thumbnail").getArray("thumbnails");
                // the last thumbnail is the one with the highest resolution
                final String url = thumbnails.getObject(thumbnails.size() - 1).getString("url");
                return fixThumbnailUrl(url);*/
            }

            @Override
            public StreamType getStreamType() {
                return StreamType.VIDEO_STREAM;
            }

            @Override
            public boolean isAd() {
                return false;
            }

            @Override
            public long getDuration() throws ParsingException {
                return YoutubeParsingHelper.parseDurationString(
                        getTextFromObject(segment.getObject("playlistSegmentRenderer")
                                .getObject("segmentAnnotation")).split("•")[0]);
            }

            @Override
            public long getViewCount() {
                return -1;
            }

            @Override
            public String getUploaderName() throws ParsingException {
                return YoutubePlaylistExtractor.this.getUploaderName();
            }

            @Override
            public String getUploaderUrl() throws ParsingException {
                return YoutubePlaylistExtractor.this.getUploaderUrl();
            }

            @Override
            public boolean isUploaderVerified() {
                return false;
            }

            @Nullable
            @Override
            public String getTextualUploadDate() {
                return null;
            }

            @Nullable
            @Override
            public DateWrapper getUploadDate() {
                return null;
            }
        });
    }
}
