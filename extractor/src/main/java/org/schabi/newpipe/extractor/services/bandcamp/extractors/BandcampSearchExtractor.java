// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampSearchStreamInfoItemExtractor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class BandcampSearchExtractor extends SearchExtractor {

    public BandcampSearchExtractor(final StreamingService service,
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
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return Collections.emptyList();
    }

    public InfoItemsPage<InfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        final Document d = Jsoup.parse(getDownloader().get(page.getUrl()).responseBody());

        for (final Element searchResult : d.getElementsByClass("searchresult")) {
            final String type = searchResult.getElementsByClass("result-info").stream()
                    .flatMap(element -> element.getElementsByClass("itemtype").stream())
                    .map(Element::text)
                    .findFirst()
                    .orElse("");

            switch (type) {
                case "ARTIST":
                    collector.commit(new BandcampChannelInfoItemExtractor(searchResult));
                    break;
                case "ALBUM":
                    collector.commit(new BandcampPlaylistInfoItemExtractor(searchResult));
                    break;
                case "TRACK":
                    collector.commit(new BandcampSearchStreamInfoItemExtractor(searchResult, null));
                    break;
                default:
                    // don't display fan results ("FAN") or other things
                    break;
            }
        }

        // Count pages
        final Elements pageLists = d.getElementsByClass("pagelist");
        if (pageLists.isEmpty()) {
            return new InfoItemsPage<>(collector, null);
        }

        final Elements pages = pageLists.stream()
                .map(element -> element.getElementsByTag("li"))
                .findFirst()
                .orElseGet(Elements::new);

        // Find current page
        int currentPage = -1;
        for (int i = 0; i < pages.size(); i++) {
            final Element pageElement = pages.get(i);
            if (!pageElement.getElementsByTag("span").isEmpty()) {
                currentPage = i + 1;
                break;
            }
        }

        // Search results appear to be capped at six pages
        assert pages.size() < 10;

        String nextUrl = null;
        if (currentPage < pages.size()) {
            nextUrl = page.getUrl().substring(0, page.getUrl().length() - 1) + (currentPage + 1);
        }

        return new InfoItemsPage<>(collector, new Page(nextUrl));

    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(getUrl()));
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
    }
}
