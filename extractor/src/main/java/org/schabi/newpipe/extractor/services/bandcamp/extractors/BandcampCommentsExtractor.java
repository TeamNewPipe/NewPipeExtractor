package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.annotation.Nonnull;

public class BandcampCommentsExtractor extends CommentsExtractor {

    private static final String REVIEWS_API_URL = BASE_API_URL + "/tralbumcollectors/2/reviews";

    private Document document;


    public BandcampCommentsExtractor(final StreamingService service,
                                     final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        document = Jsoup.parse(downloader.get(getLinkHandler().getUrl()).responseBody());
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());

        final JsonObject collectorsData = JsonUtils.toJsonObject(
                document.getElementById("collectors-data").attr("data-blob"));
        final JsonArray reviews = collectorsData.getArray("reviews");

        for (final Object review : reviews) {
            collector.commit(
                    new BandcampCommentsInfoItemExtractor((JsonObject) review, getUrl()));
        }

        if (!collectorsData.getBoolean("more_reviews_available")) {
            return new InfoItemsPage<>(collector, null);
        }

        final String trackId = getTrackId();
        final String token = getNextPageToken(reviews);
        return new InfoItemsPage<>(collector, new Page(trackId, token));
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {

        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());

        final String trackId = page.getUrl();
        final String token = page.getId();
        final JsonObject reviewsData = fetchReviewsData(trackId, token);
        final JsonArray reviews = reviewsData.getArray("results");

        for (final Object review : reviews) {
            collector.commit(
                    new BandcampCommentsInfoItemExtractor((JsonObject) review, getUrl()));
        }

        if (!reviewsData.getBoolean("more_available")) {
            return new InfoItemsPage<>(collector, null);
        }

        final String nextToken = getNextPageToken(reviews);
        return new InfoItemsPage<>(collector, new Page(trackId, nextToken));
    }

    private JsonObject fetchReviewsData(final String trackId, final String token)
            throws ParsingException {
        try {
            return JsonUtils.toJsonObject(getDownloader().postWithContentTypeJson(
                    REVIEWS_API_URL,
                    Collections.emptyMap(),
                    JsonWriter.string().object()
                            .value("tralbum_type", "t")
                            .value("tralbum_id", trackId)
                            .value("token", token)
                            .value("count", 7)
                            .array("exclude_fan_ids").end()
                            .end().done().getBytes(StandardCharsets.UTF_8)).responseBody());
        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not fetch reviews", e);
        }
    }

    private String getNextPageToken(final JsonArray reviews) throws ParsingException {
        return reviews.stream().map(JsonObject.class::cast)
                .map(o -> o.getString("token"))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new ParsingException("Could not get token"));
    }

    private String getTrackId() throws ParsingException {
        final JsonObject pageProperties = JsonUtils.toJsonObject(
                document.selectFirst("meta[name=bc-page-properties]")
                        .attr("content"));
        return Long.toString(pageProperties.getLong("item_id"));
    }
}
