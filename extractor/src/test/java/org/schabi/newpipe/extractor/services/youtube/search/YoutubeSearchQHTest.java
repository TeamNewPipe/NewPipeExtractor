package org.schabi.newpipe.extractor.services.youtube.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.Filter;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.Sorter;
import org.schabi.newpipe.extractor.utils.Localization;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

public class YoutubeSearchQHTest {

    private static final String DEFAULT_SEARCH_QUERY = "asdf";

    @BeforeClass
    public static void setupClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("GB", "en"));
    }

    @Test
    public void testRegularValues() throws Exception {
        assertEquals("https://www.youtube.com/results?q=" + DEFAULT_SEARCH_QUERY, YouTube.getSearchQHFactory().fromQuery(DEFAULT_SEARCH_QUERY).getUrl());
        assertEquals("https://www.youtube.com/results?q=hans", YouTube.getSearchQHFactory().fromQuery("hans").getUrl());
        assertEquals("https://www.youtube.com/results?q=Poifj%26jaijf", YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").getUrl());
        assertEquals("https://www.youtube.com/results?q=G%C3%BCl%C3%BCm", YouTube.getSearchQHFactory().fromQuery("Gülüm").getUrl());
        assertEquals("https://www.youtube.com/results?q=%3Fj%24%29H%C2%A7B", YouTube.getSearchQHFactory().fromQuery("?j$)H§B").getUrl());
    }

    @Test
    public void testGetContentFilter() throws Exception {
        assertEquals(Filter.Video.name(), YouTube.getSearchQHFactory()
                .fromQuery("", Collections.singletonList(Filter.Video.name()), "").getContentFilters().get(0));
        assertEquals(Filter.Channel.name(), YouTube.getSearchQHFactory()
                .fromQuery(DEFAULT_SEARCH_QUERY, Collections.singletonList(Filter.Channel.name()), "").getContentFilters().get(0));
    }

    @Test
    public void testWithChannelContentFilter() throws Exception {
        String url = getYouTubeDefaultSearchQueryHandler(
                Collections.singletonList(Filter.Channel.name()),
                ""
        ).getUrl();
        String html = getHtml(url);
        Document document = Jsoup.parse(html);
        Elements filterList = getFilterList(document);
        Element matchingElement = getMatchingElement(
                Filter.Channel.name(),
                filterList
        );
        if (matchingElement == null) {
            fail("Channel filter has not been selected");
        }
    }

    @Test
    public void testWithRatingSortFilter() throws Exception {
        String url = getYouTubeDefaultSearchQueryHandler(
                Collections.<String>emptyList(),
                Sorter.Rating.name()
        ).getUrl();
        String html = getHtml(url);
        Document document = Jsoup.parse(html);
        Elements filterList = getFilterList(document);
        Element matchingElement = getMatchingElement(
                Sorter.Rating.name(),
                filterList
        );
        if (matchingElement == null) {
            fail("Rating sorter has not been selected");
        }
    }

    @Test
    public void testWithVideoAndShortContentFilter() throws Exception {
        String url = getYouTubeDefaultSearchQueryHandler(
                Arrays.asList(Filter.Video.name(), Filter.Short.name()),
                ""
        ).getUrl();
        String html = getHtml(url);
        Document document = Jsoup.parse(html);
        Elements filterList = getFilterList(document);
        Element videoMatchingElement = getMatchingElement(
                Filter.Video.name(),
                filterList
        );
        Element shortMatchingElement = getMatchingElement(
                Filter.Short.name(),
                filterList
        );
        if (videoMatchingElement == null) {
            fail("Channel filter has not been selected");
        }
        if (shortMatchingElement == null) {
            fail("Short filter has not been selected");
        }
    }

    @Test
    public void testWithChannelContentFilterAndRatingSortFilter() throws Exception {
        String url = getYouTubeDefaultSearchQueryHandler(
                Collections.singletonList(Filter.Channel.name()),
                Sorter.Rating.name()
        ).getUrl();
        String html = getHtml(url);
        Document document = Jsoup.parse(html);
        Elements filterList = getFilterList(document);
        Element channelMatchingElement = getMatchingElement(
                Filter.Channel.name(),
                filterList
        );
        Element ratingMatchingElement = getMatchingElement(
                Sorter.Rating.name(),
                filterList
        );
        if (channelMatchingElement == null) {
            fail("Channel filter has not been selected");
        }
        if (ratingMatchingElement == null) {
            fail("Rating sorter has not been selected");
        }
    }

    @Test
    public void testWithGibberishContentFilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=" + DEFAULT_SEARCH_QUERY, YouTube.getSearchQHFactory()
                .fromQuery(DEFAULT_SEARCH_QUERY, Collections.singletonList("gibberish"), "").getUrl());
    }

    @Test
    public void testWithGibbershSortFilter() throws Exception {
        assertEquals("https://www.youtube.com/results?q=" + DEFAULT_SEARCH_QUERY, YouTube.getSearchQHFactory()
                .fromQuery(DEFAULT_SEARCH_QUERY, Collections.<String>emptyList(), "gibberish").getUrl());
    }

    @Test
    public void testGetAvailableContentFilter() {
        final String[] contentFilter = YouTube.getSearchQHFactory().getAvailableContentFilter();
        assertEquals(Filter.values().length, contentFilter.length);
    }

    @Test
    public void testGetAvailableSortFilter() {
        final String[] sortFilters = YouTube.getSearchQHFactory().getAvailableSortFilter();
        assertEquals(Sorter.values().length, sortFilters.length);
    }

    private SearchQueryHandler getYouTubeDefaultSearchQueryHandler(
            List<String> filters,
            String sorter) throws Exception {
        return getYouTubeSearchQueryHandler(DEFAULT_SEARCH_QUERY, filters, sorter);
    }

    private SearchQueryHandler getYouTubeSearchQueryHandler(
            String query,
            List<String> filters,
            String sorter) throws Exception {
        return YouTube.getSearchQHFactory().fromQuery(query, filters, sorter);
    }

    private String getHtml(String url) throws Exception {
        return NewPipe.getDownloader().download(url);
    }

    private Elements getFilterList(Document document) {
        return document.select("div.filter-col").select("ul");
    }

    @Nullable
    private Element getMatchingElement(String matchCriteriaText, Elements elements) {
        Element matchingElement = null;
        for (Element element : elements) {
            Elements filterElements = element.select("span.filter-text").not("span.filter-ghost");
            for (Element filterElement : filterElements) {
                if (filterElement.text().contains(matchCriteriaText)) {
                    matchingElement = filterElement;
                    break;
                }
            }
        }

        return matchingElement;
    }
}
