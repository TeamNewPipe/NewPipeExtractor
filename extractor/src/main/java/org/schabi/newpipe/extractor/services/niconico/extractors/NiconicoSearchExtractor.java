package org.schabi.newpipe.extractor.services.niconico.extractors;

import static org.schabi.newpipe.extractor.services.niconico.linkHandler.NiconicoSearchQueryHandlerFactory.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.niconico.NiconicoService;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class NiconicoSearchExtractor extends SearchExtractor {
    private JsonObject searchCollection;

    public NiconicoSearchExtractor(final StreamingService service,
                                   final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(final @Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        final String response = getDownloader().get(
                getLinkHandler().getUrl(), NiconicoService.LOCALE).responseBody();
        try {
            searchCollection = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ExtractionException("could not parse search results.");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return new InfoItemsPage<>(collectItems(searchCollection),
                getNextPageFromCurrentUrl(getUrl()));
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw  new IllegalArgumentException("page does not contain an URL.");
        }

        final String response = getDownloader().get(
                page.getUrl(), NiconicoService.LOCALE).responseBody();

        try {
            searchCollection = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("could not parse search results.");
        }

        return new InfoItemsPage<>(collectItems(searchCollection),
                getNextPageFromCurrentUrl(page.getUrl()));
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        return "";
    }

    @Override
    public boolean isCorrectedSearch() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return Collections.emptyList();
    }

    private InfoItemsCollector<InfoItem, InfoItemExtractor> collectItems(
            final JsonObject collection) {
        final InfoItemsSearchCollector collector
                = new InfoItemsSearchCollector(getServiceId());

        for (int i = 0; i < collection.getArray("data").size(); i++) {
            collector.commit(
                    new NiconicoStreamInfoItemExtractor(
                            collection.getArray("data").getObject(i)));
        }

        return collector;
    }

    private Page getNextPageFromCurrentUrl(final String currentUrl)
            throws ParsingException {
        final String offset = "&_offset=(\\d+?)";
        try {
            final int pageOffset = Integer.parseInt(Parser.matchGroup1(offset, currentUrl));
            return new Page(currentUrl.replace("&_offset=" + pageOffset, "&_offset="
                    + (pageOffset + ITEMS_PER_PAGE)));
        } catch (final Parser.RegexException e) {
            throw new ParsingException("could not parse search queries.");
        }
    }
}
