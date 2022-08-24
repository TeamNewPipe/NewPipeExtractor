package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.IntUnaryOperator;

import javax.annotation.Nonnull;

public class SoundcloudSearchExtractor extends SearchExtractor {
    private JsonArray initialSearchCollection;

    public SoundcloudSearchExtractor(final StreamingService service,
                                     final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() {
        return "";
    }

    @Override
    public boolean isCorrectedSearch() {
        return false;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return new InfoItemsPage<>(
                collectItems(initialSearchCollection),
                getNextPageFromCurrentUrl(getUrl(), currentOffset -> ITEMS_PER_PAGE));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Downloader dl = getDownloader();
        final JsonArray searchCollection;
        try {
            final String response = dl.get(page.getUrl(), getExtractorLocalization())
                    .responseBody();
            searchCollection = JsonParser.object().from(response).getArray("collection");
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        return new InfoItemsPage<>(collectItems(searchCollection),
                getNextPageFromCurrentUrl(page.getUrl(),
                        currentOffset -> currentOffset + ITEMS_PER_PAGE));
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final Downloader dl = getDownloader();
        final String url = getUrl();
        try {
            final String response = dl.get(url, getExtractorLocalization()).responseBody();
            initialSearchCollection = JsonParser.object().from(response).getArray("collection");
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        if (initialSearchCollection.isEmpty()) {
            throw new SearchExtractor.NothingFoundException("Nothing found");
        }
    }

    private InfoItemsCollector<InfoItem, InfoItemExtractor> collectItems(
            final JsonArray searchCollection) {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        for (final Object result : searchCollection) {
            if (!(result instanceof JsonObject)) {
                continue;
            }

            final JsonObject searchResult = (JsonObject) result;
            final String kind = searchResult.getString("kind", "");
            switch (kind) {
                case "user":
                    collector.commit(new SoundcloudChannelInfoItemExtractor(searchResult));
                    break;
                case "track":
                    collector.commit(new SoundcloudStreamInfoItemExtractor(searchResult));
                    break;
                case "playlist":
                    collector.commit(new SoundcloudPlaylistInfoItemExtractor(searchResult));
                    break;
            }
        }

        return collector;
    }

    private Page getNextPageFromCurrentUrl(final String currentUrl,
                                           final IntUnaryOperator newPageOffsetCalculator)
            throws MalformedURLException, UnsupportedEncodingException {
        final int currentPageOffset = Integer.parseInt(
                    Parser.compatParseMap(new URL(currentUrl).getQuery()).get("offset"));

        return new Page(
                currentUrl.replace(
                        "&offset=" + currentPageOffset,
                        "&offset=" + newPageOffsetCalculator.applyAsInt(currentPageOffset)));
    }
}
