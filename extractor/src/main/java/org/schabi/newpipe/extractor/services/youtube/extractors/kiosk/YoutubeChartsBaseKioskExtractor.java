package org.schabi.newpipe.extractor.services.youtube.extractors.kiosk;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.youtube.InnertubeClientRequestInfo;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getClientHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getOriginReferrerHeaders;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * Base class parsing responses from YouTube Charts for all trending video charts.
 *
 * <p>
 * Note: YouTube Charts isn't officially supported in all YouTube supported countries (there are
 * fewer countries in the {@code LAUNCHED_CHART_COUNTRIES} array of YouTube Charts' HTML responses
 * than in the YouTube country selector).
 * </p>
 *
 * <p>
 * For some trends, some videos are still returned in unsupported countries, even if there are
 * fewer than in a supported country, for others an HTTP 400 error is returned saying
 * {@code Request contains an invalid argument.}.
 * </p>
 */
abstract class YoutubeChartsBaseKioskExtractor extends KioskExtractor<StreamInfoItem> {

    // Extracted from YouTube Charts' HTML, in the array named LAUNCHED_CHART_COUNTRIES
    protected static final Set<String> YT_CHARTS_SUPPORTED_COUNTRY_CODES = Set.of(
            "AE", "AR", "AT", "AU", "BE", "BO", "BR", "CA", "CH", "CL", "CO", "CR", "CZ", "DE",
            "DK", "DO", "EC", "EE", "EG", "ES", "FI", "FR", "GB", "GT", "HN", "HU", "ID", "IE",
            "IL", "IN", "IS", "IT", "JP", "KE", "KR", "LU", "MX", "NG", "NI", "NL", "NO", "NZ",
            "PA", "PE", "PL", "PT", "PY", "RO", "RS", "RU", "SA", "SE", "SV", "TR", "TZ", "UA",
            "UG", "US", "UY", "ZA", "ZW");

    protected static final String YT_CHARTS_ENDPOINT =
            "https://charts.youtube.com/youtubei/v1/browse?alt=json&"
                    + DISABLE_PRETTY_PRINT_PARAMETER;

    protected final String chartType;
    protected JsonObject browseResponse;

    protected YoutubeChartsBaseKioskExtractor(final StreamingService streamingService,
                                              final ListLinkHandler linkHandler,
                                              final String kioskId,
                                              final String chartType) {
        super(streamingService, linkHandler, kioskId);
        this.chartType = chartType;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final Localization localization = getExtractorLocalization();
        final ContentCountry contentCountry = getExtractorContentCountry();

        final InnertubeClientRequestInfo innertubeClientRequestInfo =
                InnertubeClientRequestInfo.ofWebMusicAnalyticsChartsClient();

        final byte[] body = JsonWriter.string(prepareJsonBuilder(getExtractorLocalization(),
                contentCountry, innertubeClientRequestInfo, null)
                .value("browseId", "FEmusic_analytics_charts_home")
                .value("query", "perspective=CHART_DETAILS&chart_params_country_code="
                        + contentCountry.getCountryCode() + "&chart_params_chart_type="
                        + chartType)
                .done())
                .getBytes(StandardCharsets.UTF_8);

        final var headers = new HashMap<>(getOriginReferrerHeaders("https://charts.youtube.com"));
        headers.putAll(getClientHeaders(innertubeClientRequestInfo.clientInfo.clientId,
                innertubeClientRequestInfo.clientInfo.clientVersion));

        browseResponse = JsonUtils.toJsonObject(getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        YT_CHARTS_ENDPOINT, headers, body, localization)));
    }

    @Nonnull
    @Override
    public abstract String getName() throws ParsingException;

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final JsonArray videos = browseResponse.getObject("contents")
                .getObject("sectionListRenderer")
                .getArray("contents")
                .getObject(0)
                .getObject("musicAnalyticsSectionRenderer")
                .getObject("content")
                .getArray("videos")
                .getObject(0)
                .getArray("videoViews");

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        videos.streamAsJsonObjects()
                .forEachOrdered(video -> collector.commit(
                        new YoutubeChartsVideoInfoItemExtractor(video)));

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) {
        // There is no continuation in charts
        return InfoItemsPage.emptyPage();
    }

    static final class YoutubeChartsVideoInfoItemExtractor
            implements StreamInfoItemExtractor {

        @Nonnull
        private final JsonObject videoObject;

        YoutubeChartsVideoInfoItemExtractor(@Nonnull final JsonObject videoObject) {
            this.videoObject = videoObject;
        }

        @Override
        public StreamType getStreamType() {
            // There are only video streams in YouTube Charts, at least for now
            return StreamType.VIDEO_STREAM;
        }

        @Override
        public boolean isAd() {
            return false;
        }

        @Override
        public long getDuration() {
            return videoObject.getInt("videoDuration", -1);
        }

        @Override
        public long getViewCount() {
            // View counts aren't returned, at least for now
            return -1;
        }

        @Override
        public String getUploaderName() {
            return videoObject.getString("channelName");
        }

        @Override
        public String getUploaderUrl() throws ParsingException {
            final String channelId = videoObject.getString("externalChannelId");

            if (isNullOrEmpty(channelId)) {
                throw new ParsingException("Could not get channel ID");
            }

            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl("channel/" + channelId);
        }

        @Override
        public boolean isUploaderVerified() {
            // We don't have any info on this, at least for now
            return false;
        }

        @Nullable
        @Override
        public String getTextualUploadDate() {
            return null;
        }

        @Nonnull
        @Override
        public DateWrapper getUploadDate() {
            final JsonObject releaseDate = videoObject.getObject("releaseDate");
            final var localDate = LocalDate.of(releaseDate.getInt("year"),
                    releaseDate.getInt("month"), releaseDate.getInt("day"));
            // We request that times should be returned with 0 offset to UTC timezone in
            // the JSON body, but YouTube charts does it only in its HTTP headers
            final var instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();

            // We don't have more info than the release day
            return new DateWrapper(instant, true);
        }

        @Override
        public String getName() {
            return videoObject.getString("title");
        }

        @Override
        public String getUrl() throws ParsingException {
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(
                    videoObject.getString("id"));
        }

        @Nonnull
        @Override
        public List<Image> getThumbnails() throws ParsingException {
            return getThumbnailsFromInfoItem(videoObject);
        }
    }
}
