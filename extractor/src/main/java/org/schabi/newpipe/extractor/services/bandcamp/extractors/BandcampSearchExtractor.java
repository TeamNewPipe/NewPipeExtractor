// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampSearchExtractor extends SearchExtractor {

    public BandcampSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getSearchSuggestion() {
        return null;
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        // okay apparently this is where we DOWNLOAD the page and then COMMIT its ENTRIES to an INFOITEMPAGE
        String html = getDownloader().get(pageUrl).responseBody();

        InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());


        Document d = Jsoup.parse(html);

        Elements searchResultsElements = d.getElementsByClass("searchresult");

        for (Element searchResult :
                searchResultsElements) {

            Element resultInfo = searchResult.getElementsByClass("result-info").first();

            String type = resultInfo
                    .getElementsByClass("itemtype").first().text();

            String image = null;
            Element img = searchResult.getElementsByClass("art").first()
                    .getElementsByTag("img").first();
            if (img != null) {
                image = img.attr("src");
            }

            String heading = resultInfo.getElementsByClass("heading").text();

            String subhead = resultInfo.getElementsByClass("subhead").text();

            String url = resultInfo.getElementsByClass("itemurl").text();

            switch (type) {
                default:
                    continue;
                case "FAN":
                    //collector.commit Channel (?) with heading, url, image
                    break;

                case "ARTIST":
                    collector.commit(new BandcampChannelInfoItemExtractor(heading, url, image, subhead));
                    break;

                case "ALBUM":
                    String artist = subhead.split(" by")[0];
                    String length = resultInfo.getElementsByClass("length").text();
                    int tracks = Integer.parseInt(length.split(" track")[0]);
                    collector.commit(new BandcampPlaylistInfoItemExtractor(heading, artist, url, image, tracks));
                    break;

                case "TRACK":
                    String[] splitBy = subhead.split(" by");
                    String artist1 = null;
                    if (splitBy.length > 1) {
                        artist1 = subhead.split(" by")[1];
                    }
                    collector.commit(new BandcampStreamInfoItemExtractor(heading, url, image, artist1));
                    break;
            }

        }

        // Count pages
        Elements pageLists = d.getElementsByClass("pagelist");
        if (pageLists.size() == 0)
            return new InfoItemsPage<>(collector, null);

        Elements pages = pageLists.first().getElementsByTag("li");

        // Find current page
        int currentPage = -1;
        for (int i = 0; i < pages.size(); i++) {
            Element page = pages.get(i);
            if (page.getElementsByTag("span").size() > 0) {
                currentPage = i + 1;
                break;
            }
        }

        // Search results appear to be capped at six pages
        assert pages.size() < 10;

        String nextUrl = null;
        if (currentPage < pages.size()) {
            nextUrl = pageUrl.substring(0, pageUrl.length() - 1) + (currentPage + 1);
        }

        return new InfoItemsPage<>(collector, nextUrl);

    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        String url = getUrl();
        return url.substring(0, url.length() - 1).concat("2");
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

    }
}
