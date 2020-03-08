package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MixedInfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper.getJsonResponse;

public class YoutubeChannelVideosExtractor extends ChannelTabExtractor {
    private JsonObject videoTab;
    private String channelName;

    public YoutubeChannelVideosExtractor(StreamingService service, ListLinkHandler linkHandler, JsonObject videoTab, String channelName) {
        super(service, linkHandler);

        this.videoTab = videoTab;
        this.channelName = channelName;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Override
    public String getNextPageUrl() throws ExtractionException {
        return getNextPageUrlFrom(videoTab.getObject("content").getObject("sectionListRenderer")
                .getArray("contents").getObject(0).getObject("itemSectionRenderer")
                .getArray("contents").getObject(0).getObject("gridRenderer").getArray("continuations"));
    }
    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Videos";
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
        MixedInfoItemsCollector collector = new MixedInfoItemsCollector(getServiceId());

        JsonArray videos = videoTab.getObject("content").getObject("sectionListRenderer").getArray("contents")
                .getObject(0).getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("gridRenderer").getArray("items");
        collectStreamsFrom(collector, videos);

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        MixedInfoItemsCollector collector = new MixedInfoItemsCollector(getServiceId());
        final JsonArray ajaxJson = getJsonResponse(pageUrl, getExtractorLocalization());

        if (ajaxJson.getObject(1).getObject("response").getObject("continuationContents") == null)
            return null;

        JsonObject sectionListContinuation = ajaxJson.getObject(1).getObject("response")
                .getObject("continuationContents").getObject("gridContinuation");

        if (sectionListContinuation.getArray("items") == null)
            return null;

        collectStreamsFrom(collector, sectionListContinuation.getArray("items"));

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

    private void collectStreamsFrom(MixedInfoItemsCollector collector, JsonArray videos) throws ParsingException {
        collector.reset();

        final String uploaderUrl = getUrl();
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (Object video : videos) {
            if (((JsonObject) video).getObject("gridVideoRenderer") != null) {
                collector.commit(new YoutubeStreamInfoItemExtractor(
                        ((JsonObject) video).getObject("gridVideoRenderer"), timeAgoParser) {
                    @Override
                    public String getUploaderName() {
                        return channelName;
                    }

                    @Override
                    public String getUploaderUrl() {
                        return uploaderUrl;
                    }
                });
            }
        }
    }
}
