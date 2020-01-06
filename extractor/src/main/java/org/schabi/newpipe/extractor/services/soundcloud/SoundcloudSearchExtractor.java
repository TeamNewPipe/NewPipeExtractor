package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.*;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE;

public class SoundcloudSearchExtractor extends SearchExtractor {

    private JsonArray searchCollection;

    public SoundcloudSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getSearchSuggestion() {
        return null;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return new InfoItemsPage<>(collectItems(searchCollection), getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return getNextPageUrlFromCurrentUrl(getUrl());
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        final Downloader dl = getDownloader();
        try {
            final String response = dl.get(pageUrl, getExtractorLocalization()).responseBody();
            searchCollection = JsonParser.object().from(response).getArray("collection");
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        return new InfoItemsPage<>(collectItems(searchCollection), getNextPageUrlFromCurrentUrl(pageUrl));
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final Downloader dl = getDownloader();
        final String url = getUrl();
        try {
            final String response = dl.get(url, getExtractorLocalization()).responseBody();
            searchCollection = JsonParser.object().from(response).getArray("collection");
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        if (searchCollection.size() == 0) {
            throw new SearchExtractor.NothingFoundException("Nothing found");
        }
    }

    private InfoItemsCollector<InfoItem, InfoItemExtractor> collectItems(JsonArray searchCollection) {
        final InfoItemsSearchCollector collector = getInfoItemSearchCollector();
        collector.reset();

        for (Object result : searchCollection) {
            if (!(result instanceof JsonObject)) continue;
            //noinspection ConstantConditions
            JsonObject searchResult = (JsonObject) result;
            String kind = searchResult.getString("kind", "");
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

    private String getNextPageUrlFromCurrentUrl(String currentUrl)
            throws MalformedURLException, UnsupportedEncodingException {
        final int pageOffset = Integer.parseInt(
                Parser.compatParseMap(
                        new URL(currentUrl)
                                .getQuery())
                        .get("offset"));

        return currentUrl.replace("&offset=" +
                        Integer.toString(pageOffset),
                "&offset=" + Integer.toString(pageOffset + ITEMS_PER_PAGE));
    }
}
