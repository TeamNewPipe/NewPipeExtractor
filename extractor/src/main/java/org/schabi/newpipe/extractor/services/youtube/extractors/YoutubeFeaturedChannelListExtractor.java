package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.getChannelResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.resolveChannelId;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector;
import org.schabi.newpipe.extractor.channel.list.ChannelListExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class YoutubeFeaturedChannelListExtractor extends ChannelListExtractor {

    private  YoutubeChannelHelper.ChannelHeader channelHeader;
    private JsonObject jsonResponse;

    private JsonObject jsonRendererListData;

    private String channelId;

    private final int rendererListIndex;

    public YoutubeFeaturedChannelListExtractor(final StreamingService service,
                                               final ListLinkHandler linkHandler)
            throws ExtractionException {
        super(service, linkHandler);

        final Optional<String> rendererlist = linkHandler.getContentFilters().stream()
                .filter(filter -> filter.matches("^rendererlist_index=(\\d+)$"))
                .findFirst();

        if (rendererlist.isEmpty()) {
            throw new ExtractionException("content filter for featured channels"
                    + " must contain rendererlist_index={index}"
                    + " to extract featured channels from featured tab");
        }

        this.rendererListIndex =  YoutubeParsingHelper
                .parseParamFormatRendererListIndex(rendererlist.get());
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        try {
            return YoutubeChannelTabLinkHandlerFactory.getInstance()
                    .getUrl("channel/" + getId(), List.of(ChannelTabs.FEATURED), "");
        } catch (final ParsingException e) {
            return super.getUrl();
        }
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        return YoutubeChannelHelper.getChannelId(channelHeader, jsonResponse, channelId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String channelIdFromId = resolveChannelId(super.getId());

        final YoutubeChannelHelper.ChannelResponseData data = getChannelResponse(
                channelIdFromId,
                "EghmZWF0dXJlZPIGBAoCMgA%3D",
                getExtractorLocalization(),
                getExtractorContentCountry());

        jsonResponse = data.jsonResponse;
        channelHeader = YoutubeChannelHelper.getChannelHeader(jsonResponse);
        channelId = data.channelId;
        jsonRendererListData = YoutubeParsingHelper
                .getFeaturedTabRendererListData(this.jsonResponse, this.rendererListIndex);

        if (!this.jsonRendererListData.getObject("shelfRenderer")
                .getObject("content")
                .getObject("horizontalListRenderer")
                .getArray("items")
                .getObject(0)
                .has("gridChannelRenderer")) {
            throw new ExtractionException("rendererlist index does not point to featured channels");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<ChannelInfoItem> getInitialPage() throws IOException, ExtractionException {
        final JsonObject continuation = this.jsonRendererListData
                .getObject("shelfRenderer")
                .getObject("endpoint")
                .getObject("showEngagementPanelEndpoint")
                .getObject("engagementPanel")
                .getObject("engagementPanelSectionListRenderer")
                .getObject("content")
                .getObject("sectionListRenderer")
                .getArray("contents")
                .getObject(0)
                .getObject("itemSectionRenderer")
                .getArray("contents")
                .getObject(0)
                .getObject("continuationItemRenderer");

        final VerifiedStatus verifiedStatus;
        if (channelHeader == null) {
            verifiedStatus = VerifiedStatus.UNKNOWN;
        } else {
            verifiedStatus = YoutubeChannelHelper
                    .isChannelVerified(channelHeader)
                    ? VerifiedStatus.VERIFIED
                    : VerifiedStatus.UNVERIFIED;
        }

        final Page firstPage = getNextPageFrom(
                continuation, List.of(getChannelName(),
                        getLinkHandler().getOriginalUrl(),
                        verifiedStatus.toString()));

        return getPage(firstPage);
    }

    @Override
    public InfoItemsPage<ChannelInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final List<String> channelIds = page.getIds();

        final ChannelInfoItemsCollector collector = new ChannelInfoItemsCollector(getServiceId());

        final JsonObject ajaxJson = getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization());

        final JsonObject gridRendererContinuation = ajaxJson.getArray("onResponseReceivedEndpoints")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems")
                .getObject(0)
                .getObject("gridRenderer");

        final JsonObject continuation = collectItemsFrom(collector,
                gridRendererContinuation.getArray("items"))
                .orElse(null);

        return new InfoItemsPage<>(collector, getNextPageFrom(continuation, channelIds));
    }

    private Optional<JsonObject> collectItemsFrom(
            @Nonnull final ChannelInfoItemsCollector collector,
            @Nonnull final JsonArray items) {
        return items.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(item ->
                        collectItem(collector, item)
                )
                .reduce(Optional.empty(), (c1, c2) -> c1.or(() -> c2));
    }

    private Optional<JsonObject> collectItem(@Nonnull final ChannelInfoItemsCollector collector,
                                             @Nonnull final JsonObject item) {
        if (item.has("gridChannelRenderer")) {
            commitFeaturedChannel(collector, item.getObject("gridChannelRenderer"));
        } else if (item.has("continuationItemRenderer")) {
            return Optional.ofNullable(item.getObject("continuationItemRenderer"));
        }

        return Optional.empty();
    }

    public void commitFeaturedChannel(final ChannelInfoItemsCollector collector,
                                      final JsonObject featuredChannelInfoItem) {
        collector.commit(new YoutubeChannelInfoItemExtractor(featuredChannelInfoItem));
    }

    @Nullable
    private Page getNextPageFrom(final JsonObject continuations,
                                 final List<String> channelIds) throws IOException,
            ExtractionException {
        if (isNullOrEmpty(continuations)) {
            return null;
        }

        final JsonObject continuationEndpoint = continuations.getObject("continuationEndpoint");
        final String continuation = continuationEndpoint.getObject("continuationCommand")
                .getString("token");

        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry())
                        .value("continuation", continuation)
                        .done())
                .getBytes(StandardCharsets.UTF_8);

        return new Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, null,
                channelIds, null, body);
    }

    protected String getChannelName() throws ParsingException {
        return YoutubeChannelHelper.getChannelName(channelHeader,
                YoutubeChannelHelper.getChannelAgeGateRenderer(jsonResponse),
                jsonResponse);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        try {
            final JsonObject title = this.jsonRendererListData.getObject("shelfRenderer")
                    .getObject("title");
            final String name = getTextFromObject(title);

            if (name == null) {
                return "";
            }

            return name;
        } catch (final Exception e) {
            throw new ParsingException("Could not get name", e);
        }
    }

    /**
     * Enum representing the verified state of a channel
     */
    private enum VerifiedStatus {
        VERIFIED,
        UNVERIFIED,
        UNKNOWN
    }
}
